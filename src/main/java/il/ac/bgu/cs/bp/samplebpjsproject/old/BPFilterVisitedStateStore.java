package il.ac.bgu.cs.bp.samplebpjsproject.old;

import com.google.common.collect.ArrayListMultimap;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.VisitedStateStore;
import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;

import java.io.Serializable;
import java.util.*;

public class BPFilterVisitedStateStore implements VisitedStateStore, Serializable {

    private final Set<Integer> visited = new HashSet<>();
    ArrayListMultimap<Integer, BProgramSyncSnapshot> map = ArrayListMultimap.create();

    public BPFilterVisitedStateStore() {
    }

    @Override
    public void store(BProgramSyncSnapshot bProgramSyncSnapshot) {
        this.visited.add(bProgramSyncSnapshot.hashCode());
        storeInDataBase(bProgramSyncSnapshot);
    }

    @Override
    public boolean isVisited(BProgramSyncSnapshot bProgramSyncSnapshot) {
        return this.visited.contains(bProgramSyncSnapshot.hashCode());
    }

    @Override
    public void clear() {
        visited.clear();
        map.clear();
    }

    @Override
    public long getVisitedStateCount() {
        return (long)visited.size();
    }

    private void storeInDataBase(BProgramSyncSnapshot bProgramSyncSnapshot){
        BProgramSyncSnapshot cur = BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshot);
        List<BThreadSyncSnapshot> threadSnapshots = new ArrayList<>(cur.getBThreadSnapshots());
        Iterator<BThreadSyncSnapshot> iterator = threadSnapshots.iterator();
        List<BThreadSyncSnapshot> curThreadSnapshots;
        Integer curKey;
        while (iterator.hasNext()){
            curThreadSnapshots = new ArrayList<>(threadSnapshots);
            curThreadSnapshots.remove(iterator.next());
            curKey = Objects.hash(curThreadSnapshots);
            map.put(curKey, bProgramSyncSnapshot);
        }
    }


//    private static final class Snapshot {
//        final BProgramSyncSnapshot bProgramSyncSnapshot;
//        private final int hashCode;
//
//        Snapshot(BProgramSyncSnapshot bpss) {
//            this.bProgramSyncSnapshot = bpss;
//            this.hashCode = this.bProgramSyncSnapshot.hashCode();
//        }
//
//        public int hashCode() {
//            return this.hashCode;
//        }
//
//        public boolean equals(Object obj) {
//            if (this == obj) {
//                return true;
//            } else if (obj == null) {
//                return false;
//            } else if (this.getClass() != obj.getClass()) {
//                return false;
//            } else {
//                BPFilterVisitedStateStore.Snapshot other = (BPFilterVisitedStateStore.Snapshot)obj;
//                return this.hashCode == other.hashCode ;
//            }
//        }
//    }
    public static void main(final String[] args) throws Exception {
        String aResourceName = "old/example1.js";
        BProgram program = new ResourceBProgram(aResourceName);
        DfsBProgramVerifier vrf = new DfsBProgramVerifier();           // ... and a verifier
        vrf.setDebugMode(true);
        vrf.setMaxTraceLength(10);
        BPFilterVisitedStateStore store = new BPFilterVisitedStateStore();
        vrf.setVisitedNodeStore(store);
        VerificationResult res = vrf.verify(program);                  // this might take a while
        System.out.println(vrf.getVisitedNodeStore().getVisitedStateCount());
    }
}
