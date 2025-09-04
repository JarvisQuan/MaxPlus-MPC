package demo_maintenance;


import com.Jarvis.MaxPlusMaintenance.model.entity.Machine;
import org.ujmp.core.Matrix;

import java.util.Arrays;
import java.util.List;

/**
 * 计算TTD，VTW
 * @author yong
 *
 */
public class Compute_TTD_VTW_change {

	//开始时刻矩阵
	public Matrix[] xkMatrixs;
	//设备加工时间
	private double[] work_times;
	//缓冲区容量
	public int[] buffers;
	//原始加工随机矩阵
	public Base_RandomMatrixs maxPluStateMatrix;
	//产线机器序列（包含故障机器）
	public List<Machine> dmachines;
	//故障时刻
	public double work_down_time;
	//产线停机时的损失量
	public int[] lossKS;

	public Compute_TTD_VTW_change( Matrix[] xkMatrixs, double[] work_times, int[] buffers,
								  Base_RandomMatrixs maxPluStateMatrix, double work_down_time, List<Machine> machines) {
		super();
		this.xkMatrixs = xkMatrixs;
		this.work_times = work_times;
		this.buffers = buffers;
		this.maxPluStateMatrix = maxPluStateMatrix;
		this.work_down_time=work_down_time;
		this.dmachines=machines;
	}


	public  double[]  VTW_TTD() {
		lossKS=new int[dmachines.size()];


		double[] returnVTW_D=new double[dmachines.size()*2];

		Arrays.fill(returnVTW_D,-1);

		int bottle=0;

		double d_time=work_down_time;

		for (Machine machine : dmachines) {

			double VTW=0;

			double TTD=0;

			if (machine.isDown() == true) {

				boolean stop = false;


				Matrix d_machineMatrix = xkMatrixs[machine.getId() - 1];

				int k = 0;

				for (int i = 0; i < d_machineMatrix.getColumnCount(); i++) {
					if (d_time <= d_machineMatrix.getAsDouble(0, i)) {

						k = i-1;

						machine.setRecover_num(k+1);
						break;
					}
				}



				TTD = xkMatrixs[dmachines.size()- 1].getAsDouble(0, k + 1) - d_time;


				for (int continuousTime = 0; continuousTime < 9000; continuousTime ++) {

					double maxtime = work_times[0];
					for (int i = 1; i < work_times.length; i++) {
						if (work_times[i] > maxtime) {
							maxtime = work_times[i];
							bottle = i;
						}
					}


					int bottle_numK = 0;

					int buffer_sunm = 0;

					for (int i = 0; i < xkMatrixs[0].getColumnCount(); i++) {
						if (xkMatrixs[bottle].getAsDouble(0, i) > (d_time + continuousTime)) {

							bottle_numK = i;
							break;
						}
					}

					if (machine.getId() - 1 > bottle) {// 故障机在下游 避免瓶颈机堵塞
						for (int i = bottle; i < machine.getId() - 1; i++) {
							buffer_sunm += buffers[i];
						}

						buffer_sunm += 1;

						if (bottle_numK > (k + buffer_sunm)) {//阻塞

							stop = true;
						} else {//没有堵塞

							VTW = continuousTime;

						}

					} else if (machine.getId() - 1 == bottle) {//瓶颈机故障
						VTW = 0;

						stop = true;
					}
					double[][] randoMatrix = maxPluStateMatrix.random_times;

					double time = 0;
					for (int i = machine.getId() - 1; i < bottle; i++) {
						time += randoMatrix[k + 1][i];
					}

					if (machine.getId() - 1 < bottle) {
						double temp = d_time + continuousTime + time;

						if (temp >xkMatrixs[bottle].getAsDouble(0, k + 1)) {
							double dd = temp - (xkMatrixs[xkMatrixs.length - 1].getAsDouble(0, k) + randoMatrix[k][dmachines.size() - 1]);
							stop = true;
						}else {

							VTW = continuousTime;
						}

					}
					if (stop) {
						break;
					}

				}


				if (VTW > machine.getD_continuousTime()) {
					machine.setStop(false);
				} else {
					machine.setStop(true);
				}


				if (machine.getId() == dmachines.size()) {
					TTD = 0;
				}
				returnVTW_D[machine.getId() - 1] = VTW;

				returnVTW_D[(machine.getId() - 1) * 2 ] = TTD;

			}
		}
		return returnVTW_D;
	}

	public List<Machine> getMachinesWithRecoverNum() {
		return this.dmachines;
	}
}
