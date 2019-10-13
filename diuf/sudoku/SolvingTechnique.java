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
    DirectHiddenTriplet("Direct Hidden Triplet"),
    PointingClaiming("Pointing & Claiming"),
    NakedPair("Naked Pair"),
    XWing("X-Wing"),
    HiddenPair("Hidden Pair"),
    NakedTriplet("Naked Triplet"),
    Swordfish("Swordfish"),
    HiddenTriplet("Hidden Triplet"),
	TurbotFish("Scraper, Kite, Turbot"),
    XYWing("XY-Wing"),
    XYZWing("XYZ-Wing"),
//	WWing("W-Wing"),
	WXYZWing("WXYZ-Wing"),
    UniqueLoop("Unique Rectangle / Loop"),
    NakedQuad("Naked Quad"),
    Jellyfish("Jellyfish"),
    HiddenQuad("Hidden Quad"),
	//VWXYZWing4("VWXYZ-Wing 4"),
    //VWXYZWing5("VWXYZ-Wing 5"),
	VWXYZWing("VWXYZ-Wing"),
    BivalueUniversalGrave("Bivalue Universal Grave"),
    AlignedPairExclusion("Aligned Pair Exclusion"),
    ForcingChainCycle("Forcing Chains & Cycles"),
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
