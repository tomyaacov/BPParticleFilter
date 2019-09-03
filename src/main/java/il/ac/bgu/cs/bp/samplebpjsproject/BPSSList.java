package il.ac.bgu.cs.bp.samplebpjsproject;

import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class BPSSList {

    @Getter @Setter
    private ArrayList<BProgramSyncSnapshot> bProgramSyncSnapshots;

    public BPSSList(int size) {
        this.bProgramSyncSnapshots = new ArrayList<>(size);
        for (int i = 0; i < size; i++){
            this.bProgramSyncSnapshots.add(null);
        }
    }

    public BProgramSyncSnapshot getLast(){
        return this.bProgramSyncSnapshots.get(this.bProgramSyncSnapshots.size()-1);
    }

    public BPSSList cloneFrom(double r){
        double a = 0.0;
        int n = bProgramSyncSnapshots.size()-1;
        while (a < r && n > 0){
            n--;
            a = a + (1-a)/2;
        }
        BPSSList newCopy = new BPSSList(bProgramSyncSnapshots.size());
        for (int i = 0; i < bProgramSyncSnapshots.size(); i++){
            if (i < n){
                if (bProgramSyncSnapshots.get(i) == null){
                    continue;
                }
                newCopy.getBProgramSyncSnapshots().set(i, BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshots.get(i)));
            }
        }
        return newCopy;
    }

    public BPSSList cloneTransition(BProgramSyncSnapshot newbpss){
        BPSSList newCopy = new BPSSList(bProgramSyncSnapshots.size());
        for (int i = 1; i < bProgramSyncSnapshots.size(); i++){
            if (bProgramSyncSnapshots.get(i) == null){
                continue;
            }
            newCopy.getBProgramSyncSnapshots().set(i-1, BProgramSyncSnapshotCloner.clone(bProgramSyncSnapshots.get(i)));
        }
        newCopy.getBProgramSyncSnapshots().set(bProgramSyncSnapshots.size()-1, BProgramSyncSnapshotCloner.clone(newbpss));
        return newCopy;
    }
}
