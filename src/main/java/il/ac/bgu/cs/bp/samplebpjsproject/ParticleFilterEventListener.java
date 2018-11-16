package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertion;
import java.util.Random;


public class ParticleFilterEventListener implements BProgramRunnerListener {

    BProgramState bProgramState;
    StringBuilder builder;

    public ParticleFilterEventListener(BProgramState bProgramState, StringBuilder builder) {
        this.bProgramState = bProgramState;
        this.builder = builder;
        builder.append("Event");
    }


    @Override
    public void starting(BProgram bProgram) {

    }

    @Override
    public void started(BProgram bProgram) {
        builder.append("\n");
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
        builder.append("," + bThreadSyncSnapshot.getName());
        bProgramState.addBThread(bThreadSyncSnapshot.getName());
    }

    @Override
    public void bthreadRemoved(BProgram bProgram, BThreadSyncSnapshot bThreadSyncSnapshot) {

    }

    @Override
    public void bthreadDone(BProgram bProgram, BThreadSyncSnapshot bThreadSyncSnapshot) {

    }

    @Override
    public void eventSelected(BProgram bProgram, BEvent bEvent) {
        builder.append(bEvent.getName() + bProgramState.getStatesString());
    }

    @Override
    public void halted(BProgram bProgram) {

    }
}
