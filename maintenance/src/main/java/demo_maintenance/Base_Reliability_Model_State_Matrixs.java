package demo_maintenance;


import com.Jarvis.MaxPlusMaintenance.demo_maintenance.Reliability_Model.ReliabilityParameters;
import org.ujmp.core.Matrix;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 随机扰动下的系统矩阵和完工时间
 * 
 * @author yong
 *
 */
public class Base_Reliability_Model_State_Matrixs {

	private int stationsNum;// 工作站数量
	private int inputsNum;// 输入口数量
	private int[] buffers;// 缓冲区容量
	private double[] work_times;// 工作站加工时间
	// 并行机工作站
	private String parallelStationString;
	// 入口工作站序号
	private String inputStructString;
	// 工作站连接顺序
	private String structureString;
	// 最终的状态矩阵map集合(下标被减数，矩阵)不包含UK项矩阵
	private Map<Integer, Matrix> matrixsMap;
	// UK项矩阵
	private Matrix uK_BMatrix;

	private Matrix PU; // 入口
	private Matrix PX; // 结构
	private Matrix PB; // 缓冲区
	private Matrix PP; // 并行机
	private Matrix PY;// 出口
	// 初始的矩阵数量
	private int num;
	// 初始多项式
	private Base_Formula_String formula_string;
	// 订单量
	public int demand;
	// 随机加工时间（二维数组 行：工件数 列：工作站数）
	public double[][] random_times;
	// 随机数二维数组（行：工作站数 列：工件数）
	public double[][] randoms;
	//役龄调整因子a
	private double a;
	//故障率调整因子b
	private double b;
	//初始失效率
	private double beginFailureRate;


	public Base_Reliability_Model_State_Matrixs(int stationsNum, int inputsNum, int[] buffers, double[] work_times,
												String parallelStationString, String inputStructString, String structureString, int demand,
												double[][] randoms,double a,double b,double beginFailureRate) {
		super();
		this.stationsNum = stationsNum;
		this.inputsNum = inputsNum;
		this.buffers = buffers;
		this.work_times = work_times;
		this.parallelStationString = parallelStationString;
		this.inputStructString = inputStructString;
		this.structureString = structureString;
		this.demand = demand;
		this.randoms = randoms;
		this.a = a;
		this.b = b;
		this.beginFailureRate = beginFailureRate;
	}

	public Matrix[] mainMatrix() {
		// 入口工作站序号
		String[] inputStructStrings = inputStructString.split(";");
		// 工作站连接顺序
		String[] structureStrings = structureString.split(";");
		// 并行机工作站位置和数量
		String[] parallelStrings = parallelStationString.split(";");

		PU = Matrix.Factory.zeros(stationsNum, inputsNum); // 入口
		PX = Matrix.Factory.zeros(stationsNum, stationsNum); // 结构
		PB = Matrix.Factory.zeros(stationsNum, 1); // 缓冲区
		PP = Matrix.Factory.ones(stationsNum, 1); // 并行机
		PY = Matrix.Factory.zeros(stationsNum, 1); // 出口


		// PU
		for (int i = 0; i < inputStructStrings.length; i++) {
			String[] tempStrings = inputStructStrings[i].split(",");
			PU.setAsInt(1, Integer.parseInt(tempStrings[0]) - 1, Integer.parseInt(tempStrings[1]) - 1);
		}
		// PX
		for (int i = 0; i < structureStrings.length; i++) {
			String[] tempStrings = structureStrings[i].split(",");
			PX.setAsInt(1, Integer.parseInt(tempStrings[0]) - 1, Integer.parseInt(tempStrings[1]) - 1);
		}
		// PB
		for (int i = 0; i < buffers.length; i++) {
			PB.setAsInt(buffers[i], i, 0);
		}
		// PP
		if (parallelStrings.length > 1) {// 是否有平行站
			for (int i = 0; i < parallelStrings.length; i++) {
				String[] tempStrings = parallelStrings[i].split(",");
				PP.setAsInt(Integer.parseInt(tempStrings[1]), Integer.parseInt(tempStrings[0]) - 1, 0);
			}
		}

		// PY
		PY.setAsInt(1, stationsNum - 1, 0);


		formula_string = new Base_Formula_String(PU, PX, PB, PP, PY);

		System.out.println(formula_string.formula());

		num = formula_string.num;


		Matrix[] xKMatrixs = new Matrix[stationsNum];
		ArrayList<ReliabilityParameters> reliabilityParameters = new ArrayList<>();
		for (int i = 0; i < stationsNum; i++) {
			xKMatrixs[i] = Matrix.Factory.ones(1, demand).times(Float.NEGATIVE_INFINITY);
			//reliabilityParameters.add(new ReliabilityParameters(a,b,beginFailureRate));
		}


		double[] stable_work_times = new double[work_times.length];

		for (int i = 0; i < stable_work_times.length; i++) {
			stable_work_times[i] = work_times[i];
		}

		double[] work_times_last = new double[work_times.length];



		random_times = new double[demand][stationsNum];
		for (int i = 0; i < demand; i++) {
			for (int j = 0; j < stationsNum; j++) {

				random_times[i][j] = stable_work_times[j];
			}
		}


		for (int i = 0; i < demand; i++) {

			for(int j = 0; j < stationsNum; j++) {


				if (i==0) {
					random_times[i][j]=stable_work_times[j];
					continue;
				}



			}
			 work_times=random_times[i];

			if (i > 0) {
				work_times_last = random_times[i - 1];
			}

			Base_State_Matrix state_Matrix = new Base_State_Matrix(PU, PX, PB, PP, PY, num, work_times, work_times_last,
					buffers);
			Matrix[] stateMatrixs = state_Matrix.stateMatrixs;

			matrixsMap = new HashMap<Integer, Matrix>(); // 下标i为key，矩阵为value的集合
			ArrayList<Integer> indexArrayList = formula_string.indexArrayList; // 下标集合
			for (int i1 = 0; i1 < indexArrayList.size(); i1++) { // 遍历下标集合
				if (matrixsMap.containsKey(indexArrayList.get(i1))) { // map中若已存在该下标key则中断此次循环
					continue;
				}

				for (int j = i1; j < indexArrayList.size(); j++) {
					if ((i1 != j) && (indexArrayList.get(i1) == indexArrayList.get(j))) {
						stateMatrixs[i1] = maxCompareMatrix(stateMatrixs[i1], stateMatrixs[j]);
					}
				}

				matrixsMap.put(indexArrayList.get(i1), stateMatrixs[i1]);
			}

			uK_BMatrix = stateMatrixs[stateMatrixs.length - 1];


			Base_Single_compute_Xk cXk = new Base_Single_compute_Xk(xKMatrixs, stationsNum, i, matrixsMap, uK_BMatrix,
					demand);
			Matrix matrix = cXk.compute();

			for (int j = 0; j < stationsNum; j++) {

				xKMatrixs[j].setAsDouble(matrix.getAsDouble(j, 0), i, 0);

			}

		}

		return xKMatrixs;
	}

