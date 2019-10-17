package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class ParticleFilter {

    ArrayList<BPSSList> particles;
    int numParticles = 0;
    Random gen = new Random();
    double meanFitness;
    double medianFitness;
    double minFitness;
    double maxFitness;
    public ExecutorService executorService;

    public ParticleFilter(int numParticles) {
        this.numParticles = numParticles;
        executorService = ExecutorServiceMaker.makeWithName("BProgramRunner-" + BPFilter.INSTANCE_COUNTER.incrementAndGet());
        particles = new ArrayList<>(numParticles);
        for (int i = 0; i < numParticles; i++) {
            particles.add(i, newParticle());
        }
        gen.setSeed(BPFilter.seed);
    }

    public void move() {
        for (int i = 0; i < numParticles; i++) {
//            final int c = i;
//            executorService.execute(() -> particles.get(c).move(executorService));
//
            particles.get(i).move(executorService);
        }
    }

    public void calculateProb(){
        meanFitness = 0f;
        double[] numArray = new double[numParticles];
        for (int i = 0; i < numParticles; i++) {
            //particles.get(i).measurementProb(executorService);
            particles.get(i).measurementProb();
            meanFitness += particles.get(i).probability;
            numArray[i] = particles.get(i).probability;
        }
        Arrays.sort(numArray);
        int middle = numArray.length/2;
        if (numArray.length%2 == 1)
            medianFitness = numArray[middle];
        else
            medianFitness = (numArray[middle-1] + numArray[middle]) / 2;
        minFitness = numArray[0];
        maxFitness = numArray[numParticles-1];
        meanFitness /= numParticles;
    }

    public BPSSList resample() {
        ArrayList<BPSSList> new_particles = new ArrayList<>(numParticles);

        float B = 0f;
        BPSSList best = getBestParticle();
        int index = (int) gen.nextFloat() * numParticles;
        for (int i = 0; i < numParticles; i++) {
            B += gen.nextFloat() * 2f * best.probability;
            while (B > particles.get(index).probability) {
                B -= particles.get(index).probability;
                index = circle(index + 1, numParticles);
            }
            new_particles.add(i, new BPSSList(particles.get(index)));
        }
//        for (int i = 0; i < numParticles; i++){
//            particles.get(i).executorService.shutdown();
//        }
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
        BPSSList particle = particles.get(0);
        for (int i = 0; i < numParticles; i++) {
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
            //executorService.shutdown();
            instance.getBProgramSyncSnapshots().set(BPFilter.bpssListSize-1, bProgramSyncSnapshot);
            return instance;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void mutate(){
        return;
    }

    public void shutdown(){
//        for (int i = 0; i < numParticles; i++){
//            particles.get(i).executorService.shutdown();
//        }
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
    }

    public static void run(int round, BPFilter bpFilter, String folderName) throws IOException{
        System.out.println("Starting round " + round + " ...");
        long startTime = System.currentTimeMillis();

        BPFilter.runBprogram();

        ParticleFilter filter = new ParticleFilter(bpFilter.populationSize);

        BPFilter.programStepCounter = BPFilter.programStepCounter+BPFilter.evolutionResolution;

        for (int i=0; i < BPFilter.bpssList.size()/BPFilter.evolutionResolution-1; i++){
            filter.move();
            if (BPFilter.doMutation){

            }
            filter.calculateProb();
            BPFilter.meanFitness.add(filter.meanFitness);
            BPFilter.medianFitness.add(filter.medianFitness);
            BPFilter.minFitness.add(filter.minFitness);
            BPFilter.maxFitness.add(filter.maxFitness);
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
            BPFilter.estimationBtAccuracy.add((double)count/bpssSize);
        }
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
        ParticleFilter.saveResults(round, bpFilter, folderName);
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.println("Execution time in milliseconds: " + timeElapsed);
    }

    public static void main(final String[] args) throws Exception {
        int bpssListSize = 5;
        int populationSize = 1;
        double mutationProbability = 0.3;
        BPFilter bpFilter = new BPFilter(populationSize, mutationProbability, bpssListSize);
        BPFilter.aResourceName = "driving_car.js";
        BPFilter.evolutionResolution = 1;
        BPFilter.fitnessNumOfIterations = 10;
        BPFilter.realityBased = true;
        BPFilter.simulationBased = true;
        BPFilter.doMutation = false;
        BPFilter.seed = 1;


        String name = "results" + File.separator + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        File file = new File(name);
        boolean r = file.mkdir();

        Files.write(Paths.get(name + File.separator + "parameters.txt"), bpFilter.toString().getBytes());

        int rounds = 1;
        for (int i=0; i < rounds; i++){
            ParticleFilter.run(i, bpFilter, name);
            bpFilter.setup();
        }
    }
}
