package main;

import java.util.Random;

public class Randomizer extends Basic3DTest{

    public static int randInt(int min, int max) {
        Random rand = new Random();
        int result = rand.nextInt((max - min) + 1) + min;
        System.out.println(result);
        return result;
    }

    public static float randFloat(int min, int max) {
        float result = (float) Randomizer.randInt(min, max);
        System.out.println(result);
        return result;
    }

    public static double randDouble(int min, int max) {
        double randomNum = new Random().nextDouble();
        double result = min + (randomNum * (max - min));
        System.out.println(result);
        return result;
    }
}