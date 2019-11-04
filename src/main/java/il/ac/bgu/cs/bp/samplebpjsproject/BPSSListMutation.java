package il.ac.bgu.cs.bp.samplebpjsproject;


import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import io.jenetics.AnyGene;
import io.jenetics.Mutator;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class BPSSListMutation extends Mutator<AnyGene<BPSSList>, Double> {


    private BPFilter bpFilter;

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
            newBProgramSyncSnapshot = option1(gene.getAllele(), random, ExecutorServiceMaker.makeWithName("BProgramRunner-" + BPFilter.INSTANCE_COUNTER.incrementAndGet()));
            //newBProgramSyncSnapshot = option1(gene.getAllele(), random, gene.getAllele().executorService);
        } else {
            newBProgramSyncSnapshot = option2(gene.getAllele(), random);
        }
        return gene.newInstance(newBProgramSyncSnapshot);
    }


    private BPSSList option1(BPSSList bpssList, Random random, ExecutorService executorService) {
        BPSSList newBProgramSyncSnapshot = new BPSSList(bpssList.bProgramSyncSnapshots.size());
        if (newBProgramSyncSnapshot.bProgramSyncSnapshots.get(0) == null){
            return bpssList; //TODO: what we do if individual is not "full"
        }
        for (int i = 0; i < 10; i++){
            newBProgramSyncSnapshot = bpssList.cloneFrom(random.nextDouble());
            for (int j = 0; j < newBProgramSyncSnapshot.bProgramSyncSnapshots.size(); j++){
                if (newBProgramSyncSnapshot.bProgramSyncSnapshots.get(j) == null){
                    Set<BEvent> possibleEvents = BPFilter.bProgram.getEventSelectionStrategy().selectableEvents(newBProgramSyncSnapshot.bProgramSyncSnapshots.get(j-1));
                    if (possibleEvents.isEmpty()){
                        newBProgramSyncSnapshot.bProgramSyncSnapshots.set(j,newBProgramSyncSnapshot.bProgramSyncSnapshots.get(j-1));
                    } else {
                        Optional res = BPFilter.bProgram.getEventSelectionStrategy().select(newBProgramSyncSnapshot.bProgramSyncSnapshots.get(j-1), possibleEvents);
                        if (res.isPresent()){
                            EventSelectionResult esr = (EventSelectionResult)res.get();
                            try {
                                newBProgramSyncSnapshot.bProgramSyncSnapshots.set(j,newBProgramSyncSnapshot.bProgramSyncSnapshots.get(j-1).triggerEvent(
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
            if (!newBProgramSyncSnapshot.bProgramSyncSnapshots.get(newBProgramSyncSnapshot.bProgramSyncSnapshots.size()-1)
                    .equals(bpssList.bProgramSyncSnapshots.get(bpssList.bProgramSyncSnapshots.size()-1))){
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
