package SudokuExplainer.solver.rules.wing;

import java.util.*;
import SudokuExplainer.solver.*;
import SudokuExplainer.solver.rules.*;
import SudokuExplainer.solver.rules.chaining.*;
import SudokuExplainer.tools.*;
import SudokuExplainer.units.*;

/**
 * W-Wing hints
 */
public class WWingHint extends IndirectHint implements Rule, HasParentPotentialHint {

    private final int headValue;
    private final int bodyValue;
    private final Cell start;
    private final Cell end;
    private final Cell bridgeCell1;
    private final Cell bridgeCell2;
    private final Grid.Region region;

    public WWingHint(WWing rule, Map<Cell, BitSet> removablePotentials, int headValue, int bodyValue,
            Cell start, Cell end, Cell bridgeCell1, Cell bridgeCell2,
            Grid.Region region) {
        super(rule, removablePotentials);
        this.headValue = headValue;
        this.bodyValue = bodyValue;
        this.start = start;
        this.end = end;
        this.bridgeCell1 = bridgeCell1;
        this.bridgeCell2 = bridgeCell2;
        this.region = region;
    }


    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public Cell[] getSelectedCells() {
        return new Cell[] { start, end };
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>();
        // start and end cell (green)
        result.put(start, start.getPotentialValues());
        result.put(end, end.getPotentialValues());
        // bridge cells (orange)
        BitSet wSet = SingletonBitSet.create(bodyValue);
        result.put(bridgeCell1, wSet);
        result.put(bridgeCell2, wSet);
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        // bridge cells (orange)
        BitSet wSet = SingletonBitSet.create(bodyValue);
        result.put(bridgeCell1, wSet);
        result.put(bridgeCell2, wSet);
        return result;
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        Collection<Link> result = new ArrayList<>();
        Link link = new Link(bridgeCell1, bodyValue, bridgeCell2, bodyValue);
        result.add(link);
        Link linkToBridge1 = new Link(start, bodyValue, bridgeCell1, bodyValue);
        result.add(linkToBridge1);
        Link linkToBridge2 = new Link(bridgeCell2, bodyValue, end, bodyValue);
        result.add(linkToBridge2);
        return result;
    }

    @Override
    public Grid.Region[] getRegions() {
        return new Grid.Region[] { this.region };
    }

    @Override
    public String toString() {
        return getName() + ": " +
                Cell.toFullString(start, end) +
                " on value " + headValue + " and " + bodyValue;
    }

    @Override
    public String toHtml() {
        String result = HtmlLoader.loadHtml(this, "WWingHint.html");
        String cell1 = start.toString();
        String cell2 = end.toString();
        String bridge1 = bridgeCell1.toString();
        String bridge2 = bridgeCell2.toString();
        String region = this.region.toFullString();
        result = HtmlLoader.format(result, cell1, cell2, bridge1, bridge2, headValue, bodyValue, region);
        return result;
    }

    @Override
    public String getName() {
        return "W-Wing";
    }

    @Override
    public double getDifficulty() {
        return 4.4;
    }

    @Override
    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " on the values " + headValue + " and " + bodyValue;
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public int hashCode() {
        return start.hashCode() ^ end.hashCode();
    }

    @Override
    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new ArrayList<>();
        Cell start = initialGrid.getCell(this.start.getX(), this.start.getY());
        Cell end = initialGrid.getCell(this.end.getX(), this.end.getY());
        for (int p = 1; p <= 9; p++) {
            if (start.hasPotentialValue(p) && !this.start.hasPotentialValue(p))
                result.add(new Potential(this.start, p, false));
            if (end.hasPotentialValue(p) && !this.end.hasPotentialValue(p))
                result.add(new Potential(this.end, p, false));
        }
        return result;
    }
}
