package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.analysis.*;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class IDDfsProgressListener implements DfsBProgramVerifier.ProgressListener {

    private int minMaxTraceLength;
    private int maxMaxTraceLength;
    private int resMaxTraceLength;
    private int storeMaxSize;
    private int counter;

    public IDDfsProgressListener(int minMaxTraceLength, int maxMaxTraceLength, int resMaxTraceLength, int storeMaxSize) {
        this.minMaxTraceLength = minMaxTraceLength;
        this.maxMaxTraceLength = maxMaxTraceLength;
        this.resMaxTraceLength = resMaxTraceLength;
        this.storeMaxSize = storeMaxSize;
        this.counter = 1;
    }

    @Override
    public void started(DfsBProgramVerifier dfsBProgramVerifier) {
        dfsBProgramVerifier.setMaxTraceLength(minMaxTraceLength);
    }

    @Override
    public void iterationCount(long l, long l1, DfsBProgramVerifier dfsBProgramVerifier) {

    }

    @Override
    public void maxTraceLengthHit(List<DfsTraversalNode> list, DfsBProgramVerifier dfsBProgramVerifier) {

    }

    @Override
    public boolean violationFound(Violation violation, DfsBProgramVerifier dfsBProgramVerifier) {
        return false;
    }

    @Override
    public void done(DfsBProgramVerifier dfsBProgramVerifier) {
        if (dfsBProgramVerifier.getMaxTraceLength()+resMaxTraceLength < maxMaxTraceLength && dfsBProgramVerifier.getVisitedNodeStore().getVisitedStateCount() < storeMaxSize){
            try {
                System.out.println("done - "+counter);
                counter++;
                SimpleEventSelectionStrategy ess = new SimpleEventSelectionStrategy();
                BProgram externalBProgram = new ResourceBProgram(BPFilter.aResourceName, ess);
                externalBProgram.setWaitForExternalEvents(false);
                DfsBProgramVerifier vrf = new DfsBProgramVerifier();
                //vrf.setDebugMode(true);
                vrf.setMaxTraceLength(dfsBProgramVerifier.getMaxTraceLength()+resMaxTraceLength);
                vrf.setVisitedNodeStore(dfsBProgramVerifier.getVisitedNodeStore());
                vrf.setProgressListener(new IDDfsProgressListener((int) (dfsBProgramVerifier.getMaxTraceLength()+resMaxTraceLength), maxMaxTraceLength, resMaxTraceLength, storeMaxSize));
                VerificationResult res = vrf.verify(externalBProgram);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
