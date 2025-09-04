package demo_maintenance.Reliability_Model;






import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestReliabilityModel {

        public static void main(String[] args) {

                    // 役龄调整因子a
                    String A = "0.95";
                    // 故障率调整因子b
                    String B = "1.2";

                    String worktime="30;24;30;18;48;18";

                    Double b = Double.valueOf(B);
                    Double a = Double.valueOf(A);

                    double[]workTime= Arrays.stream(worktime.split(";")).mapToDouble(Double::parseDouble).toArray();



                    double[] ReliabilityModelLine = new double[10335];

                    ReliabilityParameters reliabilityParameters = new ReliabilityParameters(0.95,1.5,3.70,1400);


                    for (int i =0;i<10335;i++){

                        if(i==5334){
                            System.out.println("可靠度："+reliabilityParameters.GetReliability(i,i+30));
                            reliabilityParameters.FixAsNew(i);
                            Map<Integer, Integer> maintenances = reliabilityParameters.getMaintenances();

                            System.out.println("第一次维修:");

                        }
                        if (i==6334||i==7334||i==8334){

                            System.out.println("可靠度："+reliabilityParameters.GetReliability(i,i+30));
                            reliabilityParameters.FixAsNew(i);
                            Map<Integer, Integer> maintenances = reliabilityParameters.getMaintenances();

                        }
                        if(i==9334){

                            Map<Integer, Integer> maintenances = reliabilityParameters.getMaintenances();

                        }
//
                        if(i<10335){
                            ReliabilityModelLine[i]=reliabilityParameters.getFailureRate(i);
                        }

                    }



                    List datas = new ArrayList<data>();

                    for(int i=0;i<ReliabilityModelLine.length;i++){
                        data data = new data(i, ReliabilityModelLine[i]);
                        datas.add(data);
                    }
                  //EasyExcel.write("F:\\data\\dataWebull.xlsx",com.Jarvis.MaxPlusMaintenance.demo_maintenance.entity.data.class).withTemplate("F:\\data\\data.xlsx").sheet("sheet1").doWrite(datas);

        }

}
