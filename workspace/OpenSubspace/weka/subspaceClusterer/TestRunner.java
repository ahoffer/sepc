package weka.subspaceClusterer;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestRunner {

    static String metrics, subspaceClutererName, outputPath, dataSetFilename, command;

    public static void main(String[] args) {

        metrics = "F1Measure:Accuracy:Entropy";
        subspaceClutererName = "Mocca";
        outputPath = "C:\\results";
        dataSetFilename = "breast.arff";
        try {
            run();
        } catch (InterruptedException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // ArrayList<String> dataSetFilenames;

    }

    static void run() throws InterruptedException, IOException {
        double gamma, alpha, beta, epsilon, width, subspaceOverlapThreshold, objectOverlapThreshold;
        int experimentLabel, gammaSteps, sSteps, oSteps, alphaSteps, betaSteps, widthSteps, maxiter;

        maxiter = (int) 1e4;
        gammaSteps = 10;
        sSteps = 3;
        oSteps = 3;
        alphaSteps = 3;
        betaSteps = 5;
        widthSteps = 5;
        experimentLabel = 1;

        for (int sStep = 0; sStep <= sSteps; sStep++) {
            subspaceOverlapThreshold = sStep / ((double) sSteps);

            for (int oStep = 0; oStep <= oSteps; oStep++) {
                objectOverlapThreshold = oStep / ((double) oSteps);
                alpha = 0;

                for (int alphaStep = 0; alphaStep <= alphaSteps; alphaStep++) {
                    alpha = alpha + 0.1;
                    beta = 0;

                    for (int betaStep = 0; betaStep <= betaSteps; betaStep++) {
                        beta = beta + 0.1;
                        gamma = 0;

                        for (int gammaStep = 0; gammaStep <= gammaSteps; gammaStep++) {
                            gamma = gammaStep / ((double) gammaSteps);
                            width = .05;
                            for (int widthStep = 0; widthStep <= widthSteps; widthStep++) {
                                width = width + 0.5 * widthStep;

                                String argString = String
                                        .format("-label %d -M %s -sc %s -t %s -a %f -b %f -g %f -i %f -s %f -w %f -maxiter %d -path %s -c last",
                                                experimentLabel, metrics, subspaceClutererName, dataSetFilename, alpha,
                                                beta, gamma, objectOverlapThreshold, subspaceOverlapThreshold, width,
                                                maxiter, outputPath);

                                // DEBUG - Do not delete the next line. It is handy for debugging.
                                // System.out.println(args);

                                ArrayList<String> commands = new ArrayList<String>();
                                commands.add("javaw.exe");
                                commands.add("-cp");
                                commands.add("\\Users\\ahoffer\\Documents\\GitHub\\sepc\\workspace\\OpenSubspace\\lib\\*;");
                                commands.add("weka.subspaceClusterer.MySubspaceClusterEvaluation");

                                /*
                                 * TODO: Splitting the command line by spaces is a hack. This will BREAK if there are
                                 * spaces in the filenames or paths
                                 */
                                String[] args = argString.split(" ");
                                commands.addAll(Arrays.asList(args));

                                // Fork an OS process
                                ProcessBuilder pb = null;
                                Process proc = null;
                                pb = new ProcessBuilder(commands);
                                pb.inheritIO();
                                proc = pb.start();

                                experimentLabel++;
                            }
                        }
                    }
                }
            }

        }
    }
}