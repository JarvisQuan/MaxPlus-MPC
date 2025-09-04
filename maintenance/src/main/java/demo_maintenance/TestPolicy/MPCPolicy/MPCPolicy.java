package demo_maintenance.TestPolicy.MPCPolicy;


import org.ujmp.core.Matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MPCPolicy {
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


    public static void main(String[] args){


        MPCCompute();
        long endstamp = System.currentTimeMillis();


    }

    public static void MPCCompute() {
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
        Matrix[] beginxkMatixs = CloneUtil.cloneBeginxkMatixs(xkMatrixs);
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

        Matrix[] updateMatrix=null;
        List<Machine> machinesWithRecoverNum=null;
        Compute_TTD_VTW_change computeTtdVtw=null;

        double[] VTW;

        List[]list=new ArrayList[stationsNum];


        for (int x = 0; x < stationsNum; x++) {
            list[x] = new ArrayList<FailureRate>();
        }

        double beginTime = xkMatrixs[0].getAsDouble(0, 120);

        for (int j = 0; j< stationsNum; j++) {

            ReliabilityParameters Machinesreliability = machines.get(j).getReliability();
            double Reliability = Machinesreliability.GetReliability(beginTime, beginTime + work_times[j]);
            for (double i = 0; i < beginTime; i++) {
                FailureRate failureRate = new FailureRate(i, Machinesreliability.getFailureRate(i));
                list[j].add(failureRate);
            }


            if (0 <= Reliability && Reliability <= 0.75) {
                machines.get(j).setDown(true);
            }
        }

            boolean[] getpolicy = new boolean[stationsNum];
            for (int x = 0; x < stationsNum; x++) {
                getpolicy[x]=machines.get(x).isDown();
            }
        System.out.println(Arrays.toString(getpolicy));
            List<double[]> allPossibilities = new ArrayList<>();
            MPCPolicyGenerate.dfs(0,getpolicy,allPossibilities,new double[getpolicy.length]);




        MPCcompute mpc = new MPCcompute();

        int[] loss = new int[stationsNum];
        MPCComputeResult mpcComputeResult = new MPCComputeResult(stationsNum);


        mpc.Compute(5334, allPossibilities, beginxkMatixs,xkMatrixs, buffers, maxPluStateMatrix, machines, stationsNum, work_times, loss, mpcComputeResult);


        MPCComputeResult selectResult = mpc.selectBest(beginxkMatixs, mpcComputeResult, 0.0001, 100.0);








        for (int z = 0; z < stationsNum; z++) {
            //EasyExcel.write("F:\\data\\policydata\\MPC\\"+"设备"+(z+1)+"FailureRates.xlsx",com.Jarvis.MaxPlusMaintenance.demo_maintenance.entity.FailureRate.class).withTemplate("F:\\data\\data.xlsx").sheet("sheet1").doWrite(list[z]);
        }


        Matrix FinalY=selectResult.resultxKMatrixs.get(0)[selectResult.resultxKMatrixs.get(0).length-1];
        double[] Finalthroughput = computeThroughput(Y);
        ArrayList<data> Finaldata = new ArrayList<>();
        for (int i = 0; i < basethroughput.length; i++) {
            com.Jarvis.MaxPlusMaintenance.demo_maintenance.entity.data data1 = new data(FinalY.getAsDouble(0,i), Finalthroughput[i]);
            Finaldata.add(data1);
        }


//        EasyExcel.write("E:\\论文草稿\\MPC\\NC2.xlsx",com.Jarvis.MaxPlusMaintenance.demo_maintenance.entity.data.class).withTemplate("E:\\论文草稿\\MPC\\data.xlsx").sheet("sheet1").doWrite(Finaldata);

    }

    public static double[]computeThroughput(Matrix matrix){
        double[] thoughput = new double[(int) matrix.getColumnCount()];
        double temp=0;
        for (int i = 0; i < matrix.getColumnCount(); i++) {
            //生产率
            temp=(i+1)/matrix.getAsDouble(0,i);
            thoughput[i]=temp;
        }
        return thoughput;
    }

}
