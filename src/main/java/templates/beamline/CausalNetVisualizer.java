package templates.beamline;


import beamline.graphviz.Dot;
import beamline.graphviz.DotEdge;
import beamline.graphviz.DotNode;
import communication.message.Message;
import communication.message.impl.causalnet.CausalNet;
import communication.message.impl.causalnet.CausalNetBinding;
import communication.message.impl.causalnet.CausalNetNode;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.Sink;
import utils.Pair;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CausalNetVisualizer extends Sink {
    private final File saveFile;

    public CausalNetVisualizer(Configuration configuration) {
        super(configuration);

        saveFile = new File((String) configuration.get("saveFilepath"));
    }

    @Override
    public void observe(Pair<Message, Integer> pair) {
        CausalNet net = (CausalNet) pair.first();
        CausalNetSimplifiedModelView view = new CausalNetSimplifiedModelView(net, configuration.getConfiguration());
        try { view.exportToFile(saveFile); } catch (IOException e) { throw new RuntimeException("couldn't save file to: " + saveFile.toString() + e); }
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        return Map.of(CausalNet.class, 1);
    }
}


class CausalNetSimplifiedModelView extends Dot {

    private final Map<String, Object> optionsMap;
    private final CausalNet model;
    private final Map<String, DotNode> activityToNode;
    private final Set<Pair<String, String>> edges;

    public CausalNetSimplifiedModelView(CausalNet model, Map<String, Object> optionsMap) {
        this.model = model;
        this.activityToNode = new HashMap<>();
        this.edges = new HashSet<>();
        this.optionsMap = optionsMap;

        realize();
    }

    private void realize() {
        for (CausalNetNode node : model.getNodes()) {
            for (CausalNetBinding b : model.getInputBindings(node)) {
                for(CausalNetNode input : b.getBoundNodes()) {
                    addRelation(input.getLabel(), node.getLabel());
                }
            }
            for (CausalNetBinding b : model.getOutputBindings(node)) {
                for(CausalNetNode output : b.getBoundNodes()) {
                    addRelation(node.getLabel(), output.getLabel());
                }
            }
        }
    }

    public DotNode getNodeIfNeeded(String activity) {
        if (!activityToNode.containsKey(activity)) {
            CausalNetActivity node = new CausalNetActivity(activity, optionsMap);
            addNode(node);
            activityToNode.put(activity, node);
        }
        return activityToNode.get(activity);
    }

    public void addRelation(String source, String target) {
        DotNode sourceNode = getNodeIfNeeded(source);
        DotNode targetNode = getNodeIfNeeded(target);
        DotEdge edge = new DotEdge(sourceNode, targetNode);
        Pair<String, String> p = new Pair<>(source, target);
        if (!edges.contains(p)) {
            addEdge(edge);
            edges.add(p);
        }
    }
}

class CausalNetActivity extends DotNode {

    public CausalNetActivity(String label, Map<String, Object> optionsMap) {
        super(label, null);

        setOption("fontname", "arial");
        setOption("fontsize", (String) optionsMap.getOrDefault("cnet_activity_fontsize", "10"));
        setOption("fillcolor", (String) optionsMap.getOrDefault("cnet_activity_fillcolor", "white"));

        setOption("shape",     (String) optionsMap.getOrDefault("cnet_activity_shape", "rec"));
        setOption("style",     (String) optionsMap.getOrDefault("cnet_activity_style", "filled"));
        setOption("color",     (String) optionsMap.getOrDefault("cnet_activity_color", "black"));
    }

    @Override
    public int hashCode() {
        return getLabel().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return getLabel().equals(object);
    }
}