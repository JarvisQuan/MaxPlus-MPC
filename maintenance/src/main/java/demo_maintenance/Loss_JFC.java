package demo_maintenance;


import com.Jarvis.MaxPlusMaintenance.model.entity.Machine;
import org.ujmp.core.Matrix;

import java.util.Arrays;
import java.util.List;




public class Loss_JFC {
	//开始时刻矩阵
	public Matrix[] xkMatrixs;
	//加工时间
	public double[] work_times;
	//缓冲区
	public int[] buffers;
	//产线机器序列
	public List<Machine>dmachines;

	public Base_RandomMatrixs maxPluStateMatrix;


	public Loss_JFC(Matrix[] xkMatrixs, double[] work_times,
					int[] buffers, Base_RandomMatrixs maxPluStateMatrix, List<Machine>machines) {
		super();
		this.xkMatrixs = xkMatrixs;
		this.work_times = work_times;
		this.buffers = buffers;
		this.maxPluStateMatrix = maxPluStateMatrix;
		this.dmachines = machines;

	}

	public int[] loss_Jfc() {

		int[] lossKS = new int[dmachines.size()];
		Arrays.fill(lossKS,0);

		for (Machine d_machine : dmachines) {

			if (d_machine.isDown()==true) {
				Matrix d_machineMatrix = xkMatrixs[d_machine.getId() - 1];

				int k = 0;

				for (int i = 0; i < d_machineMatrix.getColumnCount(); i++) {
					if (d_machine.getD_time() <= d_machineMatrix.getAsDouble(0, i)) {

						k = i;
						break;
					}
				}


				double VTD = xkMatrixs[dmachines.size() - 1].getAsDouble(0, k + 1) - d_machine.getD_time();

				double maxtime = work_times[0];

				int bottle = 0;
				for (int i = 0; i < work_times.length; i++) {
					if (work_times[i] > maxtime) {
						maxtime = work_times[i];
						bottle = i;
					}
				}

				int loss_numK = 0;

				int bottle_numK = 0;

				int buffer_sunm = 0;

				for (int i = 0; i < xkMatrixs[0].getColumnCount(); i++) {
					if (xkMatrixs[bottle].getAsDouble(0, i) > (d_machine.getD_time() + d_machine.getD_continuousTime())) {

						bottle_numK = i;
						break;
					}
				}



				if (d_machine.getId() - 1 > bottle) {
					for (int i = bottle; i < d_machine.getId()-1; i++) {
						buffer_sunm += buffers[i];
					}

					buffer_sunm += 1;

					if (bottle_numK > (k + buffer_sunm)) {

						loss_numK = bottle_numK - k - buffer_sunm;
					} else {

						loss_numK = 0;
					}

				} else if (d_machine.getId() - 1 == bottle) {

					loss_numK = bottle_numK - k;
				}


				double[][] randoMatrix = maxPluStateMatrix.random_times;

				double time = 0;
				for (int i = d_machine.getId() - 1; i < dmachines.size() - 1; i++) {
					time += randoMatrix[k + 1][i];
				}


				double down = 0;

				if ((d_machine.getD_time() + d_machine.getD_continuousTime() + time) > (xkMatrixs[xkMatrixs.length - 1].getAsDouble(0, k) + randoMatrix[k][dmachines.size()- 1])) {

					down = (d_machine.getD_time() + d_machine.getD_continuousTime() + time);

				} else {

					down = xkMatrixs[xkMatrixs.length - 1].getAsDouble(0, k);
				}

				if (d_machine.getId() - 1 < bottle) {
					for (int i = k; i < xkMatrixs[dmachines.size() - 1].getColumnCount(); i++) {
						if (down <= xkMatrixs[dmachines.size() - 1].getAsDouble(0, i)) {
							loss_numK = i - k;
							break;
						}
					}
				}


				lossKS[d_machine.getId()-1] = loss_numK;
			}
		}

		return lossKS;
	}
}