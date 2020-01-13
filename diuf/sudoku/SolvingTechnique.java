/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku;

public enum SolvingTechnique {

    HiddenSingle("Hidden Single"),
    DirectPointing("Direct Pointing"),
    DirectHiddenPair("Direct Hidden Pair"),
    NakedSingle("Naked Single"),
	forcingCellNC("Non-Consecutive Forcing Cell"),
	lockedNC("Locked Non-Consecutive"),
	forcingCellFNC("Ferz Non-Consecutive Forcing Cell"),
	lockedFNC("Ferz Locked Non-Consecutive"),
    DirectHiddenTriplet("Direct Hidden Triplet"),
    PointingClaiming("Pointing & Claiming"),
	VLocking("Generalized Intersections"),
    NakedPair("Naked Pair"),	
    NakedPairGen("Generalized Naked Pair"),
    XWing("X-Wing"),
    HiddenPair("Hidden Pair"),
    NakedTriplet("Naked Triplet"),
    NakedTripletGen("Generalized Naked Triplet"),
    Swordfish("Swordfish"),
    HiddenTriplet("Hidden Triplet"),
	TurbotFish("Scraper, Kite, Turbot"),
    XYWing("XY-Wing"),
    XYZWing("XYZ-Wing"),
//	WWing("W-Wing"),
	WXYZWing("WXYZ-Wing"),
    UniqueLoop("Unique Rectangle / Loop"),
    NakedQuad("Naked Quad"),
    NakedQuadGen("Generalized Naked Quad"),
    Jellyfish("Jellyfish"),
    HiddenQuad("Hidden Quad"),
	ThreeStrongLinks("3 Strong-linked Fishes"),
	//VWXYZWing4("VWXYZ-Wing 4"),
    //VWXYZWing5("VWXYZ-Wing 5"),
    NakedQuintGen("Generalized Naked Quintuplet"),
	NakedSextGen("Generalized Naked Sextuplet"),
	VWXYZWing("VWXYZ-Wing"),
    BivalueUniversalGrave("Bivalue Universal Grave"),
	FourStrongLinks("4 Strong-Linked Fishes"),
    AlignedPairExclusion("Aligned Pair Exclusion"),
	FiveStrongLinks("5 Strong-Linked Fishes"),	
	SixStrongLinks("6 Strong-Linked Fishes"),   
	UVWXYZWing("UVWXYZ-Wing"),
	ForcingChainCycle("Forcing Chains & Cycles"),
	TUVWXYZWing("TUVWXYZ-Wing"),
    AlignedTripletExclusion("Aligned Triplet Exclusion"),
    NishioForcingChain("Nishio Forcing Chains"),
    MultipleForcingChain("Multiple Forcing Chains"),
    DynamicForcingChain("Dynamic Forcing Chains"),
    DynamicForcingChainPlus("Dynamic Forcing Chains (+)"),
    NestedForcingChain("Nested Forcing Chains");

    private final String name;

    private SolvingTechnique(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
