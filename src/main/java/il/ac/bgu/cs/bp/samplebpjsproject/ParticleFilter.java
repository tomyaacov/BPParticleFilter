package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

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
        int index = (int) (gen.nextFloat() * numParticles);
        for (int i = 0; i < numParticles; i++) {
            B = gen.nextFloat() * probabilitiesSum;
            while (B > 0) {
                B -= particles.get(index).probability;
                index = circle(index + 1, numParticles);
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
        BProgramSyncSnapshot initBProgramSyncSnapshot = bProgram.setup();
        BPSSList instance = new BPSSList(BPFilter.bpssListSize);
        try {
            BProgramSyncSnapshot bProgramSyncSnapshot = initBProgramSyncSnapshot.start(executorService);// TODO: instance number should maybe be different
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
                p.mutate(gen, executorService);
            }
        });
    }

    public void addPopulationMeanAccuracy(){

        BPFilter.meanPopulationAccuracy.add(particles.stream().parallel().mapToDouble(p ->{
            return p.getLast().equals(BPFilter.bpssList.get(BPFilter.programStepCounter)) ? 1.0 : 0.0;
        }).average().getAsDouble());
    }

    public void generateParticleAnalysisTable(int round, String folderName) throws IOException{
        String name = folderName + File.separator + round;
        String data = particles.stream().parallel().map(p -> {
            return String.valueOf(BPFilter.programStepCounter) + ","
                    + p.probability + ","
                    + (p.getLast().equals(BPFilter.bpssList.get(BPFilter.programStepCounter)) ? "1" : "0") + ","
                    + btAccuray(p.getLast(), BPFilter.bpssList.get(BPFilter.programStepCounter));
        }).collect( Collectors.joining( System.lineSeparator() ) );
        BPFilter.particleAnalysisData = BPFilter.particleAnalysisData + data + System.lineSeparator();
    }

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

    public void shutdown(){
        executorService.shutdown();
    }

    public static void saveResults(int round, BPFilter bpFilter, String folderName) throws IOException {
        String name = folderName + File.separator + round;
        File file = new File(name);
        boolean r = file.mkdir();
        CSVUtils.writeResults(name + File.separator + "meanFitness.csv", BPFilter.meanFitness);
        CSVUtils.writeResults(name + File.separator + "medianFitness.csv", BPFilter.medianFitness);
        CSVUtils.writeResults(name + File.separator + "minFitness.csv", BPFilter.minFitness);
        CSVUtils.writeResults(name + File.separator + "maxFitness.csv", BPFilter.maxFitness);
        CSVUtils.writeResults(name + File.separator + "estimationAccuracy.csv", BPFilter.estimationAccuracy);
        CSVUtils.writeResults(name + File.separator + "estimationBtAccuracy.csv", BPFilter.estimationBtAccuracy);
        CSVUtils.writeResults(name + File.separator + "meanPopulationAccuracy.csv", BPFilter.meanPopulationAccuracy);
        CSVUtils.writeResults(name + File.separator + "particleAnalysisTable.csv", BPFilter.particleAnalysisData);
    }

    public static void run(int round, BPFilter bpFilter, String folderName) throws IOException{
        System.out.println("Starting round " + round + " ...");
        long startTime = System.currentTimeMillis();

        BPFilter.runBprogram();

        ParticleFilter filter = new ParticleFilter(bpFilter.populationSize);

        BPFilter.programStepCounter = BPFilter.programStepCounter+BPFilter.evolutionResolution;

        for (int i=0; i < BPFilter.bpssList.size()/BPFilter.evolutionResolution-1; i++){
            filter.move();
            if(i >= BPFilter.bpssListSize - 1){
                if (BPFilter.doMutation){
                    filter.mutate();
                }
                filter.calculateProb(filter.executorService);
            }
            BPFilter.meanFitness.add(filter.meanFitness);
            BPFilter.medianFitness.add(filter.medianFitness);
            BPFilter.minFitness.add(filter.minFitness);
            BPFilter.maxFitness.add(filter.maxFitness);
            filter.addPopulationMeanAccuracy();
            if (BPFilter.debug){
                filter.generateParticleAnalysisTable(round, folderName);
            }
            BPFilter.programStepCounter = BPFilter.programStepCounter+BPFilter.evolutionResolution;
            BPSSList estimation = filter.resample();
            BPFilter.bpssEstimatedList.add(estimation.getLast());

        }
        filter.shutdown();

        BPFilter.estimationAccuracy = new ArrayList<>();
        for (int i = 0; i < BPFilter.bpssEstimatedList.size(); i++){
            BPFilter.estimationAccuracy.add(BPFilter.bpssEstimatedList.get(i).equals(BPFilter.bpssList.get(BPFilter.evolutionResolution *(i+1))) ? 1.0 : 0.0);
        }
        BPFilter.estimationBtAccuracy = new ArrayList<>();
        for (int i = 0; i < BPFilter.bpssEstimatedList.size(); i++){
            BPFilter.estimationBtAccuracy.add(btAccuray(BPFilter.bpssList.get(BPFilter.evolutionResolution *(i+1)),
                    BPFilter.bpssEstimatedList.get(i)));
        }

        if (BPFilter.debug) {
            OptionalDouble average = BPFilter.estimationAccuracy
                    .stream()
                    .mapToDouble(a -> a)
                    .average();
            System.out.println("Accuracy " + BPFilter.estimationAccuracy);
            System.out.println("Total accuracy: " + (average.isPresent() ? average.getAsDouble() : 0));

            OptionalDouble btaverage = BPFilter.estimationBtAccuracy
                    .stream()
                    .mapToDouble(a -> a)
                    .average();

            System.out.println("Bt Accuracy " + BPFilter.estimationBtAccuracy);
            System.out.println("Total Bt accuracy: " + (btaverage.isPresent() ? btaverage.getAsDouble() : 0));

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
        BPFilter.populationSize = 2;
        BPFilter.mutationProbability = 0.1;
        BPFilter.aResourceName = "driving_car.js";
        BPFilter.evolutionResolution = 1;
        BPFilter.fitnessNumOfIterations = 5;
        BPFilter.realityBased = true;
        BPFilter.simulationBased = true;
        BPFilter.doMutation = false;
        BPFilter.debug = true;


        String name = "results" + File.separator + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        File file = new File(name);
        boolean r = file.mkdir();

        Files.write(Paths.get(name + File.separator + "parameters.txt"), bpFilter.toString().getBytes());

        int rounds = 1;
        for (int i=0; i < rounds; i++){
            BPFilter.seed = i;
            ParticleFilter.run(i, bpFilter, name);
            bpFilter.setup();
        }
        String command = "python3 " + "results" + File.separator + "plot_results.py " + name;
        Process proc = Runtime.getRuntime().exec(command);
    }
}
