package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import io.jenetics.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class BProgramSyncSnapshotTransitionOperator extends Mutator<AnyGene<BProgramSyncSnapshot>, Double> {

    @Getter @Setter
    private BPFilter bpFilter;

    AnyGene<BProgramSyncSnapshot> gene = AnyGene.of(BProgramSyncSnapshotTransitionOperator::newInstance);

    public BProgramSyncSnapshotTransitionOperator(BPFilter bpFilter) {
        super(1);
        this.bpFilter = bpFilter;
    }

    @Override
    protected AnyGene<BProgramSyncSnapshot> mutate(AnyGene<BProgramSyncSnapshot> gene, Random random) {
        BProgramSyncSnapshot newBProgramSyncSnapshot = getNextBProgramSyncSnapshot(gene.getAllele());
        return gene.newInstance(newBProgramSyncSnapshot);
    }

    private BProgramSyncSnapshot getNextBProgramSyncSnapshot(BProgramSyncSnapshot bProgramSyncSnapshot) {
        BProgramSyncSnapshot newBProgramSyncSnapshot = BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshot);
        for(int j = 0; j < BPFilter.getEvolutionResolution(); j++){
            Set<BEvent> possibleEvents = BPFilter.getBProgram().getEventSelectionStrategy().selectableEvents(newBProgramSyncSnapshot.getStatements(), newBProgramSyncSnapshot.getExternalEvents());
            if(possibleEvents.contains(BPFilter.getEventList().get(BPFilter.getProgramStepCounter()+j))){
                try {
                    newBProgramSyncSnapshot = newBProgramSyncSnapshot.triggerEvent(
                            BPFilter.getEventList().get(BPFilter.getProgramStepCounter()+j),
                            ExecutorServiceMaker.makeWithName("BProgramRunner-" + 0),
                            new ArrayList<>()); // dummy
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return newBProgramSyncSnapshot;
    }

    private static BProgramSyncSnapshot newInstance(){
        return null;//dummy
    }

}
