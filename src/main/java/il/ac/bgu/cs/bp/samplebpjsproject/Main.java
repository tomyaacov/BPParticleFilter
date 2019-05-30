package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import io.jenetics.AnyChromosome;
import io.jenetics.AnyGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.stat.MinMax;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;

public class Main {
    public static void main(final String[] args) throws Exception {

        int populationSize = 15;
        double mutationProbability = 0.3;
        BPFilter bpFilter = new BPFilter(populationSize, mutationProbability);
        BPFilter.aResourceName = "driving_car2.js";
        BPFilter.evolutionResolution = 3;
        BPFilter.fitnessNumOfIterations = 10;


        BPFilter.runBprogram();

//        File storeFile = new File("store.ser");
//        if (storeFile.exists()){
//            FileInputStream fileIn = new FileInputStream(storeFile);
//            ObjectInputStream in = new ObjectInputStream(fileIn);
//            BPFilter.store = (BPFilterVisitedStateStore) in.readObject();
//            in.close();
//            fileIn.close();
//        } else {
//            BPFilter.runOfflineModelChecking();
//            FileOutputStream fileOut = new FileOutputStream("store.ser");
//            ObjectOutputStream out = new ObjectOutputStream(fileOut);
//            out.writeObject(BPFilter.store);
//            out.close();
//            fileOut.close();
//        }

        BPFilter.runOfflineModelChecking();
        final Codec<BProgramSyncSnapshot, AnyGene<BProgramSyncSnapshot>>
                CODEC = Codec.of(Genotype.of(AnyChromosome.of(BPFilter::newInstance)), gt -> gt.getGene().getAllele());

        final Engine<AnyGene<BProgramSyncSnapshot>, Double> engine = Engine
                .builder(BPFilter::fitness, CODEC)
                .populationSize(populationSize)
                .offspringFraction(1)
                //.survivorsSelector(new TournamentSelector<>(5))
                .offspringSelector(new RouletteWheelSelectorDecorator())
                .alterers(new BProgramSyncSnapshotTransitionOperator(bpFilter, false, true),
                        new BProgramSyncSnapshotMutation(mutationProbability, bpFilter))
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
        System.out.println(statistics.getFitness().getSum());
        System.out.println(statistics.getFitness().getCount());
        System.out.println(statistics.getFitness().getMean());


        List<Boolean> estimationAccuracy = new ArrayList<>();
        for (int i = 0; i < BPFilter.bpssEstimatedList.size(); i++){
            estimationAccuracy.add(BPFilter.bpssEstimatedList.get(i).equals(BPFilter.bpssList.get(BPFilter.evolutionResolution *(i+1))));
        }
        List<Double> estimationBtAccuracy = new ArrayList<>();
        int count;
        int bpssSize;
        Set<BThreadSyncSnapshot> real, estimated;
        for (int i = 0; i < BPFilter.bpssEstimatedList.size(); i++){
            count = 0;
            real = BPFilter.bpssList.get(BPFilter.evolutionResolution *(i+1)).getBThreadSnapshots();
            estimated = BPFilter.bpssEstimatedList.get(i).getBThreadSnapshots();
            bpssSize = real.size();
            for (BThreadSyncSnapshot rbt : real){
                for (BThreadSyncSnapshot ebt : estimated){
                    if (rbt.getName().equals(ebt.getName())){
                        if (ebt.equals(rbt)){
                            count++;
                        }
                    }
                }
            }
            estimationBtAccuracy.add((double)count/bpssSize);
        }
        OptionalDouble average = estimationAccuracy
                .stream()
                .mapToDouble(a -> a ? 1 : 0)
                .average();
        System.out.println("Accuracy " + estimationAccuracy);
        System.out.println("Total accuracy: " + (average.isPresent() ? average.getAsDouble() : 0));

        OptionalDouble btaverage = estimationBtAccuracy
                .stream()
                .mapToDouble(a -> a)
                .average();
        System.out.println("Bt Accuracy " + estimationBtAccuracy);
        System.out.println("Total Bt accuracy: " + (btaverage.isPresent() ? btaverage.getAsDouble() : 0));

    }
}
