package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsTraversalNode;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;

import java.util.concurrent.ExecutorService;

public class IDDfsBProgramVerifier extends DfsBProgramVerifier {

    public IDDfsBProgramVerifier() {
        super();
    }

    @Override
    protected Violation dfsUsingStack(DfsTraversalNode aStartNode, ExecutorService execSvc) throws Exception {
        return super.dfsUsingStack(aStartNode, execSvc);
    }
}
