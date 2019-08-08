/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules.chaining;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.Grid.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.solver.rules.*;
import diuf.sudoku.tools.*;

/**
 * Chaining hint. A chaining hint is any hint involving a chain of implications.
 */
public abstract class ChainingHint extends IndirectHint implements Rule, HasParentPotentialHint {

    protected final boolean isYChain;
    protected final boolean isXChain;


    public ChainingHint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials,
            boolean isYChain, boolean isXChain) {
        super(rule, removablePotentials);
        this.isYChain = isYChain;
        this.isXChain = isXChain;
    }

    Collection<Potential> getChain(Potential target) {
        List<Potential> result = new ArrayList<Potential>();
        Set<Potential> done = new HashSet<Potential>();
        Collection<Potential> todo = new ArrayList<Potential>();
        todo.add(target);
        while (!todo.isEmpty()) {
            Collection<Potential> next = new ArrayList<Potential>();
            for (Potential p : todo) {
                if (!done.contains(p)) {
                    done.add(p);
                    result.add(p);
                    next.addAll(p.parents);
                }
            }
            todo = next;
        }
        return result;
    }

    private Collection<ChainingHint> getNestedChains() {
        Collection<ChainingHint> result = new ArrayList<ChainingHint>();
        Set<FullChain> processed = new HashSet<FullChain>();
        for (Potential target : getChainsTargets()) {
            for (Potential p : getChain(target)) {
                if (p.nestedChain != null) {
                    FullChain f = new FullChain(p.nestedChain);
                    if (!processed.contains(f)) {
                        result.add(p.nestedChain);
                        processed.add(f);
                    }
                }
            }
        }
        // Recurse (in case there is more than one level of nesting)
        for (ChainingHint chain : new ArrayList<ChainingHint>(result)) {
            result.addAll(chain.getNestedChains());
        }
        return result;
    }

    protected Map<Cell, BitSet> getColorPotentials(Potential target, boolean state, boolean skipTarget) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        for (Potential p : getChain(target)) {
            if (p.isOn == state
                    || (state && (p != target || !skipTarget))) {
                BitSet potentials = result.get(p.cell);
                if (potentials == null) {
                    potentials = new BitSet();
                    result.put(p.cell, potentials);
                }
                potentials.set(p.value);
            }
        }
        return result;
    }

    private Pair<ChainingHint, Integer> getNestedChain(int nestedViewNum) {
        Set<FullChain> processed = new HashSet<FullChain>();
        for (Potential target : getChainsTargets()) {
            for (Potential p : getChain(target)) {
                if (p.nestedChain != null) {
                    FullChain f = new FullChain(p.nestedChain);
                    if (!processed.contains(f)) {
                        processed.add(f);
                        int localCount = p.nestedChain.getViewCount();
                        if (localCount > nestedViewNum) {
                            return new Pair<ChainingHint, Integer>(p.nestedChain, nestedViewNum);
                        }
                        nestedViewNum-= localCount;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the end of the subchain from which the given
     * nested chain can be deduced.
     * @param nestedChain the nested chain
     * @return the potential at which the nested chain starts
     */
    private Potential getContainerTarget(ChainingHint nestedChain) {
        for (Potential target : getChainsTargets()) {
            for (Potential p : getChain(target)) {
                if (p.nestedChain == nestedChain)
                    // return target;
                    return p;
            }
        }
        return null;
    }

    protected final Map<Cell, BitSet> getNestedGreenPotentials(int nestedViewNum) {
        nestedViewNum-= getFlatViewCount();
        Pair<ChainingHint, Integer> nest = getNestedChain(nestedViewNum);
        return nest.getValue1().getGreenPotentials(nest.getValue2());
    }

    protected final Map<Cell, BitSet> getNestedRedPotentials(int nestedViewNum) {
        nestedViewNum-= getFlatViewCount();
        Pair<ChainingHint, Integer> nest = getNestedChain(nestedViewNum);
        return nest.getValue1().getRedPotentials(nest.getValue2());
    }

    @Override
    public Map<Cell, BitSet> getBluePotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        viewNum-= getFlatViewCount();
        if (viewNum >= 0) {
            // Create the grid deduced from the container (or "main") chain
            Grid nestedGrid = new Grid();
            grid.copyTo(nestedGrid);
            Pair<ChainingHint, Integer> nest = getNestedChain(viewNum);
            ChainingHint nestedChain = nest.getValue1();
            int nestedViewNum = nest.getValue2();
            Potential target = getContainerTarget(nestedChain);
            for (Potential p : getChain(target)) {
                if (!p.isOn) // Remove deductions of the container chain
                    nestedGrid.getCell(p.cell.getX(), p.cell.getY()).removePotentialValue(p.value);
            }
            // Use the rule's parent collector
            Collection<Potential> blues = new LinkedHashSet<Potential>();
            Potential nestTarget = nestedChain.getChainTarget(nestedViewNum);
            nestedChain.collectRuleParents(grid, nestedGrid, blues, nestTarget);
            // Convert to Cell->BitSet map
            for (Potential p : blues) {
                Cell sCell = p.cell;
                // Get corresponding cell in initial grid
                Cell cell = grid.getCell(sCell.getX(), sCell.getY());
                if (result.containsKey(cell))
                    result.get(cell).set(p.value);
                else
                    result.put(cell, SingletonBitSet.create(p.value));
            }
        }
        return result;
    }

    protected final Collection<Link> getNestedLinks(int nestedViewNum) {
        nestedViewNum-= getFlatViewCount();
        Pair<ChainingHint, Integer> nest = getNestedChain(nestedViewNum);
        return nest.getValue1().getLinks(nest.getValue2());
    }

    protected final int getNestedComplexity() {
        int result = 0;
        Set<FullChain> processed = new HashSet<FullChain>();
        for (Potential target : getChainsTargets()) {
            for (Potential p : getChain(target)) {
                if (p.nestedChain != null) {
                    FullChain f = new FullChain(p.nestedChain);
                    if (!processed.contains(f)) {
                        result+= p.nestedChain.getComplexity();
                        processed.add(f);
                    }
                }
            }
        }
        return result;
    }

    protected Collection<Link> getLinks(Potential target) {
        Collection<Link> result = new ArrayList<Link>();
        for (Potential p : getChain(target)) {
            if (p.parents.size() <= 6) {
                // Add links from all parents of p to p:
                for (Potential pr : p.parents)
                    result.add(new Link(pr.cell, pr.value, p.cell, p.value));
            }
        }
        return result;
    }

    private static Class<? extends Region> getCauseRegion(Potential.Cause cause) {
        switch(cause) {
        case HiddenBlock:
            return Block.class;
        case HiddenColumn:
            return Column.class;
        case HiddenRow:
            return Row.class;
        default:
            return null;
        }
    }

    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new LinkedHashSet<Potential>();
        /*
         * Warning: Iterate on each chain target separately. Reason: they may be equal
         * according to equals() (same candidate), but they may have different parents !
         */
        for (Potential target : getChainsTargets()) {
            // Iterate on chain targets
            collectRuleParents(initialGrid, currentGrid, result, target);
        }
        return result;
    }

    protected void collectRuleParents(Grid initialGrid, Grid currentGrid,
            Collection<Potential> result, Potential target) {
        Set<Potential> done = new HashSet<Potential>();
        Collection<Potential> todo = new ArrayList<Potential>();
        todo.add(target);
        while (!todo.isEmpty()) {
            Collection<Potential> next = new ArrayList<Potential>();
            for (Potential p : todo) {
                if (!done.contains(p)) {
                    done.add(p);
                    Potential.Cause cause = p.cause;
                    if (cause == null) {
                        // This is the initial assumption
                        assert (this instanceof CycleHint) || p.parents.isEmpty();
                        if (this instanceof CellChainingHint)
                            cause = Potential.Cause.NakedSingle;
                        else if (this instanceof RegionChainingHint)
                            cause = Chaining.getRegionCause(((RegionChainingHint)this).getRegion());
                    }
                    if (p.isOn && cause != null) {
                        assert !cause.equals(Potential.Cause.Advanced);
                        Cell curCell = p.cell;
                        if (cause.equals(Potential.Cause.NakedSingle)) {
                            Cell actCell = currentGrid.getCell(curCell.getX(), curCell.getY());
                            Cell initCell = initialGrid.getCell(curCell.getX(), curCell.getY());
                            for (int value = 1; value <= 9; value++) {
                                if (initCell.hasPotentialValue(value) && !actCell.hasPotentialValue(value))
                                    result.add(new Potential(actCell, value, false));
                            }
                        } else { // Hidden single
                            Region r = currentGrid.getRegionAt(getCauseRegion(cause), curCell);
                            for (int i = 0; i < 9; i++) {
                                Cell actCell = r.getCell(i);
                                Cell initCell = initialGrid.getCell(actCell.getX(), actCell.getY());
                                if (initCell.hasPotentialValue(p.value) && !actCell.hasPotentialValue(p.value))
                                    result.add(new Potential(actCell, p.value, false));
                            }
                        }
                    }
                    next.addAll(p.parents);
                }
            }
            todo = next;
        }
    }

    protected Chaining getChainingRule() {
        return (Chaining)super.getRule();
    }

    protected abstract Potential getResult();

    protected abstract Collection<Potential> getChainsTargets();

    protected abstract Potential getChainTarget(int viewNum);

    protected abstract int getFlatViewCount();

    protected int getNestedViewCount() {
        int result = 0;
        Set<FullChain> processed = new HashSet<FullChain>();
        for (Potential target : getChainsTargets()) {
            for (Potential p : getChain(target)) {
                if (p.nestedChain != null) {
                    FullChain f = new FullChain(p.nestedChain);
                    if (!processed.contains(f)) {
                        result += p.nestedChain.getViewCount();
                        processed.add(f);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public int getViewCount() {
        return getFlatViewCount() + getNestedViewCount();
    }

    @Override
    public Cell getCell() {
        Potential result = getResult();
        if (result != null && result.isOn)
            return result.cell;
        return null;
    }

    @Override
    public int getValue() {
        Potential result = getResult();
        if (result != null && result.isOn)
            return result.value;
        return 0;
    }

    protected double getLengthDifficulty() {
        double added = 0.0;
        int ceil = 4;
        int length = getComplexity() - 2;
        boolean isOdd = false;
        while (length > ceil) {
            added += 0.1;
            if (!isOdd)
                ceil = (ceil * 3) / 2;
            else
                ceil = (ceil * 4) / 3;
            isOdd = !isOdd;
        }
        /*
        final int[] steps = new int[] {4, 6, 8, 12, 16, 24, 32, 48, 64, 96, 128,
            192, 256, 384, 512, 768, 1024, 1536, 2048, 3072, 4096, 6144, 8192};
        int length = getComplexity() - 2;
        double added = 0;
        int index = 0;
        while (index < steps.length && length > steps[index]) {
            added += 0.1;
            index++;
        }
         */
        return added;
    }

    protected String getNamePrefix() {
        Chaining rule = getChainingRule();
        if (rule.getLevel() > 0)
            return "Dynamic ";
        if (rule.isNishio())
            return "Nishio ";
        else if (rule.isDynamic())
            return "Dynamic ";
        else if (rule.isMultiple())
            return "";
        else
            return "";
    }

    protected String getNameSuffix() {
        Chaining rule = getChainingRule();
        if (rule.getLevel() >= 1)
            return " Chains" + Chaining.getNestedSuffix(rule.getLevel());
        return " Chains";
    }

    protected int getAncestorCount(Potential child) {
        Collection<Potential> ancestors = new HashSet<Potential>();
        Collection<Potential> todo = new ArrayList<Potential>();
        todo.add(child);
        while (!todo.isEmpty()) {
            Collection<Potential> next = new ArrayList<Potential>();
            for (Potential p : todo) {
                if (!ancestors.contains(p)) {
                    ancestors.add(p);
                    next.addAll(p.parents);
                }
            }
            todo = next;
        }
        return ancestors.size();
    }

    protected String getHtmlChain(Potential dst) {
        List<Potential> potentials = new ArrayList<Potential>();
        List<String> rules = new ArrayList<String>();
        addChainItem(potentials, rules, dst);
        StringBuilder result = new StringBuilder();
        for (String rule : rules) {
            result.append(rule);
            result.append("<br>");
        }
        return result.toString();
    }

    private void addChainItem(List<Potential> potentials, List<String> rules, Potential p) {
        // First add parent chains
        for (Potential parent : p.parents)
            addChainItem(potentials, rules, parent);
        if (!potentials.contains(p) && p.parents.size() > 0) {
            // Add chain item for given potential
            StringBuilder rule = new StringBuilder();
            rule.append("(");
            rule.append(rules.size() + 1);
            rule.append(") ");
            rule.append("If ");
            for (int i = p.parents.size() - 1; i >= 0; i--) {
                if (i < p.parents.size() - 1) {
                    if (i == 0)
                        rule.append(" and ");
                    else
                        rule.append(", ");
                }
                Potential parent = p.parents.get(i);
                rule.append(parent.toWeakString());
                int pIndex = potentials.indexOf(parent);
                if (pIndex < rules.size() - 1) {
                    rule.append(" (");
                    if (pIndex >= 0)
                        rule.append(pIndex + 1);
                    else
                        rule.append("initial assumption");
                    rule.append(")");
                }
            }
            rule.append(", then ");
            rule.append(p.toStrongString());
            if (p.explanation != null) {
                rule.append(" (");
                rule.append(p.explanation);
                rule.append(")");
            }

            potentials.add(p);
            rules.add(rule.toString());
        }
    }

    public abstract int getFlatComplexity();

    public int getComplexity() {
        return getFlatComplexity() + getNestedComplexity();
    }

    public abstract int getSortKey();

    protected Potential getSrcPotential(Potential target) {
        Potential result = target;
        while (!result.parents.isEmpty())
            result = result.parents.get(0);
        return result;
    }

    public String appendNestedChainsDetails(String result) {
        Collection<ChainingHint> nestedChains = getNestedChains();
        if (nestedChains.isEmpty())
            return result;
        StringBuilder nested = new StringBuilder();
        nested.append("<br><br>\n");
        nested.append("<b>Nested Forcing Chains details</b> ");
        nested.append("(Note that each Nested Forcing Chain relies on the fact that some" +
                " <font color=\"blue\">candidates</font> have been excluded by the main" +
                " Forcing Chain): <br><br>\n");
        int index = getFlatViewCount() + 1;
        for (ChainingHint nestedHint : nestedChains) {
            nested.append("<i>Nested <b>");
            nested.append(nestedHint.toString());
            nested.append("</b></i><br>\n");
            for (Potential target : nestedHint.getChainsTargets()) {
                Potential assumption = getSrcPotential(target);
                nested.append("Chain " + index + ": <b>If " + assumption.toWeakString()
                        + ", then " + target.toStrongString() + "</b>" 
                        + " (View " + index + "):<br>\n");
                nested.append(getHtmlChain(target));
                nested.append("<br>\n");
                index++;
            }
        }
        int pos = result.toLowerCase().indexOf("</body>");
        return result.substring(0, pos) + nested.toString() + result.substring(pos);
    }

    /**
     * Overriden to prevent a huge number of equivalent chains.
     * Two chaining hints are equal as soon as the removable
     * potentials are the same.
     * <p>
     * This is not dramatic, because we already return the hints
     * sorted by increasing complexity: only more complex hints
     * will be filtered out.
     * <p>
     * The embarassing case we want to prevent with this strategy
     * is when, for a given chain, all chains that have it as a
     * "suffix" are also returned. This strategy also ensures that,
     * for instance, only chains that are dynamic are actually
     * classified in the dynamic group (static chains detected
     * by the dynamic algorithm will be equal to a previously
     * detected chain, and will not be added).
     * <p>
     * This is not the filter from the GUI. The latter further filters
     * hints of different categories.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChainingHint))
            return false;
        ChainingHint other = (ChainingHint)o;
        return this.getRemovablePotentials().equals(other.getRemovablePotentials());
    }

    @Override
    public int hashCode() {
        return getRemovablePotentials().hashCode();
    }

}
