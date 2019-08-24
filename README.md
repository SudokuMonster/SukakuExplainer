# Sukaku Explainer:

Is a sukaku (Pencilmark Sudoku) and sudoku Solver/Rater with a command line entry point and a GUI. It is based on serate
modification of &quot;Sudoku Explainer&quot; by Nicolas Juillerat

## Installation:

Visit the [releases page](https://github.com/SudokuMonster/SukakuExplainer/releases) to download the latest version(s) of Sukaku Explainer and Serate.

## Usage:

### NAME

  serate - Sukaku and Sudoku Explainer command line rating

### SYNOPSIS

  serate [--diamond] [--after=FORMAT] [--before=FORMAT] [--format=FORMAT]
    [--input=FILE] [--output=FILE] [--pearl] [--start=FORMAT] [--threads=N] [puzzle ...]

### DESCRIPTION

  serate is a Sudoku/Sukaku Explainer command line entry point that rates one or more

  input puzzles.  If an --input=FILE option is specified then 729-character per line

  Sukaku puzzles are read from that file, otherwise if the file doesn&#39;t contain

  729-character per line Sukaku puzzles then 81-character sudoku puzzle

  strings are read from that file, otherwise if 81-character puzzle operands are

  not specified the puzzles are read from the standard input.  If an --output=FILE

  option is specified then the output is written to that file, otherwise output

  is written to the standard output.  The output is controlled by the

  --format=FORMAT option.

  Ratings are floating point numbers in the range 0.0 - 20.0, rounded to the

  tenths digit.  0.0 indicates a processing error and 20.0 indicates a valid

  but otherwise unsolvable input puzzle.

  For GUI, hints and pencilmarks please see INVOCATION then EXAMPLES

### OPTIONS (serate)

  -a, --after=FORMAT

      Format the output after each step according to FORMAT. Default is empty.

  -b, --before=FORMAT

      Format the output before each step according to FORMAT. Default is empty.

  -d, --diamond

      Terminate rating if the puzzle is not a diamond.

  -f, --format=FORMAT

      Format the output for each input puzzle according to FORMAT.  Format

      conversion are %CHARACTER; all other characters are output unchanged.

      The default format is %r/%p/%d.  The format conversions are:

        %d  The diamond rating.  This is the highest ER of the methods leading

            to the first candidate elimination. (F)

        %e  The elapsed time to rate the puzzle. (AF)

        %h  The long step description in multi-line HTML format. (A)

        %g  The input puzzle line. (SBAF)

        %i  The puzzle grid in 81-character [0-9] form. (SBAF)

        %l  The new line. (SBAF)

        %m  The input puzzle pencilmarks in 729-char format. (SBA)

        %M  The input puzzle pencilmarks in multi-line format. (SBA)

        %n  The input puzzle ordinal, counting from 1. (SF)

        %p  The pearl rating.  This is the highest ER of the methods leading

            to the first cell placement. (F)

        %r  The puzzle rating.  This is the highest ER of the methods leading

            to the puzzle solution. (AF)

        %s  The short step description. (A)

        %t  The tab character. (SBAF)

        %%  The % character.

  -h, --html

      List detailed info in html.

  -i, --input=FILE

      Read 81-character puzzle strings, one per line, from FILE.  By default

      operands are treated as 81-character puzzle strings.  If no operands are

      specified then the standard input is read.

  -m, --man

      List detailed info in displayed man page form.

  -o, --output=FILE

      Write output to FILE instead of the standard output.

  -p, --pearl

      Terminate rating if the puzzle is not a pearl.

  -s, --start=FORMAT

      Format the output before each puzzle according to FORMAT. Default is empty.

  -t, --threads

      Maximal degree of parrallelism. Default 0=auto. 1=no parallelism; -1=unlimited

  -V, --version

      Print the Sudoku Explainer (serate) version and exit.

### INVOCATION

  GUI:

        java.exe -jar SukakuExplainer.jar

  Command Line:

        java.exe -Xrs -Xmx500m -cp SukakuExplainer.jar diuf.sudoku.test.serate ...

### GUI

  Functionality is retained with vanilla sudoku puzzles. It also accepts parsing any
  Pencilmark grid or Sukaku

### Examples

  Note: % must be entered as %% in windows .bat files and shortcut commands.

  To rate a single or a group of puzzle(s):

        java.exe -Xrs -Xmx500m -cp SukakuExplainer.jar diuf.sudoku.test.serate
         --format=&quot;%g ED=%r/%p/%d&quot; --input=puzzles.txt --output=puzzles.rated.txt
		 
  To display all supported format parameters (at the time of writing this document):
  
        java -Xrs -Xmx1g -cp SukakuExplainer.jar diuf.sudoku.test.serate \
		
        --format="--format%l%%d: %d%l%%e: %e%l%%g: %g%l%%i: %i%l%%n: %n%l%%p: %p%l%%r: %r%l--- end of final section ---" \
		
        --start="--start%l%%g: %g%l%%i: %i%l%%m: %m%l%%M:%l%M%l%%n: %n%l--- end of start section ---" \
		
        --before="--before%l%%g: %g%l%%i: %i%l%%m: %m%l%%M:%l%M%l--- end of before section ---" \
		
        --after="--after%l%%e: %e%l%%h:%l%h%l%%g: %g%l%%i: %i%l%%m: %m%l%%M:%l%M%l%%r: %r%l%%s: %s%l--- end of after section ---" \
		
        --input=my_input_file.txt --output=- --threads=0 > my_output_file.txt
  
  To display man document:
  
         java -cp SukakuExplainer/SukakuExplainer.jar diuf.sudoku.test.serate --man

## Contributors:

@dobrichev

@1to9only

@SudokuMonster

@sudokuwiki

## About:

Sudoku Explainer (Nicolas Juillerat 2006-2019) is a popular Sudoku solver/generator/rater which solves every known Sudoku
puzzle using logical techniques. This popularity and consistency was the basis of the [Patterns game](http://forum.enjoysudoku.com/patterns-game-1-5-t5760.html) on
the &quot;New Sudoku Players Forum&quot;. It was further modified to incorporate &quot;serate&quot; which allowed
the explainer rating (ER), the pearl rating (EP) and he diamond rating (ED).

Pencilmark Sudoku (Sukaku) is the normal extension of Sudoku and [Sukaku Explainer was developed](http://forum.enjoysudoku.com/help-with-sudoku-explainer-t6677-60.html) based
on the serate modification of Sudoku explainer

## Credits:

Nicolas Juillerat, Sudoku Explainer author

gsf, serate modification of Sudoku Explainer

## License:

GNU Lesser General Public License v2.1
