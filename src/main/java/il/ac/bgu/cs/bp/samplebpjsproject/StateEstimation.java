package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import io.jenetics.AnyGene;
import io.jenetics.engine.EvolutionResult;

import java.util.List;

public class StateEstimation {

    public static BProgramSyncSnapshot fittestIndividual(EvolutionResult<AnyGene<BProgramSyncSnapshot>, Double> evolutionResult){
        //System.out.println("End of generation - "+evolutionResult.getGeneration());
        //System.out.println(evolutionResult.getBestFitness());
        return evolutionResult.getBestPhenotype().getGenotype().getGene().getAllele();
    }

    public static void fittestIndividualVisualization(EvolutionResult<AnyGene<BProgramSyncSnapshot>, Double> evolutionResult){
        return;
    }

}
