package demo_maintenance.TestPolicy.MPCPolicy;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MPCPolicyGenerate {

    public static void dfs(int i, boolean[] arr, List<double[]> allPossibilities, double[] res) {
        if (i < arr.length) {
            if (arr[i]) {
                res[i] = 200;
                dfs(i + 1, arr, allPossibilities, res);
                res[i] = 400;
                dfs(i + 1, arr, allPossibilities, res);
            } else {
                res[i] = 0;
                dfs(i + 1, arr, allPossibilities, res);
                res[i] = 400;
                dfs(i + 1, arr, allPossibilities, res);
            }
        } else {
            allPossibilities.add(res.clone());
        }
    }

    public static void main(String[] args) {
        boolean[] arr = new boolean[]{true, true, false};
        List<double[]> allPossibilities = new ArrayList<>();

        dfs(0, arr, allPossibilities, new double[arr.length]);

        for (double[] allPossibility : allPossibilities) {
            System.out.println(Arrays.toString(allPossibility));
        }
    }
}
