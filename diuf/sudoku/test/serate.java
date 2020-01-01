/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2009 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 * this entry point by gsf @ www.sudoku.com/boards (The Player's Forum)
 */
package diuf.sudoku.test;

import java.io.*;
import java.util.*;

import static diuf.sudoku.Settings.*;


import diuf.sudoku.*;
import diuf.sudoku.solver.*;

public class serate {
    static String FORMAT = "%r/%p/%d";
    static String RELEASE = Settings.releaseDate;
    static String VER = "" + Settings.VERSION + "." + Settings.REVISION + Settings.SUBREV + isItExperimental();
    static Formatter formatter;
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
        System.err.println("  serate - Sukaku Explainer / Sudoku Explainer command line rating");
        System.err.println("");
        System.err.println("SYNOPSIS");
        System.err.println("  serate [--after=FORMAT] [--before=FORMAT] [--format=FORMAT]");
        System.err.println("    [--techs=TECHSTRING] [--showArguments] [--start=FORMAT]");
        System.err.println("    [--revisedRating=N] [--threads=N] [--totalTime] [--batch=N]");
        System.err.println("    [--isBlocks=N] [--isDG=N] [--isWindows=N] [--isX=N]");
        System.err.println("    [--isGirandola=N] [--isAsterisk=N] [--isCD=N]");		
		System.err.println("    [--isAntiKnight=N] [--isAntiKing=N] [--isToroidal=N] [--isNC=N]");
		System.err.println("    [--input=FILE] [--output=FILE] [--pearl] [--diamond] [puzzle ...]");
        System.err.println("");
        System.err.println("DESCRIPTION");
        System.err.println("  serate is a Sukaku Explainer command line entry point that rates one or more");
        System.err.println("  input puzzles.  It is currently backward-compatible  with serate v1.2.1");
        System.err.println("  If an --input=FILE option is specified then 81-character puzzle");
        System.err.println("  strings are read from that file, otherwise if 81-character puzzle operands are");
        System.err.println("  not specified the puzzles are read from the standard input.  If an --output=FILE");
        System.err.println("  option is specified then the output is written to that file, otherwise output");
        System.err.println("  is written to the standard output.  The output is controlled by the");
        System.err.println("  --format=FORMAT option (F) as well as --start (S), --before (B), and --after (A) options.");
        System.err.println("");
        System.err.println("  Ratings are floating point numbers in the range 0.0 - 20.0, rounded to the");
        System.err.println("  tenths digit.  0.0 indicates a processing error and 20.0 indicates an valid");
        System.err.println("  but otherwise unsolvable input puzzle.");
        System.err.println("");
        System.err.println("OPTIONS");
        System.err.println("  -a, --after=FORMAT");
        System.err.println("      Format the output after each step according to FORMAT. Default is empty.");
        System.err.println("  -b, --before=FORMAT");
        System.err.println("      Format the output before each step according to FORMAT. Default is empty.");
        System.err.println("  -B, --batch=N");
        System.err.println("      Batch solving. Default 0=disabled.");
        System.err.println("                             1=Apply all available hints that have the lowest rating");
        System.err.println("                             2=Apply all available hints with rating lower than maximum puzzle rating");
        System.err.println("  -c, --isNC=N");
        System.err.println("  	  Control Non-Consecutive constraints 0: disable (default)");
        System.err.println("  	                                      1: NC (1,9 not included)");
        System.err.println("  	                                      2: NC+ (1,9 included)");
        System.err.println("  -d, --diamond");
        System.err.println("      Terminate rating if the puzzle is not a diamond.");
        System.err.println("  -D, --isDG=N");
        System.err.println("  	  Enable/disable Disjoint Group constraints. 0:disable (default), 1:enabled");
        System.err.println("  -E, --isCD=N");
        System.err.println("  	  Enable/disable Center Dot group constraints. 0:disable (default), 1:enabled");
        System.err.println("  -f, --format=FORMAT");
        System.err.println("      Format the output for each input puzzle according to FORMAT.  Format");
        System.err.println("      conversion are %CHARACTER; all other characters are output unchanged.");
        System.err.println("      The default format is %r/%p/%d.  The format conversions are:");
        System.err.println("        %d  The diamond rating.  This is the highest ER of the methods leading");
        System.err.println("            to the first candidate elimination. (F)");
        System.err.println("        %D  The diamond rating technique name.  This is the name of technique with highest rating of the methods leading");
        System.err.println("            to the first candidate elimination. (F)");
        System.err.println("        %e  The elapsed time to rate the puzzle. (AF)");
        System.err.println("        %h  The long step description in multi-line HTML format. (A)");
        System.err.println("        %g  The input puzzle line. (SBAF)");
        System.err.println("        %i  The puzzle grid in 81-character [0-9] form. (SBAF)");
        System.err.println("        %l  The new line. (SBAF)");
        System.err.println("        %m  The input puzzle pencilmarks in 729-char format. (SBA)");
        System.err.println("        %M  The input puzzle pencilmarks in multi-line format. (SBA)");
        System.err.println("        %n  The input puzzle ordinal, counting from 1. (SF)");
        System.err.println("        %p  The pearl rating.  This is the highest ER of the methods leading");
        System.err.println("            to the first cell placement. (F)");
        System.err.println("        %P  The pearl rating technique name.  This is the name of 1st technique with highest");
        System.err.println("             rating of the methods leading to the first cell placement. (F)");
        System.err.println("        %r  The puzzle rating.  This is the highest ER of the methods leading");
        System.err.println("            to the puzzle solution. (AF)");
        System.err.println("        %R  The puzzle highest rating technique name.  This is the name of 1st technique with");
        System.err.println("             highest ER of the methods leading to the puzzle solution. (F)");
        System.err.println("        %s  The short step description. (A)");
        System.err.println("        %S  The puzzle highest rating technique shortened name.  This is the SHORT name of 1st technique with");
        System.err.println("             highest ER of the methods leading to the puzzle solution. (AF)");
        System.err.println("        %t  The tab character. (SBAF)");
        System.err.println("        %T  The pearl rating technique shortened name.  This is the SHORT name of 1st technique with highest");
        System.err.println("             rating of the methods leading to the first cell placement. (F)");
        System.err.println("        %U  The diamond rating technique shortened name.  This is the SHORT name of technique with highest rating of the methods leading");
        System.err.println("            to the first candidate elimination. (F)");
        System.err.println("        %%  The % character.");
        System.err.println("  -G, --islkSudokuBUG=N");
        System.err.println("  	  Fix to BUG algorithm by lkSudoku. 0=disabled	1=enabled (default)");
        System.err.println("  -h, --html");
        System.err.println("      List detailed info in html.");
        System.err.println("  -i, --input=FILE");
        System.err.println("      Read 81-character puzzle strings, one per line, from FILE.  By default");
        System.err.println("      operands are treated as 81-character puzzle strings.  If no operands are");
        System.err.println("      specified then the standard input is read.");
        System.err.println("  -k, --isAntiKing=N");
        System.err.println("  	  Enable/disable Anti-King constraints. 0:disable (default), 1:enabled");
        System.err.println("  -K, --isAsterisk=N");
        System.err.println("  	  Enable/disable Asterisk group constraints. 0:disable (default), 1:enabled");
        System.err.println("  -m, --man");
        System.err.println("      List detailed info in displayed man page form.");
        System.err.println("  -n, --isAntiKnight=N");
        System.err.println("  	  Enable/disable Anti-kNight constraints. 0:disable (default), 1:enabled");
        System.err.println("  -N, --revisedRating=N");
        System.err.println("      Revised rating scheme. Default 0=disabled. 1=enabled");
        System.err.println("  -o, --output=FILE");
        System.err.println("      Write output to FILE instead of the standard output.");
        System.err.println("  -O, --isToroidal=N");
        System.err.println("  	  Enable/disable Toroidal board. 0:disable (default), 1:enabled");
        System.err.println("  -p, --pearl");
        System.err.println("      Terminate rating if the puzzle is not a pearl.");
        System.err.println("  -P, --FCPlus=N");
        System.err.println("  	  Control non-trivial implications in FC+ and nested chains	0:default (similar to SE121)");
        System.err.println("  																1:More techniques added");
        System.err.println("  																2:More techniques added");
        System.err.println("  -Q, --isBlocks=N");
        System.err.println("  	  Enable/disable block(box) constraints. 0:disable (For Latin Square), 1:enabled (default)");
        System.err.println("  -R, --isGirandola=N");
        System.err.println("  	  Enable/disable Girandola group constraints. 0:disable (default), 1:enabled");
        System.err.println("  -s, --start=FORMAT");
        System.err.println("      Format the output before each puzzle according to FORMAT. Default is empty.");
        System.err.println("  -S, --showArguments");
        System.err.println("      Show parameters used");
        System.err.println("  -t, --threads=N");
        System.err.println("      Maximal degree of parallelism. Default 0=auto. 1=no parallelism; -1=unlimited");
        System.err.println("  -T, --totalTime");
        System.err.println("      The time required to process all puzzles parsed to standard output");
        System.err.println("  -U, --islkSudokuURUL=NAME");
        System.err.println("  	  Fix to UR and UL algorithm by lkSudoku. 0=disabled	1=enabled (default)");
		System.err.println("  -~, --techs=TECHSTRING");
        System.err.println("      Specific techniques only, set which techniques to use");
        System.err.println("      the techniques string TECHSTRING is a string consisting of the letters '0' and '1',");
        System.err.println("      where '1' means the technique should be used and '0' means it should not be used");
        System.err.println("      To see which technique is in which letter, and how many techniques are");
        System.err.println("      there, just type --techs= without any string after the = in TECHSTRING");
        System.err.println("  -V, --version");
        System.err.println("      Print the Sudoku Explainer (serate) version and exit.");
        System.err.println("  -W, --isWindows=N");
        System.err.println("  	  Enable/disable Windows constraints. 0:disable (default), 1:enabled");
        System.err.println("  -X, --isX=N");
        System.err.println("  	  Enable/disable X diagonal constraints. 0:disable (default), 1:enabled");
        System.err.println("");
        System.err.println("INVOCATION");
        System.err.println("");
        System.err.println("  java -Xrs -Xmx500m -cp SudokuExplainer.jar diuf.sudoku.test.serate ...");
        System.err.println("");
/*        System.err.println("EXAMPLES");
        System.err.println("");
        System.err.println("  Note: % must be entered as %% in windows .bat files and shortcut commands.");
        System.err.println("");
        System.err.println("  To rate a single or a group of puzzle(s):");
        System.err.println("  java.exe -Xrs -Xmx500m -cp SukakuExplainer.jar diuf.sudoku.test.serate --format="%g ED=%r/%p/%d" --input=puzzles.txt --output=puzzles.rated.txt");
        System.err.println("");
        System.err.println("  To display all supported format parameters (at the time of writing this document):");
        System.err.println("  java -Xrs -Xmx1g -cp SukakuExplainer.jar diuf.sudoku.test.serate \");
        System.err.println("  --format="--format%l%%d: %d%l%%D: %D%l%%e: %e%l%%g: %g%l%%i: %i%l%%n: %n%l%%p: %p%l%%P: %P%l%%r: %r%l%%R: %R%l%%S: %S%l%%T: %T%l%%U: %U%l--- end of final section ---" \");
        System.err.println("  --start="--start%l%%g: %g%l%%i: %i%l%%m: %m%l%%M:%l%M%l%%n: %n%l--- end of start section ---" \");
        System.err.println("  --before="--before%l%%g: %g%l%%i: %i%l%%m: %m%l%%M:%l%M%l--- end of before section ---" \");
        System.err.println("  --after="--after%l%%e: %e%l%%h:%l%h%l%%g: %g%l%%i: %i%l%%m: %m%l%%M:%l%M%l%%r: %r%l%%s: %s%l--- end of after section ---" \");
        System.err.println("  --input=my_input_file.txt --output=- --threads=0 > my_output_file.txt");
        System.err.println("  ");
        System.err.println("  To display man document:");
        System.err.println("  java -cp SukakuExplainer/SukakuExplainer.jar diuf.sudoku.test.serate --man");
        System.err.println("");*/
        System.err.println("SEE ALSO");
        System.err.println("  SudokuExplainer(1), sudoku(1),");
        System.err.println("  https://github.com/SudokuMonster/SukakuExplainer/wiki/Batch-mode-command-line-parameters");
        System.err.println("  https://github.com/SudokuMonster/SukakuExplainer");
        System.err.println("  ");
        System.err.println("");
        System.err.println("IMPLEMENTATION");
    //relese
    String Experimental = "";
    if (getInstance().revisedRating() > 0) Experimental = ".1";
    System.err.println("  version     serate "+"" + VERSION + "." + REVISION + SUBREV + Experimental + " (Sudoku Explainer) " + releaseDate);
        System.err.println("  author      Nicolas Juillerat");
        //relese
    System.err.println("  copyright   Copyright (c) 2006-" + releaseYear + " Nicolas Juillerat");
        //relese
    System.err.println("  license     " + releaseLicence + "(" + releaseLicenceMini + ") v" + releaseLicenceVersion);
        if (html != 0) {
            System.err.println("</PRE>");
            System.err.println("</BODY>");
            System.err.println("</HTML>");
        }
        System.exit(2);
    }
    /*
     * An example command line that demonstrates almost all of the formatting options:
        java -Xrs -Xmx1g -cp SukakuExplainer.jar diuf.sudoku.test.serate \
        --format="--format%l%%d: %d%l%%D: %D%l%%e: %e%l%%g: %g%l%%i: %i%l%%n: %n%l%%p: %p%l%%P: %P%l%%r: %r%l%%R: %R%l%%S: %S%l%%T: %T%l%%U: %U%l--- end of final section ---" \
        --start="--start%l%%g: %g%l%%i: %i%l%%m: %m%l%%M:%l%M%l%%n: %n%l--- end of start section ---" \
        --before="--before%l%%g: %g%l%%i: %i%l%%m: %m%l%%M:%l%M%l--- end of before section ---" \
        --after="--after%l%%e: %e%l%%h:%l%h%l%%g: %g%l%%i: %i%l%%m: %m%l%%M:%l%M%l%%r: %r%l%%s: %s%l--- end of after section ---" \
        --input=my_input_file.txt --output=- --threads=0 > my_output_file.txt
    */

    static void usage(String option, int argument) {
        System.err.println("serate: " + option + ((argument == 1) ? ": option argument expected" : ": unknown option"));
        System.err.println("Usage: serate [--input=FILE] [--after=FORMAT] [--before=FORMAT] [--format=FORMAT]");
        System.err.println("    [--revisedRating=N] [--output=FILE][--start=FORMAT] [--threads=N] [--showArguments]");
        System.err.println("    [--batch=N] [--pearl] [--totalTime] [--diamond] [--techs=TECHSTRING] [puzzle ...]");
       System.exit(2);
    }
    
    private static String isItExperimental() {
        if (getInstance().revisedRating() > 0)
            return ".1";
        return "";
    }

    /**
     * set usable techniques according to binary values string
     * Each character represents a technique, 0 for don't use
     * 1 for use
     */
    private static boolean setTechniques(String techniques, boolean showArguments) {
		EnumSet<SolvingTechnique> allTechniques = EnumSet.allOf(SolvingTechnique.class);
        try {   
            EnumSet<SolvingTechnique> useTechniques = EnumSet.noneOf(SolvingTechnique.class);
            Iterator<SolvingTechnique> iter = allTechniques.iterator();

            int i=0;
            while (iter.hasNext()) {
                if (techniques.length()-1 < i) { // error, string too short
                    throw new InterruptedException();
                }

                SolvingTechnique curTech = iter.next();
                if ( techniques.charAt(i) == '1' ) {
                    useTechniques.add(curTech);
                } else if ( techniques.charAt(i) != '0' ) {
                    throw new InterruptedException();
                }
                ++i;
            }
            if (techniques.length() > i) { // error, string too long
                throw new InterruptedException();
            }

            Settings.getInstance().setTechniques(EnumSet.copyOf(useTechniques));
        } catch (InterruptedException excep) {
            System.err.println("ERROR techniques setting, need "+allTechniques.size()+" 1/0 characters in second parameter, per each technique");
                System.err.println("The techniques are (in this order)");
                int i=1;
                EnumSet<SolvingTechnique> currentTechniques = Settings.getInstance().getTechniques();
				String techSet ="";
				for ( SolvingTechnique tech: allTechniques) {
                    int enabled = 1;
					if (!currentTechniques.contains(tech)) {
						enabled = 0;
					}
					System.err.println((i<10?"0":"")+i+": " + enabled + " " + tech.toString());
                    ++i;
					techSet += enabled;
                };
			System.err.println("\r\nDefault --techs="+ techSet);
            System.exit(3);
            return false;
        }
        if (showArguments) {
            System.out.println();
            System.out.println("The following techniques where set and unset:");
            int i=1;
            for ( SolvingTechnique tech: allTechniques) {
                System.out.println((i<10?"0":"")+i+", "+(techniques.charAt(i-1)=='1'?"Set   ":"Unset ")+tech.toString());
                ++i;
            };
        }   
        return true;
    }
    
    /**
     * Solve input puzzles and print results according to the output format.
     * @param args 81-char puzzles
     */
    public static void main(String[] args) {
        String          format = FORMAT;
        String          formatStart = "";
        String          formatAfter = "";
        String          formatBefore = "";
        String          input = null;
        String          output = "-";
        String          a;
        String          s;
        String          v;
        String          puzzle;
        boolean         totalTime = false;
        boolean         showArguments = false;
        BufferedReader  reader = null;
        PrintWriter     writer = null;
        int             numThreads = 1;
            //@SudokuMonster: 
        int             revisedRating = 0;//New revised Rating
        int             batchSolving = 0;//Batch soving mode
		int				FCPlus = 0;//non-trivial implications to add in FC+
		int				whichNC = 0;//Non consecutive 0:disabled 1:NC excluding 1,9 2:NC including 1,9
		boolean			islkSudokuBUG = true; //Fix to BUG algorithm by lkSudoku
		boolean			islkSudokuURUL = true; //Fix to UR and UL algorithm by lkSudoku
        boolean			isBlocks = true; //Blocks areb y default enabled. Disable to transform into Latin square (LQ)
        boolean			isDG = false; //Disjoint Groups are by default disabled.
        boolean			isWindows = false; //Windows are by default disabled.
        boolean			isX = false; //X (Main diagonal and Anti diagonal groups) are by default disabled.
        boolean			isGirandola = false; //Girandola is by default disabled.
        boolean			isAsterisk = false; //Asterisk is by default disabled.
        boolean			isCD = false; //Center Dot is by default disabled.	
        boolean			isAntiKnight = false;//AntiKnight is by default disabled.
		boolean			isAntiKing = false;//AntiKing is by default disabled.
		boolean			isToroidal = false;//Toroidal board is by default disabled.
		boolean			isTechs = false; //This checks if --techs has been used.		
		char            want = 0;
        int             arg;
        //long            t; //not used anymore
        long            tt = System.currentTimeMillis();
        char            c;
        boolean     incArg = false;
        boolean     addedArg = false;
        try {
            for (arg = 0; arg < args.length; arg++) {
                a = s = args[arg];
                if (s.charAt(0) != '-')
                    break;
                v = null;
                incArg = false;
                addedArg = false;
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
            //@SudokuMonster: 
                    else if (s.equals("version"))
                        c = 'V';
            //@SudokuMonster: 
                    else if (s.equals("batch"))
                        c = 'B';
            //@SudokuMonster: 
                    else if (s.equals("revisedRating"))
                        c = 'N';
            //@SudokuMonster: 
					else if (s.equals("islkSudokuBUG"))
                        c = 'G';                    
            //@SudokuMonster: 
					else if (s.equals("islkSudokuURUL"))
                        c = 'U';
            //@SudokuMonster: 
                    else if (s.equals("showArguments"))
                        c = 'S'; 
            //@SudokuMonster: 
                    else if (s.equals("isBlocks"))
                        c = 'Q'; 
            //@SudokuMonster: 
                    else if (s.equals("isDG"))
                        c = 'D'; 
            //@SudokuMonster: 
                    else if (s.equals("isWindows"))
                        c = 'W'; 
            //@SudokuMonster: 
                    else if (s.equals("isX"))
                        c = 'X'; 
            //@SudokuMonster: 
                    else if (s.equals("isGirandola"))
                        c = 'R'; 
            //@SudokuMonster: 
                    else if (s.equals("isAsterisk"))
                        c = 'K'; 
            //@SudokuMonster: 
                    else if (s.equals("isAntiKnight"))
                        c = 'n'; 
            //@SudokuMonster: 
                    else if (s.equals("isAntiKing"))
                        c = 'k'; 
            //@SudokuMonster: 
                    else if (s.equals("isToroidal"))
                        c = 'O'; 
             //@SudokuMonster: 
                    else if (s.equals("isCD"))
                        c = 'E'; 
           //@SudokuMonster: 
                    else if (s.equals("FCPlus"))
                        c = 'P'; 					
           //@SudokuMonster: 
                    else if (s.equals("isNC"))
                        c = 'c'; 					
                    else if (s.equals("after"))
                        c = 'a';
                    else if (s.equals("before"))
                        c = 'b';
                    else if (s.equals("start"))
                        c = 's';
                    else if (s.equals("threads"))
                        c = 't';
            //@SudokuMonster: 
                    else if (s.equals("techs"))
                    c = '~';
                    else if (s.equals("totalTime"))
                        c = 'T';
                    else
                        c = '?';
                }
                else {
                    c = s.charAt(1);
                    if (s.length() > 2)
                        v = s.substring(2);
                    else if (++arg < args.length) {
                        v = args[arg];
                        incArg = true;
                    }
                }
                switch (c) {
                case 'a':
                case 'b':
				case 'c':
                case 's':
                case 't':
                case 'f':
                case 'i':
                case 'o':
				case 'O':
                case 'B':
                case 'N':
                case 'G':
                case 'U':
                case 'Q':
                case 'P':				
                case 'D':
                case 'W':
                case 'X':
                case 'R':
                case 'K':
				case 'k':
				case 'n':
                case 'E':				
                case '~':
                    if (v == null)
                        usage(a, 1);
                    addedArg = true;
                    break;
                default:
                    if (incArg)
                        --arg;
                    break;
                }
                switch (c) {
                case 'S':
                    showArguments= true;
					System.out.print("v"+VER+" "+RELEASE);
                    break;
                case 'a':
                    formatAfter = v;
                    break;
                case 'b':
                    formatBefore = v;
                    break;
                case 's':
                    formatStart = v;
                    break;
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
                case 't':
                    numThreads = Integer.parseInt(v);
                    if(numThreads == 0) numThreads = Runtime.getRuntime().availableProcessors();
                    if(numThreads < 1) numThreads = 1; //no parallel processing
                    break;
            //@SudokuMonster: 
                case 'V':
                    System.out.println(VER);
                    System.exit(0);
                    break;
            //@SudokuMonster: 
                case 'T':
                    totalTime = true;
                    break;                  
            //@SudokuMonster: (lkSudoku +)
                case 'B':
                    batchSolving = Integer.parseInt(v);//0: Batch solving disabled 1: Batch solve applying lowest rated hints together 2: Batch solve applying all hints lower than maximum diffculty together
                    Settings.getInstance().setBatchSolving(batchSolving);
                    break;
            //@SudokuMonster: 
                case 'N':
                    revisedRating = Integer.parseInt(v);
                    Settings.getInstance().setRevisedRating(revisedRating);//0: No revised ratings 1:1st iteration of revised ratings
                    break;
            //@SudokuMonster: 
                case 'c':
                    whichNC = Integer.parseInt(v);
                    Settings.getInstance().setNC(whichNC);//0: disabled 1:NC excluding 1,9 2: NC including 1,9
                    break;
            //@SudokuMonster: 
                case 'G':
                    islkSudokuBUG = Integer.parseInt(v) != 0 ? true : false;
                    Settings.getInstance().setlkSudokuBUG(islkSudokuBUG);//0: islkSudokuBUG disabled //1:islkSudokuBUG enabled
                    break;
            //@SudokuMonster: 
                case 'U':
                    islkSudokuURUL = Integer.parseInt(v) != 0 ? true : false;
                    Settings.getInstance().setlkSudokuURUL(islkSudokuURUL);//0: islkSudokuURUL disabled //1:islkSudokuURUL enabled
                    break;
            //@SudokuMonster: 
                case 'Q':
                    isBlocks = Integer.parseInt(v) != 0 ? true : false;
                    Settings.getInstance().setBlocks(isBlocks);//0: isBlocks disabled //1:isBlocks enabled
                    break;					
            //@SudokuMonster: 
                case 'D':
                    isDG = Integer.parseInt(v) != 0 ? true : false;
                    Settings.getInstance().setDG(isDG);//0: isDG disabled //1:isDG enabled
                    break;					
             //@SudokuMonster: 
                case 'W':
                    isWindows = Integer.parseInt(v) != 0 ? true : false;
                    Settings.getInstance().setWindows(isWindows);//0: isWindows disabled //1:isWindows enabled
                    break;					
             //@SudokuMonster: 
                case 'X':
                    isX = Integer.parseInt(v) != 0 ? true : false;
                    Settings.getInstance().setX(isX);//0: isX disabled //1:isX enabled
                    break;					
             //@SudokuMonster: 
                case 'R':
                    isGirandola = Integer.parseInt(v) != 0 ? true : false;
                    Settings.getInstance().setGirandola(isGirandola);//0: isGirandola disabled //1:isGirandola enabled
                    break;					
             //@SudokuMonster: 
                case 'K':
                    isAsterisk = Integer.parseInt(v) != 0 ? true : false;
                    Settings.getInstance().setAsterisk(isAsterisk);//0: isAsterisk disabled //1:isAsterisk enabled
                    break;					
             //@SudokuMonster: 
                case 'n':
                    isAntiKnight = Integer.parseInt(v) != 0 ? true : false;
                    Settings.getInstance().setAntiKnight(isAntiKnight);//0: isAntiKnight disabled //1:isAntiKnight enabled
                    break;					
             //@SudokuMonster: 
                case 'k':
                    isAntiKing = Integer.parseInt(v) != 0 ? true : false;
                    Settings.getInstance().setAntiFerz(isAntiKing);//0: isAntiferz disabled //1:isAntiFerz enabled
                    break;					
             //@SudokuMonster: 
                case 'O':
                    isToroidal = Integer.parseInt(v) != 0 ? true : false;
                    Settings.getInstance().setToroidal(isToroidal);//0: isToroidal disabled //1:istoroidal enabled
                    break;					
             //@SudokuMonster: 
                case 'E':
                    isCD = Integer.parseInt(v) != 0 ? true : false;
                    Settings.getInstance().setCD(isCD);//0: isCD disabled //1:isCD enabled
                    break;					
             //@SudokuMonster: 
                case 'P':
                    FCPlus = Integer.parseInt(v);
                    Settings.getInstance().setFCPlus(FCPlus);//0: Default (similar to SE121) //1:More non trivial implications added //2: More non trivial implications added 
                    break;					
            //@SudokuMonster: (lkSudoku)
                case '~':
                    isTechs = setTechniques(v, showArguments);
                    break;                  
                    default:
                    usage(a, 0);
                    break;
                }
                String command = Character.toString(c);
                switch (c) {
                    case '~': command = "techs";
                    break;
                }
                if (showArguments)
                    System.out.print(" "+command+(addedArg?(""+v):""));
            }
            if (showArguments)
                System.out.println();
            //options were parsed
            Settings.getInstance().setNumThreads(numThreads); // make numThreads accessible at runtime from everywhere
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
            formatter = new Formatter(writer, formatStart, formatAfter, formatBefore, format);
            //@SudokuMonster: The following is to default to the default set of techniques even if variants
			if (!isTechs)
				Settings.getInstance().Settings_Variants();
            //loop over input puzzles
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
                if (puzzle.length() < 81) continue; //silently ignore short input lines or parameters
                {
                    //process puzzle
                    Grid grid = new Grid();
					grid.fromString(puzzle);
                    formatter.setPuzzleLine(puzzle);
                    grid.adjustPencilmarks();
                    //t = System.currentTimeMillis();
                    Solver solver = new Solver(grid);
                    solver.want = want;
                    if (puzzle.length() >= 81 && puzzle.length() < 729) {
                        solver.rebuildPotentialValues();
                    }
                    if (batchSolving < 1) {
                        // Step mode, no batch
                        solver.getDifficulty(formatter);
                    } else {
                        // Batch mode
                        solver.getBatchDifficulty(formatter);
                    }
                    //t = System.currentTimeMillis() - t;
                }
            }
        }
        catch(FileNotFoundException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        catch (NumberFormatException ex) { // --threads
            ex.printStackTrace();
        }
        finally {
            try {
                if (totalTime)
                    System.out.println("totalTime: " + Formatter.getTimeString(tt));
                if (reader != null)
                    reader.close();
                if (writer != null)
                    writer.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        //debug/improve counters
        //System.err.printf("Update = %d\tRead = %d\tGetPP = %d\n", Grid.numCellPencilmarksUpdate, Grid.numCellPencilmarksRead, Grid.numGetPP);
    } //main
    
    
    public static class Formatter {
        private PrintWriter writer;
        private String formatStart; //before each puzzle
        private String formatAfter; //after each step
        private String formatBefore; //before each step
        private String formatFinal; //after each puzzle
        private String puzzleLine; //the input line
        
        private long stepBeginTime;
        private long puzzleBeginTime;
        
        private int ordinal;
        
        public Formatter(PrintWriter writer, String formatStart, String formatAfter, String formatBefore, String formatFinal) {
            this.writer = writer;
            this.formatStart = formatStart;
            this.formatAfter = formatAfter;
            this.formatBefore = formatBefore;
            this.formatFinal = formatFinal;
            this.ordinal = 0;
        }

        public void beforeHint(Solver solver) {
            stepBeginTime = System.currentTimeMillis();
            
            if(formatBefore.isEmpty()) return;
            String s = new String();
            for (int i = 0; i < formatBefore.length(); i++) { //parse format
                char    f = formatBefore.charAt(i);
                if (f != '%' || ++i >= formatBefore.length()) { //literal
                    s += f;
                }
                else {
                    switch (f = formatBefore.charAt(i)) { //format specifier
                        case 'M':
                            s += solver.getGrid().toStringMultilinePencilmarks();
                            break;
                        case 'g':
                            s += puzzleLine;
                            break;
                        case 'i':
                            s += solver.getGrid().toString81();
                            break;
                        case 'l':
                            s += System.lineSeparator();
                            break;
                        case 'm':
                            s += solver.getGrid().toStringPencilmarks();
                            break;
                        case 't':
                            s += '\t';
                            break;
                        default:
                            s += f; //literal
                            break;
                    }
                }
            } //parse format
            if(! s.isEmpty()) { //don't output empty rows
                writer.println(s);
                writer.flush();
            }
        }
 
        public void afterHint(Solver solver, Hint hint) {
            if(formatAfter.isEmpty()) return;
            String s = new String();
            for (int i = 0; i < formatAfter.length(); i++) { //parse format
                char    f = formatAfter.charAt(i);
                if (f != '%' || ++i >= formatAfter.length()) { //literal
                    s += f;
                }
                else {
                    switch (f = formatAfter.charAt(i)) { //format specifier
                        case 'M':
                            s += solver.getGrid().toStringMultilinePencilmarks();
                            break;
                        case 'e':
                            s += getTimeString(stepBeginTime);
                            break;
                        case 'g':
                            s += puzzleLine;
                            break;
                        case 'h':
                            s += hint.toHtml(solver.getGrid());
                            break;
                        case 'i':
                            s += solver.getGrid().toString81();
                            break;
                        case 'l':
                            s += System.lineSeparator();
                            break;
                        case 'm':
                            s += solver.getGrid().toStringPencilmarks();
                            break;
                        case 'n':
                            s += ordinal;
                            break;
            //@SudokuMonster: 
                        case 'S':
                            s += solver.shortERtN;
                            break; 
                        case 'R':
                            s += solver.ERtN;
                            break;
                        case 'r':
                            s += ratingToString(((Rule)hint).getDifficulty());
                            break;
                        case 's':
                            s += hint.toString();
                            break;
                        case 't':
                            s += '\t';
                            break;
                        default:
                            s += f; //literal
                            break;
                    }
                }
            } //parse format
            if(! s.isEmpty()) { //don't output empty rows
                writer.println(s);
                writer.flush();
            }
        }

        public void beforePuzzle(Solver solver) {
            puzzleBeginTime = System.currentTimeMillis();
            ordinal++;
            
            if(formatStart.isEmpty()) return;
            String s = new String();
            for (int i = 0; i < formatStart.length(); i++) { //parse format
                char    f = formatStart.charAt(i);
                if (f != '%' || ++i >= formatStart.length()) { //literal
                    s += f;
                }
                else {
                    switch (f = formatStart.charAt(i)) { //format specifier
                        case 'M':
                            s += solver.getGrid().toStringMultilinePencilmarks();
                            break;
                        case 'g':
                            s += puzzleLine;
                            break;
                        case 'i':
                            s += solver.getGrid().toString81();
                            break;
                        case 'l':
                            s += System.lineSeparator();
                            break;
                        case 'm':
                            s += solver.getGrid().toStringPencilmarks();
                            break;
                        case 'n':
                            s += ordinal;
                            break;
                        case 't':
                            s += '\t';
                            break;
                        default:
                            s += f; //literal
                            break;
                    }
                }
            } //parse format
            if(! s.isEmpty()) { //don't output empty rows
                writer.println(s);
                writer.flush();
            }
        }
        
        public void afterPuzzle(Solver solver) {
            if(formatFinal.isEmpty()) return;
            String s = new String();
            for (int i = 0; i < formatFinal.length(); i++) { //parse format
                char    f = formatFinal.charAt(i);
                if (f != '%' || ++i >= formatFinal.length()) { //literal
                    s += f;
                }
                else {
                    switch (f = formatFinal.charAt(i)) { //format specifier
                        
            //@SudokuMonster: 
                        case 'U':
                            s += solver.shortEDtN;
                            break;
                        case 'D':
                            s += solver.EDtN;
                            break;
                        case 'd':
                            s += ratingToString(solver.diamond);
                            break;
                        case 'e':
                            s += getTimeString(puzzleBeginTime);
                            break;
                        case 'g':
                            s += puzzleLine;
                            break;
                        case 'i':
                            s += solver.getGrid().toString81();
                            break;
                        case 'l':
                            s += System.lineSeparator();
                            break;
                        case 'n':
                            s += ordinal;
                            break;
            //@SudokuMonster: 
                        case 'T':
                            s += solver.shortEPtN;
                            break;
                        case 'P':
                            s += solver.EPtN;
                            break;
                        case 'p':
                            s += ratingToString(solver.pearl);
                            break;
            //@SudokuMonster: 
                        case 'S':
                            s += solver.shortERtN;
                            break;
                        case 'R':
                            s += solver.ERtN;
                            break;
                        case 'r':
                            s += ratingToString(solver.difficulty);
                            break;
                        case 't':
                            s += '\t';
                            break;
                        default:
                            s += f; //literal
                            break;
                    }
                }
            } //parse format
            if(! s.isEmpty()) { //don't output empty rows
                writer.println(s);
                writer.flush();
            }
        }
        
        private String ratingToString(double rating) {
            String s = new String();
            int w = (int)((rating + 0.05) * 10);
            int p = w % 10;
            w /= 10;
            s += w + "." + p;
            return s;
        }
        
        public static String getTimeString(long oldTime) {
            String s = new String();
            long t = System.currentTimeMillis() - oldTime;
            long            u;
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
            return s;
        }

        public void setPuzzleLine(String puzzleLine) {
            this.puzzleLine = puzzleLine;
        }
    } //class Formatter
}
