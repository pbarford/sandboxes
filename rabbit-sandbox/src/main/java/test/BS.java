package test;

import java.lang.Integer;import java.lang.Math;import java.lang.String;import java.lang.System;import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BS {

    private static Map<Integer, Integer> map = new HashMap<Integer, Integer>();
    private static Random rand = new Random();
    public static void main(String[] args) {
        for(int i=0; i < 1000000; i++) {
            int b = 100000;
            String h = getDest() + (b + randInt(100,100000));
            int hash = h.hashCode();
            int g = Math.abs(hash % 2) + 1;
            int c = map.containsKey(g) ? map.get(g) : 0;
            map.put(g, c + 1);
        }

        for(Integer k : map.keySet())
            System.out.println(k + "=" + map.get(k));
    }
    public static int randInt(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }

    public static String getDest() {
        String s;
        switch (randInt(1, 4)) {
            case 1:
                s = "nike.";
            case 2:
                s = "pp.";
            case 3:
                s = "ngs.";
            default:
                s = "sb.";
        }
        return s;
    }


}
