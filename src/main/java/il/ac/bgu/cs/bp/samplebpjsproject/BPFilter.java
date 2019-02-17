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
        bProgramRunner.run();
        eventList = particleFilterEventListener.eventList;
        bpssList = ess.bProgramSyncSnapshotList;
        //System.out.println(bpssList);
        bProgram = new ResourceBProgram(aResourceName);
    }

    private static BProgramSyncSnapshot newInstance (){
        BProgram bProgram = new ResourceBProgram(aResourceName);
        BProgramSyncSnapshot initBProgramSyncSnapshot = bProgram.setup();
        try {
            BProgramSyncSnapshot bProgramSyncSnapshot = initBProgramSyncSnapshot.start(ExecutorServiceMaker.makeWithName("BProgramRunner-" + 0));// TODO: instance number should maybe be different
            return bProgramSyncSnapshot;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static double fitness(final BProgramSyncSnapshot bProgramSyncSnapshot){
        List<Double> fitness = new ArrayList<>(Collections.nCopies(evolutionResolution, 0.0));
        BProgramSyncSnapshot cur;
        for(int i = 0; i < fitnessNumOfIterations; i++){
            cur = BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshot);
            for(int j = 0; j < evolutionResolution; j++){
                Set<BEvent> possibleEvents = bProgram.getEventSelectionStrategy().selectableEvents(cur);
                Optional<EventSelectionResult> res = bProgram.getEventSelectionStrategy().select(cur, possibleEvents);
                if (res.isPresent()) {
                    EventSelectionResult esr = (EventSelectionResult)res.get();
                    if(esr.getEvent().getName().equals(getEventList().get(getProgramStepCounter()+j).getName())){
                        fitness.set(j,fitness.get(j)+1.0);
                    }
                    try {
                        cur = cur.triggerEvent(esr.getEvent(),
                                ExecutorServiceMaker.makeWithName("BProgramRunner-" + 0),
                                new ArrayList<>());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {
                    break;
                }
            }
        }
        double finalFitness = 1.0;
        for(int i=0; i < fitness.size(); i++){
            finalFitness *= (fitness.get(i)/fitnessNumOfIterations);
        }
        return finalFitness;
    }

    public static void main(final String[] args) throws InterruptedException {
        aResourceName = "example1.js";
        int populationSize = 5;
        double mutationProbability = 0.1;
        int systemSeqeunceLength = 15;
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
                .offspringSelector(new RouletteWheelSelector<>())
                .alterers(new BProgramSyncSnapshotTransitionOperator(bpFilter),
                        new BProgramSyncSnapshotMutation(0.1, bpFilter))
                .maximizing()
                .build();

        EvolutionStatistics<Double, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();
        final MinMax<EvolutionResult<AnyGene<BProgramSyncSnapshot>, Double>> best = MinMax.of();

        engine.stream()
                        .limit(systemSeqeunceLength/evolutionResolution)
                        .peek(statistics)
                        .peek(best).forEach(evolutionResult -> {
            bpssEstimatedList.add(StateEstimation.fittestIndividual(evolutionResult));
            programStepCounter+=evolutionResolution;
        });
        System.out.println(bpssList);
        System.out.println(bpssEstimatedList);
        System.out.println(statistics);
    }

}
