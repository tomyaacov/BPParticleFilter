package il.ac.bgu.cs.bp.samplebpjsproject;

import io.jenetics.Optimize;
import io.jenetics.Phenotype;
import io.jenetics.RouletteWheelSelector;
import io.jenetics.util.ISeq;
import io.jenetics.util.Seq;

public class RouletteWheelSelectorDecorator extends RouletteWheelSelector {

    @Override
    public ISeq<Phenotype> select(Seq population, int count, Optimize opt) {
        //System.out.println("Selector");
        BPFilter.setProgramStepCounter(BPFilter.getProgramStepCounter()+BPFilter.getEvolutionResolution()); //TODO: temp!!!
        return super.select(population, count, opt);
    }
}
