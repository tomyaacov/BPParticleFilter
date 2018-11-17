package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BPParticleFilter {

    public static void main(String[] args) throws InterruptedException {

        Date date = new Date();
        String strDateFormat = "dd_MM_yy";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        String formattedDate= dateFormat.format(date);
        String folderName = System.getProperty("user.dir") + File.separator + "data_" + formattedDate;
        new File(folderName).mkdir();

        for (int i=0; i<100; i++){
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new File(folderName + File.separator + "id_" + String.valueOf(i) + ".csv"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            StringBuilder builder = new StringBuilder();

            BProgramState bProgramState = new BProgramState();
            // This will load the program file  <Project>/src/main/resources/example1.js
            final SingleResourceBProgram bprog = new SingleResourceBProgram("example1.js"){
                protected void setupProgramScope(Scriptable scope) {
                    putInGlobalScope("bProgramState", bProgramState);// enables getting robots status
                    super.setupProgramScope(scope);
                }
            };
            // bprog.setDaemonMode(true);
            BProgramRunner rnr = new BProgramRunner(bprog);

            // Print program events to the console
            rnr.addListener( new PrintBProgramRunnerListener() );
            rnr.addListener(new ParticleFilterEventListener(bProgramState, builder));

            // go!
            rnr.run();

            pw.write(builder.toString());
            pw.close();
        }
    }
}
