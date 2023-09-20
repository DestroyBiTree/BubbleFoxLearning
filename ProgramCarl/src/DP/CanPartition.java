package DP;
/*416.分割等和子集*/
/*
    给定一个只包含正整数的非空数组。是否可以将这个数组分割成两个子集，使得两个子集的元素和相等。
    注意: 每个数组中的元素不会超过 100 数组的大小不会超过 200
    示例 1:
    输入: [1, 5, 11, 5]
    输出: true
    解释: 数组可以分割成 [1, 5, 5] 和 [11].
*/

import java.lang.annotation.Target;
import java.util.Arrays;

/*
    dp[i][j]：从0-i中任选数字，每个数只能用一次，这些数最大值为j


*/
public class CanPartition {
    public static void main(String[] args) {
        int num [] = {1,5,10,6};
        System.out.println(canPartition2(num));
    }
    public static boolean canPartition2(int[] nums) {
        int sum = 0;
        int len = nums.length;
        for (int num : nums) {
            sum += num;
        }
        int target = sum / 2;
        if (sum % 2 != 0) {
            return false;
        }
        int dp[][] = new int[nums.length][target + 1];

        for(int j = nums[0]; j < target + 1; j++){
            dp[0][j] = nums[0];
        }

        for (int i = 1; i < nums.length; i++) {
            for (int j = 1; j < target + 1; j++) {
                if (j < nums[i]) {
                    dp[i][j] = dp[i - 1][j];
                }
                else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i - 1][j - nums[i]] + nums[i]);
                }
            }
        }
        return dp[nums.length - 1][target] == target;



    }
}
