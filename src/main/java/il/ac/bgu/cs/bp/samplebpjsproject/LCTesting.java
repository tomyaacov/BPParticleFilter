package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.PrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;

public class LCTesting {
    public static void main(final String[] args) throws Exception {

        SimpleEventSelectionStrategyFilter ess = new SimpleEventSelectionStrategyFilter(new SimpleEventSelectionStrategy(2));
        BProgram bProgram = new ResourceBProgram("level_crossing_2.js", ess);
//        BProgramRunner bProgramRunner = new BProgramRunner(bProgram);
//        bProgramRunner.addListener(new PrintBProgramRunnerListener());
//        bProgram.setWaitForExternalEvents(false);
//        bProgramRunner.run();

        DfsBProgramVerifier vrf = new DfsBProgramVerifier();           // ... and a verifier
        vrf.setDebugMode(true);
        vrf.setProgressListener(new PrintDfsVerifierListener());  // add a listener to print progress
        VerificationResult res = vrf.verify(bProgram);                  // this might take a while


        System.out.println(res.getScannedStatesCount());
        //level_crossing_1.js
        //n=1,states=18 n=2,states=1424 n=3,OutOfMemoryError
        //level_crossing_2.js
        //n=1,states=12 n=2,states=62 n=3,306 n=4,1490 n=5,6540
        System.out.println(res.isViolationFound());  // true iff a counter example was found
        System.out.println(res.getViolation());      // an Optional<Violation>
        res.getViolation().ifPresent( v -> v.getCounterExampleTrace() );
    }
}
