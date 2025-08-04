package templates.beamline.graph;

import beamline.graphviz.DotNode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PMDotEndNode extends DotNode {
    public PMDotEndNode(Map<String, Object> optionsMap) {
        super("", null);

        setOption("shape", (String) optionsMap.getOrDefault("endNodeShape", "circle"));
        setOption("style", String.join(",", (List<String>) optionsMap.getOrDefault("endNodeStyle", Arrays.asList("rounded","filled"))));
        setOption("height", String.valueOf(optionsMap.getOrDefault("endNodeHeight", 0.13)));
        setOption("width", String.valueOf(optionsMap.getOrDefault("endNodeWidth", 0.13)));

        setOption("fillcolor", "#D8BBB9"); // #D8BBB9:#BC9F9D
        setOption("gradientangle", "270");
        setOption("color", "#614847");
    }

    @Override
    public String toString() {
        return "{ rank = \"sink\"; " + super.toString() + "}";
    }
}