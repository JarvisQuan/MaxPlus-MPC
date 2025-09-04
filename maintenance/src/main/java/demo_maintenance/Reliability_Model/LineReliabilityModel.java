package demo_maintenance.Reliability_Model;




import java.util.ArrayList;
import java.util.List;
import java.util.Map;


//产线的可靠性模型集合
public class LineReliabilityModel {

    private List<ReliabilityParameters>stationReliabilityModel;

    public LineReliabilityModel(List<Machine> machines) {
        List<ReliabilityParameters> reliabilityParameters = new ArrayList<>();
        for (Machine machine : machines) {
            reliabilityParameters.add(machine.getReliability());
        }
      this.stationReliabilityModel=reliabilityParameters;
    }

    public void FixAsOld(int i,double pointTime){
        this.stationReliabilityModel.get(i-1).FixAsOld(pointTime);
    }


    public void FixAsNew(int i,double pointTime){
        this.stationReliabilityModel.get(i-1).FixAsNew(pointTime);
    }

    public double getReliability(int i,double beginTime,double endTime){
       return this.stationReliabilityModel.get(i-1).GetReliability(beginTime,endTime);
    }


    public Map<Integer,Integer> getMaintenanceTimes(int i ){
        return this.stationReliabilityModel.get(i-1).getMaintenances();
    }



}
