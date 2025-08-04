package templates.beamline.heuristicsminer.budget;

import communication.message.Message;
import communication.message.impl.causalnet.CausalNet;
import communication.message.impl.causalnet.CausalNetGenerator;
import communication.message.impl.event.Event;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.operator.Operator;
import templates.beamline.heuristicsminer.budget.models.DBudgetActivities;
import templates.beamline.heuristicsminer.budget.models.DBudgetCases;
import templates.beamline.heuristicsminer.budget.models.DBudgetRelations;
import templates.beamline.heuristicsminer.budget.models.SharedDelta;

import java.util.HashMap;
import java.util.Map;

public class HeuristicsMinerBudgetLossyCounting extends Operator<CausalNet, CausalNet> {
    private final HashMap<String, Integer> startingActivities;

    private final double dependencyThreshold;
    private final double positiveObservationThreshold;
    private final double andThreshold;
    private final DBudgetActivities activities;
    private final DBudgetRelations relations;
    private final DBudgetCases cases;
    private final int modelRefreshRate;
    private int eventsProcessed = 0;

    public HeuristicsMinerBudgetLossyCounting(Configuration configuration) {
        super(configuration);

        Map<String, Object> config = configuration.getConfiguration();

        int budget = (int) config.getOrDefault("totalBudget", 100000);
        dependencyThreshold = (double) config.getOrDefault("minimumDependencyThreshold",0.8);
        positiveObservationThreshold = (double) config.getOrDefault("positiveObservationThreshold",10.0);
        andThreshold = (double) config.getOrDefault("andThreshold",0.1);
        modelRefreshRate = (int) config.getOrDefault("modelRefreshRate",10);

        startingActivities = new HashMap<>();
        HashMap<String, Integer> endingActivities = new HashMap<>();

        SharedDelta delta = new SharedDelta();
        delta.budget = budget;
        activities = new DBudgetActivities(delta);
        relations = new DBudgetRelations(delta);
        cases = new DBudgetCases(delta, startingActivities, endingActivities);
    }

    @Override
    protected CausalNet process(Message message, int i) {
        eventsProcessed++;
        Event event = (Event) message;
        String activityName = event.getActivity();
        String caseID = event.getCaseID();

        activities.addObservation(activityName);
        String latestActivity = cases.addObservation(caseID, activityName);
        if (latestActivity != null) { relations.addObservation(latestActivity, activityName); }

        if (eventsProcessed % modelRefreshRate == 0) {
            return updateModel();
        }
        return null;
    }

    private CausalNet updateModel() {
        CausalNetGenerator generator = new CausalNetGenerator(
                activities.getActivities(),
                relations.getRelations(),
                startingActivities,
                cases.getFinishingActivities());
        return generator.generateModel(dependencyThreshold, positiveObservationThreshold, andThreshold);
    }

    @Override
    protected CausalNet convertAlgorithmOutput(CausalNet causalNet) { return causalNet; }

    @Override
    protected boolean publishCondition(CausalNet causalNet) { return causalNet != null; }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() { return Map.of(Event.class, 1); }
}
