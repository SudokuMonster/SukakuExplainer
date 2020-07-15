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
    public final static int REVISION = 17;
    public final static String SUBREV = ".7";
	public final static String releaseDate = "2020-07-15";
	public final static String releaseYear = "2020";
	public final static String releaseLicence = "Lesser General Public License";
	public final static String releaseLicenceMini = "LGPL";
	public final static String releaseLicenceVersion = "2.1";		
    
    private static Settings instance = null;


	private int revisedRating = 0;//New change in rule order and rating (Disabled by default)
	private boolean islkSudokuBUG = true; //Fix to BUG algorithm by lkSudoku
	private boolean islkSudokuURUL = true; //Fix to UR and UL algorithm by lkSudoku
    private int batchSolving = 0;//lksudoku revised bacth solving (Disabled by default)
	private int FCPlus = 0;//Increasing non-trivial implications used in FC+ //0: Default (Same as SE121) //1:More //2:More
	private boolean isRCNotation = true;
    private boolean isAntialiasing = true;
    private boolean isShowingCandidates = true;
    private boolean isShowingCandidateMasks = true;
	private boolean isBringBackSE121 = false;//SE121 technique set, order and ratings
    private String lookAndFeelClassName = null;
	public boolean isGUI = false;//true: GUI has been initialized mainly for preferences
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
	private int variantCount = 0;//Number of variants used
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
		//disabled as it may interfere with preferences when command / GUI are both is use
		//load() is called now from engine.main() once only
		//effectively loading the preferences once at GUI loading
		//No loading of preferences occurs with command line use
		//save() for options used by both command and GUI
		//therfore needs to be invoked from SudokuFrame only or Command changes
		//may result in GUI preferences changes
		//GUI only options can be left in Settings
		//load();
		Settings_Variants();
		if (isBringBackSE121())
			Settings_BBSE121();
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
    public void setGUI(boolean isGUI) {
        this.isGUI = isGUI;
    }
    public boolean isGUI() {
        return isGUI;
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
    }

    public boolean isRCNotation() {
        return this.isRCNotation;
    }

    public void setAntialiasing(boolean isAntialiasing) {
        this.isAntialiasing = isAntialiasing;
    }

    public boolean isAntialiasing() {
        return this.isAntialiasing;
    }

    public void setShowingCandidates(boolean value) {
        this.isShowingCandidates = value;
    }

    public boolean isShowingCandidates() {
        return this.isShowingCandidates;
    }

    public void setShowingCandidateMasks(boolean value) {
        this.isShowingCandidateMasks = value;
    }

    public boolean isShowingCandidateMasks() {
        return this.isShowingCandidateMasks;
    }

    public void setBringBackSE121(boolean value) {
        this.isBringBackSE121 = value;
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
		int variantsCount = 0;
		String temp = "";
		if (!isBlocks()) {
			temp += "Latin Square ";
			this.isVanilla = false;
		}
		if (isDG()) {
			temp += "Disjoint Groups ";
			variantsCount++;
		}
		if (isWindows()){
			temp += "Windows ";
			variantsCount++;
		}
		if (isAsterisk()){
			temp += "Asterisk ";
			variantsCount++;
		}
		if (isCD()){
			temp += "Center Dot ";
			variantsCount++;
		}
		if (isGirandola()){
			temp += "Girandola ";
			variantsCount++;
		}
		if (isX()){
			temp += "X ";
			variantsCount++;
		}
		if (isToroidal()){
			temp += "Toroidal ";
			variantsCount++;
		}
		if (isAntiFerz()){
			temp += "Anti-King ";
			variantsCount++;
		}
		if (isAntiKnight()){
			temp += "Anti-kNight ";
			variantsCount++;
		}
		if (isForbiddenPairs()){
			if (whichNC == 1){
				temp += "NC ";
				variantsCount++;
			}
			if (whichNC == 2){
				temp += "NC+ ";
				variantsCount++;
			}
			if (whichNC == 3){
				temp += "Ferz NC ";
				variantsCount++;
			}
			if (whichNC == 4){
				temp += "Ferz NC+ ";
				variantsCount++;
			}
		}
		if (isDG() || isWindows() || isX() || isGirandola() || isCD() || isAsterisk() || isAntiFerz() || isAntiKnight() /*|| isForbiddenPairs()*/) {
			this.isVLatin = false;
			this.isVanilla = false;
		}
		if (!isVLatin || isForbiddenPairs())
			setBringBackSE121(false);
		this.variantString = temp;
		setVariantsCount(variantsCount);
		Grid.changeVisibleCells();
    }
	
	public void setVariantsCount(int value){
		this.variantCount = value;
	}
	
    public int variantCount() {
        return this.variantCount;
    }	
	
	public void setBlocks(boolean isBlocks) {
        this.isBlocks = isBlocks;
		toggleVariants();
    }

    public boolean isBlocks() {
        return this.isBlocks;
    }

    public void setDG(boolean value) {
        this.isDG = value;
		toggleVariants();
    }

    public boolean isDG() {
        return this.isDG;
    }
	
    public void setX(boolean value) {
        this.isX = value;
		toggleVariants();
    }

    public boolean isX() {
        return this.isX;
    }

    public void setWindows(boolean value) {
        this.isWindows = value;
		toggleVariants();
    }

    public boolean isWindows() {
        return this.isWindows;
    }

    public void setGirandola(boolean value) {
        this.isGirandola = value;
		toggleVariants();
    }

    public boolean isGirandola() {
        return this.isGirandola;
    }

    public void setCD(boolean value) {
        this.isCD = value;
		toggleVariants();
    }

    public boolean isCD() {
        return this.isCD;
    }

    public void setAsterisk(boolean value) {
        this.isAsterisk = value;
		toggleVariants();
    }

    public boolean isAsterisk() {
        return this.isAsterisk;
    }

    public void setToroidal(boolean value) {
        this.isToroidal = value;
		toggleVariants();
    }

    public boolean isToroidal() {
        return this.isToroidal;
    }
	
    public void setAntiFerz(boolean value) {
        this.isAntiFerz = value;
		if (value)
			getInstance().setForbiddenPairs(true);
		else
			if (!(getInstance().isAntiKnight() || getInstance().whichNC() > 0))
				getInstance().setForbiddenPairs(false);
		toggleVariants();
    }

    public boolean isAntiFerz() {
        return this.isAntiFerz;
    }

    public void setAntiKnight(boolean value) {
        this.isAntiKnight = value;
		if (value)
			getInstance().setForbiddenPairs(true);
		else
			if (!(getInstance().isAntiFerz() || getInstance().whichNC() > 0))
				getInstance().setForbiddenPairs(false);
		toggleVariants();
    }

    public boolean isAntiKnight() {
        return this.isAntiKnight;
    }

    public void setForbiddenPairs(boolean value) {
        this.isForbiddenPairs = value;
		toggleVariants();
    }

    public boolean isForbiddenPairs() {
        return this.isForbiddenPairs;
    }

    public void setNC(int value) {
        this.whichNC = value;
		if (value > 0)
			getInstance().setForbiddenPairs(true);
		else
			if (!(getInstance().isAntiFerz() || getInstance().isAntiKnight()))
				getInstance().setForbiddenPairs(false);
		toggleVariants();
    }

    public int whichNC() {
        return this.whichNC;
    }

    public String getLookAndFeelClassName() {
        return this.lookAndFeelClassName;
    }

    public void setLookAndFeelClassName(String lookAndFeelClassName) {
        this.lookAndFeelClassName = lookAndFeelClassName;
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
		//techniques.remove(SolvingTechnique.FourStrongLinks);
		techniques.remove(SolvingTechnique.FiveStrongLinks);
 		techniques.remove(SolvingTechnique.SixStrongLinks);
		techniques.remove(SolvingTechnique.VLocking);
		techniques.remove(SolvingTechnique.NakedPairGen);
		techniques.remove(SolvingTechnique.NakedTripletGen);
		techniques.remove(SolvingTechnique.NakedQuadGen);
		techniques.remove(SolvingTechnique.NakedQuintGen);
		techniques.remove(SolvingTechnique.NakedSextGen);		
		if (isAntiFerz() || isAntiKnight() || isForbiddenPairs()) {
			//techniques.remove(SolvingTechnique.UniqueLoop);
			//techniques.remove(SolvingTechnique.BivalueUniversalGrave);
		}
    }

    private void initVariants() {
        techniques = EnumSet.allOf(SolvingTechnique.class);
		//default deselected techniques are added here
		techniques.remove(SolvingTechnique.PointingClaiming);
		//techniques.remove(SolvingTechnique.ThreeStrongLinks);
		//techniques.remove(SolvingTechnique.FourStrongLinks);
		techniques.remove(SolvingTechnique.FiveStrongLinks);
 		techniques.remove(SolvingTechnique.SixStrongLinks);
		techniques.remove(SolvingTechnique.NakedPair);
		techniques.remove(SolvingTechnique.NakedTriplet);
		techniques.remove(SolvingTechnique.NakedQuad);
		techniques.remove(SolvingTechnique.NakedQuintGen);
		techniques.remove(SolvingTechnique.NakedSextGen);		
		//SudokuMonster Deadly pattern can be restricted by FP so until Uniqueness/BUG
		//techniques are modified to accommodate FP then it is safer to remove them
		if (isAntiFerz() || isAntiKnight() || isForbiddenPairs()) {
			//techniques.remove(SolvingTechnique.UniqueLoop);
			//techniques.remove(SolvingTechnique.BivalueUniversalGrave);
		}
		if (isVLatin())
			if (isBringBackSE121() && !isForbiddenPairs())
				init121();
			else
				init();
		else
			setBringBackSE121(false);
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
		techniques.remove(SolvingTechnique.UVWXYZWing);
		techniques.remove(SolvingTechnique.TUVWXYZWing);		
		techniques.remove(SolvingTechnique.NakedPairGen);
		techniques.remove(SolvingTechnique.NakedTripletGen);
		techniques.remove(SolvingTechnique.NakedQuadGen);
		techniques.remove(SolvingTechnique.NakedQuintGen);
		techniques.remove(SolvingTechnique.NakedSextGen);		
    }

    public void load() {
		//if (this.isGUI) {
			try {
				Preferences prefs = Preferences.userNodeForPackage(Settings.class);
				if (prefs == null)
					return; // What can I do there ?
				lookAndFeelClassName = prefs.get("lookAndFeelClassName", lookAndFeelClassName);
				isRCNotation = prefs.getBoolean("isRCNotation", isRCNotation);
				isAntialiasing = prefs.getBoolean("isAntialiasing", isAntialiasing);
				isShowingCandidates = prefs.getBoolean("isShowingCandidates", isShowingCandidates);
				isShowingCandidateMasks = prefs.getBoolean("isShowingCandidateMasks", isShowingCandidateMasks);
				isBringBackSE121 = prefs.getBoolean("BringBackSE121", isBringBackSE121); 
				isBlocks = prefs.getBoolean("isBlocks", isBlocks);			
				isX = prefs.getBoolean("isX", isX);			
				isDG = prefs.getBoolean("isDG", isDG);			
				isWindows = prefs.getBoolean("isWindows", isWindows);
				isAsterisk = prefs.getBoolean("isAsterisk", isAsterisk);
				isCD = prefs.getBoolean("isCD", isCD);
				isGirandola = prefs.getBoolean("isGirandola", isGirandola);
				isForbiddenPairs = prefs.getBoolean("isForbiddenPairs", isForbiddenPairs);
				whichNC = prefs.getInt("whichNC", whichNC);
				isAntiFerz = prefs.getBoolean("isAntiFerz", isAntiFerz);
				isAntiKnight = prefs.getBoolean("isAntiKnight", isAntiKnight);            
				isToroidal = prefs.getBoolean("isToroidal", isToroidal);            
				revisedRating = prefs.getInt("RevisedRatings", revisedRating);
				toggleVariants();
				Settings_Variants();
			} catch (SecurityException ex) {
				// Maybe we are running from an applet. Do nothing
			}
		//}
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
			prefs.putInt("whichNC", whichNC);
			prefs.putBoolean("isAntiFerz", isAntiFerz);
			prefs.putBoolean("isAntiKnight", isAntiKnight);
			prefs.putBoolean("isToroidal", isToroidal);
			prefs.putBoolean("BringBackSE121", isBringBackSE121);
			prefs.putInt("RevisedRatings", revisedRating);
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
