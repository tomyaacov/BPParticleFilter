package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
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
        //System.out.println(bProgramSyncSnapshot.hashCode());
        return eventSelectionStrategy.selectableEvents(bProgramSyncSnapshot);
    }

    @Override
    public Optional<EventSelectionResult> select(BProgramSyncSnapshot bProgramSyncSnapshot, Set<BEvent> set) {
        return eventSelectionStrategy.select(bProgramSyncSnapshot, set);
    }

    public static void main(final String[] args) throws InterruptedException {
        String aResourceName = "example1.js";
        SimpleEventSelectionStrategyFilter ess = new SimpleEventSelectionStrategyFilter(new SimpleEventSelectionStrategy());
        BProgram externalBProgram = new ResourceBProgram(aResourceName, ess);
        BProgramRunner bProgramRunner = new BProgramRunner(externalBProgram);
        ParticleFilterEventListener particleFilterEventListener = new ParticleFilterEventListener();
        bProgramRunner.addListener(particleFilterEventListener);
        bProgramRunner.run();
    }

}
