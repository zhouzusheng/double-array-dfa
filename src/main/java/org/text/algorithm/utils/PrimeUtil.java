package org.text.algorithm.utils;

public class PrimeUtil {

    //判断一个数是否是质数（素数）
    public static boolean isPrimeNumber(int num){
        if(num == 2) return true;//2特殊处理
        if(num < 2 || num % 2 == 0) return false;//识别小于2的数和偶数
        int max = (int)Math.sqrt(num);
        for(int i=3; i <= max; i+=2){
            if(num % i == 0){//识别被奇数整除
                return false;
            }
        }
        return true;
    }

    /**
     * 另一个方法, 等会比较效率
     */

    public static boolean isPrime2(int x) {
        if (x <= 7) {
            if (x == 2 || x == 3 || x==5 || x == 7)
                return true;
        }
        int c = 7;
        if (x % 2 == 0)
            return false;
        if (x % 3 == 0)
            return false;
        if (x % 5 == 0)
            return false;
        int end = (int) Math.sqrt(x);
        while (c <= end) {
            if (x % c == 0) {
                return false;
            }
            c += 4;
            if (x % c == 0) {
                return false;
            }
            c += 2;
            if (x % c == 0) {
                return false;
            }
            c += 4;
            if (x % c == 0) {
                return false;
            }
            c += 2;
            if (x % c == 0) {
                return false;
            }
            c += 4;
            if (x % c == 0) {
                return false;
            }
            c += 6;
            if (x % c == 0) {
                return false;
            }
            c += 2;
            if (x % c == 0) {
                return false;
            }
            c += 6;
        }
        return true;
    }

    public static int lessPrimeNumber(int num) {
        if(num %2 == 0) {
            num--;
        }

        if(num <=3) {
            return 3;
        }
        for(int i = num; i>=2; i-=2) {
            if(isPrime2(i)) {
                return i;
            }
        }
        return 3;
    }

    /*
    static void prime(int n) {
        int i;
        boolean[] prime_values = new boolean[n+1];
        for(i=2;i<n;i++){
            prime_values[i] = true;
        }
        for(i=2;i<=n;i++){
            if(prime_values[i]){
                System.out.print(i);
                System.out.print(" ");
                for(int j=i+i;j<n;j+=i){
                    prime_values[j] = false;
                }
            }
        }
        System.out.println();
    }

     */


}
