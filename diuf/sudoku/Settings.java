/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku;

import java.util.*;
import java.util.prefs.*;

import java.io.PrintWriter;

/**
 * Global settings of the application.
 * Implemented using the singleton pattern.
 */
public class Settings {

    public final static int VERSION = 1;
    public final static int REVISION = 15;
    public final static String SUBREV = ".5";
	public final static String releaseDate = "2019-12-30";
	public final static String releaseYear = "2019";
	public final static String releaseLicence = "Lesser General Public License";
	public final static String releaseLicenceMini = "LGPL";
	public final static String releaseLicenceVersion = "2.1";		
    
    private static Settings instance = null;


	private int revisedRating = 0;//New change in rule order and rating (Disabled by default)
	private boolean islkSudokuBUG = true; //Fix to BUG algorithm by lkSudoku
	private boolean islkSudokuURUL = true; //Fix to UR and UL algorithm by lkSudoku
    private int batchSolving = 0;//lksudoku revised bacth solving (Disabled by default)
	private int FCPlus = 0;//Increasing non-trivial implications used in FC+ //0: Default (Same as SE121) //1:More //2:More
	private boolean isRCNotation = false;
    private boolean isAntialiasing = true;
    private boolean isShowingCandidates = true;
    private boolean isShowingCandidateMasks = true;
	private boolean isBringBackSE121 = false;//SE121 technique set, order and ratings
    private String lookAndFeelClassName = null;
	//Variants
	private boolean isBlocks = true;//Sudoku: true Latin Square: false
	private boolean isX = false;//2 Regions, 9 cells x 2. Each forming a main diagonal, intersecting at r5c5 (40)
	private boolean isDG = false;//Disjoint Groups (P). with Vanilla (SudokuP)
	private boolean isWindows = false;//4 Windows hich force 5 hidden groups (W). with Sudoku (Windoku, HyperSudoku, SudokuW)
	private boolean isAsterisk = false;//9 cell asterisk group
	private boolean isCD = false;//9 cell Centre (center) Dot group
	private boolean isGirandola = false;//9 cell Girandola group
	private boolean isToroidal = false;//Toroidal Board
	private boolean isAntiFerz = false;//(0,1) diagonal neighbouring cells
	private boolean isAntiKnight = false;//(1,2) cells that are a knight chess move away
	private boolean isVanilla = true;//Check to see if we are using variants (to minimize extra code calls use in Vanilla sudoku)
	private boolean isForbiddenPairs = false;//Check to see if we are using Forbidden pairs (NC or cNC, ...) but doeasn't apply to Antichess
	private int whichNC = 0;//0: disabled (default) 1:NC 1-9 but excludes (9,1) 2:cNC which include (1,9)
	private boolean isVLatin = true;//Check to see if we are using variants with Latin Square(to minimize extra code calls use in Latin square)	
	public String variantString = "";
	public final static int[] regionsWindows = 
		{0,5,5,5,0,6,6,6,0,
		 7,1,1,1,7,2,2,2,7,
		 7,1,1,1,7,2,2,2,7,
		 7,1,1,1,7,2,2,2,7,
		 0,5,5,5,0,6,6,6,0,
		 8,3,3,3,8,4,4,4,8,
		 8,3,3,3,8,4,4,4,8,
		 8,3,3,3,8,4,4,4,8,
		 0,5,5,5,0,6,6,6,0};
	public final static int[] regionsGirandola = 
		{1,0,0,0,0,0,0,0,1,
		 0,0,0,0,1,0,0,0,0,
		 0,0,0,0,0,0,0,0,0,
		 0,0,0,0,0,0,0,0,0,
		 0,1,0,0,1,0,0,1,0,
		 0,0,0,0,0,0,0,0,0,
		 0,0,0,0,0,0,0,0,0,
		 0,0,0,0,1,0,0,0,0,
		 1,0,0,0,0,0,0,0,1};
	public final static int[] regionsCD =
		{0,0,0,0,0,0,0,0,0,
		 0,1,0,0,1,0,0,1,0,
		 0,0,0,0,0,0,0,0,0,
		 0,0,0,0,0,0,0,0,0,
		 0,1,0,0,1,0,0,1,0,
		 0,0,0,0,0,0,0,0,0,
		 0,0,0,0,0,0,0,0,0,
		 0,1,0,0,1,0,0,1,0,
		 0,0,0,0,0,0,0,0,0};
	public final static int[] regionsAsterisk =
		{0,0,0,0,0,0,0,0,0,
		 0,0,0,0,1,0,0,0,0,
		 0,0,1,0,0,0,1,0,0,
		 0,0,0,0,0,0,0,0,0,
		 0,1,0,0,1,0,0,1,0,
		 0,0,0,0,0,0,0,0,0,
		 0,0,1,0,0,0,1,0,0,
		 0,0,0,0,1,0,0,0,0,
		 0,0,0,0,0,0,0,0,0};
	public final static int[] regionsMainDiagonal =
		{1,0,0,0,0,0,0,0,0,
		 0,1,0,0,0,0,0,0,0,
		 0,0,1,0,0,0,0,0,0,
		 0,0,0,1,0,0,0,0,0,
		 0,0,0,0,1,0,0,0,0,
		 0,0,0,0,0,1,0,0,0,
		 0,0,0,0,0,0,1,0,0,
		 0,0,0,0,0,0,0,1,0,
		 0,0,0,0,0,0,0,0,1};
	public final static int[] regionsAntiDiagonal =
		{0,0,0,0,0,0,0,0,1,
		 0,0,0,0,0,0,0,1,0,
		 0,0,0,0,0,0,1,0,0,
		 0,0,0,0,0,1,0,0,0,
		 0,0,0,0,1,0,0,0,0,
		 0,0,0,1,0,0,0,0,0,
		 0,0,1,0,0,0,0,0,0,
		 0,1,0,0,0,0,0,0,0,
		 1,0,0,0,0,0,0,0,0};
	public final static int[] regionsBothDiagonals =
		{1,0,0,0,0,0,0,0,1,
		 0,1,0,0,0,0,0,1,0,
		 0,0,1,0,0,0,1,0,0,
		 0,0,0,1,0,1,0,0,0,
		 0,0,0,0,1,0,0,0,0,
		 0,0,0,1,0,1,0,0,0,
		 0,0,1,0,0,0,1,0,0,
		 0,1,0,0,0,0,0,1,0,
		 1,0,0,0,0,0,0,0,1};
	public final static int[] regionsDG =
		{0,1,2,0,1,2,0,1,2,
		3,4,5,3,4,5,3,4,5,
		6,7,8,6,7,8,6,7,8,
		0,1,2,0,1,2,0,1,2,
		3,4,5,3,4,5,3,4,5,
		6,7,8,6,7,8,6,7,8,
		0,1,2,0,1,2,0,1,2,
		3,4,5,3,4,5,3,4,5,
		6,7,8,6,7,8,6,7,8};
	public final static int[] regionsNoVariants =
		{0,0,0,0,0,0,0,0,0,
		 0,0,0,0,0,0,0,0,0,
		 0,0,0,0,0,0,0,0,0,
		 0,0,0,0,0,0,0,0,0,
		 0,0,0,0,0,0,0,0,0,
		 0,0,0,0,0,0,0,0,0,
		 0,0,0,0,0,0,0,0,0,
		 0,0,0,0,0,0,0,0,0,
		 0,0,0,0,0,0,0,0,0};
	
