package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.*;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;
import il.ac.bgu.cs.bp.samplebpjsproject.old.StateEstimation;
import org.mozilla.javascript.NativeObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.DoubleStream;

public class ParticleFilter {

    ArrayList<BPSSList> particles;
    int numParticles = 0;
    Random gen;
    double meanFitness;
    double medianFitness;
    double minFitness;
    double maxFitness;
    double probabilitiesSum;
    public ExecutorService executorService;
    public SimpleEventSelectionStrategy ess;

    public ParticleFilter(int numParticles) {
        gen = new Random();
        gen.setSeed(BPFilter.seed);
        this.numParticles = numParticles;
        executorService = ExecutorServiceMaker.makeWithName("BProgramRunner-" + BPFilter.INSTANCE_COUNTER.incrementAndGet());
        ess = new SimpleEventSelectionStrategy(BPFilter.seed);
        particles = new ArrayList<>(numParticles);
        for (int i = 0; i < numParticles; i++) {
            particles.add(i, newParticle());
        }
    }

    public void move() {
        particles.stream().parallel().forEach(p -> {
            p.move(executorService);
        });
    }

    public void calculateProb(ExecutorService executorService){
        DoubleStream fitnessStream = particles.stream().parallel().mapToDouble(p -> {
            p.measurementProb(executorService);
            return p.probability;
        }).sorted();
        double[] numArray = fitnessStream.toArray();
        probabilitiesSum = DoubleStream.of(numArray).sum();
//        if (sumOfWeights == 0){
//            particles.stream().parallel().forEach(p -> p.probability = 1.0 / numParticles);
//        } else {
//            particles.stream().parallel().forEach(p -> p.probability = p.probability / sumOfWeights);
//        }

        meanFitness = DoubleStream.of(numArray).average().getAsDouble();
        int middle = numArray.length/2;
        if (numArray.length%2 == 1)
            medianFitness = numArray[middle];
        else
            medianFitness = (numArray[middle-1] + numArray[middle]) / 2;
        minFitness = numArray[0];
        maxFitness = numArray[numParticles-1];
    }

    public BPSSList resample() {
        ArrayList<BPSSList> new_particles = new ArrayList<>(numParticles);

        double B;
        BPSSList best = getBestParticle();
        for (int i = 0; i < numParticles; i++) {
            double value = gen.nextFloat() * probabilitiesSum;
            double sum = 0;
            int index = gen.nextInt(numParticles);
            while (sum + particles.get(index).probability <= value){
                sum += particles.get(index).probability;
                if (index == numParticles-1){
                    index = 0;
                } else {
                    index++;
                }
            }
            new_particles.add(i, new BPSSList(particles.get(index)));
        }
        particles = new_particles;
        return best;
    }

    private int circle(int num, int length) {
        while (num > length - 1) {
            num -= length;
        }
        while (num < 0) {
            num += length;
        }
        return num;
    }

    public BPSSList getBestParticle() {
        probabilitiesSum = 0;
        BPSSList particle = particles.get(0);
        for (int i = 0; i < numParticles; i++) {
            probabilitiesSum += particles.get(i).probability;
            if (particles.get(i).probability > particle.probability) {
                particle = particles.get(i);
            }
        }
        return particle;
    }

    @Override
    public String toString() {
        String res = "";
        for (int i = 0; i < numParticles; i++) {
            res += particles.get(i).toString() + "\n";
        }
        return res;
    }

