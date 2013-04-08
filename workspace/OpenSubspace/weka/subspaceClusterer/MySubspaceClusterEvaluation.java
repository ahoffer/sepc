package weka.subspaceClusterer;

import i9.subspace.base.Cluster;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;

import weka.clusterquality.ClusterQualityMeasure;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class MySubspaceClusterEvaluation extends SubspaceClusterEvaluation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if (args.length == 0) {
				throw new Exception("The first argument must be the name of a " + "clusterer");
			}

			String ClustererString = args[0];
			args[0] = "";
			SubspaceClusterer newClusterer = SubspaceClusterer.forName(ClustererString, null);
			// System.out.println(evaluateClusterer(newClusterer, args));
			evaluateClusterer(newClusterer, args);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}// method

	/**
	 * Evaluates a clusterer with the options given in an array of strings. It
	 * takes the string indicated by "-t" as training file, the string indicated
	 * by "-T" as test file. If the test file is missing, a stratified ten-fold
	 * cross-validation is performed (distribution clusterers only). Using "-x"
	 * you can change the number of folds to be used, and using "-s" the random
	 * seed. If the "-p" option is present it outputs the classification for
	 * each test instance. If you provide the name of an object file using "-l",
	 * a clusterer will be loaded from the given file. If you provide the name
	 * of an object file using "-d", the clusterer built from the training data
	 * will be saved to the given file.
	 * 
	 * @param clusterer
	 *            machine learning clusterer
	 * @param options
	 *            the array of string containing the options
	 * @throws Exception
	 *             if model could not be evaluated successfully
	 * @return a string describing the results
	 */
	public static String evaluateClusterer(SubspaceClusterer clusterer, String[] options) throws Exception {

		Instances train = null;
		String trainFileName;
		String measureOptionString = null;
		String[] savedOptions = null;
		StringBuffer text = new StringBuffer();
		int theClass = -1; // class based evaluation of clustering
		DataSource source = null;
		try {
			if (Utils.getFlag('h', options)) {
				throw new Exception("Help requested.");
			}

			// Get basic options (options the same for all clusterers
			trainFileName = Utils.getOption('t', options);
			if (trainFileName.length() == 0)
				throw new Exception("No input file, use -t");
			measureOptionString = Utils.getOption('M', options);

		} catch (Exception e) {
			throw new Exception('\n' + e.getMessage() + makeOptionString(clusterer));
		}

		try {
			if (trainFileName.length() != 0) {
				source = new DataSource(trainFileName);
				train = source.getStructure();

				String classString = Utils.getOption('c', options);
				if (classString.length() != 0) {
					if (classString.compareTo("last") == 0)
						theClass = train.numAttributes();
					else if (classString.compareTo("first") == 0)
						theClass = 1;
					else
						theClass = Integer.parseInt(classString);

				} else {
					// if the dataset defines a class attribute, use it
					if (train.classIndex() != -1) {
						theClass = train.classIndex() + 1;
						System.err.println("Note: using class attribute from dataset, i.e., attribute #" + theClass);
					}
				}

				if (theClass != -1) {
					if (theClass < 1 || theClass > train.numAttributes())
						throw new Exception("Class is out of range!");

					if (!train.attribute(theClass - 1).isNominal())
						throw new Exception("Class must be nominal!");

					train.setClassIndex(theClass - 1);
				}
			}

		} catch (Exception e) {
			throw new Exception("ClusterEvaluation: " + e.getMessage() + '.');
		}

		// Save options
		if (options != null) {
			savedOptions = new String[options.length];
			System.arraycopy(options, 0, savedOptions, 0, options.length);
		}

		// Set options for clusterer
		if (clusterer instanceof OptionHandler)
			((OptionHandler) clusterer).setOptions(options);

		Utils.checkForRemainingOptions(options);

		Instances inst = source.getDataSet();

		text.append("Scheme: " + clusterer.getName() + " ");
		text.append(clusterer.getParameterString());
		text.append("\n");
		text.append("Relation: " + inst.relationName() + "\n");

		// Build the clusterer
		if (theClass == -1) {
			if (measureOptionString != "") {
				System.out
						.println("You need to set the class attribute (-c) for evaluation measures to be calculated! ");
			}
			clusterer.buildSubspaceClusterer(inst);
			text.append(clusterer.toString());
		} else {
			// remove class
			Remove removeClass = new Remove();
			removeClass.setAttributeIndices("" + theClass);
			removeClass.setInvertSelection(false);
			removeClass.setInputFormat(train);
			Instances clusterTrain = Filter.useFilter(inst, removeClass);

			// cluster
			clusterer.buildSubspaceClusterer(clusterTrain);
			text.append(clusterer.toString());

			// evaluation
			SubspaceClusterEvaluation ce = new SubspaceClusterEvaluation();
			ce.setClusterer(clusterer);
			Instances evalInst = source.getDataSet();
			evalInst.setClassIndex(theClass - 1);

			// TODO set measures somehow
			ArrayList<ClusterQualityMeasure> measures = null;

			if (measureOptionString != "") {
				measures = getMeasuresByOptions(measureOptionString);
			}

			ArrayList<Cluster> trueClusters = null;
			File trueClusterFile = null;
			StringBuffer evalOutput = evaluateClustersQuality(clusterer, evalInst, measures, trueClusters,
					trueClusterFile);

			// text.append("\n\n=== Eval stats for training data ===\n\n"
			// + ce.clusterResultsToString());
			text.append(evalOutput);

			// clusterer.save(evalInst,ce);

		}

		return text.toString();
	}

	/**
	 * Make up the help string giving all the command line options
	 * 
	 * @param clusterer
	 *            the clusterer to include options for
	 * @return a string detailing the valid command line options
	 */
	private static String makeOptionString(SubspaceClusterer clusterer) {
		StringBuffer optionsText = new StringBuffer("");
		// General options
		optionsText.append("\n\nGeneral options:\n\n");

		optionsText.append("-t <name of input file>\n");
		optionsText.append("\tSets input file.\n");

		optionsText.append("-M <cluster quality measures to evaluate>\n");
		optionsText.append("\tsubspace cluster quality measures in package weka.clusterquality\n");
		optionsText.append("\tseparate measures with ':' e.g. -M F1Measure:Entropy:CE \n");

		optionsText.append("-c <class index>\n");
		optionsText.append("\tSet class attribute, starting with 1. If supplied, class is ignored");
		optionsText.append("\n\tduring clustering but is used in a classes to");
		optionsText.append("\n\tclusters evaluation.\n");

		// Get scheme-specific options
		if (clusterer instanceof OptionHandler) {
			optionsText.append("\nOptions specific to " + clusterer.getClass().getName() + ":\n\n");
			Enumeration enu = ((OptionHandler) clusterer).listOptions();

			while (enu.hasMoreElements()) {
				Option option = (Option) enu.nextElement();
				optionsText.append(option.synopsis() + '\n');
				optionsText.append(option.description() + "\n");
			}
		}

		return optionsText.toString();
	}

}// class
