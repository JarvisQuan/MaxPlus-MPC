package demo_maintenance.entity;

import com.alibaba.excel.annotation.ExcelProperty;

//导出excel数据实体
public class data {


    @ExcelProperty("时间")
    private double time;

    @ExcelProperty("生产率")
    private double throughput;

    public data() {

    }
    public data(double time, double throughput){
        this.time = time;
        this.throughput = throughput;
    }
    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getThroughput() {
        return throughput;
    }

    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }
}
