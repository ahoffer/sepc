package weka.subspaceClusterer;

import java.util.Arrays;
import java.util.Random;

public class Shuffler {
    private int[] ringBuffer;
    private int[] originalValues;
    private Random random;
    private long seed;
    private int fetchIdx;
    private int numberOfElements;

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public static void main(String[] argv) {
        // int[] a1 = { 1, 2, 3 };
        // int[] a2 = { 4, 5, 6 };
        // int[] a1 = { };
        // int[] a2 = { 4, 5, 6 };
        // int[] a1 = { 1, 2, 3 };
        // int[] a2 = {};
        // int[] test = concat(a1, a2);
        // Shuffler sh = new Shuffler(3, 1);
        // int[] t1 = sh.next(1);
        // int[] t2 = sh.next(2);
        // int[] t3 = sh.next(3);
        // int[] t4 = sh.next(4);
        // int[] t5 = sh.next(0);
        // int[] t6 = sh.next(2);
        // int[] t7 = sh.next(2);
        // int[] t8 = sh.next(3);
        // Do nothing. Just a place for a breakpoint
        // int l = test.length;
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public Shuffler(int numberOfElements, long seed) {
        this.seed = seed;
        random = new Random(seed);
        this.numberOfElements = numberOfElements;
        originalValues = getSequence(numberOfElements);
        reshuffle();
    }// method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    int[] getSequence(int numberOfElements) {
        // Return a sequence that ranges from 0 to numberofElements-1
        int sequence[] = new int[numberOfElements];
        for (int i = 0; i < numberOfElements; ++i) {
            sequence[i] = i;
        }// for
        return sequence;
    }// method

    public long getSeed(long seed) {
        return seed;
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public void setSeed(long s) {
        seed = s;
        random = new Random(seed);
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    private void shuffle(int[] array) {
        // Fisher Yates shuffle
        int size, j, temp;
        size = array.length;
        for (int i = size - 1; i > 0; --i) {
            // Upper bound on random integer is exclusive, so add 1
            // Actually, I might get a better shuffle with just i
            // because there is zero chance an element can stay where it is
            j = random.nextInt(i + 1);
            temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }// for
    }// method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public int[] next(int sampleSize) {
        int length, remaining;
        int[] sample;
        if (sampleSize > numberOfElements) {
            // Not supported
            return null;
        }
        // Calculate the number of shuffled elements that can be used.
        remaining = numberOfElements - fetchIdx;
        // If there are not enough elements to fulfill the request, re-shuffle
        // the data.
        if (sampleSize > remaining) {
            reshuffle();
        }
        // Draw the sample
        length = fetchIdx + sampleSize;
        sample = Arrays.copyOfRange(ringBuffer, fetchIdx, length);
        fetchIdx = length;
        return sample;
    }// method

    private int[] copy() {
        return Arrays.copyOf(originalValues, numberOfElements);
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    private void reshuffle() {
        ringBuffer = copy();
        shuffle(ringBuffer);
        fetchIdx = 0;
    }// method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public static int[] concat(int array1[], int array2[]) {
        int size1, size2;
        int[] newArray;
        size1 = array1.length;
        size2 = array2.length;
        newArray = new int[size1 + size2];
        for (int i = 0; i < size1; ++i) {
            newArray[i] = array1[i];
        }// for
        for (int j = 0; j < size2; ++j) {
            newArray[j + size1] = array2[j];
        }// for
        return newArray;
    }// method
}// class
