package templates.beamline.softconformance.models;

import communication.message.impl.softconformance.SoftConformanceReport;
import communication.message.impl.softconformance.models.SoftConformanceStatus;
import communication.message.impl.softconformance.models.pdfa.PDFA;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class SoftConformanceTracker extends HashMap<String, SoftConformanceStatus> {
    private final Queue<String> caseIdHistory;
    private final PDFA model;
    private final int maxCasesToStore;

    public SoftConformanceTracker(PDFA model, int maxCasesToStore) {
        this.caseIdHistory = new LinkedList<>();
        this.model = model;
        this.maxCasesToStore = maxCasesToStore;
    }

    public SoftConformanceStatus replay(String caseID, String newEventName) {
        if (containsKey(caseID)) {
            get(caseID).replayEvent(newEventName); // now we can perform the replay
            caseIdHistory.remove(caseID); // need to refresh the cache
        } else {
            // check if we can store the new case
            if (caseIdHistory.size() >= maxCasesToStore) {
                remove(caseIdHistory.poll()); // we have no room for the case, we need to remove the case id with most far update time
            }
            // now we can perform the replay
            SoftConformanceStatus cs = new SoftConformanceStatus(model, caseID);
            cs.replayEvent(newEventName);
            put(caseID, cs);

        }
        caseIdHistory.add(caseID); // put the replayed case as first one

        return get(caseID);
    }

    public SoftConformanceReport getReport() {
        SoftConformanceReport report = new SoftConformanceReport();
        for(Entry<String, SoftConformanceStatus> e : entrySet()) {
            report.put(e.getKey(), e.getValue());
        }
        return report;
    }
}
