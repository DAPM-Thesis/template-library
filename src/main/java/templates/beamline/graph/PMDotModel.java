package templates.beamline.graph;

import beamline.graphviz.Dot;
import beamline.graphviz.DotNode;
import communication.message.impl.ProcessMap;
import utils.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PMDotModel extends Dot {

    private final ProcessMap model;
    private ColorPalette.Colors activityColor;
    private final Map<String, Object> optionsMap;

    public PMDotModel(ProcessMap model, Map<String, Object> optionsMap) {
        this.model = model;
        this.optionsMap = optionsMap;
        initializeOptions(optionsMap);

        realize();
    }

    private void initializeOptions(Map<String, Object> optionsMap) {
        this.activityColor = ColorPalette.create((String) optionsMap.getOrDefault("activityColor", "blue"));
        setOption("ranksep", String.valueOf(optionsMap.getOrDefault("ranksep", "0.1")));
        setOption("fontsize", (String) optionsMap.getOrDefault("nodeFontsize", "9"));

        String marginStr;
        if (optionsMap.containsKey("margin")) {
            Map<String, Double> marginMap = (Map<String, Double>) optionsMap.get("margin");
            marginStr = String.valueOf(marginMap.get("x")) + ',' + String.valueOf(marginMap.get("y"));
        } else { marginStr = "0.0,0.0"; }
        setOption("margin", marginStr);

        setOption("remincross", "true");
        setOption("outputorder", "edgesfirst");

    }

    private void realize() {
        Map<String, DotNode> activityToNode = new HashMap<>();
        Map<String, String> nodeToActivity = new HashMap<>();

        Set<DotNode> startNodes = new HashSet<>();
        Set<DotNode> endNodes = new HashSet<>();

        for (String activity : model.getActivities()) {
            DotNode node = addNodeIfNeeded(activity, activityToNode, nodeToActivity);
            if (node instanceof PMDotNode dnode) { dnode.setColorWeight(model.getActivityRelativeFrequency(activity), activityColor); }
            if (model.isStartActivity(activity)) {startNodes.add(node);}
            if (model.isEndActivity(activity)) {endNodes.add(node);}
        }

        for (Pair<String, String> relation : model.getRelations()) {
            String sourceActivity = relation.first();
            String targetActivity = relation.second();

            DotNode sourceNode = addNodeIfNeeded(sourceActivity, activityToNode, nodeToActivity);
            DotNode targetNode = addNodeIfNeeded(targetActivity, activityToNode, nodeToActivity);

            addRelation(sourceNode, targetNode, model.getRelationRelativeFrequency(relation), model.getRelationAbsoluteFrequency(relation));
        }

        if (!startNodes.isEmpty()) {
            PMDotStartNode start = new PMDotStartNode(optionsMap);
            addNode(start);
            startNodes.forEach(node -> addRelation(start, node, null, null));
        }
        if (!endNodes.isEmpty()) {
            PMDotEndNode end = new PMDotEndNode(optionsMap);
            addNode(end);
            endNodes.forEach(node -> addRelation(node, end, null, null));
        }
    }

    private void addRelation(DotNode sourceNode, DotNode targetNode, Double relativeFrequency, Double absoluteFrequency) {
        String freqLabel = "";
        if (relativeFrequency != null && absoluteFrequency != null) {
            freqLabel = String.format("%.2g ", relativeFrequency) + '(' + absoluteFrequency.intValue() + ')';
        }
        addEdge(new PMDotEdge(sourceNode, targetNode, freqLabel, relativeFrequency, optionsMap));
    }

    private DotNode addNodeIfNeeded(String activity, Map<String, DotNode> activityToNode, Map<String, String> nodeToActivity) {
        DotNode existingNode = activityToNode.get(activity);
        if (existingNode != null)
            { return existingNode; }

        PMDotNode newNode = new PMDotNode(activity, optionsMap);
        newNode.setColorWeight(model.getActivityRelativeFrequency(activity), activityColor);
        newNode.setSecondLine(String.format("%.2g%n", model.getActivityRelativeFrequency(activity)) + " (" + model.getActivityAbsoluteFrequency(activity).intValue() + ")");
        addNode(newNode);
        activityToNode.put(activity, newNode);
        nodeToActivity.put(newNode.getId(), activity);
        return newNode;
    }

}

