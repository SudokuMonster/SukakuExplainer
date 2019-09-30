package SudokuExplainer.solver.rules.chaining.aic;

import java.util.*;
import SudokuExplainer.solver.*;
import SudokuExplainer.solver.rules.*;
import SudokuExplainer.solver.rules.chaining.*;
import SudokuExplainer.tools.*;
import SudokuExplainer.units.*;


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
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>();
        BitSet fishDigitSet = SingletonBitSet.create(value);
        result.put(startCell, fishDigitSet); // orange
        result.put(bridgeCell1, fishDigitSet);
        result.put(bridgeCell2, fishDigitSet); // orange
        result.put(endCell, fishDigitSet);
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        BitSet fishDigitSet = SingletonBitSet.create(value);
        result.put(startCell, fishDigitSet);
        result.put(bridgeCell2, fishDigitSet);
        return result;
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        Collection<Link> result = new ArrayList<>();
        result.add(new Link(startCell, value, bridgeCell1, value));
        result.add(new Link(bridgeCell1, value, bridgeCell2, value));
        result.add(new Link(bridgeCell2, value, endCell, value));
        return result;
    }

    @Override
    public Grid.Region[] getRegions() {
        return new Grid.Region[] { shareRegion };
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
    public String toHtml() {
        String result = HtmlLoader.loadHtml(this, "TurbotFishHint.html");
        String name = getName();
        String base = this.baseSet.toFullString();
        String cover = this.coverSet.toFullString();
        String value = Integer.toString(this.value);
        String cell1 = startCell.toString();
        String cell2 = bridgeCell1.toString();
        String cell3 = bridgeCell2.toString();
        String cell4 = endCell.toString();
        result = HtmlLoader.format(result, name, value, cell1, cell2, cell3, cell4, base, cover);
        return result;
    }

    @Override
    public String getName() {
        Class<? extends Grid.Region> region1 = baseSet.getClass();
        Class<? extends Grid.Region> region2 = coverSet.getClass();
        if (region1 == Grid.Row.class) {
            if (region2 == Grid.Row.class) {
                return "Skyscraper";
            } else if (region2 == Grid.Column.class) {
                return "Two-string Kite";
            } else {
                return "Turbot Fish (Normal)";
            }
        } else if (region1 == Grid.Column.class) {
            if (region2 == Grid.Row.class) {
                return "Two-string Kite";
            } else if (region2 == Grid.Column.class) {
                return "Skyscraper";
            } else {
                return "Turbot Fish (Normal)";
            }
        } else {
            return "Turbot Fish (Normal)";
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
    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() + " on the value " + value;
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public int hashCode() {
        return startCell.hashCode() ^ endCell.hashCode() ^
                bridgeCell1.hashCode() ^ bridgeCell2.hashCode() ^ value;
    }

    @Override
    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new ArrayList<>();
        Cell startCell = initialGrid.getCell(this.startCell.getX(), this.startCell.getY());
        Cell endCell = initialGrid.getCell(this.endCell.getX(), this.endCell.getY());
        Cell bridgeCell1 = initialGrid.getCell(this.bridgeCell1.getX(), this.bridgeCell1.getY());
        Cell bridgeCell2 = initialGrid.getCell(this.bridgeCell2.getX(), this.bridgeCell2.getY());
        if (startCell.hasPotentialValue(value) && !this.startCell.hasPotentialValue(value))
            result.add(new Potential(this.startCell, value, false));
        if (bridgeCell1.hasPotentialValue(value) && !this.bridgeCell1.hasPotentialValue(value))
            result.add(new Potential(this.bridgeCell1, value, false));
        if (bridgeCell2.hasPotentialValue(value) && !this.bridgeCell2.hasPotentialValue(value))
            result.add(new Potential(this.bridgeCell2, value, false));
        if (endCell.hasPotentialValue(value) && !this.endCell.hasPotentialValue(value))
            result.add(new Potential(this.endCell, value, false));
        return result;
    }
}
