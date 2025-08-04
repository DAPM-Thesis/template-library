package templates.beamline.heuristicsminer.budget.models;

import utils.Pair;

import java.util.HashMap;
import java.util.Iterator;

public class LossyCountingBudget<K> extends HashMap<K, Pair<Integer, Integer>> {
    
    private SharedDelta delta;
    
    public LossyCountingBudget(SharedDelta delta) { this.delta = delta; }

    public void addObservation(K newValue) {
        if (containsKey(newValue)) {
            compute(newValue, (k, v) -> new Pair<>(v.first() + 1, v.second()));
        } else {
            if (delta.getSize() == delta.budget) { cleanup(); }
            put(newValue, new Pair<Integer, Integer>(1, delta.currentBucket));
        }
    }

    public boolean removeBelowDelta() {
        boolean removedOneItem = false;
        Iterator<Entry<K, Pair<Integer, Integer>>> iter = entrySet().iterator();
        while (iter.hasNext()) {
            Pair<Integer, Integer> pair = iter.next().getValue();
            if (pair.first() + pair.second() <= delta.currentBucket) {
                iter.remove();
                removedOneItem = true;
            }
        }
        return removedOneItem;
    }

    private void cleanup() {
        delta.currentBucket++;

        boolean removedOneItem = removeBelowDelta();

        if (!removedOneItem) {
            int minValue = Integer.MAX_VALUE;
            for (Entry<K, Pair<Integer, Integer>> kPairEntry : entrySet()) {
                Pair<Integer, Integer> pair = kPairEntry.getValue();
                if (pair.first() + pair.second() < minValue) {
                    minValue = pair.first() + pair.second();
                }
            }
            delta.currentBucket = minValue;
            delta.removeFromAll();
        }
    }
}
