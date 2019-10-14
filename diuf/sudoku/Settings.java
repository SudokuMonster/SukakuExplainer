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
    public final static int REVISION = 6;
    public final static String SUBREV = ".0";
	public final static String releaseDate = "2019-10-14";
	public final static String releaseYear = "2019";
	public final static String releaseLicence = "Lesser General Public License";
	public final static String releaseLicenceMini = "LGPL";
	public final static String releaseLicenceVersion = "2.1";		
    
    private static Settings instance = null;


	private int Fixed14Chaining = 0;//lksudoku chaining fix (Disabled by default)
	private int revisedRating = 0;//New change in rule order and rating (Disabled by default)
    private int batchSolving = 0;//lksudoku revised bacth solving (Disabled by default)
	private boolean isRCNotation = false;
    private boolean isAntialiasing = true;
    private boolean isShowingCandidates = true;
    private boolean isShowingCandidateMasks = true;
    private String lookAndFeelClassName = null;

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

    public static Settings getInstance() {
        if (instance == null)
            instance = new Settings();
        return instance;
    }

    public void setFixed14Chaining(int Fixed14Chaining) {
        this.Fixed14Chaining = Fixed14Chaining;
    }
    public int Fixed14Chaining() {
        return Fixed14Chaining;
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
