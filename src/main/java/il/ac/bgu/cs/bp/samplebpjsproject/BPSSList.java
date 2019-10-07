package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class BPSSList {

    @Getter @Setter
    private ArrayList<BProgramSyncSnapshot> bProgramSyncSnapshots;

    //@Getter @Setter
    public ExecutorService executorService;

    public double probability;

    public ArrayList<BEvent> eventArrayList;

    public BPSSList(int size) {
        this.bProgramSyncSnapshots = new ArrayList<>(size);
        this.eventArrayList = new ArrayList<>(size);
        for (int i = 0; i < size; i++){
            this.bProgramSyncSnapshots.add(null);
            this.eventArrayList.add(null);
        }
        executorService = ExecutorServiceMaker.makeWithName("BProgramRunner-" + BPFilter.INSTANCE_COUNTER.incrementAndGet());
    }

    public BPSSList(BPSSList bpssList) {
        this.bProgramSyncSnapshots = new ArrayList<>(bpssList.bProgramSyncSnapshots.size());
        this.eventArrayList = new ArrayList<>(bpssList.bProgramSyncSnapshots.size());
        for (int i = 0; i < bpssList.bProgramSyncSnapshots.size(); i++){
            this.bProgramSyncSnapshots.add(bpssList.bProgramSyncSnapshots.get(i));
            this.eventArrayList.add(bpssList.eventArrayList.get(i));
        }
        executorService = ExecutorServiceMaker.makeWithName("BProgramRunner-" + BPFilter.INSTANCE_COUNTER.incrementAndGet());
    }

    public BProgramSyncSnapshot getLast(){
        return this.bProgramSyncSnapshots.get(this.bProgramSyncSnapshots.size()-1);
    }

    public BPSSList cloneFrom(double r){
        double a = 0.0;
        int n = bProgramSyncSnapshots.size()-1;
        while (a < r && n > 0){
            n--;
            a = a + (1-a)/2;
        }
        BPSSList newCopy = new BPSSList(bProgramSyncSnapshots.size());
        for (int i = 0; i < bProgramSyncSnapshots.size(); i++){
            if (i < n){
                if (bProgramSyncSnapshots.get(i) == null){
                    continue;
                }
                newCopy.getBProgramSyncSnapshots().set(i, BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshots.get(i)));
            }
        }
        return newCopy;
    }

    public BPSSList cloneTransition(BProgramSyncSnapshot newbpss){
        BPSSList newCopy = new BPSSList(bProgramSyncSnapshots.size());
        for (int i = 1; i < bProgramSyncSnapshots.size(); i++){
            if (bProgramSyncSnapshots.get(i) == null){
                continue;
            }
            newCopy.getBProgramSyncSnapshots().set(i-1, BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshots.get(i)));
        }
        newCopy.getBProgramSyncSnapshots().set(bProgramSyncSnapshots.size()-1, BProgramSyncSnapshotCloner.clone(newbpss));
        return newCopy;
    }

    public int set(){
        return 0;
    }

//    public void setNoise(float Fnoise, float Tnoise, float Snoise) {
//        this.forwardNoise = Fnoise;
//        this.turnNoise = Tnoise;
//        this.senseNoise = Snoise;
//    }

    /**
     * Senses the distance of the particle to each of its landmarks
     *
     * @return a float array of distances to landmarks
     */
//    public float[] sense() {
//        float[] ret = new float[landmarks.length];
//
//        for(int i=0;i<landmarks.length;i++){
//            float dist = (float) MathX.distance(x, y, landmarks[i].x, landmarks[i].y);
//            ret[i] = dist + (float)random.nextGaussian() * senseNoise;
//        }
//        return ret;
//    }


    public BPSSList move() {
        boolean realityBased = true;
        boolean simulationBased = true;
        ExecutorService executorService = ExecutorServiceMaker.makeWithName("BProgramRunner-" + BPFilter.INSTANCE_COUNTER.incrementAndGet());
        BProgramSyncSnapshot newBProgramSyncSnapshot = BProgramSyncSnapshotCloner.clone(this.getLast());
        for(int j = 0; j < BPFilter.evolutionResolution; j++){
            Set<BEvent> possibleEvents = BPFilter.bProgram.getEventSelectionStrategy().selectableEvents(newBProgramSyncSnapshot);
            if (realityBased){
                if(possibleEvents.contains(BPFilter.eventList.get(BPFilter.programStepCounter-BPFilter.evolutionResolution+j))){
                    newBProgramSyncSnapshot = this.triggerFromReality(executorService, newBProgramSyncSnapshot, j);
                } else {
                    if (simulationBased){
                        newBProgramSyncSnapshot = this.triggerFromSimulation(executorService, newBProgramSyncSnapshot, possibleEvents);

                    }
                }
            } else {
                newBProgramSyncSnapshot = this.triggerFromSimulation(executorService, newBProgramSyncSnapshot, possibleEvents);
            }

        }
        executorService.shutdown();
        return this.cloneTransition(newBProgramSyncSnapshot);
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


    public double measurementProb() {
        BProgramSyncSnapshot bProgramSyncSnapshot = this.getBProgramSyncSnapshots().get(bProgramSyncSnapshots.size()-1);
        ExecutorService executorService = ExecutorServiceMaker.makeWithName("BProgramRunner-" + BPFilter.INSTANCE_COUNTER.incrementAndGet());
        List<Double> fitness = new ArrayList<>(Collections.nCopies(BPFilter.evolutionResolution, 0.0));
        BProgramSyncSnapshot cur;
        for(int i = 0; i < BPFilter.fitnessNumOfIterations; i++){
            cur = BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshot);
            for(int j = 0; j < BPFilter.evolutionResolution; j++){
                Set<BEvent> possibleEvents = BPFilter.bProgram.getEventSelectionStrategy().selectableEvents(cur);
                Optional<EventSelectionResult> res = BPFilter.bProgram.getEventSelectionStrategy().select(cur, possibleEvents);
                if (res.isPresent()) {
                    EventSelectionResult esr = (EventSelectionResult)res.get();
                    if(esr.getEvent().equals(BPFilter.eventList.get(BPFilter.programStepCounter+j))){
                        fitness.set(j,fitness.get(j)+1.0);
                    }
                    try {
                        cur = cur.triggerEvent(esr.getEvent(),
                                executorService,
                                new ArrayList<>());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {
                    break;
                }
            }
        }
        executorService.shutdown();
        double finalFitness = 1.0;
        for(int i=0; i < fitness.size(); i++){
            finalFitness *= (fitness.get(i)/BPFilter.fitnessNumOfIterations);
        }

        probability = finalFitness;

        return finalFitness;
    }

    @Override
    public String toString() {
        return "";
    }

}

