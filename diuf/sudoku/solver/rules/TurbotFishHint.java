package diuf.sudoku.solver.rules;

import java.util.*;
import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.chaining.*;
import diuf.sudoku.tools.*;


/**
 * Turbot Fish hints
 */
public class TurbotFishHint extends IndirectHint implements Rule, HasParentPotentialHint {

    private final int value;
    private final Cell startCell;
    private final Cell endCell;
    private final Cell bridgeCell1;
    private final Cell bridgeCell2;
    private final Grid.Region baseSet;
    private final Grid.Region coverSet;
    private final Grid.Region shareRegion;

    public TurbotFishHint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials,
            Cell startCell, Cell endCell, Cell bridgeCell1, Cell bridgeCell2,
            int value, Grid.Region base, Grid.Region cover, Grid.Region shareRegion) {
        super(rule, removablePotentials);
        this.value = value;
        this.startCell = startCell;
        this.endCell = endCell;
        this.bridgeCell1 = bridgeCell1;
        this.bridgeCell2 = bridgeCell2;
        this.baseSet = base;
        this.coverSet = cover;
        this.shareRegion = shareRegion;
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public Cell[] getSelectedCells() {
        return new Cell[] { startCell, endCell };
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>();
        BitSet fishDigitSet = SingletonBitSet.create(value);
        result.put(startCell, fishDigitSet); // orange
        result.put(bridgeCell1, fishDigitSet);
        result.put(bridgeCell2, fishDigitSet); // orange
        result.put(endCell, fishDigitSet);
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        BitSet fishDigitSet = SingletonBitSet.create(value);
        result.put(startCell, fishDigitSet);
        result.put(bridgeCell2, fishDigitSet);
        return result;
    }

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        Collection<Link> result = new ArrayList<>();
        result.add(new Link(startCell, value, bridgeCell1, value));
        result.add(new Link(bridgeCell1, value, bridgeCell2, value));
        result.add(new Link(bridgeCell2, value, endCell, value));
        return result;
    }

    @Override
    public Grid.Region[] getRegions() {
        //return new Grid.Region[] { shareRegion };
		        return null;
    }

    @Override
    public String toString() {
        return getName() +
                ": " +
                Cell.toFullString(startCell, bridgeCell1, bridgeCell2, endCell) +
                " on value " +
                value;
    }

    @Override
    public String toHtml(Grid grid) {
        String result = HtmlLoader.loadHtml(this, "TurbotFishHint.html");
        String name = getName();
        String base = this.baseSet.toFullString();
        String cover = this.coverSet.toFullString();
		String shared = this.shareRegion.toFullString();
        String value = Integer.toString(this.value);
        String cell1 = startCell.toString();
        String cell2 = bridgeCell1.toString();
        String cell3 = bridgeCell2.toString();
        String cell4 = endCell.toString();
        result = HtmlLoader.format(result, name, value, cell1, cell2, cell3, cell4, base, cover, shared);
        return result;
    }

    @Override
    public String getName() {
        Class<? extends Grid.Region> region1 = baseSet.getClass();
        Class<? extends Grid.Region> region2 = coverSet.getClass();
        if (region1 == Grid.Row.class) {
            if (region2 == Grid.Row.class)
                return "Skyscraper";
            else
				if (region2 == Grid.Column.class)
					return "Two-string Kite";
				else
					return "Turbot Fish";
        }
		else {
			if (region1 == Grid.Column.class)
				if (region2 == Grid.Row.class)
					return "Two-string Kite";
				else
					if (region2 == Grid.Column.class)
						return "Skyscraper";
					else 
                return "Turbot Fish";
			else
				return "Turbot Fish";
        }
    }	
	
    @Override
    public String getShortName() {
        Class<? extends Grid.Region> region1 = baseSet.getClass();
        Class<? extends Grid.Region> region2 = coverSet.getClass();
        if (region1 == Grid.Row.class) {
            if (region2 == Grid.Row.class)
                return "Sky";
            else
				if (region2 == Grid.Column.class)
					return "2SK";
				else
					return "TF";
        }
		else {
			if (region1 == Grid.Column.class)
				if (region2 == Grid.Row.class)
					return "2SK";
				else
					if (region2 == Grid.Column.class)
						return "Sky";
					else 
                return "TF";
			else
				if (region2 == Grid.Block.class) 
					return "GXW";
				else
					return "TF";
        }
    }

    @Override
    public double getDifficulty() {
        String name = getName();
        if (name.equals("Skyscraper")) {
            return 4.0;
        } else if (name.equals("Two-string Kite")) {
            return 4.1;
        } else {
            return 4.2;
        }
    }

    @Override
    public int hashCode() {
        return startCell.hashCode() ^ endCell.hashCode() ^
                bridgeCell1.hashCode() ^ bridgeCell2.hashCode() ^ value;
    }


    public String getClueHtml(Grid grid, boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() + " on the value " + value;
        } else {
            return "Look for a " + getName();
        }
    }



    @Override
    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new ArrayList<>();
        Cell startCell = Grid.getCell(this.startCell.getIndex());
        Cell endCell = Grid.getCell(this.endCell.getIndex());
        Cell bridgeCell1 = Grid.getCell(this.bridgeCell1.getIndex());
        Cell bridgeCell2 = Grid.getCell(this.bridgeCell2.getIndex());
        if (initialGrid.hasCellPotentialValue(startCell.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.startCell.getIndex(), value))
            result.add(new Potential(this.startCell, value, false));
        if (initialGrid.hasCellPotentialValue(bridgeCell1.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.bridgeCell1.getIndex(), value))
            result.add(new Potential(this.bridgeCell1, value, false));
        if (initialGrid.hasCellPotentialValue(bridgeCell2.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.bridgeCell2.getIndex(), value))
            result.add(new Potential(this.bridgeCell2, value, false));
        if (initialGrid.hasCellPotentialValue(endCell.getIndex(), value) && !initialGrid.hasCellPotentialValue(this.endCell.getIndex(), value))
            result.add(new Potential(this.endCell, value, false));
        return result;
    }
}
