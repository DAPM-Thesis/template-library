package templates.beamline.graph;

import beamline.graphviz.DotNode;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PMDotNode extends DotNode {
    private String label;

    public PMDotNode(String label, Map<String, Object> optionsMap) {
        super(label, null);

        this.label = label;

        setColorWeight(1.0, ColorPalette.create((String) optionsMap.getOrDefault("activityColor", "blue")));

        setOption("shape", (String) optionsMap.getOrDefault("nodeShape", "box"));
        setOption("fixedsize", String.valueOf(optionsMap.getOrDefault("fixedsize", "true")));
        setOption("height", String.valueOf(optionsMap.getOrDefault("nodeHeight", 0.23)));
        setOption("width", String.valueOf(optionsMap.getOrDefault("nodeWidth", 1.2)));
        setOption("style", String.join(",", (List<String>) optionsMap.getOrDefault("nodeStyle", Arrays.asList("rounded","filled"))));

        setOption("fontname", "Arial");
    }

    public void setSecondLine(String secondLine) {
        if (secondLine != null) { setLabel("<<font point-size='22'>" + label + "</font> <br/><font point-size='11'>" + secondLine + "</font>>"); }
    }

    public void setColorWeight(Double weight, ColorPalette.Colors activityColor) {
        if (weight != null) {
            setOption("fillcolor", "#FDEFD8");
            return;
        }

        Color backgroundColor = ColorPalette.getValue(activityColor, weight);
        Color fontColor = ColorPalette.getFontColor(backgroundColor);
        setOption("fillcolor", ColorPalette.colorToString(backgroundColor));
        setOption("fontcolor", ColorPalette.colorToString(fontColor));
        setOption("fixedsize", "false");

    }
}
