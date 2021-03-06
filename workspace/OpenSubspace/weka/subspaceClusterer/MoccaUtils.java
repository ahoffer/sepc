package weka.subspaceClusterer;

import i9.subspace.base.Cluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class MoccaUtils {
    // Pick a small values that means zero.
    public static double epsilon = 1E-8;

    public static boolean isZero(double d) {
        return Math.abs(d) < epsilon;
    }

    public static void backspace(StringBuffer sb) {
        sb.deleteCharAt(sb.length() - 1);
    }

    public static int countTrueValues(boolean[] input) {
        int count = 0;
        for (boolean b : input) {
            if (b) {
                count++;
            }// end if
        }// end for
        return count;
    }// end method

    public static boolean equalToOne(double d) {
        return Math.abs(d - 1) < epsilon;
    }

    public static boolean[] greaterThanOrEqualTo(double[] input, double value) {
        int length = input.length;
        boolean result[] = new boolean[length];
        for (int i = 0; i < length; ++i) {
            result[i] = (input[i] >= value) ? true : false;
        }
        return result;
    }

    public static boolean greaterThanZero(double d) {
        return d > epsilon;
    }

    public static boolean hasClassAttribute(Instances data) {
        return data.classIndex() > 0;
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /*
     * Binary search on a sorted array of integers to determine if the element exists Use Arrays.binarySearch() instead.
     */
    // public static boolean binarySearch(int[] sortedList, int target) {
    //
    // int high = sortedList.length - 1;
    // int low = 0;
    // int currentVal, mid;
    //
    // while (high >= low) {
    // mid = (low + high) / 2;
    // currentVal = sortedList[mid];
    // if (target == currentVal) {
    // return true;
    // }
    //
    // if (currentVal > target) {
    // // Too big. Look smaller.
    // high = mid - 1;
    // } else {
    // // Too small. Look bigger.
    // low = mid + 1;
    // }
    // }// end while
    //
    // return false;
    //
    // }// end method
    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /*
     * Compare two SORTED lists of integers and return the elements in common.
     * 
     * This routine probably isn't as fast as the intersection routine that uses a hash table. This routine should run
     * O(n*log(n)) and the other intersection routine in this class should run in O(n).
     */
    // public static int[] intersection(int[] list1, int[] list2) {
    // // PRECONDITION: Both lists are sorted lowest to highest.
    //
    // ArrayList<Integer> results;
    // int[] toSearch, toIterate;
    // int size1, size2;
    // size1 = list1.length;
    // size2 = list2.length;
    //
    // // Iterate over the shortest O(n)
    // // Search over the longest O(log n)
    // if (size1 > size2) {
    // toSearch = list1;
    // toIterate = list2;
    // results = new ArrayList<Integer>(size1);
    // } else {
    // toSearch = list2;
    // toIterate = list1;
    // results = new ArrayList<Integer>(size2);
    // }
    //
    // for (int each : toIterate) {
    // // -1 is magic value that means "not found"
    // if (Arrays.binarySearch(toSearch, each) != -1) {
    // results.add(Integer.valueOf(each));
    // }
    // }
    // return toArray(results);
    // }// method
    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /*
     * Compare two lists of integers and return the number of elements in common.
     */
    public static int intersection(List<Integer> list1, List<Integer> list2) {
        // Iterate over the shortest O(n)
        // Create hash map of the longest
        int size1, size2, count;
        List<Integer> toIterate;
        HashSet<Integer> hash;
        size1 = list1.size();
        size2 = list2.size();
        count = 0;
        if (size1 > size2) {
            hash = new HashSet<Integer>(list2);
            toIterate = list1;
        } else {
            hash = new HashSet<Integer>(list1);
            toIterate = list2;
        }
        for (Integer each : toIterate) {
            if (hash.contains(each)) {
                count++;
            }// end if
        }// end for
        return count;
    }// end method

    public static boolean[] lessThanOrEqualTo(double[] input, double value) {
        int length = input.length;
        boolean result[] = new boolean[length];
        for (int i = 0; i < length; ++i) {
            result[i] = (input[i] <= value) ? true : false;
        }
        return result;
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    // Return the LARGEST value from each column in the input matrix
    public static double[] max(double[][] input) {
        int rows = input.length;
        int cols = input[0].length;
        double[] maximums = new double[cols];
        // Not memory alignment friendly. :-(
        double largest, value;
        for (int j = 0; j < cols; ++j) {
            largest = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < rows; ++i) {
                value = input[i][j];
                largest = Math.max(largest, value);
            }// end for
            maximums[j] = largest;
        }// end for
        return maximums;
    }// end method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    // Return the SMALLEST value from each column in the input matrix
    public static double[] min(double[][] input) {
        int rows = input.length;
        int cols = input[0].length;
        double[] minimums = new double[cols];
        // Not memory alignment friendly. :-(
        double smallest, value;
        for (int j = 0; j < cols; ++j) {
            smallest = Double.POSITIVE_INFINITY;
            for (int i = 0; i < rows; ++i) {
                value = input[i][j];
                smallest = Math.min(smallest, value);
            }// end for
            minimums[j] = smallest;
        }// end for
        return minimums;
    }// end method

    public static int numDims(Instances data) {
        int temp = data.numAttributes();
        return hasClassAttribute(data) ? temp - 1 : temp;
    }

    public static ArrayList<Cluster> setToFullsapce(List<Cluster> clusters) {
        // Return a list of cluster objects where all the subspaces have been set to the full space.
        // These clusters are only useful when evaluating the CE or RNIA metrics.
        // Copy the objects so the original clusters are not mutated.
        ArrayList<Cluster> newClusters = new ArrayList<Cluster>();
        Cluster temp;
        for (Cluster each : clusters) {
            temp = new Cluster(each.m_subspace.clone(), new ArrayList<Integer>(each.m_objects));
            for (int i = 0; i < temp.m_subspace.length; ++i) {
                temp.m_subspace[i] = true;
            }// for
            newClusters.add(temp);
        }// for
        return newClusters;
    }// method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public static double[] subtract(double[] minuend, double[] subtrahend) {
        /*
         * PRECONDITIONS: minuend and subtrahend are same length.
         * 
         * c - b = a minuend (c) - subtrahend (b) = difference (a).
         */
        int size = minuend.length;
        double[] difference = new double[size];
        for (int i = 0; i < size; ++i) {
            difference[i] = minuend[i] - subtrahend[i];
        }// for
        return difference;
    }// method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /*
     * Convert a list of integers to an array of primitive integers.
     */
    public static int[] toArray(List<Integer> input) {
        int size = input.size();
        int array[] = new int[size];
        for (int i = 0; i < size; ++i) {
            array[i] = input.get(i).intValue();
        }// end for
        return array;
    }// end method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public static ArrayList<Integer> toList(int input[]) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int value : input) {
            list.add(value);
        }
        return list;
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /*
     * Convert a list of Strings to an array of Strings.
     */
    public static String[] toStringArray(List<String> input) {
        int size = input.size();
        String array[] = new String[size];
        for (int i = 0; i < size; ++i) {
            array[i] = input.get(i);
        }// end for
        return array;
    }

    /**
     * 
     * @param inst
     *            The set of instances to remove the class label from.
     * @return A set of instances sans class label.
     */
    public static Instances removeClassAttribute(Instances inst) {
        Remove af = new Remove();
        Instances retI = null;
        try {
            if (inst.classIndex() < 0) {
                retI = inst;
            } else {
                af.setAttributeIndices("" + (inst.classIndex() + 1));
                af.setInvertSelection(false);
                af.setInputFormat(inst);
                retI = Filter.useFilter(inst, af);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retI;
    }// method
}// end class
