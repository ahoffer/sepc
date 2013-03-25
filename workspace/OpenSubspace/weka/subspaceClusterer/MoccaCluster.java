package weka.subspaceClusterer;

import java.util.List;

import i9.subspace.base.Cluster;

public class MoccaCluster extends Cluster {

	private static final long serialVersionUID = 1L;
	double quality;
	int numDims;

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	public MoccaCluster(boolean[] subspace, List<Integer> objects, int numDims, double beta) {
		super(subspace, objects);
		this.numDims = numDims;
		this.quality = quality(getCardinality(), numDims, beta);
	}

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	@Override
	public String toStringWeka() {
		// TODO Auto-generated method stub
		return String.format("%,4.2f", quality) + " " + (super.toStringWeka());
	}// end method

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	/*
	 * Normalized overlap is defined as the number of points two clusters have
	 * in common, divided by the cardinality of the smaller cluster.
	 */
	public double getClusterOverlapScore(MoccaCluster otherCluster) {
		int overlap, smallerCardinality;
		double normalizedOverlap;

		overlap = MoccaUtils.intersection(m_objects, otherCluster.m_objects);
		smallerCardinality = Math.min(getCardinality(), otherCluster.getCardinality());
		normalizedOverlap = overlap / smallerCardinality;
		return normalizedOverlap;
	}

	public double getSubspaceOverlapScore(MoccaCluster otherCluster) {
		// PRECONDITION: Length of subspace arrays must be identical
		int overlap, smallerNumDims;
		double normalizedOverlap;

		overlap = 0;
		for (int i = 0; i < m_subspace.length; ++i) {
			if (m_subspace[i] && otherCluster.m_subspace[i]) {
				overlap++;
			}
		}// for

		smallerNumDims = Math.min(numDims, otherCluster.numDims);
		normalizedOverlap = overlap / smallerNumDims;
		return normalizedOverlap;
	}// method

	int getCardinality() {
		return m_objects.size();
	}

	/*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/

	public static double quality(int cardinality, int numCongregatingDims, double beta) {

		return cardinality * Math.pow(1.0 / beta, numCongregatingDims);
	}

}// end class