    private EnumSet<SolvingTechnique> techniques;
    
    private int numThreads = 1;
    private boolean bestHintOnly = false;

	// lksudoku serate log steps
	private boolean 		isLogSolution = false;
	private PrintWriter		logWriter = null;

	public void setLog( PrintWriter writeLog ) {
		isLogSolution = true;
		logWriter = writeLog;
	}

	public boolean isLog() {
		return isLogSolution;
	}

	public PrintWriter getLogWriter() {
		return logWriter;
	}


    private Settings() {
        init();
        load();
    }
	
    public void Settings_BBSE121() {
        init121();
    }	

    public void Settings_Variants() {
        initVariants();
    }	

    public static Settings getInstance() {
        if (instance == null)
            instance = new Settings();
        return instance;
    }
    public void setlkSudokuBUG(boolean islkSudokuBUG) {
        this.islkSudokuBUG = islkSudokuBUG;
    }
    public boolean islkSudokuBUG() {
        return islkSudokuBUG;
    }	
    public void setlkSudokuURUL(boolean islkSudokuURUL) {
        this.islkSudokuURUL = islkSudokuURUL;
    }
    public boolean islkSudokuURUL() {
        return islkSudokuURUL;
    }	
	public void setRevisedRating(int revisedRating) {
        this.revisedRating = revisedRating;
    }
    public int revisedRating() {
        return revisedRating;
    }

