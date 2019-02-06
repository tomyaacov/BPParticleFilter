package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertion;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class ParticleFilterEventListener implements BProgramRunnerListener {

    public List<BEvent> eventList;

    public ParticleFilterEventListener() {
        this.eventList = new LinkedList<>();
    }


    @Override
    public void starting(BProgram bProgram) {

    }

    @Override
    public void started(BProgram bProgram) {

    }

    @Override
    public void superstepDone(BProgram bProgram) {

    }

    @Override
    public void ended(BProgram bProgram) {

    }

    @Override
    public void assertionFailed(BProgram bProgram, FailedAssertion failedAssertion) {

    }

    @Override
    public void bthreadAdded(BProgram bProgram, BThreadSyncSnapshot bThreadSyncSnapshot) {
    }

    @Override
    public void bthreadRemoved(BProgram bProgram, BThreadSyncSnapshot bThreadSyncSnapshot) {

    }

    @Override
    public void bthreadDone(BProgram bProgram, BThreadSyncSnapshot bThreadSyncSnapshot) {

    }

    @Override
    public void eventSelected(BProgram bProgram, BEvent bEvent) {
        eventList.add(bEvent);
    }

    @Override
    public void halted(BProgram bProgram) {

    }
}
