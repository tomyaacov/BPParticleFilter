package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;

import java.util.concurrent.ExecutorService;

public class TestFitnessTime {
    public static void main(final String[] args) throws Exception {

        int bpssListSize = 5;
        int populationSize = 10;
        double mutationProbability = 0.3;
        BPFilter bpFilter = new BPFilter(populationSize, mutationProbability, bpssListSize);
        BPFilter.aResourceName = "driving_car.js";
        BPFilter.evolutionResolution = 1;
        BPFilter.fitnessNumOfIterations = 100;
        BPFilter.realityBased = true;
        BPFilter.simulationBased = true;
        BPFilter.doMutation = false;
        BPFilter.seed = 1;
        BPFilter.debug = true;

        ExecutorService executorService = ExecutorServiceMaker.makeWithName("BProgramRunner-" + BPFilter.INSTANCE_COUNTER.incrementAndGet());

        BPFilter.runBprogram();

        BPFilter.programStepCounter = BPFilter.programStepCounter+BPFilter.evolutionResolution;

        ParticleFilter filter = new ParticleFilter(bpFilter.populationSize);

        BPSSList particle = filter.newParticle();

        particle.move(executorService);
        BPFilter.programStepCounter = BPFilter.programStepCounter+BPFilter.evolutionResolution;
        particle.move(executorService);
        BPFilter.programStepCounter = BPFilter.programStepCounter+BPFilter.evolutionResolution;
        particle.move(executorService);
        BPFilter.programStepCounter = BPFilter.programStepCounter+BPFilter.evolutionResolution;
        particle.move(executorService);
        BPFilter.programStepCounter = BPFilter.programStepCounter+BPFilter.evolutionResolution;
        particle.move(executorService);


        long startTime = System.currentTimeMillis();
        double fitness = particle.measurementProbOld(executorService);
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.println("Execution time in milliseconds: " + timeElapsed);

        //double fitness = particle.measurementProb(executorService);

        System.out.println(particle);
        System.out.println(fitness);

//        startTime = System.currentTimeMillis();
//        fitness = particle.measurementProbNew(executorService);
//        endTime = System.currentTimeMillis();
//        timeElapsed = endTime - startTime;
//        System.out.println("Execution time in milliseconds: " + timeElapsed);
//
//        System.out.println(particle);
//        System.out.println(fitness);


        System.out.println(particle.cloneFromIndex(0.1));
        System.out.println(particle.cloneFromIndex(0.2));
        System.out.println(particle.cloneFromIndex(0.3));
        System.out.println(particle.cloneFromIndex(0.4));
        System.out.println(particle.cloneFromIndex(0.5));
        System.out.println(particle.cloneFromIndex(0.6));
        System.out.println(particle.cloneFromIndex(0.7));
        System.out.println(particle.cloneFromIndex(0.8));
        System.out.println(particle.cloneFromIndex(0.9));
        System.out.println(particle.cloneFromIndex(1.0));


        filter.shutdown();
        executorService.shutdown();
    }
}
