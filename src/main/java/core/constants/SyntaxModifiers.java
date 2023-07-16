package core.constants;

public class SyntaxModifiers {
    public static final String KEYWORDS_REGEXP;


    static  {
        StringBuilder stringBuilder = new StringBuilder()
                .append("\\b")
                .append("(")
                .append("private");
        appendKeywords(stringBuilder, "public", "protected");
        appendKeywords(stringBuilder, "false", "true", "int");
        appendKeywords(stringBuilder, "if", "else", "while", "for");
        appendKeywords(stringBuilder, "abstract", "assert", "boolean", "break");
        appendKeywords(stringBuilder, "case", "catch", "char");
        appendKeywords(stringBuilder, "class", "continue", "default", "do");
        appendKeywords(stringBuilder, "double", "enum", "extends", "final");
        appendKeywords(stringBuilder, "finally", "float", "implements");
        appendKeywords(stringBuilder, "import", "instanceof", "interface", "long", "native");
        appendKeywords(stringBuilder, "new", "package", "return", "short");
        appendKeywords(stringBuilder, "static", "strictfp", "super");
        appendKeywords(stringBuilder, "switch", "synchronized", "this");
        appendKeywords(stringBuilder, "throw", "throws", "try", "void", "volatile");
        stringBuilder.append(")")
                .append("\\b");
        KEYWORDS_REGEXP = stringBuilder.toString();

    }

    private static void appendKeywords(StringBuilder stringBuilder, String... keywords) {
        for (String keyword : keywords) {
            stringBuilder.append("|").append(keyword);
        }
    }

}
