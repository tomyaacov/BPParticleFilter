package il.ac.bgu.cs.bp.samplebpjsproject;


import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;
import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.NativeObject;

import java.text.DecimalFormat;
import java.util.*;

public class DrivingCarVisualization {

    public static List<List<String>> getStatesVisualization(List<BEvent> eventList){
        String START_ROW = String.join("", Collections.nCopies(4, " ")) + "|" + String.join("", Collections.nCopies(4, " "));
        List<List<String>> finalList = new ArrayList<>();
        ArrayList<String> currentState = new ArrayList<>(Collections.nCopies(4, START_ROW));
        for (int i=0; i < eventList.size(); i++){
            if (i > 0){
                currentState = new ArrayList<>(finalList.get(i-1));
            }
            Optional<Object> data = eventList.get(i).getDataField();
            double x = (double) ((NativeObject) data.get()).get("x");
            double y = (double) ((NativeObject) data.get()).get("y");
            String row = getRow(START_ROW, x, y, currentState.get((int)(y-1)));
            currentState.set((int)(y-1), row);
            finalList.add(currentState);
        }
        return finalList;
    }

    public static String getRow(String START_ROW, double x, double y, String currentRow) {
        String arrow;
        int len = START_ROW.length();
        if (y%2==0){
            if (x==-1){
                arrow = "~";
                return START_ROW.substring(0,currentRow.indexOf("\u2190")-1)+arrow+START_ROW.substring(currentRow.indexOf("\u2190"));
            } else{
                arrow = "\u2190";
                return START_ROW.substring(0,len-(int)(x)-1)+arrow+START_ROW.substring(len-(int)(x));
            }
        } else {
            if (x==-1){
                arrow = "~";
                return START_ROW.substring(0,currentRow.indexOf("\u2192")+1)+arrow+START_ROW.substring((currentRow.indexOf("\u2192")+2));
            } else{
                arrow = "\u2192";
                return START_ROW.substring(0,(int)(x))+arrow+START_ROW.substring((int)(x)+1);
            }
        }
    }

    public static String frameStates(List<String> left, List<String> right){
        left = new ArrayList<>(left);
        right = new ArrayList<>(right);
        left.add(StringUtils.center("CAR",9));
        right.add(StringUtils.center("CAR",9));
        left.add(String.join("", Collections.nCopies(9, "-")));
        right.add(String.join("", Collections.nCopies(9, "-")));
        left.add(StringUtils.center("REAL",9));
        right.add(StringUtils.center("ESTIMATED",9));
        left.add(0, String.join("", Collections.nCopies(9, "-")));
        right.add(0, String.join("", Collections.nCopies(9, "-")));
        List<String> finalList = new ArrayList<>();
        for (int i=0; i < left.size(); i++){
            if (i==0 || i==left.size()-2){
                finalList.add("+"+left.get(i)+"+ +"+right.get(i)+"+");
            } else {
                if (i==left.size()-1){
                    finalList.add(" "+left.get(i)+"   "+right.get(i)+" ");
                } else {
                    finalList.add("|"+left.get(i)+"| |"+right.get(i)+"|");
                }
            }
        }
        return String.join("\n", finalList);
    }

    public static void printDemoRun(List<List<String>> states, int evolutionResolution){
        Random random = new Random();
        int i = evolutionResolution-1;
        while (i < states.size()){
            if (random.nextDouble() < 0.1){
                double rand = random.nextDouble();
                if (0 <= rand && rand < 0.25){
                    System.out.println(frameStatesAndData(frameStates(states.get(i), states.get(i-1)), i, 1-(0.25*(random.nextDouble())), false));
                }
                if (0.25 <= rand && rand < 0.5){
                    System.out.println(frameStatesAndData(frameStates(states.get(i), states.get(i+1)), i, 1-(0.25*(random.nextDouble())), false));
                }
                if (0.5 <= rand && rand < 0.75){
                    System.out.println(frameStatesAndData(frameStates(states.get(i), states.get(i-2)), i, 1-(0.35*(random.nextDouble())), false));
                }
                if (0.75 <= rand && rand <= 1){
                    System.out.println(frameStatesAndData(frameStates(states.get(i), states.get(i+2)), i, 1-(0.35*(random.nextDouble())), false));
                }
            } else {
                System.out.println(frameStatesAndData(frameStates(states.get(i), states.get(i)), i, 1-(0.15*(random.nextDouble())), true));;
            }
            i += evolutionResolution;
        }
    }

    public static String frameStatesAndData(String frame, int generation, double score, boolean match){
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_RED_BACKGROUND = "\u001B[41m";
        final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
        frame += "\n" + StringUtils.center("Generation: "+generation,23) + "\n";
        DecimalFormat df = new DecimalFormat("###.###");
        frame += StringUtils.center("Score: "+df.format(score),23) + "\n";
        if(match){
            frame += ANSI_GREEN_BACKGROUND + StringUtils.center("MATCH",23) + ANSI_RESET + "\n";
        } else {
            frame += ANSI_RED_BACKGROUND + StringUtils.center("NO MATCH",23) + ANSI_RESET + "\n";
        }
        return frame;
    }

    public static void main(final String[] args) throws InterruptedException {
        String aResourceName = "driving_car_toy.js";
        SimpleEventSelectionStrategyFilter ess = new SimpleEventSelectionStrategyFilter(new SimpleEventSelectionStrategy());
        BProgram externalBProgram = new ResourceBProgram(aResourceName, ess);
        BProgramRunner bProgramRunner = new BProgramRunner(externalBProgram);
        ParticleFilterEventListener particleFilterEventListener = new ParticleFilterEventListener();
        bProgramRunner.addListener(new PrintBProgramRunnerListener());
        bProgramRunner.run();

    }
}
