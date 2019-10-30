/**
 * @author Mladen Dobrichev, 2019
 *
 */
package diuf.sudoku.tools;

import java.util.BitSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import diuf.sudoku.Grid;

public class HintsCache {
	private static ConcurrentHashMap<Grid,ConcurrentHashMap<String,Object>> cache = new ConcurrentHashMap<>();
	
	public static Object get(Grid grid, String signature) {
		ConcurrentHashMap<String,Object> item = cache.get(grid);
		if(item == null) return null;
		return item.get(signature);
	}
	public static void put(Grid grid, String signature, Object result) {
        Grid gridCopy = new Grid();
        grid.copyTo(gridCopy);
        gridCopy.clearDigitCells();
        gridCopy.clearInitialGrid();
        ConcurrentHashMap<String,Object> empty = new ConcurrentHashMap<String,Object>();
		ConcurrentHashMap<String,Object> item = cache.putIfAbsent(gridCopy, empty);
		if(item != null) {
			item.put(signature, result);
		}
		else {
			empty.put(signature, result);
		}
	}
	
	//erase inapplicable cache items after grid is advanced to newGrid
	public static void purge(Grid newGrid) {
		//cache.clear(); //brute but is slower
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
	//erase after puzzle is done
	public static void clear() {
		cache.clear();
	}
}
