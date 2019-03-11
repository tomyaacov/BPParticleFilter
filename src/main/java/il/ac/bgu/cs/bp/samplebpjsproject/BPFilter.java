package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;

import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;
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

    private static String aResourceName;

    @Getter @Setter
    private int populationSize;

    @Getter @Setter
    private double mutationProbability;

    @Getter @Setter
    private static int evolutionResolution;

    @Getter @Setter
    private static int fitnessNumOfIterations;

    @Getter @Setter
    private static int programStepCounter;

    @Getter @Setter
    private static BProgram bProgram;

    @Getter @Setter
    private BProgramRunner bProgramRunner;

    @Getter @Setter
    private static List<BEvent> eventList;

    @Getter @Setter
    private static List<BProgramSyncSnapshot> bpssList;

    @Getter @Setter
    private static List<BProgramSyncSnapshot> bpssEstimatedList = new LinkedList<>();

    public BPFilter(int populationSize, double mutationProbability) {
        this.populationSize = populationSize;
        this.mutationProbability = mutationProbability;
        programStepCounter = 0;
    }

    public void runBprogram(){
        SimpleEventSelectionStrategyFilter ess = new SimpleEventSelectionStrategyFilter(new SimpleEventSelectionStrategy());
        BProgram externalBProgram = new ResourceBProgram(aResourceName, ess);
        this.bProgramRunner = new BProgramRunner(externalBProgram);
        bProgramRunner.addListener(new PrintBProgramRunnerListener());
        ParticleFilterEventListener particleFilterEventListener = new ParticleFilterEventListener();
        bProgramRunner.addListener(particleFilterEventListener);
        externalBProgram.setWaitForExternalEvents(false);
        bProgramRunner.run();
        eventList = particleFilterEventListener.eventList;
        bpssList = ess.bProgramSyncSnapshotList;
        List<List<String>> states = DrivingCarVisualization.getStatesVisualization(eventList);
//        int generation = 3;
//        double score = 0.87343;
//        boolean match = false;
//        System.out.println(DrivingCarVisualization.frameStatesAndData(frame, generation, score, match));
        //System.out.println(bpssList);
        //DrivingCarVisualization.printDemoRun(states, evolutionResolution);
        bProgram = new ResourceBProgram(aResourceName);
    }

    private static BProgramSyncSnapshot newInstance (){
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
                    if(esr.getEvent().equals(getEventList().get(getProgramStepCounter()+j))){
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

    public static void main(final String[] args) throws InterruptedException {
        aResourceName = "driving_car.js";
        int populationSize = 3;
        double mutationProbability = 0.1;
        evolutionResolution = 3;
        fitnessNumOfIterations = 10;

        BPFilter bpFilter = new BPFilter(populationSize, mutationProbability);
        bpFilter.runBprogram();


        final Codec<BProgramSyncSnapshot, AnyGene<BProgramSyncSnapshot>>
                CODEC = Codec.of(Genotype.of(AnyChromosome.of(BPFilter::newInstance)), gt -> gt.getGene().getAllele());

        final Engine<AnyGene<BProgramSyncSnapshot>, Double> engine = Engine
                .builder(BPFilter::fitness, CODEC)
                .populationSize(populationSize)
                .offspringFraction(1)
                //.survivorsSelector(new TournamentSelector<>(5))
                .offspringSelector(new RouletteWheelSelectorDecorator())
                .alterers(new BProgramSyncSnapshotTransitionOperator(bpFilter),
                        new BProgramSyncSnapshotMutation(0.1, bpFilter))
                .maximizing()
                .build();

        EvolutionStatistics<Double, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();
        final MinMax<EvolutionResult<AnyGene<BProgramSyncSnapshot>, Double>> best = MinMax.of();

        engine.stream()
                //.limit(r -> programStepCounter*evolutionResolution >= bpssList.size())
                .limit(bpssList.size()/evolutionResolution-1) // -1 remove the state in the end of the b-thread
                .peek(statistics)
                .peek(best).forEach(evolutionResult -> {
            bpssEstimatedList.add(StateEstimation.fittestIndividual(evolutionResult));
            //programStepCounter+=evolutionResolution;
        });
        //System.out.println(bpssList.size());
        //System.out.println(bpssEstimatedList);
        System.out.println(statistics);

        List<Boolean> estimationAccuracy = new ArrayList<>();
        for (int i=0; i < bpssEstimatedList.size(); i++){
            estimationAccuracy.add(bpssEstimatedList.get(i).equals(bpssList.get(evolutionResolution*(i+1))));
        }
        OptionalDouble average = estimationAccuracy
                .stream()
                .mapToDouble(a -> a ? 1 : 0)
                .average();
        System.out.println("Accuracy " + estimationAccuracy);
        System.out.println("Total accuracy: " + (average.isPresent() ? average.getAsDouble() : 0));

    }

}
