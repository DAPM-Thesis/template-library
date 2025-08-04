package templates.beamline.graph;

import beamline.graphviz.DotNode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PMDotStartNode extends DotNode {

    public PMDotStartNode(Map<String, Object> optionsMap) {
        super("", null);

        setOption("shape", (String) optionsMap.getOrDefault("startNodeShape", "circle"));
        setOption("style", String.join(",", (List<String>) optionsMap.getOrDefault("startNodeStyle", Arrays.asList("filled"))));
        setOption("height", String.valueOf(optionsMap.getOrDefault("startNodeHeight", 0.13)));
        setOption("width", String.valueOf(optionsMap.getOrDefault("startNodeWidth", 0.13)));

        setOption("fillcolor", "#CED6BD"); // #CED6BD:#B3BBA2
        setOption("gradientangle", "270");
        setOption("color", "#595F45");
    }

    @Override
    public String toString() {
        return "{ rank = \"source\"; " + super.toString() + " }";
    }
}