    public void setBatchSolving(int batchSolving) {
        this.batchSolving = batchSolving;
    }
    public int batchSolving() {
        return batchSolving;
    }

    public void setFCPlus(int FCPlus) {
        this.FCPlus = FCPlus;
    }
    public int FCPlus() {
        return FCPlus;
    }

    public void setRCNotation(boolean isRCNotation) {
        this.isRCNotation = isRCNotation;
        save();
    }

    public boolean isRCNotation() {
        return isRCNotation;
    }

    public void setAntialiasing(boolean isAntialiasing) {
        this.isAntialiasing = isAntialiasing;
        save();
    }

    public boolean isAntialiasing() {
        return this.isAntialiasing;
    }

    public void setShowingCandidates(boolean value) {
        this.isShowingCandidates = value;
        save();
    }

    public boolean isShowingCandidates() {
        return this.isShowingCandidates;
    }

    public void setShowingCandidateMasks(boolean value) {
        this.isShowingCandidateMasks = value;
        save();
    }

    public boolean isShowingCandidateMasks() {
        return this.isShowingCandidateMasks;
    }

    public void setBringBackSE121(boolean value) {
        this.isBringBackSE121 = value;
        save();
    }

    public boolean isBringBackSE121() {
        return this.isBringBackSE121;
    }
//SudokuMonster: isVanilla() controls added variants and therefore is the check needed for Old style sukaku Explainer
    public boolean isVanilla() {
        return this.isVanilla;
    }
//SudokuMonster: isVLatin() controls added variants and therefore is the check needed for Latin square without added variants
    public boolean isVLatin() {
        return this.isVLatin;
    }	

	public void toggleVariants() {
        this.isVanilla = true;
		this.isVLatin = true;
		String temp = "";
		if (!isBlocks()) {
			temp += "Latin Square ";
			this.isVanilla = false;
		}
		if (isDG())
			temp += "Disjoint Groups ";
		if (isWindows())
			temp += "Windows ";
		if (isAsterisk())
			temp += "Asterisk ";
		if (isCD())
			temp += "Center Dot ";
		if (isGirandola())
			temp += "Girandola ";
		if (isX())
			temp += "X ";
		if (isToroidal())
			temp += "Toroidal ";
		if (isAntiFerz())
			temp += "Anti-King ";
		if (isAntiKnight())
			temp += "Anti-kNight ";
		if (isForbiddenPairs())
			if (whichNC == 1)
				temp += "NC ";
			else
				if (whichNC == 2)
					temp += "NC+ ";
		if (isDG() || isWindows() || isX() || isGirandola() || isCD() || isAsterisk() || isAntiFerz() || isAntiKnight()/* || isForbiddenPairs()*/) {
			this.isVLatin = false;
			this.isVanilla = false;
		}
		this.variantString = temp;
		Grid.changeVisibleCells();
    }
	
	public void setBlocks(boolean isBlocks) {
        this.isBlocks = isBlocks;
		toggleVariants();;
        save();
    }

    public boolean isBlocks() {
        return this.isBlocks;
    }

    public void setDG(boolean value) {
        this.isDG = value;
		toggleVariants();;
        save();
    }

    public boolean isDG() {
        return this.isDG;
    }
	
    public void setX(boolean value) {
        this.isX = value;
		toggleVariants();
        save();
    }

    public boolean isX() {
        return this.isX;
    }

    public void setWindows(boolean value) {
        this.isWindows = value;
		toggleVariants();;
         save();
    }

