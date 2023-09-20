package DP;
/*121. 买卖股票的最佳时机*/
/*
    给定一个数组 prices ，它的第 i 个元素 prices[i] 表示一支给定股票第 i 天的价格。
    你只能选择 某一天 买入这只股票，并选择在 未来的某一个不同的日子 卖出该股票。设计一个算法来计算你所能获取的最大利润。
    返回你可以从这笔交易中获取的最大利润。如果你不能获取任何利润，返回 0 。
    示例 1：
    输入：[7,1,5,3,6,4]
    输出：5
    解释：在第 2 天（股票价格 = 1）的时候买入，
    在第 5 天（股票价格 = 6）的时候卖出，最大利润 = 6-1 = 5 。
    注意利润不能是 7-1 = 6, 因为卖出价格需要大于买入价格；同时，你不能在买入前卖出股票。
*/

import java.util.Arrays;

/*
    dp[i][0]的含义为表示第i天持有股票所得最多现金：
        如果第i-1天就持有股票，则i天还是那么多dp[i - 1][0]
        如果第i天买入，则钱为 - price[i]


    dp[i][1]的含义为表示第i天不持有股票所得最多现金:
        如果第i - 1天就卖出了，dp[i - 1][1]
        如果第i天卖出去,dp[i - 1][0] + price[1]
*/
public class MaxProfit {
    public static void main(String[] args) {
        int[] nums = {7,6,4,3,1};
        System.out.println(maxProfit(nums));
        System.out.println(maxProfit2(nums));
    }
    public static int maxProfit(int[] prices) {
        int days = prices.length;
        int dp[][] = new int[days + 1][2];
        dp[0][0] = -prices[0];
        dp[0][1] = 0;
        for (int i = 1; i < days + 1; i++) {
            dp[i][0] = Math.max(dp[i - 1][0], - prices[i - 1]);
            dp[i][1] = Math.max(dp[i - 1][1],dp[i - 1][0] + prices[i - 1]);
        }

        System.out.println(Arrays.deepToString(dp));
        return dp[days][1];
    }
    public static int maxProfit2(int[] prices) {
        if (prices == null || prices.length == 0) return 0;
        int length = prices.length;
        // dp[i][0]代表第i天持有股票的最大收益
        // dp[i][1]代表第i天不持有股票的最大收益
        int[][] dp = new int[length][2];
        dp[0][0] = -prices[0];
        dp[0][1] = 0;
        for (int i = 1; i < length; i++) {
            dp[i][0] = Math.max(dp[i - 1][0], -prices[i]);
            dp[i][1] = Math.max(dp[i - 1][0] + prices[i], dp[i - 1][1]);
        }
        System.out.println(Arrays.deepToString(dp));
        return dp[length - 1][1];
    }

}
