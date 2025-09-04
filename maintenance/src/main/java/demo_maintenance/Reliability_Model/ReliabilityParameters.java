package demo_maintenance.Reliability_Model;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.RombergIntegrator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/*
* 单个设备的可靠性模型
* */

public class ReliabilityParameters {

    //役龄调整因子a
    private double a;

    //故障率调整因子b
    private double b;

    private double beginFailureRate;

    private double shapeParameters;

    private double characteristicLife;


    private double TaskCycleTime=0.0;

    private double LastTaskCycleTime=0.0;


    private double BeginPoint=0.0;

   private double EndPoint;


    private int k;


    private  double FIXASOLDTIME=200.0;
    //(非预防性维修)修复如新需要的维修时间
    private  double FIXASNEWTIME=400.0;



    private double WEIGHTFIXASOLDTIME=100;

    private  double WEIGHTFIXASNEWTIME1=200.0;

    private double WEIGHTFIXASNEWTIME2=150;





    public Map<Integer,Integer>Maintenances;


    public ReliabilityParameters() {
    }

    public ReliabilityParameters(double a, double b, double shapeParameters ,double characteristicLife) {
        this.k=0;
        this.a = a;
        this.b = b;
        //this.beginFailureRate = beginFailureRate;
        this.Maintenances = new HashMap<>();
        this.shapeParameters = shapeParameters;
        this.characteristicLife = characteristicLife;

    }


    public int getK(){
        return k;
    }
    public void setK(int k){
        this.k = k;
    }
    public double getA() {
        return this.a;
    }
    public void setA(double a) {
        this.a = a;
    }
    public double getB() {
        return this.b;
    }
    public void setB(double b) {
        this.b = b;
    }
    public void setShapeParameters(double shapeParameters) {
        this.shapeParameters = shapeParameters;
    }
    public double getShapeParameters() {
        return this.shapeParameters;
    }
    public void setCharacteristicLife(double characteristicLife) {
        this.characteristicLife = characteristicLife;
    }
    public double getCharacteristicLife() {
        return this.characteristicLife;
    }

    public double getBeginFailureRate() {
        return this.beginFailureRate;
    }

    public void setLastTaskCycleTime(double lastTaskCycleTime) {
        this.LastTaskCycleTime=lastTaskCycleTime;
    }
    public double getLastTaskCycleTime() {
        return this.LastTaskCycleTime;
    }
    public void setTaskCycleTime(double taskCycleTime) {
        this.TaskCycleTime = taskCycleTime;
    }
    public double getTaskCycleTime() {
        return this.TaskCycleTime;
    }

    public double getBeginPoint() {
        return BeginPoint;
    }
    public void setBeginPoint(double beginPoint) {
        this.BeginPoint = beginPoint;
    }
    public void setEndPoint(double endPoint) {
        this.EndPoint = endPoint;
    }
    public double getEndPoint() {
        return EndPoint;
    }

    public void setWEIGHTFIXASOLDTIME(double weightfixasoldtime) {this.WEIGHTFIXASOLDTIME = weightfixasoldtime;}
    public double getWEIGHTFIXASOLDTIME() {return this.WEIGHTFIXASOLDTIME;}

    public void setWEIGHTFIXASNEWTIME1(double weightfixasnewtime){this.WEIGHTFIXASNEWTIME1 = weightfixasnewtime;}
    public double getWEIGHTFIXASNEWTIME1() {return this.WEIGHTFIXASNEWTIME1;}

    public void setWEIGHTFIXASNEWTIME2(double weightfixasnewtime){this.WEIGHTFIXASNEWTIME2 = weightfixasnewtime;}
    public double getWEIGHTFIXASNEWTIME2() {return this.WEIGHTFIXASNEWTIME2;}



    public Map<Integer,Integer> getMaintenances(){
        return this.Maintenances;
    }


    public void setBeginFailureRate(double beginFailureRate) {
        this.beginFailureRate = beginFailureRate;
    }




    public double getFailureRate(double time) {
        if (k==0){
            return   b*(shapeParameters/characteristicLife)*Math.pow((time/characteristicLife),shapeParameters-1);
        }
        return Maintenances.get(k)==0?b*(shapeParameters/characteristicLife)*Math.pow((time - a *  LastTaskCycleTime)/characteristicLife,shapeParameters-1):b*(shapeParameters/characteristicLife)*Math.pow((time - a *  TaskCycleTime)/characteristicLife,shapeParameters-1);
    }




