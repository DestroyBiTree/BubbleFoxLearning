package DP;
/*打家劫舍*/

/*
    你是一个专业的小偷，计划偷窃沿街的房屋。每间房内都藏有一定的现金，影响你偷窃的唯一制约因素就是相邻的房屋装有相互连通的防盗系统，
    如果两间相邻的房屋在同一晚上被小偷闯入，系统会自动报警。
    给定一个代表每个房屋存放金额的非负整数数组，计算你不触动警报装置的情况下 ，一夜之内能够偷窃到的最高金额。
    示例 1：
    输入：[1,2,3,1]
    输出：4
    解释：偷窃 1 号房屋 (金额 = 1) ，然后偷窃 3 号房屋 (金额 = 3)。   偷窃到的最高金额 = 1 + 3 = 4 。
*/

/*
    dp[i]：考虑当前下标房屋的情况下，价格最大值
    状态转移方程： 不偷i，是dp[i - 1]；
                 偷i，是dp[i - 2] + nums[i]
*/
public class Rob {
    public int rob(int[] nums) {
        int len = nums.length;
        int dp[] = new int[len];
        if (len == 1) {
            return nums[0];
        }

        if (len == 2) {
            return Math.max(nums[0],nums[1]);
        }

        dp[0] = nums[0];
        dp[1] = Math.max(nums[0],nums[1]);
        for (int i = 2; i < len; i++) {
            dp[i] = Math.max(dp[i - 1], dp[i - 2] + nums[i]);
        }
        return dp[len - 1];

    }
}
