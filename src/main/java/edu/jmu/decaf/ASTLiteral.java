package edu.jmu.decaf;

/**
 * Decaf literal value.
 * 
 * <p>Here are the Decaf data types and their corresponding Java data types:
 * 
 * <table border="1">
 * <tr><th>{@link ASTNode.DataType}</th><th>Java type</th></tr>
 * <tr><td>{@code INT}</td><td>{@code Integer}</td>
 * <tr><td>{@code BOOL}</td><td>{@code Boolean}</td>
 * <tr><td>{@code STR}</td><td>{@code String}</td>
 * <tr><td>{@code VOID}</td><td>{@code null}</td>
 * <caption>Decaf/Java type equivalences</caption>
 * </table>
 */
public class ASTLiteral extends ASTExpression
{
    /**
     * Remove escape codes from string literals and replace them with
     * the corresponding special character (quotes, newlines, or tabs)
     * @param str String to manipulate
     * @return String with escape codes replaced by special characters
     */
    public static String removeEscapeCodes(String str)
    {
        return str.replaceAll("\\\\\"", "\"")
                  .replaceAll("\\\\n", "\n")
                  .replaceAll("\\\\t", "\t")
                  .replaceAll("\\\\\\\\", "\\\\");
    }

    /**
     * Remove quotes, newlines, and tabs from string literals
     * and replace them with their escape codes.
     * @param str String to manipulate
     * @return String with special characters replaced by escape codes
     */
    public static String addEscapeCodes(String str)
    {
        return str.replaceAll("\\\\", "\\\\\\\\")
                  .replaceAll("\"", "\\\\\"")
                  .replaceAll("\n", "\\\\n")
                  .replaceAll("\t", "\\\\t");
    }

    public ASTNode.DataType type;
    public Object value;

    public ASTLiteral(ASTNode.DataType type, Object value)
    {
        this.type = type;
        this.value = value;
    }

    @Override
    public void traverse(ASTVisitor visitor)
    {
        visitor.preVisit(this);
        visitor.postVisit(this);
    }

    @Override
    public String toString()
    {
        if (this.value instanceof String) {
            return addEscapeCodes(value.toString());
        } else {
            return this.value.toString();
        }
    }
}

