package demo_maintenance.entity;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimeData {


    @ExcelProperty("在制品序号")
    private int num;

    @ExcelProperty("进入产线时间")
    private double time;
}
