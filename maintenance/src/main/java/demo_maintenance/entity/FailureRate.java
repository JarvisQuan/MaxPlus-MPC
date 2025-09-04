package demo_maintenance.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FailureRate {

    @ExcelProperty("时间")
    private double time;

    @ExcelProperty("失效率")
    private double failureRate;
}
