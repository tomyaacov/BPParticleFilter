package il.ac.bgu.cs.bp.samplebpjsproject.old;

import il.ac.bgu.cs.bp.samplebpjsproject.BPFilter;
import io.jenetics.Optimize;
import io.jenetics.Phenotype;
import io.jenetics.RouletteWheelSelector;
import io.jenetics.util.ISeq;
import io.jenetics.util.Seq;

public class RouletteWheelSelectorDecorator extends RouletteWheelSelector {

    @Override
    public ISeq<Phenotype> select(Seq population, int count, Optimize opt) {
        //System.out.println("Selector");
        BPFilter.programStepCounter = BPFilter.programStepCounter+BPFilter.evolutionResolution; //TODO: temp!!!
        return super.select(population, count, opt);
    }
}
