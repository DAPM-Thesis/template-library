package templates.beamline;

import communication.message.Message;
import communication.message.impl.ProcessMap;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.Sink;
import templates.beamline.graph.PMDotModel;
import utils.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ProcessMapVisualizer extends Sink {
    private final File saveFile;

    public ProcessMapVisualizer(Configuration configuration) {
        super(configuration);

        saveFile = new File((String) configuration.get("saveFilepath"));
    }

    @Override
    public void observe(Pair<Message, Integer> pair) {
        ProcessMap process = (ProcessMap) pair.first();
        PMDotModel dot = new PMDotModel(process, configuration.getConfiguration());
        try { dot.exportToFile(saveFile); } catch (IOException e) { throw new RuntimeException(e); }
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        return Map.of(ProcessMap.class, 1);
    }
}
