package templates.beamline.softconformance;

import communication.message.Message;
import communication.message.impl.event.Event;
import communication.message.impl.softconformance.SoftConformanceReport;
import communication.message.impl.softconformance.models.pdfa.PDFA;
import communication.message.serialization.deserialization.impl.SoftConformanceReportDeserializationStrategy;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.operator.Operator;
import templates.beamline.softconformance.models.SoftConformanceTracker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class PDFAConformance extends Operator<SoftConformanceReport, SoftConformanceReport> {

    private final SoftConformanceTracker tracker;
    private final int resultsRefreshRate;
    private String attributeForDiscovery = null;
    private int processedEvents = 0;

    public PDFAConformance(Configuration configuration) throws IOException {
        super(configuration);

        double alpha = (double) configuration.getConfiguration().getOrDefault("alpha", 0.8);
        int maxCasesToStore = (int) configuration.getConfiguration().get("maxCasesToStore");
        resultsRefreshRate = (int) configuration.getConfiguration().getOrDefault("resultsRefreshRate", 10);

        // the PDFA is deserialized based on the file contents of the configuration's provided file
        String contents = Files.readString(Paths.get((String) configuration.get("modelPath")));
        PDFA model = (new SoftConformanceReportDeserializationStrategy()).deserializeModel(contents);
        model = WeightsNormalizer.normalize(model, alpha);
        this.tracker = new SoftConformanceTracker(model, maxCasesToStore);
    }

    public PDFAConformance setAttributeForDiscovery(String attributeForDiscovery) {
        this.attributeForDiscovery = attributeForDiscovery;
        return this;
    }

    @Override
    protected SoftConformanceReport process(Message message, int i) {
        processedEvents++;
        Event event = (Event) message;
        String caseID = event.getCaseID();
        String activityName = attributeForDiscovery == null ? event.getActivity() : attributeForDiscovery; // TODO: deviation; potential cause of error

        tracker.replay(caseID, activityName);
        if (processedEvents % resultsRefreshRate == 0) {
            return tracker.getReport();
        }

        return null;
    }

    @Override
    protected SoftConformanceReport convertAlgorithmOutput(SoftConformanceReport softConformanceReport) {
        return softConformanceReport;
    }

    @Override
    protected boolean publishCondition(SoftConformanceReport softConformanceReport) {
        return softConformanceReport != null;
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        return Map.of(Event.class, 1);
    }
}
