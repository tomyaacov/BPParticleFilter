package il.ac.bgu.cs.bp.samplebpjsproject;


import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import io.jenetics.AnyGene;
import io.jenetics.Mutator;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class BPSSListMutation extends Mutator<AnyGene<BPSSList>, Double> {


    @Getter @Setter
    private BPFilter bpFilter;


    @Getter @Setter
    private int option;

    AnyGene<BPSSList> gene = AnyGene.of(BPSSListMutation::newInstance);

    public BPSSListMutation(double probability, BPFilter bpFilter, int option) {
        super(probability);
        this.bpFilter = bpFilter;
        this.option = option;
    }

    @Override
    protected AnyGene<BPSSList> mutate(AnyGene<BPSSList> gene, Random random) {
        BPSSList newBProgramSyncSnapshot;
        if (option == 1){
            newBProgramSyncSnapshot = option1(gene.getAllele(), random);
        } else {
            newBProgramSyncSnapshot = option2(gene.getAllele(), random);
        }
        return gene.newInstance(newBProgramSyncSnapshot);
    }


    private BPSSList option1(BPSSList bpssList, Random random) {
        ExecutorService executorService = ExecutorServiceMaker.makeWithName("BProgramRunner-" + 0);
        BPSSList newBProgramSyncSnapshot = new BPSSList(bpssList.getBProgramSyncSnapshots().size());
        if (newBProgramSyncSnapshot.getBProgramSyncSnapshots().get(0) == null){
            return bpssList; //TODO: what we do if individual is not "full"
        }
        for (int i = 0; i < 10; i++){
            newBProgramSyncSnapshot = bpssList.cloneFrom(random.nextDouble());
            for (int j = 0; j < newBProgramSyncSnapshot.getBProgramSyncSnapshots().size(); j++){
                if (newBProgramSyncSnapshot.getBProgramSyncSnapshots().get(j) == null){
                    Set<BEvent> possibleEvents = BPFilter.bProgram.getEventSelectionStrategy().selectableEvents(newBProgramSyncSnapshot.getBProgramSyncSnapshots().get(j-1));
                    if (possibleEvents.isEmpty()){
                        newBProgramSyncSnapshot.getBProgramSyncSnapshots().set(j,newBProgramSyncSnapshot.getBProgramSyncSnapshots().get(j-1));
                    } else {
                        Optional res = BPFilter.bProgram.getEventSelectionStrategy().select(newBProgramSyncSnapshot.getBProgramSyncSnapshots().get(j-1), possibleEvents);
                        if (res.isPresent()){
                            EventSelectionResult esr = (EventSelectionResult)res.get();
                            try {
                                newBProgramSyncSnapshot.getBProgramSyncSnapshots().set(j,newBProgramSyncSnapshot.getBProgramSyncSnapshots().get(j-1).triggerEvent(
                                        esr.getEvent(),
                                        executorService,
                                        new ArrayList<>())); // dummy
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
            if (!newBProgramSyncSnapshot.getBProgramSyncSnapshots().get(newBProgramSyncSnapshot.getBProgramSyncSnapshots().size()-1)
                    .equals(bpssList.getBProgramSyncSnapshots().get(bpssList.getBProgramSyncSnapshots().size()-1))){
                break;
            }
        }
        return newBProgramSyncSnapshot;
    }

    private BPSSList option2(BPSSList bProgramSyncSnapshot, Random random) {
        return null;
    }

    private static BPSSList newInstance(){
        return null;//dummy
    }
}
