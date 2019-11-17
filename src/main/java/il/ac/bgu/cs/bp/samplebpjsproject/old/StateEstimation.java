package il.ac.bgu.cs.bp.samplebpjsproject.old;

import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.samplebpjsproject.BPSSList;
import io.jenetics.AnyGene;
import io.jenetics.engine.EvolutionResult;

import java.util.List;

public class StateEstimation {

    public static BProgramSyncSnapshot fittestIndividual(EvolutionResult<AnyGene<BPSSList>, Double> evolutionResult){
        //System.out.println("End of generation - "+evolutionResult.getGeneration());
        //System.out.println(evolutionResult.getBestFitness());
        return evolutionResult.getBestPhenotype().getGenotype().getGene().getAllele().getLast();
    }

    public static void fittestIndividualVisualization(EvolutionResult<AnyGene<BPSSList>, Double> evolutionResult){
        return;
    }

}
