package root.core.classmanipulating;

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
                File file = getFile(unit);
                if (isSpecialClassTypeThenHandleIt(file, typeDeclarations, classStructureDTO, unit)) {
                    continue;
                }
                JCTree.JCClassDecl classDeclaration = (JCTree.JCClassDecl) typeDeclarations.get(0);
                setClassType(classStructureDTO, classDeclaration);
                handleImports(unit, classStructureDTO);
                handleClassDeclaration(classStructureDTO, classDeclaration);
                collectClassIfItIsAccessible(origin, rootDirectory, unit, classDeclaration);
                Set<String> fieldNames = new HashSet<>();
                for (JCTree member : classDeclaration.getMembers()) {
                    if (member instanceof JCTree.JCVariableDecl){
                        handleFieldDeclarationPosition(classStructureDTO, fieldNames, (JCTree.JCVariableDecl) member);
                    }
                    if (member instanceof JCTree.JCMethodDecl){
                        handleMethodDeclaration(classStructureDTO, fieldNames, (JCTree.JCMethodDecl) member, file);
                    }
                }


                applicationState.putClassStructure(file, classStructureDTO);
            }

        }
    }

    private void collectClassIfItIsAccessible(ClassOrigin origin, String rootDirectory, CompilationUnitTree unit, JCTree.JCClassDecl classDeclaration) {
        String packageName = getPackageName(unit);
        String className = classDeclaration.getSimpleName().toString();
        classNamesCollector.addClassIfAccessible(className, packageName, origin, rootDirectory, true);
    }

    private void setClassType(ClassStructureDTO classStructureDTO, JCTree.JCClassDecl classDeclaration) {
        Tree.Kind classKind = classDeclaration.getKind();
        ClassType classType = ClassType.valueOf(classKind.toString());
        classStructureDTO.setClassType(classType);
    }

    private void handleImports(CompilationUnitTree unit, ClassStructureDTO classStructureDTO) {
        for (ImportTree anImport : unit.getImports()) {
            classStructureDTO.addImport(anImport.toString());
        }
    }

    private boolean isSpecialClassTypeThenHandleIt(File file, List<? extends Tree> typeDeclarations, ClassStructureDTO classStructureDTO, CompilationUnitTree unit) {
        if (typeDeclarations.isEmpty()){
            classStructureDTO.setClassType(ClassType.PACKAGE_DECLARATION);
            applicationState.putClassStructure(file, classStructureDTO);
            return true;
        }
        Tree typeDeclaration = typeDeclarations.get(0);
        if (typeDeclaration instanceof JCTree.JCModuleDecl){
            classStructureDTO.setClassType(ClassType.MODULE);
            applicationState.putClassStructure(file, classStructureDTO);
            return true;
        }
        return typeDeclaration instanceof JCTree.JCErroneous;
    }

    private void handleMethodDeclaration(ClassStructureDTO classStructureDTO, Set<String> fieldNames, JCTree.JCMethodDecl member, File file) {
        boolean isMainMethod = isMainMethod(member);
        if (isMainMethod){
            applicationState.addClassWithMainMethod(file);
        }
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

    private boolean isMainMethod(JCTree.JCMethodDecl methodDeclaration) {
        String modifiers = methodDeclaration.getModifiers().toString();
        com.sun.tools.javac.util.List<JCTree.JCVariableDecl> parameters = methodDeclaration.getParameters();
        if (modifiers.contains("public") && modifiers.contains("static")){
            if (parameters.size()==1){
                JCTree.JCVariableDecl parameter = parameters.get(0);
                return parameter.getType().toString().equals("String[]");
            }
        }
        return false;
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

    private void handleFieldDeclarationPosition(ClassStructureDTO classStructureDTO, Set<String> fieldNames, JCTree.JCVariableDecl member) {
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

    private void handleClassDeclaration(ClassStructureDTO classStructureDTO, JCTree.JCClassDecl classDeclaration) {
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

}
