package templates.beamline.graph;

import beamline.graphviz.DotEdge;
import beamline.graphviz.DotNode;

import java.util.Map;

public class PMDotEdge extends DotEdge {

    public PMDotEdge(DotNode source, DotNode target, String edgeText, Double weight, Map<String, Object> optionsMap) {
        super(source, target);

        setOption("decorate", String.valueOf(optionsMap.getOrDefault("decorate", false)));
        setOption("fontsize", String.valueOf(optionsMap.getOrDefault("edgeFontsize", 8)));
        setOption("arrowsize", String.valueOf(optionsMap.getOrDefault("arrowsize", 8)));
        setOption("tailclip", String.valueOf(optionsMap.getOrDefault("tailclip", false)));

        setOption("fontname", "Arial");

        if (edgeText != null) { setLabel(" " + edgeText); }
        if (weight != null) {
            setOption("color", ColorPalette.colorToString(ColorPalette.getValue(ColorPalette.Colors.DARK_GRAY, weight)));
            int factor = (source instanceof PMDotStartNode || target instanceof PMDotEndNode) ? 5 : 8;
            setOption("penwidth", "" + (1+factor*weight));
        } else {
            String val = (source instanceof PMDotStartNode || target instanceof PMDotEndNode) ? "2" : "3";
            setOption("penwidth", val);
        }

        if (source instanceof PMDotStartNode) {
            setOption("style", "dashed");
            setOption("color", "#ACB89C");
        }

        if (target instanceof PMDotEndNode) {
            setOption("style", "dashed");
            setOption("color", "#C2B0AB");
        }
    }
}
