package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import io.jenetics.AnyGene;
import io.jenetics.Mutator;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class BProgramSyncSnapshotMutation extends Mutator<AnyGene<BProgramSyncSnapshot>, Double> {

    @Getter @Setter
    private BPFilter bpFilter;

    AnyGene<BProgramSyncSnapshot> gene = AnyGene.of(BProgramSyncSnapshotMutation::newInstance);

    public BProgramSyncSnapshotMutation(double probability, BPFilter bpFilter) {
        super(probability);
        this.bpFilter = bpFilter;
    }

    @Override
    protected AnyGene<BProgramSyncSnapshot> mutate(AnyGene<BProgramSyncSnapshot> gene, Random random) {
        BProgramSyncSnapshot newBProgramSyncSnapshot = getMutatedBProgramSyncSnapshot(gene.getAllele(), random);
        return gene.newInstance(newBProgramSyncSnapshot);
    }

    private BProgramSyncSnapshot getMutatedBProgramSyncSnapshot(BProgramSyncSnapshot bProgramSyncSnapshot, Random random) {
        //TODO: Is this the right approach for mutation
        BProgramSyncSnapshot cur = BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshot);
        Set<BThreadSyncSnapshot> threadSnapshots = cur.getBThreadSnapshots();
        int replace = random.nextInt(threadSnapshots.size());
        List<BThreadSyncSnapshot> curThreadSnapshots = new ArrayList<>(threadSnapshots);
        curThreadSnapshots.remove(replace);
        int curKey = Objects.hash(curThreadSnapshots);
        if (bpFilter.store.map.containsKey(Objects.hash(curThreadSnapshots))){
            int replaceWith = random.nextInt(bpFilter.store.map.get(Objects.hash(curThreadSnapshots)).size());
            return BProgramSyncSnapshotCloner.clone(bpFilter.store.map.get(Objects.hash(curThreadSnapshots)).get(replaceWith));
        } else {
            return cur;
        }
    }

    private static BProgramSyncSnapshot newInstance(){
        return null;//dummy
    }

}
