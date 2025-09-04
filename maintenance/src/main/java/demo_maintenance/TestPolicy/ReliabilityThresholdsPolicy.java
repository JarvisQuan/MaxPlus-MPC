package demo_maintenance.TestPolicy;


import com.alibaba.excel.EasyExcel;
import org.ujmp.core.Matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ReliabilityThresholdsPolicy {
    public static int stationsNum;// 工作站数量
    public static int inputsNum;// 输入口数量
    public static int[] buffers;// 缓冲区容量
    public static double[] work_times;// 工作站加工时间
    public static Matrix[] xkMatrixs; // 开始加工时间
    public static Base_RandomMatrixs maxPluStateMatrix;
    public static int demand = 1000; // 加工数量
    public static double[]A;
    public static double[]B;
    public static double[]ShapeParameters;
    public static double[]CharacteristicLife;

    public static void main(String[] args) {

        // 设备数量
        String stationsNumString ="6";
        // 输入口数量
        String inputNumString ="1";
        // 缓冲区
        String bufferString ="1;2;2;2;1;0";
        // 加工时间
        String workTimeString ="30.0;24.0;30.0;18.0;48.0;18.0";
        // 并行机（行：设备序号，列：并行机数量）
        String parallelStationString ="1,1;2,1;3,1;4,1;5,1;6,1";
        // 入口连接（行列序号）
        String inputStructString ="1,1";
        // 工作站连接顺序(拓扑矩阵行列序号)
        String structureString ="2,1;3,2;4,3;5,4;6,5";

        //每个机器的役龄调整因子a
        String a="0.95;0.95;0.95;0.95;0.95;0.95";

        //每个机器的故障率调整因子b
        String b="1.5;1.2;1.3;1.2;1.6;1.2";


        //每个机器的初始失效率函数参数
        String M ="3.70;3.40;4.00;2.60;4.00;2.60";

        String N="1400.0;1500.0;1655.0;1600.0;1200.0;1600.0";


        stationsNum = Integer.parseInt(stationsNumString);

        inputsNum = Integer.parseInt(inputNumString);
        // 缓冲区
        String[] bufferStrings = bufferString.split(";");
        buffers = Arrays.stream(bufferStrings).mapToInt(Integer::parseInt).toArray();
        // 工作站加工时间
        String[] workTimeStrings = workTimeString.split(";");
        work_times = Arrays.stream(workTimeStrings).mapToDouble(Double::parseDouble).toArray();
        // 随机加工时间数二维数组
        double[][] randoms = new double[stationsNum][demand];
        //故障率调整因子b
        String[]bStrings=b.split(";");
        B = Arrays.stream(bStrings).mapToDouble(Double::parseDouble).toArray();
        //役龄调整因子a
        String[]aStrings=a.split(";");
        A = Arrays.stream(aStrings).mapToDouble(Double::parseDouble).toArray();
        //初始失效率函数的参数
        String[]MStrings=M.split(";");
        ShapeParameters= Arrays.stream(MStrings).mapToDouble(Double::parseDouble).toArray();

        String[]NStrings=N.split(";");
        CharacteristicLife= Arrays.stream(NStrings).mapToDouble(Double::parseDouble).toArray();


        maxPluStateMatrix = new Base_RandomMatrixs(stationsNum, inputsNum, buffers, work_times,
                parallelStationString, inputStructString, structureString, demand, randoms);

        xkMatrixs = maxPluStateMatrix.mainMatrix();
        Matrix[]BasexMatrix = CloneUtil.cloneBeginxkMatixs(xkMatrixs);

        Matrix Y=xkMatrixs[xkMatrixs.length - 1];
        double[] basethroughput = computeThroughput(Y);
        ArrayList<data> data = new ArrayList<>();
        for (int i = 0; i < basethroughput.length; i++) {
            com.Jarvis.MaxPlusMaintenance.demo_maintenance.entity.data data1 = new data(Y.getAsDouble(0,i), basethroughput[i]);
            data.add(data1);
        }
        //EasyExcel.write("F:\\data\\BaseThroughput.xlsx",com.Jarvis.MaxPlusMaintenance.demo_maintenance.entity.data.class).withTemplate("F:\\data\\data.xlsx").sheet("sheet1").doWrite(data);


        List<Machine> machines = new ArrayList<>();
        for (int i = 0; i < stationsNum; i++) {
            Machine machine = new Machine(i + 1, false, false, A[i], B[i], ShapeParameters[i],CharacteristicLife[i]);
            machines.add(machine);
        }

        Matrix[] updateMatrix;
        int[]Loss=new int[stationsNum];


        List[]list=new ArrayList[stationsNum];

        for (int x = 0; x < stationsNum; x++) {
            list[x] = new ArrayList<FailureRate>();
        }

        double beginTime = 5334;

        for (int j = 0; j< stationsNum; j++) {
            //失效率数据集合
            ReliabilityParameters Machinesreliability = machines.get(j).getReliability();
            double Reliability = Machinesreliability.GetReliability(beginTime, beginTime+work_times[j]);
            for (double i = 0; i <beginTime; i++) {
                FailureRate failureRate = new FailureRate(i, Machinesreliability.getFailureRate(i));
                list[j].add(failureRate);
            }

            if (0<=Reliability&&Reliability<=0.90) {
                machines.get(j).setDown(true);
                machines.get(j).setD_time(beginTime);
                machines.get(j).setD_continuousTime(400.0);
                //根据维修策略更新可靠性模型
                machines.get(j).setReliabilityFixAsNew(beginTime);
            }

        }

        Compute_TTD_VTW_change computeTtdVtw = new Compute_TTD_VTW_change(xkMatrixs, work_times, buffers, maxPluStateMatrix, beginTime, machines);
        double[] VTW = computeTtdVtw.VTW_TTD();
        List<Machine> machinesWithRecoverNum = computeTtdVtw.getMachinesWithRecoverNum();
        Loss_JFC computeloss = new Loss_JFC(xkMatrixs, work_times, buffers, maxPluStateMatrix, machines);
        int[] loss = computeloss.loss_Jfc();
        for (int i=0;i< loss.length;i++){
            Loss[i]+=loss[i];
        }


        updateMatrix=maxPluStateMatrix.updateMatrix1(xkMatrixs,machinesWithRecoverNum);


        for (Machine machine : machinesWithRecoverNum) {
            machine.setDown(false);
            machine.setStop(false);
            machine.setD_time(0.0);
            machine.setRecover_num(0);
            machine.setD_continuousTime(0);
        }

        for (int i = 5334; i < 30000; i=i+1000) {
            beginTime =i;


            if( 5334<i&&i <= 6334){
                for (int j = 0; j < stationsNum; j++) {

                    ReliabilityParameters machinereliability = machinesWithRecoverNum.get(j).getReliability();
                    double v = machinereliability.GetReliability(beginTime, beginTime +work_times[j]);

                    if (0 <= v && v <= 0.90) {
                        machinesWithRecoverNum.get(j).setDown(true);
                        machinesWithRecoverNum.get(j).setD_time(beginTime);
                        machinesWithRecoverNum.get(j).setD_continuousTime(400.0);

                        machinesWithRecoverNum.get(j).setReliabilityFixAsNew(beginTime);
                    }
                    list[j].add(new FailureRate(beginTime,machinereliability.getFailureRate(beginTime)));
                }
                computeTtdVtw = new Compute_TTD_VTW_change(updateMatrix, work_times, buffers, maxPluStateMatrix, beginTime, machinesWithRecoverNum);
                VTW = computeTtdVtw.VTW_TTD();
                machinesWithRecoverNum = computeTtdVtw.getMachinesWithRecoverNum();

                 computeloss = new Loss_JFC(xkMatrixs, work_times, buffers, maxPluStateMatrix, machinesWithRecoverNum);
                 loss = computeloss.loss_Jfc();

                for (int k=0;i< loss.length;k++){
                    Loss[k]+=loss[k];
                }

                updateMatrix = maxPluStateMatrix.updateMatrix1(updateMatrix, machinesWithRecoverNum);


                for (Machine machine : machinesWithRecoverNum) {
                    machine.setDown(false);
                    machine.setStop(false);
                    machine.setD_time(0.0);
                    machine.setRecover_num(0);
                    machine.setD_continuousTime(0);
                }

            }


            if (i>6334){
                for (int j = 0; j < stationsNum; j++) {
                    ReliabilityParameters machinereliability = machinesWithRecoverNum.get(j).getReliability();
                    list[j].add(new FailureRate(beginTime,machinereliability.getFailureRate(beginTime)));
                }
            }

        }

        MPCcompute mpCcompute = new MPCcompute();
        MPCComputeResult mpcComputeResult = new MPCComputeResult(stationsNum);
        mpcComputeResult.resultxKMatrixs.add(updateMatrix);
        mpcComputeResult.resultLoss.add(Loss);
        mpcComputeResult.resultMachines.add(machinesWithRecoverNum);
        mpCcompute.selectBest(BasexMatrix, mpcComputeResult, 0.0001, 100.0);

        for (int z = 0; z < stationsNum; z++) {
            // EasyExcel.write("F:\\data\\"+"设备"+(z+1)+"FailureRates.xlsx",com.Jarvis.MaxPlusMaintenance.demo_maintenance.entity.FailureRate.class).withTemplate("F:\\data\\data.xlsx").sheet("sheet1").doWrite(list[z]);
        }

        Matrix FinalY=updateMatrix[updateMatrix.length - 1];
        double[] Finalthroughput = computeThroughput(Y);
        ArrayList<data> Finaldata = new ArrayList<>();
        for (int i = 0; i < basethroughput.length; i++) {
            com.Jarvis.MaxPlusMaintenance.demo_maintenance.entity.data data1 = new data(FinalY.getAsDouble(0,i), Finalthroughput[i]);
            Finaldata.add(data1);
        }

        EasyExcel.write("E:\\论文草稿\\MPC\\RP0.90.xlsx",com.Jarvis.MaxPlusMaintenance.demo_maintenance.entity.data.class).withTemplate("E:\\论文草稿\\MPC\\data.xlsx").sheet("sheet1").doWrite(Finaldata);

    }

    public static double[]computeThroughput(Matrix matrix){
        double[] thoughput = new double[(int) matrix.getColumnCount()];
        double temp=0;
        for (int i = 0; i < matrix.getColumnCount(); i++) {

            temp=(i+1)/matrix.getAsDouble(0,i);
            thoughput[i]=temp;
        }
        return thoughput;
    }
}
