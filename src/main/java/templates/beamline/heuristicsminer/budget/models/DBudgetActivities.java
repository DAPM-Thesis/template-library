package templates.beamline.heuristicsminer.budget.models;

import java.util.HashMap;

public class DBudgetActivities extends LossyCountingBudget<String> {

    public DBudgetActivities(SharedDelta delta) {
        super(delta);
        delta.activities = this;
    }

    public HashMap<String, Double> getActivities() {
        HashMap<String, Double> activities = new HashMap<String, Double>();
        for (String activity : keySet()) { activities.put(activity, (double) get(activity).first()); }
        return activities;
    }
}
