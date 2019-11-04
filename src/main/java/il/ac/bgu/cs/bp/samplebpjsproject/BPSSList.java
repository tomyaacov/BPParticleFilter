package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public class BPSSList {

    public ArrayList<BProgramSyncSnapshot> bProgramSyncSnapshots;

    public ExecutorService executorService1;

    public double probability;

    public ArrayList<BEvent> eventArrayList;

    public int listSize;

    public BPSSList(int size) {
        listSize = size;
        this.bProgramSyncSnapshots = new ArrayList<>(size);
        this.eventArrayList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.bProgramSyncSnapshots.add(null);
            this.eventArrayList.add(null);
        }
        //executorService = ExecutorServiceMaker.makeWithName("BProgramRunner-" + BPFilter.INSTANCE_COUNTER.incrementAndGet());
    }

    public BPSSList(BPSSList bpssList) {
        this.listSize = bpssList.listSize;
        this.bProgramSyncSnapshots = new ArrayList<>(bpssList.bProgramSyncSnapshots.size());
        this.eventArrayList = new ArrayList<>(bpssList.bProgramSyncSnapshots.size());
        for (int i = 0; i < listSize - 1; i++) {
            this.bProgramSyncSnapshots.add(bpssList.bProgramSyncSnapshots.get(i));
            this.eventArrayList.add(bpssList.eventArrayList.get(i));
        }
        this.bProgramSyncSnapshots.add(BProgramSyncSnapshotCloner.clone(bpssList.bProgramSyncSnapshots.get(listSize-1)));
        this.eventArrayList.add(bpssList.eventArrayList.get(listSize-1));
        //executorService = ExecutorServiceMaker.makeWithName("BProgramRunner-" + BPFilter.INSTANCE_COUNTER.incrementAndGet());
    }

    public BProgramSyncSnapshot getLast() {
        return this.bProgramSyncSnapshots.get(this.bProgramSyncSnapshots.size() - 1);
    }

    public BPSSList cloneFrom(double r) {
        double a = 0.0;
        int n = bProgramSyncSnapshots.size() - 1;
        while (a < r && n > 0) {
            n--;
            a = a + (1 - a) / 2;
        }
        BPSSList newCopy = new BPSSList(bProgramSyncSnapshots.size());
        for (int i = 0; i < bProgramSyncSnapshots.size(); i++) {
            if (i < n) {
                if (bProgramSyncSnapshots.get(i) == null) {
                    continue;
                }
                newCopy.bProgramSyncSnapshots.set(i, BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshots.get(i)));
                newCopy.eventArrayList.set(i, eventArrayList.get(i));
            }
        }
        return newCopy;
    }

    public BPSSList cloneTransition(BProgramSyncSnapshot newbpss, BEvent e) {
        BPSSList newCopy = new BPSSList(bProgramSyncSnapshots.size());
        for (int i = 1; i < bProgramSyncSnapshots.size(); i++) {
            if (bProgramSyncSnapshots.get(i) == null) {
                continue;
            }
            newCopy.bProgramSyncSnapshots.set(i - 1, BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshots.get(i)));
            newCopy.eventArrayList.set(i - 1, eventArrayList.get(i));
        }
        newCopy.bProgramSyncSnapshots.set(bProgramSyncSnapshots.size() - 1, BProgramSyncSnapshotCloner.clone(newbpss));
        newCopy.eventArrayList.set(bProgramSyncSnapshots.size() - 1, e);
        return newCopy;
    }

    public BPSSList cloneTransition(BProgramSyncSnapshot newbpss) {
        return this.cloneTransition(newbpss, null);
    }

    public void move(ExecutorService executorService) {
        BProgramSyncSnapshot newBProgramSyncSnapshot = null;
        Set<BEvent> possibleEvents = BPFilter.bProgram.getEventSelectionStrategy().selectableEvents(this.getLast());
        if (BPFilter.realityBased) {
            if (possibleEvents.contains(BPFilter.eventList.get(BPFilter.programStepCounter - BPFilter.evolutionResolution))) {
                newBProgramSyncSnapshot = this.triggerFromReality(executorService, this.getLast());
            } else {
                if (BPFilter.simulationBased) {
                    newBProgramSyncSnapshot = this.triggerFromSimulation(executorService, this.getLast(), possibleEvents);
                }
            }
        } else {
            newBProgramSyncSnapshot = this.triggerFromSimulation(executorService, this.getLast(), possibleEvents);
        }

        for (int i = 1; i < bProgramSyncSnapshots.size(); i++) {
            if (bProgramSyncSnapshots.get(i) == null) {
                continue;
            }
            bProgramSyncSnapshots.set(i - 1, bProgramSyncSnapshots.get(i));
        }
        bProgramSyncSnapshots.set(bProgramSyncSnapshots.size() - 1, newBProgramSyncSnapshot);

    }

    private BProgramSyncSnapshot triggerFromReality(ExecutorService executorService, BProgramSyncSnapshot bProgramSyncSnapshot) {
        BProgramSyncSnapshot newBProgramSyncSnapshot = null;
        try {
            newBProgramSyncSnapshot = bProgramSyncSnapshot.triggerEvent(
                    BPFilter.eventList.get(BPFilter.programStepCounter - BPFilter.evolutionResolution),
                    executorService,
                    new ArrayList<>()); // dummy
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 1; i < bProgramSyncSnapshots.size(); i++) {
            if (eventArrayList.get(i) == null) {
                continue;
            }
            eventArrayList.set(i - 1, eventArrayList.get(i));
        }
        eventArrayList.set(bProgramSyncSnapshots.size() - 1, BPFilter.eventList.get(BPFilter.programStepCounter - BPFilter.evolutionResolution));
        return newBProgramSyncSnapshot;
    }

    private BProgramSyncSnapshot triggerFromSimulation(ExecutorService executorService, BProgramSyncSnapshot newBProgramSyncSnapshot, Set<BEvent> possibleEvents) {
        if (possibleEvents.isEmpty()) {
            return newBProgramSyncSnapshot;
        }
        Optional res = BPFilter.bProgram.getEventSelectionStrategy().select(newBProgramSyncSnapshot, possibleEvents);
        if (res.isPresent()) {
            EventSelectionResult esr = (EventSelectionResult) res.get();
            try {
                newBProgramSyncSnapshot = newBProgramSyncSnapshot.triggerEvent(
                        esr.getEvent(),
                        executorService,
                        new ArrayList<>()); // dummy
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 1; i < bProgramSyncSnapshots.size(); i++) {
                if (eventArrayList.get(i) == null) {
                    continue;
                }
                eventArrayList.set(i - 1, eventArrayList.get(i));
            }
            eventArrayList.set(bProgramSyncSnapshots.size() - 1, esr.getEvent());
        }
        return newBProgramSyncSnapshot;
    }

    public double compareEventLists(List<BEvent> l1, List<BEvent> l2){
        double score = 0.0;
        int num = 0;
        for (int i = 0; i < listSize; i++) {
            if (eventArrayList.get(i) == null) {
                continue;
            }
            num++;
            int ind = l2.indexOf(l1.get(i));
            if (ind > -1) {
                score += 1.0 / (Math.abs(ind - i) + 1.0);
            }
        }
        return score / num;
    }

    public double measurementProb1() {
        List<BEvent> eventList = new LinkedList<>();
        for (int i = 0; i < listSize; i++) {
            if (eventArrayList.get(i) == null) {
                eventList.add(new BEvent("dummy"));
                continue;
            }
            eventList.add(BPFilter.eventList.get(BPFilter.programStepCounter - listSize + i));
        }
        return compareEventLists(eventArrayList, eventList);
    }

    public double measurementProb2(ExecutorService executorService) {
        BProgramSyncSnapshot bProgramSyncSnapshot = this.bProgramSyncSnapshots.get(0);

        double fitness = IntStream.range(0, BPFilter.fitnessNumOfIterations).parallel().mapToDouble(value -> {
            List<BEvent> eventList = new LinkedList<>();
            BProgramSyncSnapshot cur = BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshot);
            for (int j = 0; j < BPFilter.evolutionResolution; j++) {
                // generate array of snapshots and pass it to measurementProb(array1, array2)
                // set iterationFitness to the returned value
                Set<BEvent> possibleEvents = BPFilter.bProgram.getEventSelectionStrategy().selectableEvents(cur);
                Optional<EventSelectionResult> res = BPFilter.bProgram.getEventSelectionStrategy().select(cur, possibleEvents);
                if (res.isPresent()) {
                    EventSelectionResult esr = (EventSelectionResult) res.get();
                    eventList.add(j, esr.getEvent());
                    try {
                        cur = cur.triggerEvent(esr.getEvent(),
                                executorService,
                                new ArrayList<>());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return compareEventLists(eventArrayList, eventList);
        }).average().getAsDouble();
        return fitness;
    }

    public void measurementProb(ExecutorService executorService){
        probability = 0.5 * measurementProb1() + 0.5 * measurementProb2(executorService);
    }

    public double measurementProbOld(ExecutorService executorService) {
        BProgramSyncSnapshot bProgramSyncSnapshot = this.bProgramSyncSnapshots.get(bProgramSyncSnapshots.size() - 1);
        //ExecutorService executorService = ExecutorServiceMaker.makeWithName("BProgramRunner-" + BPFilter.INSTANCE_COUNTER.incrementAndGet());
        List<Double> fitness = new ArrayList<>(Collections.nCopies(BPFilter.bpssListSize, 0.0));
        BProgramSyncSnapshot cur;
        for (int i = 0; i < BPFilter.fitnessNumOfIterations; i++) {
            cur = BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshot);
            for (int j = 0; j < BPFilter.bpssListSize; j++) {
                Set<BEvent> possibleEvents = BPFilter.bProgram.getEventSelectionStrategy().selectableEvents(cur);
                Optional<EventSelectionResult> res = BPFilter.bProgram.getEventSelectionStrategy().select(cur, possibleEvents);
                if (res.isPresent()) {
                    EventSelectionResult esr = (EventSelectionResult) res.get();
                    if (esr.getEvent().equals(BPFilter.eventList.get(BPFilter.programStepCounter + j))) {
                        fitness.set(j, fitness.get(j) + 1.0);
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
        //executorService.shutdown();
        double finalFitness = 1.0;
        for (int i = 0; i < fitness.size(); i++) {
            finalFitness *= (fitness.get(i) / BPFilter.fitnessNumOfIterations);
        }

        probability = finalFitness;

        return finalFitness;
    }

    public double measurementProbNew(ExecutorService executorService) {
        BProgramSyncSnapshot bProgramSyncSnapshot = this.bProgramSyncSnapshots.get(bProgramSyncSnapshots.size() - 1);
        List<Double> fitness = new ArrayList<>(Collections.nCopies(BPFilter.bpssListSize, 0.0));
        BProgramSyncSnapshot cur;
        for (int i = 0; i < BPFilter.fitnessNumOfIterations; i++) {
            cur = bProgramSyncSnapshot;
            for (int j = 0; j < BPFilter.bpssListSize; j++) {
                Set<BEvent> possibleEvents = BPFilter.bProgram.getEventSelectionStrategy().selectableEvents(cur);
                Optional<EventSelectionResult> res = BPFilter.bProgram.getEventSelectionStrategy().select(cur, possibleEvents);
                if (res.isPresent()) {
                    EventSelectionResult esr = (EventSelectionResult) res.get();
                    if (esr.getEvent().equals(BPFilter.eventList.get(BPFilter.programStepCounter + j))) {
                        fitness.set(j, fitness.get(j) + 1.0);
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
        //executorService.shutdown();
        double finalFitness = 1.0;
        for (int i = 0; i < fitness.size(); i++) {
            finalFitness *= (fitness.get(i) / BPFilter.fitnessNumOfIterations);
        }

        probability = finalFitness;

        return finalFitness;
    }

    public int cloneFromIndex(double r){
        double a = 0.0;
        int n = bProgramSyncSnapshots.size() - 1;
        while (a < r && n > 0) {
            n--;
            a = a + (1 - a) / 2;
        }
        return n;
    }

    public void mutate(Random random, ExecutorService executorService){
        BProgramSyncSnapshot lastStateBefore = this.getLast();
        int index;
        if (this.bProgramSyncSnapshots.get(0) == null){
            return; //TODO: what we do if individual is not "full"
        }
        for (int i = 0; i < 10; i++){
            index = this.cloneFromIndex(random.nextDouble());
            bProgramSyncSnapshots.set(index, BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshots.get(index)));
            for (int j = index+1; j < this.bProgramSyncSnapshots.size(); j++){
                    Set<BEvent> possibleEvents = BPFilter.bProgram.getEventSelectionStrategy().selectableEvents(this.bProgramSyncSnapshots.get(j-1));
                    if (possibleEvents.isEmpty()){
                        this.bProgramSyncSnapshots.set(j,this.bProgramSyncSnapshots.get(j-1));
                    } else {
                        Optional res = BPFilter.bProgram.getEventSelectionStrategy().select(this.bProgramSyncSnapshots.get(j - 1), possibleEvents);
                        if (res.isPresent()) {
                            EventSelectionResult esr = (EventSelectionResult) res.get();
                            try {
                                this.bProgramSyncSnapshots.set(j, this.bProgramSyncSnapshots.get(j - 1).triggerEvent(
                                        esr.getEvent(),
                                        executorService,
                                        new ArrayList<>())); // dummy
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            eventArrayList.set(j, esr.getEvent());
                        }
                    }
            }
            if (!lastStateBefore.equals(this.getLast())){
                break;
            }
        }
    }

    @Override
    public String toString() {
        return eventArrayList.toString();
    }

}

