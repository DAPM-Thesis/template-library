package templates.beamline.heuristicsminer.budget.models;

import utils.Pair;

import java.util.HashMap;


public class DBudgetRelations extends LossyCountingBudget<Pair<String, String>> {

    public DBudgetRelations(SharedDelta delta) {
        super(delta);
        delta.relations = this;
    }

    public void addObservation(String source, String target) { addObservation(new Pair<>(source, target)); }

    public HashMap<Pair<String, String>, Double> getRelations() {
        HashMap<Pair<String, String>, Double> relations = new HashMap<>();
        for (Pair<String, String> relationName : keySet()) {
            relations.put(relationName, (double) get(relationName).first());
        }
        return relations;
    }
}
