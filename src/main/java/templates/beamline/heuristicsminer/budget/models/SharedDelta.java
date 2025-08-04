package templates.beamline.heuristicsminer.budget.models;

import java.io.Serializable;

public class SharedDelta implements Serializable {

    public int currentBucket = 0;
    public int budget = -1;

    public DBudgetCases cases = null;
    public DBudgetActivities activities = null;
    public DBudgetRelations relations = null;

    public void removeFromAll() {
        cases.removeBelowDelta();
        activities.removeBelowDelta();
        relations.removeBelowDelta();
    }

    public int getSize() { return cases.size() + activities.size() + relations.size(); }
}
