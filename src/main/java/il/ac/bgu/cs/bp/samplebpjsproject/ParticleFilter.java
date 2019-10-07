package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;

import java.util.*;

public class ParticleFilter {

    BPSSList[] particles;
    int numParticles = 0;
    Random gen = new Random();
    double meanFitness;

    public ParticleFilter(int numParticles) {
        this.numParticles = numParticles;

        particles = new BPSSList[numParticles];
        for (int i = 0; i < numParticles; i++) {
            particles[i] = BPFilter.newInstance();
        }
    }

//    public void setNoise(float Fnoise, float Tnoise, float Snoise) {
//        for (int i = 0; i < numParticles; i++) {
//            particles[i].setNoise(Fnoise, Tnoise, Snoise);
//        }
//    }

    public void move() {
        for (int i = 0; i < numParticles; i++) {
            particles[i] = particles[i].move();
        }
    }

    public void calculateProb(){
        meanFitness = 0f;
        for (int i = 0; i < numParticles; i++) {
            particles[i].measurementProb();
            meanFitness += particles[i].probability;
        }
        meanFitness /= numParticles;
    }

    public BPSSList resample() {
        BPSSList[] new_particles = new BPSSList[numParticles];

        float B = 0f;
        BPSSList best = getBestParticle();
        int index = (int) gen.nextFloat() * numParticles;
        for (int i = 0; i < numParticles; i++) {
            B += gen.nextFloat() * 2f * best.probability;
            while (B > particles[index].probability) {
                B -= particles[index].probability;
                index = circle(index + 1, numParticles);
            }
            new_particles[i] = new BPSSList(particles[index]);
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
        BPSSList particle = particles[0];
        for (int i = 0; i < numParticles; i++) {
            if (particles[i].probability > particle.probability) {
                particle = particles[i];
            }
        }
        return particle;
    }

//    public Particle getAverageParticle() {
//        Particle p = new Particle(particles[0].landmarks, particles[0].worldWidth, particles[0].worldHeight);
//        float x = 0, y = 0, orient = 0, prob = 0;
//        for(int i=0;i<numParticles;i++) {
//            x += particles[i].x;
//            y += particles[i].y;
//            orient += particles[i].orientation;
//            prob += particles[i].probability;
//        }
//        x /= numParticles;
//        y /= numParticles;
//        orient /= numParticles;
//        prob /= numParticles;
//        try {
//            p.set(x, y, orient, prob);
//        } catch (Exception ex) {
//            Logger.getLogger(ParticleFilter.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        p.setNoise(particles[0].forwardNoise, particles[0].turnNoise, particles[0].senseNoise);
//
//        return p;
//    }

    @Override
    public String toString() {
        String res = "";
        for (int i = 0; i < numParticles; i++) {
            res += particles[i].toString() + "\n";
        }
        return res;
    }

    public static void main(final String[] args) throws Exception {
        int bpssListSize = 5;
        int populationSize = 100;
        double mutationProbability = 0.3;
        BPFilter bpFilter = new BPFilter(populationSize, mutationProbability, bpssListSize);
        BPFilter.aResourceName = "driving_car.js";
        BPFilter.evolutionResolution = 1;
        BPFilter.fitnessNumOfIterations = 10;

        BPFilter.runBprogram();

        ParticleFilter filter = new ParticleFilter(populationSize);

        BPFilter.programStepCounter = BPFilter.programStepCounter+BPFilter.evolutionResolution;

        for (int i=0; i < BPFilter.bpssList.size()/BPFilter.evolutionResolution-1; i++){
            filter.move();
            filter.calculateProb();
            BPFilter.meanFitness.add(filter.meanFitness);
            BPFilter.programStepCounter = BPFilter.programStepCounter+BPFilter.evolutionResolution;
            BPSSList estimation = filter.resample();
            BPFilter.bpssEstimatedList.add(estimation.getLast());

        }

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
