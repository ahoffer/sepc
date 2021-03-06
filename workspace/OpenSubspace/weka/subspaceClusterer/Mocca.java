package weka.subspaceClusterer;

import i9.subspace.base.Cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import Jama.Matrix;

public class Mocca extends SubspaceClusterer implements OptionHandler {
    private static final long serialVersionUID = 5624336775621682596L;

    public static void main(String[] argv) throws IOException {
        runSubspaceClusterer(new Mocca(), argv);
    }

    List<Cluster> clusters = new ArrayList<Cluster>();
    double clusterSimilarityThreshold, subspaceSimilarityThreshold, width, epsilon, alpha, beta;
    double qualityThreshold = 0; // Keep all clusters by default.
    Instances instances;
    int maxiter, numDims, numInstances;
    int minDiscrimSetSize = 2;
    boolean usePca = false;

    private void add(MoccaCluster newCluster) {
        double subspaceSimilarity, clusterSimilarity;
        if (newCluster.getQuality() < getQualityThreshold()) {
            // Do not keep cluster
            return;
        }
        /*
         * Modifying a list invalidates any iterator objects created from it. Do not remove objects while iterating over
         * the list. Add them to a "remove items list" and remove them before exiting.
         */
        List<Cluster> removeList = new ArrayList<Cluster>();
        for (Cluster otherCluster : clusters) {
            subspaceSimilarity = newCluster.getSubspaceSimilarity((MoccaCluster) otherCluster);
            /*
             * Test if the clusters' subspaces are similar enought to be considered the same. If threshold is 0, all
             * subspaces with at least one dimension in common are considered to be in the same subspace. If threshold
             * is 1, only subspaces with exactly the same dimensions are considered to be in the same subspace. If the
             * threshold is negative, all clusters are considered to be in the same subspace. ONLY clusters with the same
             * subspace will be checked for duplicate object sets.
             */
            if (subspaceSimilarity > subspaceSimilarityThreshold || MoccaUtils.equalToOne(subspaceSimilarity)) {
                /*
                 * OK, the cluster's subsapces are considered the same. Test if the clusters' contain enough similar
                 * objects to be considered the same. If threshold is 0 (permissive), clusters with at least one object
                 * in common are considered to be the same. If threshold is 1 (strict), clusters are considered the same
                 * iff every object in the smaller cluster is also part of the larger cluster.
                 */
                clusterSimilarity = newCluster.getClusterSimilarity((MoccaCluster) otherCluster);
                if (clusterSimilarity > clusterSimilarityThreshold || MoccaUtils.equalToOne(clusterSimilarity)) {
                    /*
                     * The clusters' subspaces and sets of objects are similar enought that we consider them to be the
                     * same cluster. Keep the cluster with the highest quality
                     */
                    if (newCluster.quality > ((MoccaCluster) otherCluster).quality) {
                        // Keep new cluster, remove other cluster
                        removeList.add(otherCluster);
                    } else {
                        // Do not keep the new cluster.
                        return;
                    }
                }// inner if
            }// outer if
        }// for
        clusters.removeAll(removeList);
        /*
         * There is no sufficiently similar cluster with higher quality. Add this cluster.
         */
        clusters.add(newCluster);
    }

    @Override
    public void buildSubspaceClusterer(Instances data) throws Exception {
        this.instances = MoccaUtils.removeClassAttribute(data);
        numDims = MoccaUtils.numDims(data);
        numInstances = data.numInstances();
        // Do it!
        run();
        // Sort quality from high to low.
        Collections.sort(clusters, new Comparator<Cluster>() {
            public int compare(Cluster a, Cluster b) {
                return Double.valueOf(((MoccaCluster) b).quality).compareTo(((MoccaCluster) a).quality);
            }
        });
        // Set results
        setSubspaceClustering(clusters);
        // Print results
        toString();
    }

    public double getAlpha() {
        return alpha;
    }

