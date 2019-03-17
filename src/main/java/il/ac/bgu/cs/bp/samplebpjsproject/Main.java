package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import io.jenetics.AnyChromosome;
import io.jenetics.AnyGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.stat.MinMax;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

public class Main {
    public static void main(final String[] args) throws Exception {

        int populationSize = 3;
        double mutationProbability = 0.9;
        BPFilter bpFilter = new BPFilter(populationSize, mutationProbability);
        bpFilter.aResourceName = "example1.js";
        bpFilter.evolutionResolution = 3;
        bpFilter.fitnessNumOfIterations = 10;


        bpFilter.runBprogram();
        bpFilter.runOfflineModelChecking();

        final Codec<BProgramSyncSnapshot, AnyGene<BProgramSyncSnapshot>>
                CODEC = Codec.of(Genotype.of(AnyChromosome.of(BPFilter::newInstance)), gt -> gt.getGene().getAllele());

        final Engine<AnyGene<BProgramSyncSnapshot>, Double> engine = Engine
                .builder(BPFilter::fitness, CODEC)
                .populationSize(populationSize)
                .offspringFraction(1)
                //.survivorsSelector(new TournamentSelector<>(5))
                .offspringSelector(new RouletteWheelSelectorDecorator())
                .alterers(new BProgramSyncSnapshotTransitionOperator(bpFilter),
                        new BProgramSyncSnapshotMutation(0.8, bpFilter))
                .maximizing()
                .build();

        EvolutionStatistics<Double, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();
        final MinMax<EvolutionResult<AnyGene<BProgramSyncSnapshot>, Double>> best = MinMax.of();

        engine.stream()
                //.limit(r -> programStepCounter*evolutionResolution >= bpssList.size())
                .limit(bpFilter.bpssList.size()/bpFilter.evolutionResolution-1) // -1 remove the state in the end of the b-thread
                .peek(statistics)
                .peek(best).forEach(evolutionResult -> {
            bpFilter.bpssEstimatedList.add(StateEstimation.fittestIndividual(evolutionResult));
            //programStepCounter+=evolutionResolution;
        });
        //System.out.println(bpssList.size());
        //System.out.println(bpssEstimatedList);
        System.out.println(statistics);

        List<Boolean> estimationAccuracy = new ArrayList<>();
        for (int i=0; i < bpFilter.bpssEstimatedList.size(); i++){
            estimationAccuracy.add(bpFilter.bpssEstimatedList.get(i).equals(bpFilter.bpssList.get(bpFilter.evolutionResolution*(i+1))));
        }
        OptionalDouble average = estimationAccuracy
                .stream()
                .mapToDouble(a -> a ? 1 : 0)
                .average();
        System.out.println("Accuracy " + estimationAccuracy);
        System.out.println("Total accuracy: " + (average.isPresent() ? average.getAsDouble() : 0));

    }
}
