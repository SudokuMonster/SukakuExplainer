/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.io;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;
import java.util.List;

import diuf.sudoku.*;

/**
 * Static methods to load and store Sudokus from and to
 * files or the clipboard.
 * <p>
 * The support for formats is minimal and quick&dirty.
 * Only plain text formats are supported when reading:
 * <ul>
 * <li>A single line of 81 characters (all characters not in the
 * '1' - '9' range is considered as an empty cell).
 * <li>9 lines of 9 characters.
 * <li>Other multi-lines formats, with more than one character per cell,
 * or more than one line per row, or even with a few characters between
 * blocks might be supported, but there is no warranty. If a given format
 * works, and is not one of the first two above, you should consider you are lucky.
 * </ul>
 * <p>
 * When writing, the following format is used:
 * <ul>
 * <li>9 lines of 9 characters
 * <li>empty cells are represented by a '.'
 * </ul>
 */
public class SudokuIO {

    private static final int RES_OK = 2;
    private static final int RES_WARN = 1;
    private static final int RES_ERROR = 0;

    private static final String ERROR_MSG = "Unreadable Sudoku format";
    private static final String WARNING_MSG = "Warning: the Sudoku format was not recognized.\nThe Sudoku may not have been read correctly";

    private static int loadFromReader(Grid grid, Reader reader) throws IOException {
        boolean isValid = true;
        List<String> lines = new ArrayList<String>();
        LineNumberReader lineReader = new LineNumberReader(reader);
        String line = lineReader.readLine();
        while (line != null) {
            if (line.length() >= 9)
                lines.add(line);
            else
                isValid = false;
            line = lineReader.readLine();
        }
        if (lines.size() >= 9 && lines.size() <= 30) {
            String[] arrLines = new String[lines.size()];
            lines.toArray(arrLines);
            for (int i = 0; i < arrLines.length; i++)
                arrLines[i] = arrLines[i].trim();
            int result = loadFromLines(grid, arrLines);
            if (result == RES_OK && !isValid)
                result = RES_WARN;
            return result;
        } else if (lines.size() == 1) {
            int result = loadFromSingleLine(grid, lines.get(0));
            if (result == RES_OK && !isValid)
                result = RES_WARN;
            return result;
        }
        return RES_ERROR;
    }

    private static int loadFromLines(Grid grid, String[] lines) {
        boolean isStandard = (lines.length == 9);

        int lineSize = lines.length / 9;
        int loffset = (lineSize - 1) / 2;
        int borderLines = lines.length - 9 * lineSize;
        if (borderLines < 0)
            borderLines = 0;
        int outerLines; // Number of lines before the grid
        int innerLines; // Number of additional lines between blocks
        if (borderLines % 4 == 0) {
            outerLines = borderLines / 4;
            innerLines = outerLines;
        } else {
            outerLines = 0;
            innerLines = borderLines / 2;
        }
        int index = outerLines + loffset;
        for (int y = 0; y < 9; y++) {
            /*
             * This is very ugly code, without real logic. Maybe a case-by-case version
             * would be more understandable. Or I should try some real AI stuff...
             */
            String line = lines[index];

            // Check line format
            if (line.length() != 9)
                isStandard = false;
            for (int i = 0; i < line.length(); i++) {
                char ch = line.charAt(i);
                if (ch != '.' && (ch < '0' || ch > '9'))
                    isStandard = false;
            }

            // Read line
            int cellSize = (line.length() + 1) / 9;
            int borderChars = line.length() - 9 * cellSize;
            if (borderChars < 0)
                borderChars = 0;
            int outerChars, innerChars;
            if (borderChars % 4 == 0 || (borderChars % 4 == 3 && cellSize == 2 && borderChars > 4)) {
                innerChars = (borderChars + 1) / 4;
                outerChars = (borderChars - innerChars * 2) / 2;
            } else {
                outerChars = 0;
                // The last cell, if cell size > 1, may only have half its size
                innerChars = (borderChars + cellSize / 2) / 2;
            }
            int pos = outerChars;
            for (int x = 0; x < 9; x++) {
                for (int offset = 0; offset < cellSize; offset++) {
                    if (pos + offset < line.length()) {
                        char ch = line.charAt(pos + offset);
                        int value = 0;
                        if (ch >= '1' && ch <= '9')
                            value = ch - '0';
                        if (offset == 0 || value > 0)
                            grid.setCellValue(x, y, value);
                    }
                }

                pos += cellSize;
                if (x == 2 || x == 5)
                    pos += innerChars;
            }

            index += lineSize;
            if (y == 2 || y == 5)
                index += innerLines;
        }
        return (isStandard ? RES_OK : RES_WARN);
    }

