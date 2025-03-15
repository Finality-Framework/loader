package team.rainfall.finality.loader.util;

public class StringUtil {
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
