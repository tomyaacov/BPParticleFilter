package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import org.mozilla.javascript.NativeObject;

public class LocalizationState {

    public Double x;

    public Double y;

    public LocalizationState(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public LocalizationState(BEvent event) {
        Object state_y = ((NativeObject)event.getData()).get("y");
        if(state_y instanceof Integer){
            state_y = ((Integer)state_y).doubleValue();
        }
        this.y = (Double)state_y;
        Object state_x = ((NativeObject)event.getData()).get("x");
        if(state_x instanceof Integer){
            state_x = ((Integer)state_x).doubleValue();
        }
        this.x = (Double)state_x;
    }

    public Double distanceTo(LocalizationState state){
        return Math.sqrt(Math.pow(this.x-state.x,2)+ Math.pow(this.y-state.y,2));
    }

    public boolean equals(LocalizationState state){
        return this.distanceTo(state) == 0;
    }

    @Override
    public String toString() {
        return "LocalizationState{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
