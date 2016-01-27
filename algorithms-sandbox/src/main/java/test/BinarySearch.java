package test;

import com.google.common.base.Stopwatch;


public class BinarySearch {

    public static int[] a = { 1, 3, 3, 5,6,8,11,23,56,78,99, 112, 234, 567, 345};

    public static void main(String[] args) {
        Stopwatch s = new Stopwatch();
        s.start();
        System.out.println(search(6, a));
        s.stop();
        System.out.println("elasped time : " + s.toString());
    }

    public static int search(int key, int[] a) {
        return iter(key, a, 0, a.length -1);
    }

    private static int iter(int key, int[] a, int lo, int hi) {
        int mid = lo + (hi - lo) / 2;

        System.out.println("lo[" + lo + "], mid[" + mid + "], hi [" + hi + "]");

        if(a[mid] == key)
            return mid;
        else if(lo >= hi)
            return -1;
        else if (key > a[mid])
            return iter(key, a, mid + 1, hi);
        else
            return iter(key, a, lo, mid - 1);

    }
}
