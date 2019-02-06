package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import io.jenetics.AnyGene;
import io.jenetics.Mutator;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

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
        return gene; //TODO: implement mutation
    }

    private static BProgramSyncSnapshot newInstance(){
        return null;//dummy
    }

}
