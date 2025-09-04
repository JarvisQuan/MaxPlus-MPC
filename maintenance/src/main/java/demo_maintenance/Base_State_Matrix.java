package demo_maintenance;

import org.ujmp.core.Matrix;


public class Base_State_Matrix {
	private Matrix PU;
	private Matrix PX;
	private Matrix PB;
	private Matrix PP;
	private Matrix PY;

	private int matrixNum; // 初始状态矩阵个数
	private double[] work_time; // 加工时间(s)
	private double[] work_time_last;//加工时间（s-1）
	private int[] buffers; // 缓冲区容量
	private int para; // 状态矩阵数组下标

	public Matrix[] stateMatrixs; // 状态矩阵集合

	public  Base_State_Matrix(Matrix pU, Matrix pX, Matrix pB, Matrix pP, Matrix pY, int matrixNum, double[] work_time,double[] work_time_last,
			int[] buffers) {
		super();
		PU = pU; 
		PX = pX;
		PB = pB;
		PP = pP;
		PY = pY;
		this.matrixNum = matrixNum;
		this.work_time = work_time;
		this.work_time_last=work_time_last;
		this.buffers = buffers;
		initialMatrixs();
	}


	private void initialMatrixs() {
		stateMatrixs = new Matrix[matrixNum];
		int stationNum = (int) PX.getRowCount();
		for (int i = 0; i < matrixNum - 1; i++) {
			Matrix tempMatrix = Matrix.Factory.ones(stationNum, stationNum).times(Float.NEGATIVE_INFINITY);
			stateMatrixs[i] = tempMatrix;
		}


		Matrix matrixA = generateTempMatrix();

		stateMatrixs[0] = matrixA.clone(); // 初始A1
		for (int i = 0; i < stationNum; i++) {
			if (PP.getAsDouble(i, 0) > 1) {
				for (int j = 0; j < stationNum; j++) {
					// A1去值
					stateMatrixs[0].setAsDouble(Float.NEGATIVE_INFINITY, j, i);
					// 并行机工作站矩阵赋值
					stateMatrixs[para + 1].setAsDouble(matrixA.getAsDouble(j, i), j, i);
				}
				para++;
			}
		}
 

		for (int i = 0; i < stationNum; i++) {
			if (PB.getAsDouble(i, 0) > 0) { // 工作站后接缓冲区
				for (int j = 0; j < stationNum; j++) {
					if (PX.getAsDouble(j, i) == 1) { // 该站的直连下游工作站j
						for (int m = 0; m < stationNum; m++) {
							stateMatrixs[para + 1].setAsDouble(matrixA.getAsDouble(m, i) - work_time[i], m, j);
						}
						para++;
					}
				}
			}
		}


		Matrix tempMatrix = Matrix.Factory.ones(stationNum, PU.getColumnCount()).times(Float.NEGATIVE_INFINITY);
		stateMatrixs[matrixNum - 1] = tempMatrix;
		generateUkMatrix(stateMatrixs[matrixNum - 1]);


	}


	private void generateUkMatrix(Matrix matrix) {
		for (int i = 0; i < PU.getRowCount(); i++) {
			for (int j = 0; j < PU.getColumnCount(); j++) {
				double temp = 0; // 每列的中间值，工作站加工时间的累加
				if (PU.getAsInt(i, j) == 1) { // 入口j从工作站i开始
					matrix.setAsDouble(temp, i, j);
					int connect = i; // 表示工作站序号
					for (int n = i; n < PX.getRowCount(); n++) {
						if (PX.getAsInt(n, connect) == 1) {
							temp += work_time[connect];
							connect = n; // 更改标志，寻找工作站n的下游工作站
							matrix.setAsDouble(temp, n, j);
						}
					}
				}
			}
		}
	}


	private Matrix generateTempMatrix() {
		int stationNum = (int) PX.getRowCount();
		Matrix matrix = Matrix.Factory.ones(stationNum, stationNum).times(Float.NEGATIVE_INFINITY);
		for (int i = 0; i < PU.getRowCount(); i++) {
			for (int j = 0; j < PU.getColumnCount(); j++) {
				if (PU.getAsInt(i, j) == 1) { // 入口j从工作站i开始


					double temp=work_time_last[i];
					matrix.setAsDouble(temp, i, i);
					int connect = i; // 表示工作站序号
					for (int n = i; n < PX.getRowCount(); n++) { // 在支线上，下游工作站的序号只会比上游大，从i开始
						if (PX.getAsInt(n, connect) == 1) { // 判断工作站n为工作站connect的直连下游工作站
							temp += work_time[connect];
							connect = n; // 更改标志，寻找工作站n的下游工作站
							MMM(connect, matrix);
							matrix.setAsDouble(temp, n, i);
						}
					}
				}
			}
		}
		return matrix;
	}

	private void MMM(int connect, Matrix matrix) {
		int column = connect;
		double temp = work_time[connect];
		matrix.setAsDouble(temp, connect, connect);
		for (int n = connect; n < PX.getRowCount(); n++) { // 在支线上，下游工作站的序号只会比上游大，从connect开始
			if (PX.getAsInt(n, connect) == 1) { // 判断工作站n为工作站connect的直连下游工作站
				temp += work_time[connect];
				connect = n; // 更改标志，寻找工作站n的下游工作站
				matrix.setAsDouble(temp, n, column);
			}
		}
	}

}
