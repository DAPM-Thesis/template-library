package templates.beamline;

import communication.message.Message;
import communication.message.impl.ProcessMap;
import communication.message.impl.event.Event;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.operator.Operator;
import utils.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the Trivial Miner, based on <a href="https://github.com/beamline/discovery-trivial">Andrea Burattin's Beamline implementation</a>.
 * */
public class TrivialMiner extends Operator<ProcessMap, ProcessMap> {
    private final Map<String, String> latestActivityInCase = new HashMap<>();
    private final Map<Pair<String, String>, Double> relations = new HashMap<>();
    private final Map<String, Double> activities = new HashMap<>();
    private final Map<String, Double> startingActivities = new HashMap<>();
    private Double maxActivityFreq = 1d;
    private Double maxRelationsFreq = 1d;
    private double minDependency = 0.8;
    private int modelRefreshRate = 10;
    long processedEvents = 0;

    public TrivialMiner(Configuration configuration) {
        super(configuration);

        if (configuration.get("minDependency") != null) { minDependency = (double) configuration.get("minDependency"); }
        if (configuration.get("modelRefreshRate") != null) { modelRefreshRate = (int) configuration.get("modelRefreshRate"); }
    }

    @Override
    protected ProcessMap process(Message message, int i) {
        processedEvents++;
        Event event = (Event) message;
        String caseID = event.getCaseID();
        String activityName = event.getActivity();

        double activityFreq = activities.merge(activityName, 1d, Double::sum);
        maxActivityFreq = Math.max(maxActivityFreq, activityFreq);

        if (latestActivityInCase.containsKey(caseID)) {
            Pair<String, String> relation = new Pair<>(latestActivityInCase.get(caseID), activityName);
            double relationFreq = relations.merge(relation, 1d, Double::sum);
            maxRelationsFreq = Math.max(maxRelationsFreq, relationFreq);
        } else
            { startingActivities.merge(activityName, 1d, Double::sum); }
        latestActivityInCase.put(caseID, activityName);

        if (processedEvents % modelRefreshRate == 0)
            { return mine(); }

        return null;
    }

    public ProcessMap mine() {
        ProcessMap process = new ProcessMap();

        activities.forEach((activityName, freq) ->
                process.addActivity(activityName, freq/maxActivityFreq, freq)
        );

        for (Map.Entry<Pair<String, String>, Double> entry : relations.entrySet()) {
            double dependency = entry.getValue() / maxRelationsFreq;
            if (dependency >= minDependency)
                { process.addRelation(entry.getKey().first(), entry.getKey().second(), dependency, entry.getValue()); }
        }

        activities.keySet().stream()
                .filter(activity -> process.isStartActivity(activity) && process.isEndActivity(activity))
                .forEach(process::removeActivity);

        startingActivities.entrySet().stream()
                .filter(entry -> entry.getValue()/maxRelationsFreq >= minDependency)
                .forEach(entry -> process.addStartingActivity(entry.getKey()));

        return process;
    }

    @Override
    protected ProcessMap convertAlgorithmOutput(ProcessMap processMap) { return processMap; }

    @Override
    protected boolean publishCondition(ProcessMap processMap) {
        return processMap != null;
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        return Map.of(Event.class, 1);
    }
}
