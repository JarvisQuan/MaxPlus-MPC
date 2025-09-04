package demo_maintenance.Util;


import org.ujmp.core.Matrix;

import java.util.ArrayList;
import java.util.List;

public class CloneUtil {



    public static List<Machine> cloneMachine(List<Machine> machines){

        List<Machine> machinesClone = new ArrayList<Machine>();
        for(Machine machine : machines){
            Machine newmachine = new Machine();
            newmachine.setId(machine.getId());
            newmachine.setStop(false);
            newmachine.setDown(false);
            newmachine.setD_continuousTime(0.0);
            newmachine.setD_time(0.0);
            newmachine.setRecover_num(0);
            newmachine.reliability=machine.CloneReliabilityParameters();
            machinesClone.add(newmachine);
        }
        return machinesClone;
    }

    public static Matrix[]cloneBeginxkMatixs(Matrix[] xkMatrix){
        Matrix[] xKMatrixClone = new Matrix[xkMatrix.length];
        for (int i = 0; i < xkMatrix.length; i++) {
            xKMatrixClone[i] = xkMatrix[i].clone();
        }
        return xKMatrixClone;
    }

    public static int[]cloneLoss(int[] loss){
        int[] lossClone = new int[loss.length];
        for (int i = 0; i < loss.length; i++) {
            lossClone[i] = loss[i];
        }
     return lossClone;
    }

}
