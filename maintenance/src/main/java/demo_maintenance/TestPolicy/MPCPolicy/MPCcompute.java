package demo_maintenance.TestPolicy.MPCPolicy;


import org.ujmp.core.Matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

//MPC滚动计算
public class MPCcompute {

    public static void  Compute(double computeTime, List<double[]>allPossibilities,Matrix[]StandardxKMatrix, Matrix[] beginxkMatrixs, int[] buffers, Base_RandomMatrixs maxPluStateMatrix, List<Machine>machinesWithRecoverNum,int stationsNum,double[] work_times,int[]loss,MPCComputeResult mpcComputeResult) {
        if (computeTime>=7334){
            System.out.println("可能结果输出-------");
            compareBest(StandardxKMatrix,beginxkMatrixs,machinesWithRecoverNum,loss,mpcComputeResult,0.0001,100);
            return ;
        }
        for (int x = 0; x < allPossibilities.size(); x++) {
            Matrix[] BreadthCloneMatrices = CloneUtil.cloneBeginxkMatixs(beginxkMatrixs);
            List<Machine> BreadthCloneMachines = CloneUtil.cloneMachine(machinesWithRecoverNum);
            int[] BreadthLoss = CloneUtil.cloneLoss(loss);

            double beginTime= computeTime;
                for (int z = 0; z < stationsNum; z++) {

                    double D_countinus = allPossibilities.get(x)[z];

                    if (D_countinus == 0.0) {
                        continue;
                    }

                    if (D_countinus == 200.0) {
                        BreadthCloneMachines.get(z).setDown(true);
                        BreadthCloneMachines.get(z).setD_time(beginTime);
                        BreadthCloneMachines.get(z).setD_continuousTime(200);
                        BreadthCloneMachines.get(z).setReliabilityFixAsOld(beginTime);
                    }

                    if (D_countinus == 400.0) {
                        BreadthCloneMachines.get(z).setDown(true);
                        BreadthCloneMachines.get(z).setD_time(beginTime);
                        BreadthCloneMachines.get(z).setD_continuousTime(400);
                        BreadthCloneMachines.get(z).setReliabilityFixAsNew(beginTime);
                    }
                }

            Compute_TTD_VTW_change computeTtdVtwChange = new Compute_TTD_VTW_change(BreadthCloneMatrices, work_times, buffers, maxPluStateMatrix, beginTime, BreadthCloneMachines);
            double[] doubles = computeTtdVtwChange.VTW_TTD();
            BreadthCloneMachines = computeTtdVtwChange.getMachinesWithRecoverNum();
            Loss_JFC computeloss = new Loss_JFC(BreadthCloneMatrices, work_times, buffers, maxPluStateMatrix, BreadthCloneMachines);
            int[] Loss = computeloss.loss_Jfc();


            for (int i = 0; i < Loss.length; i++) {
                BreadthLoss[i] += Loss[i];
            }

            beginxkMatrixs = maxPluStateMatrix.updateMatrix1(BreadthCloneMatrices, BreadthCloneMachines);


            Matrix[] DeepthClonexkMatrixs = CloneUtil.cloneBeginxkMatixs(beginxkMatrixs);
            List<Machine> DeepthCloneMachines = CloneUtil.cloneMachine(BreadthCloneMachines);
            int[] DeepthCloneLoss = CloneUtil.cloneLoss(BreadthLoss);

                beginTime = computeTime+1000;
                for (int j = 0; j< stationsNum; j++) {

                    System.out.println("设备："+DeepthCloneMachines.get(j).getId()+"=============================");
                    ReliabilityParameters Machinesreliability = DeepthCloneMachines.get(j).getReliability();
                    double Reliability = Machinesreliability.GetReliability(beginTime , beginTime+ work_times[j]);

                    if (0 <= Reliability && Reliability <= 0.75) {
                        DeepthCloneMachines.get(j).setDown(true);
                    }
                }

                boolean[] getpolicy = new boolean[stationsNum];
                for (int j = 0; j < stationsNum; j++) {
                    getpolicy[j]=DeepthCloneMachines.get(j).isDown();
                }
                List<double[]> NextallPossibilities = new ArrayList<>();
                MPCPolicyGenerate.dfs(0,getpolicy,NextallPossibilities,new double[getpolicy.length]);

                Compute( computeTime+1000, NextallPossibilities,StandardxKMatrix, DeepthClonexkMatrixs, buffers,maxPluStateMatrix, DeepthCloneMachines, stationsNum, work_times,DeepthCloneLoss,mpcComputeResult);
        }

        }