    public boolean isWindows() {
        return this.isWindows;
    }

    public void setGirandola(boolean value) {
        this.isGirandola = value;
		toggleVariants();;
         save();
    }

    public boolean isGirandola() {
        return this.isGirandola;
    }

    public void setCD(boolean value) {
        this.isCD = value;
		toggleVariants();;
         save();
    }

    public boolean isCD() {
        return this.isCD;
    }

    public void setAsterisk(boolean value) {
        this.isAsterisk = value;
		toggleVariants();
         save();
    }

    public boolean isAsterisk() {
        return this.isAsterisk;
    }

    public void setToroidal(boolean value) {
        this.isToroidal = value;
		toggleVariants();		
        save();
    }

    public boolean isToroidal() {
        return this.isToroidal;
    }
	
    public void setAntiFerz(boolean value) {
        this.isAntiFerz = value;
		toggleVariants();
        save();
    }

    public boolean isAntiFerz() {
        return this.isAntiFerz;
    }

    public void setAntiKnight(boolean value) {
        this.isAntiKnight = value;
		toggleVariants();
        save();
    }

    public boolean isAntiKnight() {
        return this.isAntiKnight;
    }

    public void setForbiddenPairs(boolean value) {
        this.isForbiddenPairs = value;
		toggleVariants();
        save();
    }

    public boolean isForbiddenPairs() {
        return this.isForbiddenPairs;
    }

    public void setNC(int value) {
        this.whichNC = value;
		if (value > 0)
			setForbiddenPairs(true);
		toggleVariants();
        save();
    }

    public int whichNC() {
        return this.whichNC;
    }

    public String getLookAndFeelClassName() {
        return lookAndFeelClassName;
    }

    public void setLookAndFeelClassName(String lookAndFeelClassName) {
        this.lookAndFeelClassName = lookAndFeelClassName;
        save();
    }

    public EnumSet<SolvingTechnique> getTechniques() {
        return EnumSet.copyOf(this.techniques);
    }	

    public void setTechniques(EnumSet<SolvingTechnique> techniques) {
        this.techniques = techniques;
    }

    public boolean isUsingAllTechniques() {
        EnumSet<SolvingTechnique> all = EnumSet.allOf(SolvingTechnique.class);
        return this.techniques.equals(all);
    }

    public boolean isUsingOneOf(SolvingTechnique... solvingTechniques) {
        for (SolvingTechnique st : solvingTechniques) {
            if (this.techniques.contains(st))
                return true;
        }
        return false;
    }

    public boolean isusingAll(SolvingTechnique... solvingTechniques) {
        for (SolvingTechnique st : solvingTechniques) {
            if (!this.techniques.contains(st))
                return false;
        }
        return true;
    }

    public boolean isUsingAllButMaybeNot(SolvingTechnique... solvingTechniques) {
        List<SolvingTechnique> list = Arrays.asList(solvingTechniques);
        for (SolvingTechnique st : EnumSet.allOf(SolvingTechnique.class)) {
            if (!this.techniques.contains(st) && !list.contains(st))
                return false;
        }
        return true;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }
    
    public int getNumThreads() {
        return this.numThreads;
    }
    
    public void setBestHintOnly(boolean bestHintOnly) {
        this.bestHintOnly = bestHintOnly;
    }
    
    public boolean getBestHintOnly() {
        return this.bestHintOnly;
    }
    
    //  Load / Save

    private void init() {
        techniques = EnumSet.allOf(SolvingTechnique.class);
		//default deselected techniques are added here
		techniques.remove(SolvingTechnique.FourStrongLinks);
		techniques.remove(SolvingTechnique.FiveStrongLinks);
 		techniques.remove(SolvingTechnique.SixStrongLinks);
		techniques.remove(SolvingTechnique.VLocking);
		techniques.remove(SolvingTechnique.NakedPairGen);
		techniques.remove(SolvingTechnique.NakedTripletGen);
		techniques.remove(SolvingTechnique.NakedQuadGen);
		techniques.remove(SolvingTechnique.NakedQuintGen);
    }