    public double getBeta() {
        return beta;
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public double getClusterSimilarityThreshold() {
        return clusterSimilarityThreshold;
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    private ArrayList<Integer> getCongregatingObjects(Matrix pointsToCluster, MoccaSubspace subspaceObj) {
        // Sanity Check - No longer relevant.
        // if (subspaceObj.getNumDimsDiscrimObjs() != pointsToCluster.getColumnDimension()) {
        // System.err.println("Constraint violation. Debug here");
        // System.exit(-2);
        // }
        ArrayList<Integer> pointsIndexes = new ArrayList<Integer>();
        double[][] points = pointsToCluster.getArray();
        boolean[] subspace = subspaceObj.getSubspace();
        double[] lowerBounds = subspaceObj.getLower();
        double[] upperBounds = subspaceObj.getUpper();
        congregate: for (int i = 0; i < numInstances; ++i) {
            // Get the actual point
            double[] point = points[i];
            // Check to see if the point is in the cluster
            for (int j = 0; j < subspaceObj.getNumDimsDiscrimObjs(); ++j) {
                /*
                 * Only check bounds if the cluster congregates in this dimension. We don't care about dimensions that
                 * are not part of the subspace
                 */
                if (subspace[j]) {
                    if (point[j] > upperBounds[j] || point[j] < lowerBounds[j]) {
                        /*
                         * Point is not inside the hyper volume for congregating dimension j. Therefore the point is not
                         * part of the cluster.
                         * 
                         * Return to top of loop to examine next object in the data set.
                         */
                        continue congregate;
                    }// end if for bounds check
                }// end if for subspace check
            }// end for
             // The point is part of the cluster
            pointsIndexes.add(Integer.valueOf(i));
        }// end for
        return pointsIndexes;
    }// method

    public int getDiscrimSetSize() {
        // Need at least two discriminating points to find a cluster
        return Math.max(getMinDiscrimSetSize(), getEstimatedDiscrimSetSize());
    }

    public double getEpsilon() {
        return epsilon;
    }

    public int getEstimatedDiscrimSetSize() {
        double s_est = Math.log10(numDims / Math.log(4)) / Math.log10(1 / beta);
        int temp = Utils.round(s_est);
        return temp;
    }

    public int getEstimatedNumTrials() {
        double d = numDims;
        double ln4 = Math.log(4);
        double log10alpha = Math.log10(alpha);
        double log10beta = Math.log10(beta);
        double est = 1 + 4 / alpha * Math.pow(d / ln4, log10alpha / log10beta) * Math.log(1 / epsilon);
        int numTrials = Utils.round(est);
        return numTrials;
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public int getMaxiter() {
        return maxiter;
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public int getMinDiscrimSetSize() {
        return minDiscrimSetSize;
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    @Override
    public String getName() {
        return "MOCCA";
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public int getNumTrials() {
        if (MoccaUtils.isZero(getMaxiter())) {
            return getEstimatedNumTrials();
        } else {
            return Math.min(getEstimatedNumTrials(), getMaxiter());
        }
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /**
     * Gets the current option settings for the OptionHandler.
     * 
     * @return String[] The list of current option settings as an array of strings
     */
    public String[] getOptions() {
        ArrayList<String> options = new ArrayList<String>();
        options.add("-a");
        options.add("" + alpha);
        options.add("-b");
        options.add("" + beta);
        options.add("-e");
        options.add("" + epsilon);
        options.add("-s");
        options.add("" + subspaceSimilarityThreshold);
        options.add("-i");
        options.add("" + clusterSimilarityThreshold);
        options.add("-w");
        options.add("" + width);
        options.add("-maxiter");
        options.add("" + maxiter);
        options.add("-pca");
        options.add("" + getPca());
        options.add("-mindiscrim");
        options.add("" + getMinDiscrimSetSize());
        options.add("-minqual");
        options.add("" + getQualityThreshold());
        return MoccaUtils.toStringArray(options);
    }

    @Override
    public String getParameterString() {
        return "alpha=" + alpha + ";beta=" + beta + ";epsilon=" + epsilon + ";subspace similarity threshold="
                + subspaceSimilarityThreshold + ";cluster similarity threshold=" + clusterSimilarityThreshold
                + ";width=" + width + ";pca=" + getPca() + ";maxiter=" + maxiter + ";minqual=" + getQualityThreshold();
    }

    String getPca() {
        if (usePca) {
            return "y";
        }
        return "n";
    }

    public double getQualityThreshold() {
        return qualityThreshold;
    }

    public double getSubspaceSimilarityThreshold() {
        return subspaceSimilarityThreshold;
    }

    public double getWidth() {
        return width;
    }

    public String globalInfo() {
        return "Monte Carlo Cluster Analysis (MOCCA)";
    }

    boolean isPcaAssisted() {
        return usePca;
    }

    /*
     * Returns an enumeration of all the available options.
     * 
     * @return Enumeration An enumeration of all available options.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Enumeration listOptions() {
        Vector vector = new Vector();
        // Option(description, name, numArguments, synopsis)
        vector.addElement(new Option("\talpha", "alpha", 1, "-a <double>"));
        vector.addElement(new Option("\tbeta", "beta", 1, "-b <double>"));
        vector.addElement(new Option("\tepsilon", "epsilon", 1, "-e <double>"));
        vector.addElement(new Option("\tsubspace similarity threshold", "subsapceSimilarityThreshold", 1, "-s <double>"));
        vector.addElement(new Option("\tobject similarity threshold", "clusterSimilarityThreshold", 1, "-i <double>"));
        vector.addElement(new Option("\twidth", "width", 1, "-w <double>"));
        vector.addElement(new Option("\tgamma", "gamma", 1, "-g <double>"));
        vector.addElement(new Option("\tmaximum iteration", "maxiter", 1, "-maxiter <int>"));
        vector.addElement(new Option("\tminimum discriminating set size (default = 2)", "minDiscrimSetSize", 1,
                "-mindiscrim <int>"));
        return vector.elements();
    }

    public void printHeartBeat(int trialNum) {
        // DEBUG PRINT
        if (trialNum % 50000 == 0 && trialNum > 0) {
            System.out.println(String.format("%,d of %,d", trialNum, getNumTrials()));
        }
    }

    void run() throws Exception {
        MoccaSubspace subspace;
        Shuffler shuffler;
        int numCongregatingDims;
        ArrayList<Integer> pointIndexes;
        Pca pca = null;
        shuffler = new Shuffler(numInstances, 1);
        Matrix objects = MatrixUtils.toMatrix(instances);
        Matrix objectsToCluster = objects.copy();
        // LOOP
        for (int k = 0; k < getNumTrials(); k++) {
            printHeartBeat(k);
            // Randomly select discriminating set
            int samples[] = shuffler.next(getDiscrimSetSize());
            Matrix sampleObjects = MatrixUtils.getRowsByIndex(objects, samples);
            if (isPcaAssisted()) {
                // Align data to least significant principle components
                // ********************DEBUG************************
                // System.out.println("Original Points");
                // objects.print(8, 4);
                pca = new Pca(sampleObjects);
                objectsToCluster = pca.rotate(objects);
                // System.out.println("Rotated Points");
                // objectsToCluster.print(8, 4);
            }
            Matrix discrimObjects = MatrixUtils.getRowsByIndex(objectsToCluster, samples);
            // Determine the subspace where the points congregate, if any
            subspace = new MoccaSubspace(discrimObjects.getArray(), width, numDims);
            subspace.computeCongregratingSubspace();
            numCongregatingDims = subspace.getNumCongregatingDims();
            if (numCongregatingDims > 0) {
                // Determine which points are in the cluster
                pointIndexes = getCongregatingObjects(objectsToCluster, subspace);
                // Create cluster object.
                MoccaCluster newCluster = new MoccaCluster(subspace.getSubspace(), pointIndexes, numCongregatingDims,
                        beta);
                if (newCluster.getCardinality() > numInstances) {
                    System.err.println("There are most objects in the clusters than objects!");
                }
                // Add cluster if it meets certain criteria
                add(newCluster);
            }// if
        }// end k trials loop
    }// end method

    public void setAlpha(double alpha) {
        if (alpha > 0.0 && alpha < 1.0)
            this.alpha = alpha;
    }

    public void setBeta(double beta) {
        if (beta > 0.0 && beta < 1.0)
            this.beta = beta;
    }

    public void setClusterSimilarityThreshold(double maxOverlap) {
        if (maxOverlap > 0.0)
            clusterSimilarityThreshold = maxOverlap;
    }

    public void setEpsilon(double epsilon) {
        if (epsilon > 0.0 && epsilon < 1.0)
            this.epsilon = epsilon;
    }

    public void setMaxiter(int d) {
        this.maxiter = d;
    }

    public void setMinDiscrimSetSize(int minDiscrimSetSize) {
        this.minDiscrimSetSize = minDiscrimSetSize;
    }

    public void setOptions(String[] options) throws Exception {
        String optionString = Utils.getOption("a", options);
        if (optionString.length() != 0) {
            setAlpha(Double.parseDouble(optionString));
        }
        optionString = Utils.getOption("b", options);
        if (optionString.length() != 0) {
            setBeta(Double.parseDouble(optionString));
        }
        optionString = Utils.getOption("e", options);
        if (optionString.length() != 0) {
            setEpsilon(Double.parseDouble(optionString));
        }
        optionString = Utils.getOption("s", options);
        if (optionString.length() != 0) {
            setSubspaceSimilarityThreshold(Double.parseDouble(optionString));
        }
        optionString = Utils.getOption("i", options);
        if (optionString.length() != 0) {
            setClusterSimilarityThreshold(Double.parseDouble(optionString));
        }
        optionString = Utils.getOption("w", options);
        if (optionString.length() != 0) {
            setWidth(Double.parseDouble(optionString));
        }
        optionString = Utils.getOption("pca", options);
        if (optionString.length() != 0) {
            setPca(optionString);
        }
        optionString = Utils.getOption("maxiter", options);
        if (optionString.length() != 0) {
            setMaxiter(Integer.parseInt(optionString));
        }
        optionString = Utils.getOption("mindiscrim", options);
        if (optionString.length() != 0) {
            setMinDiscrimSetSize(Integer.parseInt(optionString));
        }
        optionString = Utils.getOption("minqual", options);
        if (optionString.length() != 0) {
            setQualityThreshold(Double.parseDouble(optionString));
        }
    }

    void setPca(String val) {
        usePca = val.equalsIgnoreCase("y");
    }

    public void setQualityThreshold(double qualityThreshold) {
        this.qualityThreshold = qualityThreshold;
    }

    /*
     * Setter
     */
    public void setSubspaceSimilarityThreshold(double maxSimilarity) {
        subspaceSimilarityThreshold = maxSimilarity;
    }

    /*
     * Setter
     */
    public void setWidth(double w) {
        if (w > 0.0)
            this.width = w;
    }
} // end class
