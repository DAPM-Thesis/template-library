package templates.beamline.softconformance;

import communication.message.Message;
import communication.message.impl.softconformance.SoftConformanceReport;
import communication.message.impl.softconformance.models.SoftConformanceStatus;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.Sink;
import utils.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class SoftConformanceSink extends Sink {
    private final File saveFile;

    public SoftConformanceSink(Configuration configuration) {
        super(configuration);
        saveFile = new File((String) configuration.get("saveFilepath"));
    }

    @Override
    public void observe(Pair<Message, Integer> pair) {
        SoftConformanceReport report = (SoftConformanceReport) pair.first();

        try (FileWriter writer = new FileWriter(saveFile)) {
            for (Map.Entry<String, SoftConformanceStatus> entry : report.entrySet()) {
                String output = "Case: " + entry.getKey() + "\t" +
                        "soft conformance: " + entry.getValue().getSoftConformance() + "\t" +
                        "mean of probs: " + entry.getValue().getMeanProbabilities();
                writer.write(output + '\n');
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        return Map.of(SoftConformanceReport.class, 1);
    }
}
