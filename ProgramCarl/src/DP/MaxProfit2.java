package DP;
/*122. 买卖股票的最佳时机 II*/
/*
    给定一个数组，它的第 i 个元素是一支给定股票第 i 天的价格。
    设计一个算法来计算你所能获取的最大利润。你可以尽可能地完成更多的交易（多次买卖一支股票）。
    注意：你不能同时参与多笔交易（你必须在再次购买前出售掉之前的股票）。
    示例 1:
    输入: [7,1,5,3,6,4]
    输出: 7
    解释: 在第 2 天（股票价格 = 1）的时候买入，
    在第 3 天（股票价格 = 5）的时候卖出, 这笔交易所能获得利润 = 5-1 = 4。
    随后，在第 4 天（股票价格 = 3）的时候买入，在第 5 天（股票价格 = 6）的时候卖出, 这笔交易所能获得利润 = 6-3 = 3 。
*/

/*
    dp[i][0]：第i天持有股票的最多现金：
        第i - 1天或之前买的dp[i - 1][0]，
        第 i 天买dp[i - 1][1] - price[i]。
    dp[i][1]：第i天不持有股票最多现金：
        第i - 1天或之前卖的dp[i - 1][1]，
        第 i 天卖的dp[i - 1][0] + price[i]
*/
public class MaxProfit2 {
    public int maxProfit(int[] prices) {
        int days = prices.length;
        int dp [][] = new int[days + 1][2];
        dp[0][0] = -prices[0];
        dp[0][1] = 0;
        for (int i = 1; i < days + 1; i++) {
            dp[i][0] = Math.max(dp[i - 1][0], dp[i - 1][1] - prices[i - 1]);
            dp[i][1] = Math.max(dp[i - 1][1], dp[i - 1][0] + prices[i - 1]);
        }

        return dp[days][1];
    }

}
