package weka.subspaceClusterer;

import java.util.ArrayList;
import java.util.List;

public class MoccaExperimentGenerator {
    public static void main(String[] args) {
    }

    // TODO: Use JSON to read write all experiment parameters.
    static String[] pca = { "y", "n" };
    static int[] minDiscrimSetSize = { 2, 3, 4 };
    static double[] alphas = { 0.01 };
    static double[] widths = { 0.05, 0.1, 0.2 };
    static double[] qualityThresholds = { 0, 1e4 };
    static double[] betas = { 0.1, 0.2, 0.4 };
    static double[] epsilons = { 0.001 };
    static double[] subspaceSimilarityThresholds = { -1 };
    static double[] clusterSimilarityThresholds = { 0, 0.1, 0.2 };
    static int maxiter = 1000000;
    static String subspaceClutererName = "Mocca";

    public static List<List<String>> getArgLines() {
        ArrayList<List<String>> argLines = new ArrayList<List<String>>();
        ArrayList<String> args;
        for (double e : epsilons) {
            for (double a : alphas) {
                for (double b : betas) {
                    for (double s : subspaceSimilarityThresholds) {
                        for (double i : clusterSimilarityThresholds) {
                            for (String value : pca) {
                                for (int minSetSize : minDiscrimSetSize) {
                                    for (double w : widths) {
                                        for (double q : qualityThresholds) {
                                            args = new ArrayList<String>();
                                            args.add("-sc");
                                            args.add(subspaceClutererName);
                                            args.add("-a");
                                            args.add("" + a);
                                            args.add("-b");
                                            args.add("" + b);
                                            args.add("-e");
                                            args.add("" + e);
                                            args.add("-pca");
                                            args.add("" + value);
                                            args.add("-i");
                                            args.add("" + i);
                                            args.add("-s");
                                            args.add("" + s);
                                            args.add("-w");
                                            args.add("" + w);
                                            args.add("-maxiter");
                                            args.add("" + maxiter);
                                            args.add("-mindiscrim");
                                            args.add("" + minSetSize);
                                            args.add("-minqual");
                                            args.add("" + q);
                                            argLines.add(args);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return argLines;
    }
}// class