	//
	public Matrix[] updateMatrix(int recover_num, double recover_time, int d_machine, double d_continuousTime,
			Matrix[] xkMatrixs) {
		Matrix[] resultMatrixs = new Matrix[stationsNum];
		for (int i = 0; i < resultMatrixs.length; i++) {
			resultMatrixs[i] = Matrix.Factory.ones(1, demand).times(Float.NEGATIVE_INFINITY);
		}

		for (int i = 0; i < demand; i++) {
			if (i < recover_num) {
				for (int j = 0; j < resultMatrixs.length; j++) {
					resultMatrixs[j].setAsDouble(xkMatrixs[j].getAsDouble(0, i), 0, i);
				}
			}
			if (i == recover_num) {
				for (int j = 0; j < resultMatrixs.length; j++) {
					if (j < d_machine) {
						resultMatrixs[j].setAsDouble(xkMatrixs[j].getAsDouble(0, i), 0, i);
					} else if (j == d_machine) {
						resultMatrixs[d_machine].setAsDouble(recover_time, 0, recover_num);
					} else {
						if (recover_time > xkMatrixs[stationsNum - 1].getAsDouble(0, recover_num)) {
							double temp = xkMatrixs[j].getAsDouble(0, recover_num) + d_continuousTime
									+ random_times[i][j - 1];
							resultMatrixs[j].setAsDouble(temp, 0, recover_num);
						} else {
							double temp = xkMatrixs[j].getAsDouble(0, recover_num) + d_continuousTime;
							resultMatrixs[j].setAsDouble(temp, 0, recover_num);
						}
					}
				}

			} else {
				work_times = random_times[i];

				Base_State_Matrix state_Matrix = new Base_State_Matrix(PU, PX, PB, PP, PY, num, work_times,
						new double[] {}, buffers);
				Matrix[] stateMatrixs = state_Matrix.stateMatrixs;

				matrixsMap = new HashMap<Integer, Matrix>(); // 下标i为key，矩阵为value的集合
				ArrayList<Integer> indexArrayList = formula_string.indexArrayList; // 下标集合
				for (int i1 = 0; i1 < indexArrayList.size(); i1++) { // 遍历下标集合
					if (matrixsMap.containsKey(indexArrayList.get(i1))) { // map中若已存在该下标key则中断此次循环
						continue;
					}

					for (int j = i1; j < indexArrayList.size(); j++) {
						if ((i1 != j) && (indexArrayList.get(i1) == indexArrayList.get(j))) {
							stateMatrixs[i1] = maxCompareMatrix(stateMatrixs[i1], stateMatrixs[j]);
						}
					}

					matrixsMap.put(indexArrayList.get(i1), stateMatrixs[i1]);
				}

				uK_BMatrix = stateMatrixs[stateMatrixs.length - 1];

				Base_Single_compute_Xk cXk = new Base_Single_compute_Xk(resultMatrixs, stationsNum, i, matrixsMap,
						uK_BMatrix, demand);
				Matrix matrix = cXk.compute();

				for (int j = 0; j < stationsNum; j++) {

					resultMatrixs[j].setAsDouble(matrix.getAsDouble(j, 0), i, 0);

				}
			}

		}

		return resultMatrixs;

	}


	public double getExpRandomValue(double lambda) {
		// 负指数分布
		return (-1.0 / lambda) * Math.log(1 - new SecureRandom().nextDouble());
	}


	public double getUnifRandomValue(double lower, double higher) {
		return (lower + new Random().nextDouble() * (higher - lower));
	}


	public double getNormalRandomValue(double u, double deta) {
		return Math.sqrt(deta) * (new Random().nextGaussian()) + u;
	}


	public static Matrix maxCompareMatrix(Matrix matrix1, Matrix matrix2) {
		for (int i = 0; i < matrix1.getRowCount(); i++) {
			for (int j = 0; j < matrix1.getColumnCount(); j++) {
				double temp = matrix1.getAsDouble(i, j) > matrix2.getAsDouble(i, j) ? matrix1.getAsDouble(i, j)
						: matrix2.getAsDouble(i, j);
				matrix1.setAsDouble(temp, i, j);
			}
		}
		return matrix1;
	}

}
