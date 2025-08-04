package templates.beamline.heuristicsminer;

import communication.message.Message;
import communication.message.impl.causalnet.CausalNet;
import communication.message.impl.causalnet.CausalNetGenerator;
import communication.message.impl.event.Event;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.operator.Operator;
import utils.Pair;

import java.util.HashMap;
import java.util.Map;

public class HeuristicsMinerLossyCounting extends Operator<CausalNet, CausalNet> {
    private final HashMap<String, Integer> startingActivities = new HashMap<>();
    private final HashMap<String, Integer> endingActivities = new HashMap<>();
    private final HashMap<String, Pair<Integer, Integer>> activities = new HashMap<>();
    private final HashMap<Pair<String, String>, Pair<Integer, Integer>> relations = new HashMap<>();
    private final HashMap<String, Pair<Pair<String, Integer>, Integer>> cases = new HashMap<>();
    private int processedEventCount = 0;


    private final double dependencyThreshold;
    private final double andThreshold;
    private final double positiveObservationThreshold;
    private final int bucketWidth;
    private final int modelRefreshRate;

    public HeuristicsMinerLossyCounting(Configuration configuration) {
        super(configuration);

        dependencyThreshold = (double) configuration.getConfiguration().getOrDefault("minimumDependencyThreshold",0.8);
        andThreshold = (double) configuration.getConfiguration().getOrDefault("andThreshold",0.1);
        positiveObservationThreshold = (double) configuration.getConfiguration().getOrDefault("positiveObservationThreshold",10.0);
        modelRefreshRate = (int) configuration.getConfiguration().getOrDefault("modelRefreshRate",10);

        double maxApproximationError = (double) configuration.getConfiguration().get("maximalApproximationError");
        bucketWidth = (int)(1.0 / maxApproximationError);
    }

    @Override
    protected CausalNet process(Message message, int i) {
        processedEventCount++;
        int currentBucket = processedEventCount / bucketWidth;
        Event event = (Event) message;
        String activityName = event.getActivity();
        String caseID = event.getCaseID();

        activities.merge(activityName,
                new Pair<>(1, currentBucket - 1),
                (oldVal, ignored) -> new Pair<>(oldVal.first() + 1, oldVal.second())
        );

        String latestActivity = addCaseObservation(caseID, activityName, currentBucket);
        if (latestActivity != null) {
            relations.merge(new Pair<>(latestActivity, activityName),
                    new Pair<>(1, currentBucket-1),
                    (oldVal, ignored) -> new Pair<>(oldVal.first() + 1, oldVal.second()));
        }

        if (processedEventCount % bucketWidth == 0) { cleanup(currentBucket); }
        if (processedEventCount % modelRefreshRate == 0)
            { return updateModel(); }

        return null;
    }

    private CausalNet updateModel() {
        HashMap<String, Double> modelActivities = new HashMap<>();
        activities.forEach( (activity, value) -> modelActivities.put(activity, (double) value.first()));
        HashMap<Pair<String,String>, Double> modelRelations = new HashMap<>();
        relations.forEach((relationName, value) -> modelRelations.put(relationName, (double) value.first()));

        Map<String, Integer> endingActivities = new HashMap<>();
        for (String caseId : cases.keySet()) { endingActivities.merge(cases.get(caseId).first().first(), 1, Integer::sum); }

        CausalNetGenerator generator = new CausalNetGenerator(
                modelActivities,
                modelRelations,
                startingActivities,
                endingActivities);
        return generator.generateModel(dependencyThreshold, positiveObservationThreshold, andThreshold);
    }

    private void cleanup(int currentBucket) {
        activities.entrySet().removeIf(entry -> entry.getValue().first() + entry.getValue().second() <= currentBucket);

        for (Map.Entry<String, Pair<Pair<String, Integer>, Integer>> entry : cases.entrySet()) {
            int age = entry.getValue().first().second() + entry.getValue().second();
            if (age > currentBucket)
                { continue; }
            endingActivities.merge(entry.getValue().first().first(), 1, Integer::sum);
        }

        relations.entrySet().removeIf(entry -> entry.getValue().first() + entry.getValue().second() <= currentBucket);
    }

    private String addCaseObservation(String caseID, String latestActivity, int currentBucket) {
        if (cases.containsKey(caseID)) {
            Pair<Pair<String, Integer>, Integer> value = cases.get(caseID);
            cases.put(caseID, new Pair<>(new Pair<>(latestActivity, value.first().second()), value.second()));
            return value.first().first();
        }
        cases.put(caseID, new Pair<>(new Pair<>(latestActivity, 1), currentBucket-1));
        startingActivities.merge(latestActivity, 1, Integer::sum);
        return null;
    }

    @Override
    protected CausalNet convertAlgorithmOutput(CausalNet causalNet) {
        return causalNet;
    }

    @Override
    protected boolean publishCondition(CausalNet causalNet) {
        return causalNet != null;
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        return Map.of(Event.class, 1);
    }
}
