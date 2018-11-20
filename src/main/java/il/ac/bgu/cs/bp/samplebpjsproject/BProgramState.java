package il.ac.bgu.cs.bp.samplebpjsproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BProgramState {

    Map<String, String> currentState;
    List<String> bThreadsOrder;

    public BProgramState() {
        currentState = new HashMap<String, String>();
        bThreadsOrder = new ArrayList<String>();
    }

    public void updateState(String bThread, String state){
        currentState.put(bThread, state);
    }

    public void addBThread(String name){
        bThreadsOrder.add(name);
    }

    public String getStatesString(){
        String states = "";
        for(String name : bThreadsOrder){
            states = states + currentState.get(name);
        }
        return "," + states + "\n";
    }

}
