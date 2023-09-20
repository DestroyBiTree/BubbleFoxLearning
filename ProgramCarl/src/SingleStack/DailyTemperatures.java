package SingleStack;

import java.util.Arrays;
import java.util.Stack;

/*739.每日温度*/
/*
    请根据每日气温列表，重新生成一个列表。
    对应位置的输出为：要想观测到更高的气温，至少需要等待的天数。如果气温在这之后都不会升高，请在该位置用 0 来代替。
    例如，给定一个列表 temperatures = [73, 74, 75, 71, 69, 72, 76, 73]，
    你的输出应该是 [1, 1, 4, 2, 1, 1, 0, 0]。
*/
public class DailyTemperatures {
    public static void main(String[] args) {
        int [] nums = {73, 74, 75, 71, 71, 72, 76, 73};
        System.out.println(Arrays.toString(dailyTemperatures(nums)));
    }
    public static int[] dailyTemperatures(int[] temperatures) {
        int len = temperatures.length;
        Stack<Integer> stack = new Stack<>();
        int res [] = new int[len];
        stack.push(0);
        for (int i = 1; i < len; i++) {
            if (temperatures[i] < temperatures[stack.peek()]) {
                stack.push(i);
            } else {
                while (!stack.isEmpty() && temperatures[stack.peek()] < temperatures[i]) {
                    res[stack.peek()] = i - stack.peek();
                    stack.pop();
                }
                stack.push(i);
            }
        }
        return res;
    }
}