    private void initVariants() {
        techniques = EnumSet.allOf(SolvingTechnique.class);
		//default deselected techniques are added here
		techniques.remove(SolvingTechnique.PointingClaiming);
		techniques.remove(SolvingTechnique.ThreeStrongLinks);
		techniques.remove(SolvingTechnique.FourStrongLinks);
		techniques.remove(SolvingTechnique.FiveStrongLinks);
 		techniques.remove(SolvingTechnique.SixStrongLinks);
		techniques.remove(SolvingTechnique.NakedPair);
		techniques.remove(SolvingTechnique.NakedTriplet);
		techniques.remove(SolvingTechnique.NakedQuad);
		techniques.remove(SolvingTechnique.NakedQuintGen);
		//SudokuMonster Deadly pattern can be restricted by FP so until Uniqueness/BUG
		//techniques are modified to accommodate FP then it is safer to remove them
		if (isAntiFerz() || isAntiKnight() || isForbiddenPairs()) {
			techniques.remove(SolvingTechnique.UniqueLoop);
			techniques.remove(SolvingTechnique.BivalueUniversalGrave);
		}
		if (isVLatin())
			init();
    }
    private void init121() {
        techniques = EnumSet.allOf(SolvingTechnique.class);
		//The following techniques are not part of SE121
		techniques.remove(SolvingTechnique.VLocking);
		techniques.remove(SolvingTechnique.TurbotFish);
		techniques.remove(SolvingTechnique.ThreeStrongLinks);
		techniques.remove(SolvingTechnique.FourStrongLinks);
		techniques.remove(SolvingTechnique.FiveStrongLinks);
 		techniques.remove(SolvingTechnique.SixStrongLinks);
		techniques.remove(SolvingTechnique.WXYZWing);
		techniques.remove(SolvingTechnique.VWXYZWing);
		techniques.remove(SolvingTechnique.NakedPairGen);
		techniques.remove(SolvingTechnique.NakedTripletGen);
		techniques.remove(SolvingTechnique.NakedQuadGen);
		techniques.remove(SolvingTechnique.NakedQuintGen);
    }

    public void load() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(Settings.class);
            if (prefs == null)
                return; // What can I do there ?
            isRCNotation = prefs.getBoolean("isRCNotation", isRCNotation);
            isAntialiasing = prefs.getBoolean("isAntialiasing", isAntialiasing);
            isShowingCandidates = prefs.getBoolean("isShowingCandidates", isShowingCandidates);
            isShowingCandidateMasks = prefs.getBoolean("isShowingCandidateMasks", isShowingCandidateMasks);
            isBringBackSE121 = prefs.getBoolean("BringBackSE121", isBringBackSE121);            
			lookAndFeelClassName = prefs.get("lookAndFeelClassName", lookAndFeelClassName);
        } catch (SecurityException ex) {
            // Maybe we are running from an applet. Do nothing
        }
    }

    public void save() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(Settings.class);
            if (prefs == null)
                return;
            prefs.putBoolean("isRCNotation", isRCNotation);
            prefs.putBoolean("isAntialiasing", isAntialiasing);
            prefs.putBoolean("isShowingCandidates", isShowingCandidates);
            prefs.putBoolean("isShowingCandidateMasks", isShowingCandidateMasks);
            prefs.putBoolean("isBlocks", isBlocks);			
            prefs.putBoolean("isX", isX);			
            prefs.putBoolean("isDG", isDG);			
            prefs.putBoolean("isWindows", isWindows);
			prefs.putBoolean("isAsterisk", isAsterisk);
			prefs.putBoolean("isCD", isCD);
			prefs.putBoolean("isGirandola", isGirandola);
			prefs.putBoolean("isForbiddenPairs", isForbiddenPairs);
			prefs.putBoolean("isAntiFerz", isAntiFerz);
			prefs.putBoolean("isAntiKnight", isAntiKnight);			
			if (lookAndFeelClassName != null)
                prefs.put("lookAndFeelClassName", lookAndFeelClassName);
            try {
                prefs.flush();
            } catch (BackingStoreException ex) {
                ex.printStackTrace();
            }
        } catch (SecurityException ex) {
            // Maybe we are running from an applet. Do nothing
        }
    }

}
