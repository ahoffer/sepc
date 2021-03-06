package weka.subspaceClusterer;

import weka.core.Instance;
import weka.core.Instances;
import Jama.Matrix;

public class MatrixUtils {
    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /*
     * Mean-center the columns of a matrix. This data must be mean-centered for proper PCA
     */
    public static Matrix center(Matrix input, Matrix columnMeans) {
        int rows = input.getRowDimension();
        int cols = input.getColumnDimension();
        Matrix mat = new Matrix(rows, cols);
        double val, mean;
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                val = input.get(i, j);
                mean = columnMeans.get(0, j);
                mat.set(i, j, val - mean);
            }// end for
        }// end for
        return mat;
    }// end method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /*
     * Mean-center the columns of a matrix if the mean of each attribute/dim is not known.
     */
    public static Matrix center(Matrix input) {
        return center(input, columnMeans(input));
    }// end method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public static Matrix columnMeans(Matrix input) {
        int cols = input.getColumnDimension();
        int rows = input.getRowDimension();
        Matrix rowVector = new Matrix(1, cols);
        double sum;
        for (int j = 0; j < cols; ++j) {
            sum = 0;
            for (int i = 0; i < rows; ++i) {
                sum += input.get(i, j);
            }// end for
            rowVector.set(0, j, sum / rows);
        }// end for
        return rowVector;
    }// end method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /*
     * Given an (m x n) matrix, return the (n x n) covariance matrix
     */
    public static Matrix covariance(Matrix input) {
        // PRECONDITION: MATRIX MUST BE MEAN-CENTERED
        // Allocate the covariance matrix
        int rows = input.getRowDimension();
        int cols = input.getColumnDimension();
        double inputArray[][] = input.getArrayCopy();
        double outputArray[][] = new double[cols][cols];
        // Compute the covariance matrix
        double covar_ij;
        double[] colVec1, colVec2;
        for (int i = 0; i < cols; ++i) {
            for (int j = i; j < cols; ++j) {
                colVec1 = copyColumn(inputArray, i);
                colVec2 = copyColumn(inputArray, j);
                covar_ij = dotProduct(colVec1, colVec2) / (rows - 1);
                outputArray[i][j] = covar_ij;
                outputArray[j][i] = covar_ij;
            }// end for
        }// end for
        Matrix covMatrix = new Matrix(outputArray);
        return covMatrix;
    }// end method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /*
     * Return the dot product of two column vectors.
     */
    public static double dotProduct(double[] colVec1, double[] colVec2) {
        double sum = 0;
        int rows1 = colVec1.length;
        int rows2 = colVec2.length;
        if (rows1 != rows2) {
            System.err.println("Dimension error in dotProduct.");
        }// end if
        for (int i = 0; i < rows1; ++i) {
            sum += colVec1[i] * colVec2[i];
        }// end for
        return sum;
    }// end method

    public static Instances toInstances(Instances template, Matrix values) {
        double array[][] = values.getArray();
        weka.core.Instances output = new Instances(template, template.numInstances());
        for (int i = 0; i < values.getRowDimension(); ++i) {
            for (int j = 0; j < values.getColumnDimension(); ++j) {
                output.instance(i).setValue(j, array[i][j]);
            }// end for
        }// end for
        return output;
    }

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /*
     * Convert the data in an Instances object to a Matrix object.
     */
    public static Matrix toMatrix(Instances input) {
        int cols = input.numAttributes();
        int rows = input.numInstances();
        Matrix mat = new Matrix(rows, cols);
        for (int i = 0; i < rows; ++i) {
            Instance inst = input.instance(i);
            for (int j = 0; j < cols; ++j) {
                mat.set(i, j, inst.value(j));
            }// end for
        }// end for
        return mat;
    }// end method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /*
     * Return a sub-matrix with the given rows
     */
    public static Matrix getRowsByIndex(Matrix input, int[] indexes) {
        int cols = input.getColumnDimension();
        return input.getMatrix(indexes, 0, cols - 1);
    }// end method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    // Return the largest value from each column
    public static Matrix max(Matrix input) {
        int cols = input.getColumnDimension();
        Matrix maxs = new Matrix(1, cols);
        double largest, val;
        for (int j = 0; j < cols; ++j) {
            largest = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < input.getRowDimension(); ++i) {
                val = input.get(i, j);
                if (val > largest) {
                    largest = val;
                }// end if
                maxs.set(0, j, largest);
            }// end for
        }// end for
        return maxs;
    }// end method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    /*
     * Find the smallest value from each column of a (m x n) matrix. ----------- Return a (1 x n) vector.
     */
    public static Matrix min(Matrix input) {
        int cols = input.getColumnDimension();
        Matrix mins = new Matrix(1, cols);
        double smallest, val;
        for (int j = 0; j < cols; ++j) {
            smallest = Double.POSITIVE_INFINITY;
            for (int i = 0; i < input.getRowDimension(); ++i) {
                val = input.get(i, j);
                if (val < smallest) {
                    smallest = val;
                }// end if
                mins.set(0, j, smallest);
            }// end for
        }// end for
        return mins;
    }// end method

    /*-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----*/
    public static Matrix fliplr(Matrix input) {
        // For some reason, Matrix.setMatrix call fails. Code up my own flip
        // left-right
        int cols, rows, colIdx;
        double value;
        cols = input.getColumnDimension();
        rows = input.getRowDimension();
        Matrix flipColumns = new Matrix(rows, cols);
        for (int j = 0; j < cols; ++j) {
            colIdx = cols - 1 - j;
            for (int i = 0; i < rows; ++i) {
                value = input.get(i, colIdx);
                flipColumns.set(i, j, value);
            }// for
        }// for
        return flipColumns;
    }// method

    public static double[] copyColumn(double[][] input, int j) {
        int rows = input.length;
        double[] output = new double[rows];
        for (int i = 0; i < rows; ++i) {
            output[i] = input[i][j];
        }
        return output;
    }// method

    public static Matrix concat(Matrix A, Matrix B) {
        int rows = A.getRowDimension();
        if (rows != B.getRowDimension()) {
            return null;
        }
        int colsA = A.getColumnDimension();
        int colsB = B.getColumnDimension();
        Matrix mat = new Matrix(rows, colsA + colsB);
        mat.setMatrix(0, rows - 1, 0, colsA - 1, A);
        mat.setMatrix(0, rows - 1, colsA, colsA + colsB - 1, B);
        return mat;
    }// end method
}// end class
