package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;

import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;
import io.jenetics.*;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.stat.MinMax;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class BPFilter {

    public static String aResourceName;

    public int populationSize;

    public double mutationProbability;

    public static int evolutionResolution;

    public static int fitnessNumOfIterations;

    public static int programStepCounter;

    public static BProgram bProgram;

    public static BProgramRunner bProgramRunner;

    public static List<BEvent> eventList;

    public static List<BProgramSyncSnapshot> bpssList;

    public static List<BProgramSyncSnapshot> bpssEstimatedList = new LinkedList<>();

    public static BPFilterVisitedStateStore store;

    public static BProgram externalBProgram;

    public BPFilter(int populationSize, double mutationProbability) {
        this.populationSize = populationSize;
        this.mutationProbability = mutationProbability;
        programStepCounter = 0;
    }

    public static void runBprogram(){
        SimpleEventSelectionStrategyFilter ess = new SimpleEventSelectionStrategyFilter(new SimpleEventSelectionStrategy());
        externalBProgram = new ResourceBProgram(aResourceName, ess);
        bProgramRunner = new BProgramRunner(externalBProgram);
        //bProgramRunner.addListener(new PrintBProgramRunnerListener());
        ParticleFilterEventListener particleFilterEventListener = new ParticleFilterEventListener();
        bProgramRunner.addListener(particleFilterEventListener);
        externalBProgram.setWaitForExternalEvents(false);
        bProgramRunner.run();
        eventList = particleFilterEventListener.eventList;
        bpssList = ess.bProgramSyncSnapshotList;
        //List<List<String>> states = DrivingCarVisualization.getStatesVisualization(eventList);
        bProgram = new ResourceBProgram(aResourceName);
    }

    public static BProgramSyncSnapshot newInstance (){
        BProgram bProgram = new ResourceBProgram(aResourceName);
        BProgramSyncSnapshot initBProgramSyncSnapshot = bProgram.setup();
        try {
            ExecutorService executorService = ExecutorServiceMaker.makeWithName("BProgramRunner-" + 0);
            BProgramSyncSnapshot bProgramSyncSnapshot = initBProgramSyncSnapshot.start(executorService);// TODO: instance number should maybe be different
            executorService.shutdown();
            return bProgramSyncSnapshot;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static double fitness(final BProgramSyncSnapshot bProgramSyncSnapshot){
        ExecutorService executorService = ExecutorServiceMaker.makeWithName("BProgramRunner-" + 0);
        List<Double> fitness = new ArrayList<>(Collections.nCopies(evolutionResolution, 0.0));
        BProgramSyncSnapshot cur;
        for(int i = 0; i < fitnessNumOfIterations; i++){
            cur = BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshot);
            for(int j = 0; j < evolutionResolution; j++){
                Set<BEvent> possibleEvents = bProgram.getEventSelectionStrategy().selectableEvents(cur);
                Optional<EventSelectionResult> res = bProgram.getEventSelectionStrategy().select(cur, possibleEvents);
                if (res.isPresent()) {
                    EventSelectionResult esr = (EventSelectionResult)res.get();
                    if(esr.getEvent().equals(eventList.get(programStepCounter+j))){
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
            finalFitness *= (fitness.get(i)/fitnessNumOfIterations);
        }
        //System.out.println("Fitness");
        return finalFitness;
    }

    public static void runOfflineModelChecking() throws Exception{
        SimpleEventSelectionStrategy ess = new SimpleEventSelectionStrategy();
        BProgram externalBProgram = new ResourceBProgram(aResourceName, ess);
        externalBProgram.setWaitForExternalEvents(false);
        DfsBProgramVerifier vrf = new DfsBProgramVerifier();
        //vrf.setDebugMode(true);
        vrf.setMaxTraceLength(15);
        store = new BPFilterVisitedStateStore();
        vrf.setVisitedNodeStore(store);
        VerificationResult res = vrf.verify(externalBProgram);
    }



}
