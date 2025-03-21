package team.rainfall.finality.loader.util;

/**
 * <p>Utility class for string operations.</p>
 * <p>This class provides methods to escape special characters in a string.</p>
 * <p>Note: This class handles null input by returning null.</p>
 *
 * @author RedreamR
 */
public class StringUtil {

    /**
     * <p>Escapes special characters in the input string.</p>
     * <p>This method replaces special characters with their escape sequences.</p>
     *
     * @param input the input string to be escaped
     * @return the escaped string, or null if the input is null
     * @author RedreamR
     */
    public static String escapeString(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder escapedString = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '\n':
                    escapedString.append("\\n");
                    break;
                case '\t':
                    escapedString.append("\\t");
                    break;
                case '\b':
                    escapedString.append("\\b");
                    break;
                case '\r':
                    escapedString.append("\\r");
                    break;
                case '\f':
                    escapedString.append("\\f");
                    break;
                case '\\':
                    escapedString.append("\\\\");
                    break;
                case '\"':
                    escapedString.append("\\\"");
                    break;
                case '\'':
                    escapedString.append("\\\'");
                    break;
                default:
                    if (c < ' ' || c > '~') {
                        escapedString.append(String.format("\\u%04x", (int) c));
                    } else {
                        escapedString.append(c);
                    }
                    break;
            }
        }
        return escapedString.toString();
    }

}
