package root.core.classmanipulating;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.tools.javac.tree.JCTree;
import org.springframework.stereotype.Component;
import root.core.codecompletion.ClassNamesCollector;
import root.core.constants.ClassType;
import root.core.dto.*;
import root.core.jdk.manipulating.JavaSourcesExtractor;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClassStructureParser {

    public static final String PACKAGE_KEYWORD = "package ";
    private final Node.TreeTraversal treeTraversal = Node.TreeTraversal.PREORDER;
    private ApplicationState applicationState;

    private ClassNamesCollector classNamesCollector;

    private JavaSourcesExtractor javaSourcesExtractor;


    public ClassStructureParser(ApplicationState applicationState, ClassNamesCollector classNamesCollector, JavaSourcesExtractor javaSourcesExtractor) {
        this.applicationState = applicationState;
        this.classNamesCollector = classNamesCollector;
        this.javaSourcesExtractor = javaSourcesExtractor;
    }

    public void parseClassContent (File file, ClassOrigin origin){
        try {
            parseClasses(List.of(file), origin, "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void parseClasses(List<File> files, ClassOrigin origin, String rootDirectory) throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
            final Iterable<? extends CompilationUnitTree> compilationUnitTrees = getCompilationUnitTrees(files, compiler, fileManager);
            for (CompilationUnitTree unit : compilationUnitTrees) {
                ClassStructureDTO classStructureDTO = new ClassStructureDTO();
                List<? extends Tree> typeDeclarations = unit.getTypeDecls();
                if (typeDeclarations.isEmpty()){
                    classStructureDTO.setClassType(ClassType.PACKAGE_DECLARATION);
                    File file = getFile(unit);
                    applicationState.putClassStructure(file, classStructureDTO);
                    continue;
                }
                Tree typeDeclaration = typeDeclarations.get(0);
                if (typeDeclaration instanceof JCTree.JCModuleDecl){
                    classStructureDTO.setClassType(ClassType.MODULE);
                    File file = getFile(unit);
                    applicationState.putClassStructure(file, classStructureDTO);
                    continue;
                }
                if (typeDeclaration instanceof JCTree.JCErroneous){
                    continue;
                }
                JCTree.JCClassDecl classDeclaration = (JCTree.JCClassDecl) typeDeclaration;
                Tree.Kind classKind = classDeclaration.getKind();
                ClassType classType = ClassType.valueOf(classKind.toString());
                classStructureDTO.setClassType(classType);
                for (ImportTree anImport : unit.getImports()) {
                    classStructureDTO.addImport(anImport.toString());
                }
                addClassDeclarationPosition(classStructureDTO, classDeclaration);
                String packageName = getPackageName(unit);
                Set<String> fieldNames = new HashSet<>();
                String className = classDeclaration.getSimpleName().toString();
                classNamesCollector.addClassIfAccessible(className, packageName, origin, rootDirectory, true);
                for (JCTree member : classDeclaration.getMembers()) {
                    if (member instanceof JCTree.JCVariableDecl){
                        addFieldDeclarationPosition(classStructureDTO, fieldNames, (JCTree.JCVariableDecl) member);
                    }
                    if (member instanceof JCTree.JCMethodDecl){
                        extractFieldAccessFromMethod(classStructureDTO, fieldNames, (JCTree.JCMethodDecl) member);
                    }
                }


                File file = getFile(unit);
                applicationState.putClassStructure(file, classStructureDTO);
            }

        }
    }

    private void extractFieldAccessFromMethod(ClassStructureDTO classStructureDTO, Set<String> fieldNames, JCTree.JCMethodDecl member) {
        addMethodDeclarationToStructure(classStructureDTO, member);
        JCTree.JCBlock body = member.getBody();
        if (body != null){
            for (JCTree.JCStatement statement : body.getStatements()) {
                if (statement instanceof JCTree.JCExpressionStatement){
                    JCTree.JCExpressionStatement expressionStatement = (JCTree.JCExpressionStatement) statement;
                    JCTree.JCExpression expression = expressionStatement.getExpression();
                    extractFieldAccessFromExpression(classStructureDTO, fieldNames, expression);

                }
            }
        }
    }

    private void addMethodDeclarationToStructure(ClassStructureDTO classStructureDTO, JCTree.JCMethodDecl member) {
        int position = member.pos;
        String name = member.getName().toString();
        String modifiers = member.getModifiers().toString();
        JCTree returnType = member.getReturnType();
        String type = "";
        if (returnType!=null){
            type = returnType.toString();
        }
        name = name.replace("<init>", classStructureDTO.getClassDeclaration().getName());
        String params = member.getParameters().stream().map(param -> param.getType().toString()).collect(Collectors.joining(", "));
        MethodDeclarationDTO methodDeclarationDTO = new MethodDeclarationDTO(position, name, modifiers, type);
        methodDeclarationDTO.setParameters(params);
        classStructureDTO.addMethodDeclaration(methodDeclarationDTO);
    }

    private void extractFieldAccessFromExpression(ClassStructureDTO classStructureDTO, Set<String> fieldNames, JCTree.JCExpression expression) {
        if (expression instanceof JCTree.JCAssign){
            JCTree.JCAssign assign = (JCTree.JCAssign) expression;
            JCTree.JCExpression variable = assign.getVariable();
            if (variable instanceof JCTree.JCFieldAccess){
                addFieldAccessPosition(classStructureDTO, fieldNames, (JCTree.JCFieldAccess) variable);
            }
            if (variable instanceof JCTree.JCIdent){
                addFieldAccessPosition(classStructureDTO, fieldNames, (JCTree.JCIdent) variable);
            }
        }
    }

    private void addFieldAccessPosition(ClassStructureDTO classStructureDTO, Set<String> fieldNames, JCTree.JCIdent variable) {
        String name = variable.getName().toString();
        if (fieldNames.contains(name)){
            classStructureDTO.addFieldAccess(new TokenPositionDTO(variable.pos,  name.length()));
        }
    }

    private void addFieldAccessPosition(ClassStructureDTO classStructureDTO, Set<String> fieldNames, JCTree.JCFieldAccess variable) {
        String name = variable.name.toString();
        if (fieldNames.contains(name)){
            classStructureDTO.addFieldAccess(new TokenPositionDTO(variable.pos + 1,  name.length()));
        }
    }

    private void addFieldDeclarationPosition(ClassStructureDTO classStructureDTO, Set<String> fieldNames, JCTree.JCVariableDecl member) {
        fieldNames.add(member.getName().toString());
        int variableDeclarationPosition = member.pos;
        String modifiers = member.getModifiers().toString();
        String type = member.getType().toString();
        String names = member.getName().toString();
        FieldDeclarationDTO fieldDeclarationDTO = new FieldDeclarationDTO(variableDeclarationPosition, names, modifiers, type);
        classStructureDTO.addFieldDeclaration(fieldDeclarationDTO);
    }

    private String getPackageName(CompilationUnitTree unit) {
        ExpressionTree packageNameExpression = unit.getPackageName();
        String packageName = "";
        if (packageNameExpression!=null){
            packageName = packageNameExpression.toString();
        }
        return packageName;
    }

    private void addClassDeclarationPosition(ClassStructureDTO classStructureDTO, JCTree.JCClassDecl classDeclaration) {
        int declarationPosition = classDeclaration.pos;
        String className = classDeclaration.getSimpleName().toString();
        ClassDeclarationDTO classDeclarationDTO = new ClassDeclarationDTO(declarationPosition, className);
        classStructureDTO.setClassDeclaration(classDeclarationDTO);
    }

    private File getFile(CompilationUnitTree unit) {
        JavaFileObject sourceFile = unit.getSourceFile();
        URI uri = sourceFile.toUri();
        File file = Paths.get(uri).toFile();
        return file;
    }

    private Iterable<? extends CompilationUnitTree> getCompilationUnitTrees(List<File> files, JavaCompiler compiler, StandardJavaFileManager fileManager) throws IOException {
        final Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);
        final JavacTask javacTask = (JavacTask) compiler.getTask(null, fileManager, null, null, null, compilationUnits);
        return javacTask.parse();
    }

    public boolean parseClassStructure(List<File> files, ClassOrigin origin){
        try {
            String rootDirectory = "";
            if (origin.equals(ClassOrigin.JDK)){
                File file = files.get(0);
                Path javaSourcesDirectory = javaSourcesExtractor.getJavaSourcesDirectory();
                Path fileRelativeToJavaSources = javaSourcesDirectory.relativize(file.toPath());
                Path rootDirectoryObject = fileRelativeToJavaSources.subpath(0, 1);
                rootDirectory = rootDirectoryObject + "/";
            }
            parseClasses(files, origin, rootDirectory);
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private boolean containsMainMethod(File file, CompilationUnit cu) {
        boolean hasMainMethod = false;
        for (MethodDeclaration methodDeclaration : cu.findAll(MethodDeclaration.class, treeTraversal)) {
            if (isMethodSignatureMatchingMain(methodDeclaration)) {
                boolean isMainMethod = checkIfMethodHas1ArrayParameterOfTypeString(file, methodDeclaration);
                if (isMainMethod) {
                    hasMainMethod = true;
                }
            }
        }
        return hasMainMethod;
    }

    private boolean isMethodSignatureMatchingMain(MethodDeclaration method) {
        return method.isStatic() && method.isPublic() && method.getType().isVoidType();
    }

    private boolean checkIfMethodHas1ArrayParameterOfTypeString(File file, MethodDeclaration method) {
        NodeList<Parameter> parameters = method.getParameters();
        if (parameters.size() ==1){
            Parameter parameter = parameters.iterator().next();
            if (parameter.getType().isArrayType() && parameter.getType().getElementType().toString().equals("String")){
                return true;
            }
        }
        return false;
    }

}
