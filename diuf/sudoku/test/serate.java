/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2009 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 * this entry point by gsf @ www.sudoku.com/boards (The Player's Forum)
 */
package diuf.sudoku.test;

import java.io.*;
import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;

public class serate {
    static String FORMAT = "%r/%p/%d";
    static String RELEASE = "2009-01-01";
    static String VERSION = "1.2.1.3";
    static void help(int html) {
	if (html != 0) {
            System.err.println("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">");
            System.err.println("<HTML>");
            System.err.println("<HEAD>");
            System.err.println("<TITLE>Sudoku Explainer serate man document</TITLE>");
            System.err.println("</HEAD>");
            System.err.println("<BODY bgcolor=white>");
            System.err.println("<PRE>");
	}
        System.err.println("NAME");
        System.err.println("  serate - Sudoku Explainer command line rating");
        System.err.println("");
        System.err.println("SYNOPSIS");
	System.err.println("  serate [ --diamond ] [ --format=FORMAT ] [ --input=FILE ] [ --output=FILE ] [ --pearl ] [ puzzle ... ]");
        System.err.println("");
        System.err.println("DESCRIPTION");
	System.err.println("  serate is a Sudoku Explainer command line entry point that rates one or more");
	System.err.println("  input puzzles.  If an --input=FILE option is specified then 81-character puzzle");
	System.err.println("  strings are read from that file, otherwise if 81-character puzzle operands are");
	System.err.println("  not specified the puzzles are read from the standard input.  If an --output=FILE");
	System.err.println("  option is specified then the output is written to that file, otherwise output");
	System.err.println("  is written to the standard output.  The output is controlled by the");
	System.err.println("  --format=FORMAT option.");
        System.err.println("");
	System.err.println("  Ratings are floating point numbers in the range 0.0 - 20.0, rounded to the");
	System.err.println("  tenths digit.  0.0 indicates a processing error and 20.0 indicates an valid");
	System.err.println("  but otherwise unsolvable input puzzle.");
        System.err.println("");
        System.err.println("OPTIONS");
	System.err.println("  -d, --diamond");
	System.err.println("      Terminate rating if the puzzle is not a diamond.");
	System.err.println("  -f, --format=FORMAT");
	System.err.println("      Format the output for each input puzzle according to FORMAT.  Format");
	System.err.println("      conversion are %CHARACTER; all other characters are output unchanged.");
	System.err.println("      The default format is " + FORMAT + ".  The format conversions are:");
	System.err.println("        %d  The diamond rating.  This is the highest ER of the methods leading");
	System.err.println("            to the first candidate elimination.");
	System.err.println("        %e  The elapsed time to rate the puzzle.");
	System.err.println("        %g  The puzzle grid in 81-character [0-9] form.");
	System.err.println("        %n  The input puzzle ordinal, counting from 1.");
	System.err.println("        %p  The pearl rating.  This is the highest ER of the methods leading");
	System.err.println("            to the first cell placement.");
	System.err.println("        %r  The puzzle rating.  This is the highest ER of the methods leading");
	System.err.println("            to the puzzle solution.");
	System.err.println("        %%  The % character.");
	System.err.println("  -h, --html");
	System.err.println("      List detailed info in html.");
	System.err.println("  -i, --input=FILE");
	System.err.println("      Read 81-character puzzle strings, one per line, from FILE.  By default");
	System.err.println("      operands are treated as 81-character puzzle strings.  If no operands are");
	System.err.println("      specified then the standard input is read.");
	System.err.println("  -m, --man");
	System.err.println("      List detailed info in displayed man page form.");
	System.err.println("  -o, --output=FILE");
	System.err.println("      Write output to FILE instead of the standard output.");
	System.err.println("  -p, --pearl");
	System.err.println("      Terminate rating if the puzzle is not a pearl.");
	System.err.println("  -V, --version");
	System.err.println("      Print the Sudoku Explainer (serate) version and exit.");
        System.err.println("");
        System.err.println("INVOCATION");
	System.err.println("  java -Xrs -Xmx500m -cp SudokuExplainer.jar diuf.sudoku.test.serate ...");
        System.err.println("");
        System.err.println("SEE ALSO");
	System.err.println("  SudokuExplainer(1), sudoku(1)");
        System.err.println("");
        System.err.println("IMPLEMENTATION");
	System.err.println("  version     serate " + VERSION + " (Sudoku Explainer) " + RELEASE);
	System.err.println("  author      Nicolas Juillerat");
	System.err.println("  copyright   Copyright (c) 2006-2009 Nicolas Juillerat");
	System.err.println("  license     Lesser General Public License (LGPL)");
	if (html != 0) {
            System.err.println("</PRE>");
            System.err.println("</BODY>");
            System.err.println("</HTML>");
	}
	System.exit(2);
    }
    static void usage(String option, int argument) {
	System.err.println("serate: " + option + ((argument == 1) ? ": option argument expected" : ": unknown option"));
	System.err.println("Usage: serate [ --diamond ] [ --format=FORMAT ] [ --input=FILE ] [ --output=FILE ] [ --pearl ]");
	System.exit(2);
    }
    /**
     * Solve input puzzles and print results according to the output format.
     * @param args 81-char puzzles
     */
    public static void main(String[] args) {
	String		format = FORMAT;
	String		input = null;
	String		output = "-";
	String		a;
	String		s;
	String		v;
	String		puzzle;
	BufferedReader	reader = null;
	PrintWriter	writer = null;
        int		ordinal = 0;
	char		want = 0;
	int		arg;
	long		t;
	char		c;
	try {
            for (arg = 0; arg < args.length; arg++) {
    	        a = s = args[arg];
    	        if (s.charAt(0) != '-')
    		    break;
		v = null;
		if (s.charAt(1) == '-') {
		    if (s.length() == 2) {
			arg++;
    		        break;
		    }
		    s = s.substring(2);
		    for (int i = 2; i < s.length(); i++)
			if (s.charAt(i) == '=') {
			    v = s.substring(i+1);
			    s = s.substring(0, i);
			}
		    if (s.equals("diamond"))
			c = 'd';
		    else if (s.equals("format"))
			c = 'f';
		    else if (s.equals("html"))
			c = 'h';
		    else if (s.equals("in") || s.equals("input"))
			c = 'i';
		    else if (s.equals("man"))
			c = 'm';
		    else if (s.equals("out") || s.equals("output"))
			c = 'o';
		    else if (s.equals("pearl"))
			c = 'p';
		    else if (s.equals("version"))
			c = 'V';
		    else
			c = '?';
		}
		else {
		    c = s.charAt(1);
		    if (s.length() > 2)
		        v = s.substring(2);
		    else if (++arg < args.length)
		        v = args[arg];
		}
		switch (c) {
		case 'f':
		case 'i':
		case 'o':
		    if (v == null)
		        usage(a, 1);
		    break;
		}
		switch (c) {
		case 'd':
		case 'p':
		    want = c;
		    break;
		case 'f':
		    format = v;
		    break;
		case 'h':
		    help(1);
		    break;
		case 'i':
		    input = v;
		    break;
		case 'm':
		    help(0);
		    break;
		case 'o':
		    output = v;
		    break;
		case 'V':
		    System.out.println(VERSION);
	            System.exit(0);
		    break;
		default:
		    usage(a, 0);
		    break;
		}
    	    }
	    if (input != null) {
                if (input.equals("-")) {
                    InputStreamReader reader0 = new InputStreamReader(System.in);
                    reader = new BufferedReader(reader0);
                }
                else {
                    Reader reader0 = new FileReader(input);
                    reader = new BufferedReader(reader0);
                }
	    }
	    if (output.equals("-")) {
                OutputStreamWriter writer0 = new OutputStreamWriter(System.out);
                BufferedWriter writer1 = new BufferedWriter(writer0);
                writer = new PrintWriter(writer1);
	    }
	    else {
                Writer writer0 = new FileWriter(output);
                BufferedWriter writer1 = new BufferedWriter(writer0);
                writer = new PrintWriter(writer1);
	    }
	    for (;;) {
		if (reader != null) {
		    puzzle = reader.readLine();
		    if (puzzle == null)
			break;
		}
		else if (arg < args.length)
		    puzzle = args[arg++];
		else
		    break;
                if (puzzle.length() >= 81) {
                    Grid grid = new Grid();
                    for (int i = 0; i < 81; i++) {
                        char ch = puzzle.charAt(i);
                        if (ch >= '1' && ch <= '9') {
                            int value = (ch - '0');
                            grid.setCellValue(i % 9, i / 9, value);
                        }
                    }
		    t = System.currentTimeMillis();
                    Solver solver = new Solver(grid);
		    solver.want = want;
                    solver.rebuildPotentialValues();
		    ordinal++;
                    try {
                        solver.getDifficulty();
                    } catch (UnsupportedOperationException ex) {
			solver.difficulty = solver.pearl = solver.diamond = 0.0;
                    }
		    t = System.currentTimeMillis() - t;
		    s = "";
		    for (int i = 0; i < format.length(); i++) {
			int		w;
			int		p;
			long		u;
			char	f = format.charAt(i);
			if (f != '%' || ++i >= format.length())
			    s += f;
			else
			    switch (format.charAt(i)) {
			    case 'd':
                                w = (int)((solver.diamond + 0.05) * 10);
    		                p = w % 10;
    		                w /= 10;
			        s += w + "." + p;
				break;
			    case 'e':
				t /= 10;
				u = t % 100;
				t /= 100;
				if (t < 60) {
				    s += t + ".";
				    if (u < 10)
					s += "0";
				    s += u + "s";
				}
				else if (t < 60*60) {
				    s += (t / 60) + "m";
				    u = t - (t / 60) * 60;
				    if (u < 10)
					s += "0";
				    s += u + "s";
				}
				else if (t < 24*60*60) {
				    s += (t / (60*60)) + "h";
				    u = (t - (t / (60*60)) * (60*60)) / 60;
				    if (u < 10)
					s += "0";
				    s += u + "m";
				}
				else {
				    s += (t / (24*60*60)) + "d";
				    u = (t - (t / (24*60*60)) * (24*60*60)) / (60*60);
				    if (u < 10)
					s += "0";
				    s += u + "h";
				}
				break;
			    case 'g':
				s += puzzle;
				break;
			    case 'n':
				s += ordinal;
				break;
			    case 'p':
                                w = (int)((solver.pearl + 0.05) * 10);
    		                p = w % 10;
    		                w /= 10;
			        s += w + "." + p;
				break;
			    case 'r':
                                w = (int)((solver.difficulty + 0.05) * 10);
    		                p = w % 10;
    		                w /= 10;
			        s += w + "." + p;
				break;
			    default:
				s += f;
				break;
			    }
		    }
                    writer.println(s);
		    writer.flush();
                }
	    }
        } catch(FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
                if (writer != null)
                    writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
	    }
        }
    }
}
