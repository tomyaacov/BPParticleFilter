package il.ac.bgu.cs.bp.samplebpjsproject.old;

import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import il.ac.bgu.cs.bp.samplebpjsproject.BPFilter;
import il.ac.bgu.cs.bp.samplebpjsproject.BPSSList;
import io.jenetics.AnyGene;
import io.jenetics.Mutator;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class BPSSListTransitionOperator extends Mutator<AnyGene<BPSSList>, Double> {

    private BPFilter bpFilter;
    private boolean realityBased;
    private boolean simulationBased;

    public BPFilter getBpFilter() {
        return bpFilter;
    }

    public void setBpFilter(BPFilter bpFilter) {
        this.bpFilter = bpFilter;
    }

    public boolean isRealityBased() {
        return realityBased;
    }

    public void setRealityBased(boolean realityBased) {
        this.realityBased = realityBased;
    }

    public boolean isSimulationBased() {
        return simulationBased;
    }

    public void setSimulationBased(boolean simulationBased) {
        this.simulationBased = simulationBased;
    }

    AnyGene<BPSSList> gene = AnyGene.of(BPSSListTransitionOperator::newInstance);

    public BPSSListTransitionOperator(BPFilter bpFilter, boolean realityBased, boolean simulationBased) {
        super(1);
        this.bpFilter = bpFilter;
        this.realityBased = realityBased;
        this.simulationBased = simulationBased;
    }

    @Override
    protected AnyGene<BPSSList> mutate(AnyGene<BPSSList> gene, Random random) {
        //BProgramSyncSnapshot newBProgramSyncSnapshot = getNextBProgramSyncSnapshot(gene.getAllele().getLast(), gene.getAllele().executorService);
        BProgramSyncSnapshot newBProgramSyncSnapshot = getNextBProgramSyncSnapshot(gene.getAllele().getLast(), ExecutorServiceMaker.makeWithName("BProgramRunner-" + BPFilter.INSTANCE_COUNTER.incrementAndGet()));
        BPSSList newCopy = gene.getAllele().cloneTransition(newBProgramSyncSnapshot);
        return gene.newInstance(newCopy);
    }

    private BProgramSyncSnapshot getNextBProgramSyncSnapshot(BProgramSyncSnapshot bProgramSyncSnapshot, ExecutorService executorService) {
        //TODO: Is this the right approach if we dont have it in possible events
        BProgramSyncSnapshot newBProgramSyncSnapshot = BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshot);
        for(int j = 0; j < BPFilter.evolutionResolution; j++){
            Set<BEvent> possibleEvents = BPFilter.bProgram.getEventSelectionStrategy().selectableEvents(newBProgramSyncSnapshot);
            if (realityBased){
                if(possibleEvents.contains(BPFilter.eventList.get(BPFilter.programStepCounter-BPFilter.evolutionResolution+j))){
                    newBProgramSyncSnapshot = triggerFromReality(executorService, newBProgramSyncSnapshot, j);
                } else {
                    if (simulationBased){
                        newBProgramSyncSnapshot = triggerFromSimulation(executorService, newBProgramSyncSnapshot, possibleEvents);

                    }
                }
            } else {
                newBProgramSyncSnapshot = triggerFromSimulation(executorService, newBProgramSyncSnapshot, possibleEvents);
            }

        }
        executorService.shutdown();
        //System.out.println("Transition");
        return newBProgramSyncSnapshot;
    }

    private BProgramSyncSnapshot triggerFromReality(ExecutorService executorService, BProgramSyncSnapshot newBProgramSyncSnapshot, int j) {
        try {
            newBProgramSyncSnapshot = newBProgramSyncSnapshot.triggerEvent(
                    BPFilter.eventList.get(BPFilter.programStepCounter-BPFilter.evolutionResolution+j),
                    executorService,
                    new ArrayList<>()); // dummy
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return newBProgramSyncSnapshot;
    }

    private BProgramSyncSnapshot triggerFromSimulation(ExecutorService executorService, BProgramSyncSnapshot newBProgramSyncSnapshot, Set<BEvent> possibleEvents) {
        if (possibleEvents.isEmpty()){
            return newBProgramSyncSnapshot;
        }
        Optional res = BPFilter.bProgram.getEventSelectionStrategy().select(newBProgramSyncSnapshot, possibleEvents);
        if (res.isPresent()){
            EventSelectionResult esr = (EventSelectionResult)res.get();
            try {
                newBProgramSyncSnapshot = newBProgramSyncSnapshot.triggerEvent(
                        esr.getEvent(),
                        executorService,
                        new ArrayList<>()); // dummy
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return newBProgramSyncSnapshot;
    }

    private static BPSSList newInstance(){
        return null;//dummy
    }
}
