package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;

import java.util.*;

public class SimpleEventSelectionStrategyFilter implements EventSelectionStrategy {

    private EventSelectionStrategy eventSelectionStrategy;

    public List<BProgramSyncSnapshot> bProgramSyncSnapshotList;

    public SimpleEventSelectionStrategyFilter(EventSelectionStrategy eventSelectionStrategy) {
        this.eventSelectionStrategy = eventSelectionStrategy;
        bProgramSyncSnapshotList = new LinkedList<>();
    }

    @Override
    public Set<BEvent> selectableEvents(BProgramSyncSnapshot bProgramSyncSnapshot) {
        bProgramSyncSnapshotList.add(bProgramSyncSnapshot);
        return eventSelectionStrategy.selectableEvents(bProgramSyncSnapshot);
    }

    @Override
    public Optional<EventSelectionResult> select(BProgramSyncSnapshot bProgramSyncSnapshot, Set<BEvent> set) {
        return eventSelectionStrategy.select(bProgramSyncSnapshot, set);
    }
}