    private static int loadFromSingleLine(Grid grid, String line) {
        boolean isStandard = (line.length() == 81);
        // Detect Sudoku Susser format (Although the SS cannot cut/past to itself)
        if (line.endsWith("\t"))
            line = line.substring(0, line.length() - 1);
        boolean hasAlphaLabel = false;
        for (int i = 0; i < line.length() - 81; i++) {
            if (Character.isLetter(line.charAt(i)))
                hasAlphaLabel = true;
        }
        for (int i = line.length() - 81; i < line.length(); i++) {
            if (i >= 0 && Character.isLetter(line.charAt(i)))
                hasAlphaLabel = false;
        }
        if (hasAlphaLabel && line.length() > 81)
            line = line.substring(line.length() - 81);
        else if (line.trim().length() >= 81)
            line = line.trim();

        if (line.length() >= 81) {
            int rowGap = (line.length() - 81) / 8;
            int srcIndex = 0;
            for (int y = 0; y < 9; y++) {
                for (int x = 0; x < 9; x++) {
                    char ch = line.charAt(srcIndex++);
                    int value = 0;
                    if (ch >= '1' && ch <= '9')
                        value = ch - '0';
                    else if (ch != '.' && ch != '0')
                        isStandard = false;
                    grid.setCellValue(x, y, value);
                }
                srcIndex += rowGap;
            }
            return (isStandard ? RES_OK : RES_WARN);
        }
        return RES_ERROR;
    }

    private static void saveToWriter(Grid grid, Writer writer) throws IOException {
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                int value = grid.getCellValue(x, y);
                int ch = '.';
                if (value > 0)
                    ch = '0' + value;
                writer.write(ch);
            }
            writer.write("\r\n");
        }
    }

    /**
     * Test whether a Sudoku can be loaded from the current
     * content of the clipboard.
     * @return whether a Sudoku can be loaded from the current
     * content of the clipboard
     */
    public static boolean isClipboardLoadable() {
        Grid grid = new Grid();
        return (loadFromClipboard(grid) == null);
    }

    public static ErrorMessage loadFromClipboard(Grid grid) {
        Transferable content =
            Toolkit.getDefaultToolkit().getSystemClipboard().getContents(grid);
        if (content == null)
            return new ErrorMessage("The clipboard is empty");
        Reader reader = null;
        try {
            DataFlavor flavor = new DataFlavor(String.class, "Plain text");
            reader = flavor.getReaderForText(content);
            int result = loadFromReader(grid, reader);
            if (result == RES_OK) // success
                return null;
            if (result == RES_WARN) // warning
                return new ErrorMessage(WARNING_MSG, false);
            else // error
                return new ErrorMessage(ERROR_MSG, true);
        } catch (IOException ex) {
            return new ErrorMessage("Error while copying:\n{0}", ex);
        } catch (UnsupportedFlavorException ex) {
            return new ErrorMessage("Unsupported data type");
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch(Exception ex) {}
        }
    }

    public static void saveToClipboard(Grid grid) {
        StringWriter writer = new StringWriter();
        try {
            saveToWriter(grid, writer);
            StringSelection data = new StringSelection(writer.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(data, data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static ErrorMessage loadFromFile(Grid grid, File file) {
        Reader reader = null;
        try {
            FileReader freader = new FileReader(file);
            reader = new BufferedReader(freader);
            int result = loadFromReader(grid, reader);
            if (result == RES_OK)
                return null;
            else if (result == RES_WARN)
                return new ErrorMessage(WARNING_MSG, false);
            else
                return new ErrorMessage(ERROR_MSG, true);
        } catch (FileNotFoundException ex) {
            return new ErrorMessage("File not found: {0}", file);
        } catch (IOException ex) {
            return new ErrorMessage("Error while reading file {0}:\n{1}", file, ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static ErrorMessage saveToFile(Grid grid, File file) {
        Writer writer = null;
        try {
            FileWriter fwriter = new FileWriter(file);
            writer = new BufferedWriter(fwriter);
            saveToWriter(grid, writer);
            return null;
        } catch (IOException ex) {
            return new ErrorMessage("Error while writing file {0}:\n{1}", file, ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
