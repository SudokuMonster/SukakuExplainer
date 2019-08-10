/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.tools;

import java.io.*;


public class HtmlLoader {

    private static final String LOAD_ERROR = "<html><body>" +
            "Error while loading resource: {0}</body></html>";

    /**
     * Load the specified HTML file.
     * Custom color tags are automatically replaced by regular HTML color tags.
     * @param caller the caller object (used to fetch the class loader)
     * @param fileName the name of the HTML file
     * @return the content of the HTML file
     */
    public static String loadHtml(Object caller, String fileName) {
        Class<?> callerClass = caller.getClass();
        InputStream input = callerClass.getResourceAsStream(fileName);
        if (input == null) {
            System.err.println("Resource not found: " + fileName);
            return LOAD_ERROR.replace("{0}", fileName);
        }
        try {
            Reader reader0 = new InputStreamReader(input, "ISO-8859-1");
            Reader reader = new BufferedReader(reader0);
            char[] buffer = new char[4096];
            StringBuilder builder = new StringBuilder();
            int read = reader.read(buffer);
            while (read > 0) {
                builder.append(buffer, 0, read);
                read = reader.read(buffer);
            }
            reader.close();
            String result = builder.toString();
            // Replace generic coloring HTML tags
            return formatColors(result);
        } catch (IOException ex) {
            ex.printStackTrace();
            return LOAD_ERROR.replace("{0}", fileName);
        }
    }

    /**
     * Replace custom color tags by regular HTML color tags.
     * @param html the HTML to convert
     * @return the converted HTML
     */
    public static String formatColors(String html) {
        String result = html;
        result = result.replace("<r>", "<font color=\"red\">"); // red
        result = result.replace("</r>", "</font>");
        result = result.replace("<g>", "<font color=\"#009000\">"); // green (candidate)
        result = result.replace("</g>", "</font>");
        result = result.replace("<o>", "<font color=\"#E08000\">"); // orange
        result = result.replace("</o>", "</font>");
        result = result.replace("<b1>", "<font color=\"#0000A0\">"); // blue (region)
        result = result.replace("</b1>", "</font>");
        result = result.replace("<b2>", "<font color=\"#005000\">"); // green (region)
        result = result.replace("</b2>", "</font>");
        result = result.replace("<c>", "<font color=\"#00AAAA\">"); // cyan (cell)
        result = result.replace("</c>", "</font>");
        return result;
    }

    /**
     * Format a string. Replace the patterns "{0}", "{1}", etc
     * by <tt>args[0]</tt>, <tt>args[1]</tt>, etc.
     */
    public static String format(String source, Object... args) {
        for (int i = 0; i < args.length; i++) {
            String pattern = "{" + i + "}";
            source = source.replace(pattern, args[i].toString());
        }
        return source;
    }

    public static String formatList(Object[] elements) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) {
                if (i < elements.length - 1)
                    result.append(", ");
                else
                    result.append(" and ");
            }
            result.append(elements[i].toString());
        }
        return result.toString();
    }

    public static String formatValues(int[] values) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                if (i < values.length - 1)
                    result.append(", ");
                else
                    result.append(" and ");
            }
            result.append(Integer.toString(values[i]));
        }
        return result.toString();
    }

}
