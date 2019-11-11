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
import il.ac.bgu.cs.bp.bpjs.model.eventselection.PrioritizedBSyncEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;
import io.jenetics.*;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.stat.MinMax;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class BPFilter {

    public static String aResourceName;

    public static int populationSize;

    public static double mutationProbability;

    public static int evolutionResolution;

    public static int fitnessNumOfIterations;

    public static int programStepCounter;

    public static BProgram bProgram;

    public static BProgramRunner bProgramRunner;

    public static List<BEvent> eventList;

    public static List<BProgramSyncSnapshot> bpssList;

    public static List<BProgramSyncSnapshot> bpssEstimatedList = new LinkedList<>();

    public static List<Double> meanFitness = new LinkedList<>();

    public static List<Double> medianFitness = new LinkedList<>();

    public static List<Double> minFitness = new LinkedList<>();

    public static List<Double> maxFitness = new LinkedList<>();

    public static List<Double> meanPopulationAccuracy = new LinkedList<>();

    public static List<Double> estimationAccuracy;

    public static List<Double> estimationBtAccuracy;

    public static BPFilterVisitedStateStore store;

    public static BProgram externalBProgram;

    public static int bpssListSize;

    public static boolean realityBased;

    public static boolean simulationBased;

    public static boolean doMutation;

    public static boolean debug;

    public static long seed;

    public static String particleAnalysisData = "";

    public static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();

    public BPFilter(int populationSize, double mutationProbability, int bpssListSize1) {
        populationSize = populationSize;
        mutationProbability = mutationProbability;
        bpssListSize = bpssListSize1;
        programStepCounter = 0;
    }

    public BPFilter(){

    }

    public void setup(){
        programStepCounter = 0;
        meanFitness = new LinkedList<>();
        medianFitness = new LinkedList<>();
        minFitness = new LinkedList<>();
        maxFitness = new LinkedList<>();
        meanPopulationAccuracy = new LinkedList<>();
        bpssEstimatedList = new LinkedList<>();
        particleAnalysisData = "";
    }

    public static void runBprogram(){
        SimpleEventSelectionStrategyFilter ess = new SimpleEventSelectionStrategyFilter(new SimpleEventSelectionStrategy(seed));
        externalBProgram = new ResourceBProgram(aResourceName, ess);
        bProgramRunner = new BProgramRunner(externalBProgram);
        if (debug) {
            bProgramRunner.addListener(new PrintBProgramRunnerListener());
        }
        ParticleFilterEventListener particleFilterEventListener = new ParticleFilterEventListener();
        bProgramRunner.addListener(particleFilterEventListener);
        externalBProgram.setWaitForExternalEvents(false);
        bProgramRunner.run();
        eventList = particleFilterEventListener.eventList;
        bpssList = ess.bProgramSyncSnapshotList;
        //List<List<String>> states = DrivingCarVisualization.getStatesVisualization(eventList);
        bProgram = new ResourceBProgram(aResourceName);
    }

    public static BPSSList newInstance (){
        BProgram bProgram = new ResourceBProgram(aResourceName);
        BProgramSyncSnapshot initBProgramSyncSnapshot = bProgram.setup();
        BPSSList instance = new BPSSList(bpssListSize);
        try {
            ExecutorService executorService = instance.executorService1;
            BProgramSyncSnapshot bProgramSyncSnapshot = initBProgramSyncSnapshot.start(executorService);// TODO: instance number should maybe be different
            executorService.shutdown();
            instance.bProgramSyncSnapshots.set(bpssListSize-1, bProgramSyncSnapshot);
            return instance;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static double fitness(final BPSSList bpssList){
        BProgramSyncSnapshot bProgramSyncSnapshot = bpssList.bProgramSyncSnapshots.get(bpssListSize-1);
        //ExecutorService executorService = bpssList.executorService;
        ExecutorService executorService = ExecutorServiceMaker.makeWithName("BProgramRunner-" + BPFilter.INSTANCE_COUNTER.incrementAndGet());;
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
        vrf.setMaxTraceLength(80);
        store = new BPFilterVisitedStateStore();
        vrf.setVisitedNodeStore(store);
        vrf.setProgressListener(new IDDfsProgressListener(60, 60, 20, 1500));
        VerificationResult res = vrf.verify(externalBProgram);
    }

    public String toString(){
        StringBuilder str = new StringBuilder();
        Class<?> c = this.getClass();
        Field[] fields = c.getDeclaredFields();

        for( Field field : fields ){
            try {
                str.append(field.getName());
                str.append(": ");
                str.append(field.get(this));
                str.append(System.lineSeparator());
            } catch (IllegalArgumentException e1) {
            } catch (IllegalAccessException e1) {
            }
        }
        return str.toString();
    }



}