    private static void compareBest(Matrix[]StandardxKMatrix,Matrix[] beginxkMatrixs,List<Machine>machines,int[]loss, MPCComputeResult mpcComputeResult, double percentageOfProductivity, int percentageOfLoss) {
        if (mpcComputeResult.resultxKMatrixs.size()<=0||mpcComputeResult.resultLoss.size()<=0||mpcComputeResult.resultMachines.size()<=0){
            mpcComputeResult.computeRusultIndex=0;
            mpcComputeResult.resultMachines.add(machines);
            mpcComputeResult.resultLoss.add(loss);
            mpcComputeResult.resultxKMatrixs.add(beginxkMatrixs);
            return ;
        }

        Matrix[] preResultxKMatrix=mpcComputeResult.resultxKMatrixs.get(0);
        int[] preResultLoss=mpcComputeResult.resultLoss.get(0);

        double preproductivityCost=0;

        int prelossCost=0;


        for (int j=0;j<preResultxKMatrix[0].getColumnCount();j++){
            preproductivityCost+=(preResultxKMatrix[0].getAsDouble(0,j)-StandardxKMatrix[StandardxKMatrix.length-1].getAsDouble(0,j));
        }
        for (int j=0;j<preResultLoss.length;j++){
            prelossCost+=preResultLoss[j];
        }

        double preCost=(prelossCost*percentageOfLoss)+(preproductivityCost*percentageOfProductivity);


        double productivityCost=0;

        int lossCost=0;



        for (int j=0;j<beginxkMatrixs[0].getColumnCount();j++){
            productivityCost+=(beginxkMatrixs[0].getAsDouble(0,j)-StandardxKMatrix[StandardxKMatrix.length-1].getAsDouble(0,j));
        }
        for (int j=0;j<loss.length;j++){
            lossCost+=loss[j];
        }
        double tempCost=(lossCost*percentageOfLoss)+(productivityCost*percentageOfProductivity);

        if (tempCost<preCost){
            mpcComputeResult.resultLoss.clear();
            mpcComputeResult.resultMachines.clear();
            mpcComputeResult.resultxKMatrixs.clear();
            mpcComputeResult.computeRusultIndex=0;
          mpcComputeResult.resultMachines.add(machines);
          mpcComputeResult.resultLoss.add(loss);
          mpcComputeResult.resultxKMatrixs.add(beginxkMatrixs);

        }
    }


    //选择最后策略
        public static MPCComputeResult selectBest(Matrix[]StandardxKMatrix,MPCComputeResult computeResult,double percentageOfProductivity,double percentageOfLoss) {

            Matrix[] selectResultxKMatrix=null;
            int[] selectResultLoss=null;
            int selectResultIndex=-1;
            List<Machine>selectResultMachines=null;

            //维修总成本
            double MataintenanceCost=Double.MAX_VALUE;

        for (int i =0;i<computeResult.resultxKMatrixs.size();i++){
            //生产率损失
            double productivityCost=0;
            //产能损失
            int lossCost=0;

            double tempCost=0;

            Matrix matrix = computeResult.resultxKMatrixs.get(i)[computeResult.resultxKMatrixs.get(i).length - 1];
            for (int j=0;j<matrix.getColumnCount();j++){
                productivityCost+=(matrix.getAsDouble(0,j)-StandardxKMatrix[StandardxKMatrix.length-1].getAsDouble(0,j));
            }
            for (int j=0;j<computeResult.resultLoss.get(i).length;j++){
                lossCost+=computeResult.resultLoss.get(i)[j];
            }

            tempCost=(lossCost*percentageOfLoss)+(productivityCost*percentageOfProductivity);
            if (tempCost<=MataintenanceCost){
                selectResultIndex=i;
                selectResultLoss=computeResult.resultLoss.get(i);
                selectResultxKMatrix=computeResult.resultxKMatrixs.get(i);
                selectResultMachines=computeResult.resultMachines.get(i);
               MataintenanceCost=tempCost;
            }
        }

        if (MataintenanceCost==Double.MAX_VALUE){
            throw new RuntimeException("无最优策略");
        }

            System.out.println("MinLoss1决策结果为："+selectResultIndex+"   维修总成本为:"+MataintenanceCost+"  生产损失为："+ Arrays.toString(selectResultLoss)+"===================");
        for (int i=0;i<selectResultMachines.size();i++  ){
            Set<Integer> keySet = selectResultMachines.get(i).reliability.Maintenances.keySet();
            StringBuilder stringBuilder = new StringBuilder("设备：" + (i + 1));
            for (Integer key:keySet){
                Integer integer = selectResultMachines.get(i).reliability.Maintenances.get(key);
                stringBuilder.append("第"+key+"次维修:"+ integer );
            }
            System.out.println(stringBuilder.toString());

        }


        MPCComputeResult mpcComputeResult = new MPCComputeResult(selectResultLoss.length);
            mpcComputeResult.resultxKMatrixs.add(selectResultxKMatrix);
            mpcComputeResult.resultLoss.add(selectResultLoss);
            mpcComputeResult.resultMachines.add(selectResultMachines);
            mpcComputeResult.computeRusultIndex=selectResultIndex;
        return  mpcComputeResult;
        }

    }

