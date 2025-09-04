package demo_maintenance;

import org.ujmp.core.Matrix;

import java.util.Map;

public class Base_Single_compute_Xk { 
	// 工作站序号
	private int num;
	//当前在制品序号（从0开始）
	private int nowPieceNum;
	// 最终的状态矩阵map集合(下标被减数，矩阵)不包含UK项矩阵
	private Map<Integer, Matrix> matrixsMap;
	// UK项矩阵
	private Matrix uK_BMatrix;
	//原料输入时间(可能存在多个入口)
	private Matrix[] UK;
	//
	private Matrix[] xKMatrixs;

	private int total_demand;
		
	public Base_Single_compute_Xk(Matrix[] xKMatrixs,int num, int demand, Map<Integer, Matrix> matrixsMap, Matrix uK_BMatrix,int total_demand) {
		super();
		this.num = num;
		this.nowPieceNum = demand;
		this.matrixsMap = matrixsMap;
		this.uK_BMatrix = uK_BMatrix;
		this.xKMatrixs=xKMatrixs;
		this.total_demand=total_demand;
		//入口数量
		UK=new Matrix[(int) uK_BMatrix.getColumnCount()];
	}

 


	public Matrix compute() {

		Matrix singleMatrix=Matrix.Factory.zeros(1,num);

		

		for (int i = 0; i < uK_BMatrix.getColumnCount(); i++) {
			UK[i]=Matrix.Factory.ones(1,total_demand);
			for (int j = 0; j < total_demand; j++) {
				UK[i].setAsDouble(j, 0,j);
			}
		}

		Object[] index_keys=matrixsMap.keySet().toArray();
		

			Matrix matrix_uk=Matrix.Factory.ones(UK.length,1);
			for (int i = 0; i < UK.length; i++) {
				matrix_uk.setAsDouble(UK[i].getAsDouble(0,nowPieceNum), i,0);
			}
			Matrix bUkMatrix=plus_mulMatrix(uK_BMatrix, matrix_uk);
			

			Matrix temp_Xk=Matrix.Factory.ones(num,1).times(Float.NEGATIVE_INFINITY);
			if (nowPieceNum-1>=0) {
				for (int i = 0; i < num; i++) {
					temp_Xk.setAsDouble(xKMatrixs[i].getAsDouble(0,nowPieceNum-1),i, 0);
				}
			}
			Matrix temp_Xk_1=plus_mulMatrix(matrixsMap.get(1), temp_Xk);

			Matrix[] temp_Xk_b=new Matrix[index_keys.length-1];
			for (int i = 1; i < index_keys.length; i++) {
				Matrix temp_Xk_bi=Matrix.Factory.ones(num,1).times(Float.NEGATIVE_INFINITY);
				if (nowPieceNum-(int)index_keys[i]>=0) {
					for (int j = 0; j < num; j++) {
						temp_Xk_bi.setAsDouble(xKMatrixs[j].getAsDouble(0,nowPieceNum-(int)index_keys[i]), j,0);
					}
				}
				temp_Xk_b[i-1]=plus_mulMatrix(matrixsMap.get(index_keys[i]), temp_Xk_bi);
			}
			

			Matrix temp=Matrix.Factory.ones(index_keys.length+1,1).times(Float.NEGATIVE_INFINITY);
			for (int n = 0; n < num; n++) {
				for (int j = 0; j < index_keys.length-1; j++) {
					temp.setAsDouble(temp_Xk_b[j].getAsDouble(n,0), j,0);
				}
				temp.setAsDouble(bUkMatrix.getAsDouble(n,0),index_keys.length-1,0);
				temp.setAsDouble(temp_Xk_1.getAsDouble(n,0), index_keys.length,0);
				

				singleMatrix.setAsDouble(temp.getMaxValue(), n,0);
			}
		
		return singleMatrix;
		
	}
	

	public Matrix plus_mulMatrix(Matrix A,Matrix B) {
		Matrix matrix=Matrix.Factory.ones(A.getRowCount(),B.getColumnCount()).times(Float.NEGATIVE_INFINITY);
		Matrix temp=Matrix.Factory.ones(1,A.getColumnCount()).times(Float.NEGATIVE_INFINITY);
		for (int i = 0; i < A.getRowCount(); i++) {
			for (int j = 0; j < B.getColumnCount(); j++) {
				for (int j2 = 0; j2 < A.getColumnCount(); j2++) {
					temp.setAsFloat(A.getAsFloat(i,j2)+B.getAsFloat(j2,j), j,j2);
				}
				matrix.setAsDouble(temp.getMaxValue(), i,j);
			}
		}
		return matrix;
	}
}
