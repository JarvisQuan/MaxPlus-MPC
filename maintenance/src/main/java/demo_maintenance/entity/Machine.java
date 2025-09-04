package demo_maintenance.entity;


import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
//机器实体类
public class Machine {
    //机器序号
    private int id;
    //每个机器的可靠性模型
    public ReliabilityParameters reliability;
    //在发生故障后计算VTW后产线是否停机
    private boolean isStop;
    //机器是否故障
    private boolean isDown;
    //故障开始时间
    private double d_time;
    //如果故障时维修策略对应的维修时间
    private double d_continuousTime;
    //维修完成后第一个加工的工件序号
    private int recover_num;

    public Machine(){}

    public Machine(int id, boolean isStop, boolean isDown, double a, double b, double shapeParameters, double characteristicLife) {
        this.reliability=new ReliabilityParameters(a,b,shapeParameters,characteristicLife);
        this.isStop = isStop;
        this.isDown = isDown;
        this.id = id;
    }
    public void setReliabilityFixAsOld(double d) {
        this.reliability.FixAsOld(d);
    }
    public void setReliabilityFixAsNew(double d) {
        this.reliability.FixAsNew(d);
    }


    public void WeightFixAsOld1(double d) {
        this.reliability.WeightFixAsOld1(d);
    }
    public void WeightFixAsNew1(double d) {
        this.reliability.WeightFixAsNew1(d);
    }
    public void WeightFixAsNew2(double d) {
        this.reliability.WeightFixAsNew2(d);
    }



    public ReliabilityParameters CloneReliabilityParameters(){
        ReliabilityParameters newreliabilityParameters = new ReliabilityParameters();
        newreliabilityParameters.setA(this.reliability.getA());
        newreliabilityParameters.setB(this.reliability.getB());
        newreliabilityParameters.setTaskCycleTime(this.reliability.getTaskCycleTime());
        newreliabilityParameters.setShapeParameters(this.reliability.getShapeParameters());
        newreliabilityParameters.setCharacteristicLife(this.reliability.getCharacteristicLife());
        newreliabilityParameters.setLastTaskCycleTime(this.reliability.getLastTaskCycleTime());
        newreliabilityParameters.setK(this.reliability.getK());
        newreliabilityParameters.setBeginPoint(this.reliability.getBeginPoint());
        newreliabilityParameters.setEndPoint(this.reliability.getEndPoint());
        HashMap<Integer, Integer> newMaintenances = new HashMap<>();
        Map<Integer, Integer> maintenances = this.reliability.getMaintenances();
        Set<Integer> keySet = maintenances.keySet();
        for (Integer key : keySet) {
            newMaintenances.put(key, maintenances.get(key));
        }
        newreliabilityParameters.Maintenances=newMaintenances;
        return newreliabilityParameters;
    }
}