    public void FixAsOld(double pointTime) {
        this.EndPoint=pointTime;
        this.TaskCycleTime+=(this.EndPoint-this.BeginPoint);
        //this.BeginPoint=pointTime+FIXASOLDTIME;
        this.BeginPoint=pointTime;
        this.k++;
        this.Maintenances.put(k,0);
    }


    public void FixAsNew(double pointTime) {
        this.k++;
        this.TaskCycleTime +=(pointTime-this.BeginPoint);
        this.LastTaskCycleTime=this.TaskCycleTime;
        //this.BeginPoint=pointTime+FIXASNEWTIME;
        this.BeginPoint=pointTime;
        this.Maintenances.put(k,1);
    }

    public void WeightFixAsOld1(double pointTime) {
        this.EndPoint=pointTime;
        this.TaskCycleTime+=(this.EndPoint-this.BeginPoint);
        //this.BeginPoint=pointTime+FIXASOLDTIME;
        this.BeginPoint=pointTime;
        this.k++;
        this.Maintenances.put(k,2);
    }


    public void WeightFixAsNew1(double pointTime) {
        this.EndPoint=pointTime;
        this.TaskCycleTime+=(this.EndPoint-this.BeginPoint);
        //this.BeginPoint=pointTime+FIXASOLDTIME;
        this.BeginPoint=pointTime;
        this.k++;
        this.Maintenances.put(k,3);
    }

    public void WeightFixAsNew2(double pointTime) {
        this.EndPoint=pointTime;
        this.TaskCycleTime+=(this.EndPoint-this.BeginPoint);
        //this.BeginPoint=pointTime+FIXASOLDTIME;
        this.BeginPoint=pointTime;
        this.k++;
        this.Maintenances.put(k,4);
    }







    public double GetReliability(double Minimum_Point_cap, double Maximum_Point_cap) {

        int FixAsOld=0;
        int FixAsNew=0;
        for (Integer k:Maintenances.keySet()){

            Integer i = Maintenances.get(k);
            if (i==0){
                FixAsOld++;
            }if (i==1){
                FixAsNew++;
            }
        }




        Minimum_Point_cap-=(FIXASOLDTIME*FixAsOld+FIXASNEWTIME*FixAsNew);


        Maximum_Point_cap-=(FIXASOLDTIME*FixAsOld+FIXASNEWTIME*FixAsNew);

        Set<Integer> integers = Maintenances.keySet();
       for (Integer i:integers){
           System.out.print("第"+i+"次维修："+Maintenances.get(i));
       }



        RombergIntegrator integrator = new RombergIntegrator();

        try {
            double Reliability;

            if (k == 0) {
                Reliability = integrator.integrate(10000, new UnivariateFunction() {
                    @Override
                    public double value(double v) {

                        if (v <= 0) return 0;
                        return b * (shapeParameters / characteristicLife) * Math.pow(v / characteristicLife, shapeParameters - 1);
                    }
                }, Minimum_Point_cap, Maximum_Point_cap);

            } else if (Maintenances.get(k) == 0) {
                Reliability = integrator.integrate(10000, new UnivariateFunction() {
                    @Override
                    public double value(double v) {
                        double tAdj = v - a * LastTaskCycleTime;

                        if (tAdj <= 0) return 0;
                        return b * (shapeParameters / characteristicLife) * Math.pow(tAdj / characteristicLife, shapeParameters - 1);
                    }
                }, Minimum_Point_cap, Maximum_Point_cap);

            } else {
                Reliability = integrator.integrate(10000, new UnivariateFunction() {
                    @Override
                    public double value(double v) {
                        double tAdj = v - a * TaskCycleTime;

                        if (tAdj <= 0) return 0;
                        return b * (shapeParameters / characteristicLife) * Math.pow(tAdj / characteristicLife, shapeParameters - 1);
                    }
                }, Minimum_Point_cap, Maximum_Point_cap);
            }

            return Math.exp(-Reliability);

        } catch (Exception e) {
            System.err.println("积分失败: " + e.getMessage());
            System.err.println("使用保底值 exp(-0.001 * Δt)");

            return Math.exp(-0.001 * (Minimum_Point_cap - Maximum_Point_cap));
        }

    }

}
