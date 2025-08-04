package templates.beamline.heuristicsminer.budget.models;

import utils.Pair;
import java.util.HashMap;
import java.util.Iterator;

public class DBudgetCases extends HashMap<String, Pair<Pair<String, Integer>, Integer>> {
    private final HashMap<String, Integer> startingActivities;
    private final HashMap<String, Integer> finishingActivities;
    private final SharedDelta delta;

    public DBudgetCases(SharedDelta delta, HashMap<String, Integer> startingActivities, HashMap<String, Integer> finishingActivities) {
        this.startingActivities = startingActivities;
        this.finishingActivities = finishingActivities;
        this.delta = delta;
        delta.cases = this;
    }

    public String addObservation(String caseId, String latestActivity) {
        if (containsKey(caseId)) {
            Pair<Pair<String, Integer>, Integer> v = get(caseId);
            put(caseId, new Pair<>(new Pair<>(latestActivity, v.first().second() + 1), v.second()));
            return v.first().first();
        } else {
            if (delta.getSize() == delta.budget) { cleanup(); }
            incrementIntHashMap(startingActivities, latestActivity);
            put(caseId, new Pair<>(new Pair<>(latestActivity, 1), delta.currentBucket));
        }
        return null;
    }

    public boolean removeBelowDelta() {
        boolean removedOneItem = false;
        Iterator<Entry<String, Pair<Pair<String, Integer>, Integer>>> iter = entrySet().iterator();
        while (iter.hasNext()) {
            Pair<Pair<String, Integer>, Integer> pair = iter.next().getValue();
            if (pair.first().second() + pair.second() <= delta.currentBucket) {
                incrementIntHashMap(finishingActivities, pair.first().first());
                iter.remove();
                removedOneItem = true;
            }
        }
        return removedOneItem;
    }

    public HashMap<String, Integer> getFinishingActivities() {
        HashMap<String, Integer> tmp = new HashMap<String, Integer>();
        for (String caseId : keySet()) {
            String activity = get(caseId).first().first();
            tmp.merge(activity, 1, Integer::sum);
        }
        return tmp;
    }

    private void cleanup() {
        delta.currentBucket++;
        boolean removedOneItem = removeBelowDelta();

        if (!removedOneItem) {
            int minValue = Integer.MAX_VALUE;
            for (Entry<String, Pair<Pair<String, Integer>, Integer>> stringPairEntry : entrySet()) {
                Pair<Pair<String, Integer>, Integer> pair = stringPairEntry.getValue();
                if (pair.first().second() + pair.second() < minValue) { minValue = pair.first().second() + pair.second(); }
            }
            delta.currentBucket = minValue;
            delta.removeFromAll();
        }
    }

    private void incrementIntHashMap(HashMap<String, Integer> hm, String key) { hm.merge(key, 1, Integer::sum); }
}
