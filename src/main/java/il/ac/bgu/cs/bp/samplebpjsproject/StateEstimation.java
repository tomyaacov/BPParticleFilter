package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import io.jenetics.AnyGene;
import io.jenetics.engine.EvolutionResult;

public class StateEstimation {

    public static BProgramSyncSnapshot fittestIndividual(EvolutionResult<AnyGene<BProgramSyncSnapshot>, Double> evolutionResult){
        return evolutionResult.getBestPhenotype().getGenotype().getGene().getAllele();
    }
}
