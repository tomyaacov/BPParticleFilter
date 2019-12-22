package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class CloneError {
    public static void main(final String[] args) throws Exception {
        ExecutorService executorService = ExecutorServiceMaker.makeWithName("BProgramRunner-" + 1);
        SimpleEventSelectionStrategyFilter ess = new SimpleEventSelectionStrategyFilter(new SimpleEventSelectionStrategy(1));
        BProgram externalBProgram = new ResourceBProgram("localization_clone_test.js", ess);
        BProgramSyncSnapshot bpss = externalBProgram.setup();
        bpss = bpss.start(executorService);
        for (int i=0; i < 100; i++){
            System.out.println("round "+i);
            Set<BEvent> possibleEvents = externalBProgram.getEventSelectionStrategy().selectableEvents(bpss);
            Optional res = externalBProgram.getEventSelectionStrategy().select(bpss, possibleEvents);
            if (res.isPresent()) {
                EventSelectionResult esr = (EventSelectionResult) res.get();
                try {
                    bpss = bpss.triggerEvent(
                            esr.getEvent(),
                            executorService,
                            new ArrayList<>()); // dummy
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                bpss = BProgramSyncSnapshotCloner.clone(bpss);
            } catch (Exception e){
                e.printStackTrace();
                i = 101;
            }
        }
        executorService.shutdown();
    }
}