    public BPSSList newParticle (){
        BProgram bProgram = new ResourceBProgram(BPFilter.aResourceName);
        bProgram.putInGlobalScope("map", BPFilter.map);
        BProgramSyncSnapshot initBProgramSyncSnapshot = bProgram.setup();
        BPSSList instance = new BPSSList(BPFilter.bpssListSize);
        try {
            BProgramSyncSnapshot bProgramSyncSnapshot = initBProgramSyncSnapshot.start(executorService);
            instance.bProgramSyncSnapshots.set(BPFilter.bpssListSize-1, bProgramSyncSnapshot);
            return instance;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void mutate(){
        particles.stream().parallel().forEach(p ->{
            if(gen.nextFloat() < BPFilter.mutationProbability){
                //System.out.println(BPFilter.programStepCounter);
                p.mutate(gen, executorService);
            }
        });
    }

    public void addPopulationMeanAccuracy(){

        BPFilter.meanPopulationAccuracy.add(particles.stream().parallel().mapToDouble(p ->{
            return p.getLast().equals(BPFilter.stateList.get(BPFilter.programStepCounter)) ? 1.0 : 0.0;
        }).average().getAsDouble());
    }

//    public void generateParticleAnalysisTable(int round, String folderName) throws IOException{
//        String name = folderName + File.separator + round;
//        String data = particles.stream().parallel().map(p -> {
//            return String.valueOf(BPFilter.programStepCounter) + ","
//                    + p.probability + ","
//                    + (p.getLast().equals(BPFilter.stateList.get(BPFilter.programStepCounter)) ? "1" : "0") + ","
//                    + btAccuray(p.getLast(), BPFilter.stateList.get(BPFilter.programStepCounter));
//        }).collect( Collectors.joining( System.lineSeparator() ) );
//        BPFilter.particleAnalysisData = BPFilter.particleAnalysisData + data + System.lineSeparator();
//    }

    public static double btAccuray(BProgramSyncSnapshot a, BProgramSyncSnapshot b){
        int count = 0;
        Set<BThreadSyncSnapshot> real = a.getBThreadSnapshots();
        Set<BThreadSyncSnapshot> estimated = b.getBThreadSnapshots();
        int bpssSize = real.size();
        for (BThreadSyncSnapshot rbt : real){
            for (BThreadSyncSnapshot ebt : estimated){
                if (rbt.getName().equals(ebt.getName())){
                    if (ebt.equals(rbt)){
                        count++;
                    }
                }
            }
        }
        return (double)count/bpssSize;
    }

    public LocalizationState predict(){
        OptionalDouble mean_x = particles
                .stream()
                .mapToDouble(p -> {
                    Object state_x = ((NativeObject)p.getLastState().getData()).get("x");
                    if(state_x instanceof Integer){
                        state_x = ((Integer)state_x).doubleValue();
                    }
                    return (Double)state_x;
                })
                .average();
        OptionalDouble mean_y = particles
                .stream()
                .mapToDouble(p -> {
                    Object state_y = ((NativeObject)p.getLastState().getData()).get("y");
                    if(state_y instanceof Integer){
                        state_y = ((Integer)state_y).doubleValue();
                    }
                    return (Double)state_y;
                })
                .average();
        return new LocalizationState(mean_x.getAsDouble(), mean_y.getAsDouble());
    }

    public void shutdown(){
        executorService.shutdown();
    }

    public static void saveResults(int round, BPFilter bpFilter, String folderName) throws IOException {
        String name = folderName + File.separator + round;
        File file = new File(name);
        boolean r = file.mkdir();
        //CSVUtils.writeResults(name + File.separator + "meanFitness.csv", BPFilter.meanFitness);
        //CSVUtils.writeResults(name + File.separator + "medianFitness.csv", BPFilter.medianFitness);
        //CSVUtils.writeResults(name + File.separator + "minFitness.csv", BPFilter.minFitness);
        //CSVUtils.writeResults(name + File.separator + "maxFitness.csv", BPFilter.maxFitness);
        //CSVUtils.writeResults(name + File.separator + "estimationAccuracy.csv", BPFilter.estimationAccuracy);
        CSVUtils.writeResults(name + File.separator + "meanAfterResamplingEstimationAccDis.csv", BPFilter.meanAfterResamplingEstimationAccDis);
        CSVUtils.writeResults(name + File.separator + "meanBeforeResamplingEstimationAccDis.csv", BPFilter.meanBeforeResamplingEstimationAccDis);
        CSVUtils.writeResults(name + File.separator + "bestParticleEstimationAccDis.csv", BPFilter.bestParticleEstimationAccDis);
    }

    public static void run(int round, BPFilter bpFilter, String folderName) throws IOException, InterruptedException{
        System.out.println("Starting round " + round + " ...");
        long startTime = System.currentTimeMillis();

        BPFilter.runBprogram();

        ParticleFilter filter = new ParticleFilter(bpFilter.populationSize);

        BPFilter.programStepCounter = BPFilter.programStepCounter+BPFilter.evolutionResolution;

        //BPFilter.buildStatisticalModel(filter.gen, filter.executorService);

        for (int i = 0; i < BPFilter.stateList.size()/BPFilter.evolutionResolution; i++){
            System.out.println(i);
            filter.move();
            System.out.println("a");
            if (BPFilter.doMutation){
                filter.mutate();
            }
            filter.calculateProb(filter.executorService);
            System.out.println("b");
            BPFilter.meanFitness.add(filter.meanFitness);
            BPFilter.medianFitness.add(filter.medianFitness);
            BPFilter.minFitness.add(filter.minFitness);
            BPFilter.maxFitness.add(filter.maxFitness);
//            filter.addPopulationMeanAccuracy();
//            if (BPFilter.debug){
//                filter.generateParticleAnalysisTable(round, folderName);
//            }

            System.out.println("c");
            BPFilter.programStepCounter = BPFilter.programStepCounter+BPFilter.evolutionResolution;
            BPFilter.meanBeforeResamplingEstimation.add(filter.predict());
            System.out.println("d");
            BPSSList estimation = filter.resample();
            System.out.println("e");
            BPFilter.bestParticleEstimation.add(new LocalizationState(estimation.getLastState()));
            System.out.println("f");
            BPFilter.meanAfterResamplingEstimation.add(filter.predict());

        }
        filter.shutdown();

        BPFilter.estimationAccuracy = new ArrayList<>();
        for (int i = 0; i < BPFilter.meanAfterResamplingEstimation.size(); i++){
            BPFilter.estimationAccuracy.add(BPFilter.meanAfterResamplingEstimation.get(i).equals(BPFilter.stateList.get(BPFilter.evolutionResolution *(i))) ? 1.0 : 0.0);
        }

        BPFilter.meanAfterResamplingEstimationAccDis = new ArrayList<>();
        for (int i = 0; i < BPFilter.meanAfterResamplingEstimation.size(); i++){
            BPFilter.meanAfterResamplingEstimationAccDis.add(BPFilter.meanAfterResamplingEstimation.get(i).distanceTo(BPFilter.stateList.get(BPFilter.evolutionResolution *(i))));
        }
        BPFilter.meanBeforeResamplingEstimationAccDis = new ArrayList<>();
        for (int i = 0; i < BPFilter.meanBeforeResamplingEstimation.size(); i++){
            BPFilter.meanBeforeResamplingEstimationAccDis.add(BPFilter.meanBeforeResamplingEstimation.get(i).distanceTo(BPFilter.stateList.get(BPFilter.evolutionResolution *(i))));
        }

        BPFilter.bestParticleEstimationAccDis = new ArrayList<>();
        for (int i = 0; i < BPFilter.bestParticleEstimation.size(); i++){
            BPFilter.bestParticleEstimationAccDis.add(BPFilter.bestParticleEstimation.get(i).distanceTo(BPFilter.stateList.get(BPFilter.evolutionResolution *(i))));
        }

//        BPFilter.estimationBtAccuracy = new ArrayList<>();
//        for (int i = 0; i < BPFilter.stateEstimatedList.size(); i++){
//            BPFilter.estimationBtAccuracy.add(btAccuray(BPFilter.stateList.get(BPFilter.evolutionResolution *(i+1)),
//                    BPFilter.stateEstimatedList.get(i)));
//        }

        if (BPFilter.debug) {
            OptionalDouble average = BPFilter.estimationAccuracy
                    .stream()
                    .mapToDouble(a -> a)
                    .average();
            System.out.println("Accuracy " + BPFilter.estimationAccuracy);
            System.out.println("Total accuracy: " + (average.isPresent() ? average.getAsDouble() : 0));

            OptionalDouble btaverage = BPFilter.meanAfterResamplingEstimationAccDis
                    .stream()
                    .mapToDouble(a -> a)
                    .average();

            System.out.println("Estimation Distance " + BPFilter.meanAfterResamplingEstimationAccDis);
            System.out.println("Total Estimation Distance: " + (btaverage.isPresent() ? btaverage.getAsDouble() : 0));

            System.out.println("Mean fitness " + BPFilter.meanFitness);
            System.out.println("Median fitness " + BPFilter.medianFitness);
            System.out.println("Min fitness " + BPFilter.minFitness);
            System.out.println("Max fitness " + BPFilter.maxFitness);
        }
        ParticleFilter.saveResults(round, bpFilter, folderName);
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.println("Execution time in milliseconds: " + timeElapsed);
    }

    public static void main(final String[] args) throws Exception {
        BPFilter bpFilter = new BPFilter();
        BPFilter.bpssListSize = 5;
        BPFilter.populationSize = 300;
        BPFilter.mutationProbability = 0.1;
        BPFilter.aResourceName = "localization.js";
        BPFilter.evolutionResolution = 1;
        BPFilter.fitnessNumOfIterations = 5;
        BPFilter.realityBased = true;
        BPFilter.simulationBased = true;
        BPFilter.doMutation = false;
        BPFilter.debug = true;
        BPFilter.statisticalModelNumOfIteration = 1000;


        String name = "results" + File.separator + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        File file = new File(name);
        boolean r = file.mkdir();

        Files.write(Paths.get(name + File.separator + "parameters.txt"), bpFilter.toString().getBytes());

        Random rand = new Random(1);

        BPFilter.generateMap(rand);

        int rounds = 5;
        for (int i=0; i < rounds; i++){
            BPFilter.seed = i;
            ParticleFilter.run(i, bpFilter, name);
            bpFilter.setup();
        }
        String command = "python3 " + "results" + File.separator + "plot_results.py " + name;
        //Process proc = Runtime.getRuntime().exec(command);
    }
}
