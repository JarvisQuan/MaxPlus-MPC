package demo_maintenance;

import org.ujmp.core.Matrix;

import java.util.ArrayList;

public class Base_Formula_String {
	private Matrix PU;
	private Matrix PX;
	private Matrix PB;
	private Matrix PP;
	private Matrix PY;
 
	private String formulaString = "A1∙X(k-1)"; // 初始模型多项式
	public int num = 2; // 初始矩阵下标
	public ArrayList<Integer> indexArrayList = new ArrayList<Integer>(); // 下标被减数（多项式化简）

	public Base_Formula_String(Matrix pU, Matrix pX, Matrix pB, Matrix pP, Matrix pY) {
		super();
		PU = pU;
		PX = pX;
		PB = pB;
		PP = pP;
		PY = pY;
	}

	public String formula() {
		indexArrayList.add(1); // 首项k-1

		for (int i = 0; i < PP.getRowCount(); i++) {
			if (PP.getAsInt(i, 0) > 1) {
				formulaString += "⊕A" + num + "∙X(k-" + PP.getAsInt(i, 0) + ")";
				num++;
				indexArrayList.add(PP.getAsInt(i, 0));
			}
		}

		for (int i = 0; i < PB.getRowCount(); i++) {
			if (PB.getAsInt(i, 0) > 0) {
				int temp = PB.getAsInt(i, 0) + 1;
				formulaString += "⊕A" + num + "∙X(k-" + temp + ")";
				num++;
				indexArrayList.add(temp);
			}
		}
		formulaString += "⊕A" + num + "∙U(k)";

		return formulaString;
	}

}
