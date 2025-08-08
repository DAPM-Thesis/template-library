package templates.beamline.softconformance;

import communication.message.impl.softconformance.models.pdfa.PDFA;
import communication.message.impl.softconformance.models.pdfa.PDFAEdge;
import communication.message.impl.softconformance.models.pdfa.PDFANode;

public class WeightsNormalizer {
    public static PDFA normalize(PDFA model, double alpha) {
        PDFA newPdfa = model.getNewCopy();
        newPdfa.setWeightFactor(alpha);

        // update old connections
        double ratio = 1d / newPdfa.getNodes().size();
        for (PDFAEdge edge : newPdfa.getEdges()) {
            double value = edge.getProbability();
            edge.setProbability((alpha * value) + ((1d - alpha) * ratio));
        }

        // add rest of connections
        for (PDFANode source : newPdfa.getNodes()) {
            for (PDFANode target : newPdfa.getNodes()) {
                if (newPdfa.findEdge(source.label(), target.label()) == null) {
                    newPdfa.addEdge(source.label(), target.label(), (1d - alpha) * ratio);
                }
            }
        }

        return newPdfa;
    }
}
