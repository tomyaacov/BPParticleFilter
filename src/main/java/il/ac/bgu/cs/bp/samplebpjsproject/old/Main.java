package il.ac.bgu.cs.bp.samplebpjsproject.old;

import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.samplebpjsproject.BPFilter;
import il.ac.bgu.cs.bp.samplebpjsproject.BPSSList;
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
import java.util.Set;

public class Main {
    public static void main(final String[] args) throws Exception {

        int bpssListSize = 5;
        int populationSize = 100;
        double mutationProbability = 0.3;
        BPFilter bpFilter = new BPFilter(populationSize, mutationProbability, bpssListSize);
        BPFilter.aResourceName = "driving_car.js";
        BPFilter.evolutionResolution = 1;
        BPFilter.fitnessNumOfIterations = 10;


        BPFilter.runBprogram();

        final Codec<BPSSList, AnyGene<BPSSList>>
                CODEC = Codec.of(Genotype.of(AnyChromosome.of(BPFilter::newInstance)), gt -> gt.getGene().getAllele());

        final Engine<AnyGene<BPSSList>, Double> engine = Engine
                .builder(BPFilter::fitness, CODEC)
                .populationSize(populationSize)
                .offspringFraction(1)
                //.survivorsSelector(new TournamentSelector<>(5))
                .offspringSelector(new RouletteWheelSelectorDecorator())
                .alterers(new BPSSListTransitionOperator(bpFilter, true, true),
                        new BPSSListMutation(mutationProbability, bpFilter, 1))
                .maximizing()
                .build();

        EvolutionStatistics<Double, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();
        final MinMax<EvolutionResult<AnyGene<BPSSList>, Double>> best = MinMax.of();

        engine.stream()
                //.limit(r -> programStepCounter*evolutionResolution >= bpssList.size())
                .limit(bpFilter.bpssList.size()/bpFilter.evolutionResolution-1) // -1 remove the state in the end of the b-thread
                .peek(statistics)
                .peek(best).forEach(evolutionResult -> {
                    OptionalDouble mf = evolutionResult.getPopulation().stream()
                            .map(a -> a.getFitness())
                            .mapToDouble(a -> a).average();
                    if (mf.isPresent()){
                        bpFilter.meanFitness.add(mf.getAsDouble());
                    }
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

        System.out.println("Mean fitness " + bpFilter.meanFitness);

    }
}

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
//
//        BPFilter.runOfflineModelChecking();