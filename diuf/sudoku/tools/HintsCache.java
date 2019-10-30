/**
 * @author Mladen Dobrichev, 2019
 *
 */
package diuf.sudoku.tools;

import java.util.BitSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import diuf.sudoku.Grid;
import diuf.sudoku.solver.rules.chaining.ChainingHint;

public class HintsCache {
	private static ConcurrentHashMap<Grid,ConcurrentHashMap<String,Iterable<ChainingHint>>> cache = new ConcurrentHashMap<>();
	
	public static Iterable<ChainingHint> get(Grid grid, String signature) {
		ConcurrentHashMap<String,Iterable<ChainingHint>> item = cache.get(grid);
		if(item == null) return null;
		return item.get(signature);
	}
	public static void put(Grid grid, String signature, Iterable<ChainingHint> result) {
        Grid gridCopy = new Grid();
        grid.copyTo(gridCopy);
        ConcurrentHashMap<String,Iterable<ChainingHint>> empty = new ConcurrentHashMap<String,Iterable<ChainingHint>>();
		ConcurrentHashMap<String,Iterable<ChainingHint>> item = cache.putIfAbsent(gridCopy, empty);
		if(item != null) {
			item.put(signature, result);
		}
		else {
			empty.put(signature, result);
		}
	}
	
	//erase inapplicable cache items after grid is advanced to newGrid
	public static void purge(Grid newGrid) {
		for(Iterator<Grid> gIter = cache.keySet().iterator(); gIter.hasNext();) {
			Grid g = gIter.next();
			for(int i = 0; i < 81; i++) {
				BitSet pm = (BitSet)g.getCellPotentialValues(i).clone();
				pm.andNot(newGrid.getCellPotentialValues(i));
				if(!pm.isEmpty()) {
					//g has potential that is cleared in newGrid. All associated to g cache entries are obsolete.
					gIter.remove();
					break;
				}
			}
		}
	}
}
