package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.analysis.BThreadSnapshotVisitedStateStore;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.VisitedStateStore;
import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;

import java.util.*;

public class BPFilterVisitedStateStore implements VisitedStateStore {

    private final Set<BPFilterVisitedStateStore.Snapshot> visited = new HashSet();
    private final Map<Integer, List<BProgramSyncSnapshot>> dataBase = new HashMap<>();

    public BPFilterVisitedStateStore() {
    }

    @Override
    public void store(BProgramSyncSnapshot bProgramSyncSnapshot) {
        this.visited.add(new BPFilterVisitedStateStore.Snapshot(bProgramSyncSnapshot));
        //storeInDataBase(bProgramSyncSnapshot);
    }

    @Override
    public boolean isVisited(BProgramSyncSnapshot bProgramSyncSnapshot) {
        return this.visited.contains(new BPFilterVisitedStateStore.Snapshot(bProgramSyncSnapshot));
    }

    @Override
    public void clear() {
        visited.clear();
        //dataBase.clear();
    }

    @Override
    public long getVisitedStateCount() {
        return (long)visited.size();
    }

    private void storeInDataBase(BProgramSyncSnapshot bProgramSyncSnapshot){
        BProgramSyncSnapshot cur = BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshot);
        Set<BThreadSyncSnapshot> threadSnapshots = cur.getBThreadSnapshots();
        Iterator<BThreadSyncSnapshot> iterator = threadSnapshots.iterator();
        Set<BThreadSyncSnapshot> curThreadSnapshots;
        Integer curKey;
        while (iterator.hasNext()){
            curThreadSnapshots = new HashSet<>(threadSnapshots);
            curThreadSnapshots.remove(iterator.next());
            curKey = Objects.hash(curThreadSnapshots);
            if(!dataBase.containsKey(curKey)){
                dataBase.put(curKey, new ArrayList<>());
            }
            dataBase.get(curKey).add(cur);
        }
    }

    private static final class Snapshot {
        final BProgramSyncSnapshot bProgramSyncSnapshot;
        private final int hashCode;

        Snapshot(BProgramSyncSnapshot bpss) {
            this.bProgramSyncSnapshot = bpss;
            this.hashCode = this.bProgramSyncSnapshot.hashCode();
        }

        public int hashCode() {
            return this.hashCode;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (this.getClass() != obj.getClass()) {
                return false;
            } else {
                BPFilterVisitedStateStore.Snapshot other = (BPFilterVisitedStateStore.Snapshot)obj;
                return this.hashCode == other.hashCode && this.bProgramSyncSnapshot.equals(other.bProgramSyncSnapshot);
            }
        }
    }
    public static void main(final String[] args) throws Exception {
        String aResourceName = "example1.js";
        BProgram program = new ResourceBProgram(aResourceName);
        DfsBProgramVerifier vrf = new DfsBProgramVerifier();           // ... and a verifier
        vrf.setDebugMode(true);
        vrf.setMaxTraceLength(10);
        vrf.setVisitedNodeStore(new BPFilterVisitedStateStore());
        VerificationResult res = vrf.verify(program);                  // this might take a while
        System.out.println(vrf.getVisitedNodeStore().getVisitedStateCount());
    }
}
