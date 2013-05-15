package weka.filters;

import java.io.File;

import Jama.Matrix;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.subspaceClusterer.MatrixUtils;
import weka.subspaceClusterer.MoccaUtils;

public class Rotate extends SimpleBatchFilter {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public String getRevision() {
        // TODO Auto-generated method stub
        return "A";
    }

    @Override
    protected Instances determineOutputFormat(Instances arg0) throws Exception {
        return arg0;
    }

    @Override
    protected Instances process(Instances arg0) throws Exception {
        double angle = Math.PI / 4;
        Matrix rotMat = new Matrix(2, 2);
        rotMat.set(0, 0, Math.cos(angle));
        rotMat.set(0, 1, -1 * Math.sin(angle));
        rotMat.set(1, 0, Math.sin(angle));
        rotMat.set(1, 1, Math.cos(angle));
        Instances noclass = MoccaUtils.removeClassAttribute(arg0);
        Matrix A = MatrixUtils.toMatrix(noclass);
        return MatrixUtils.toInstances(arg0, A.times(rotMat));
    }

    @Override
    public String globalInfo() {
        return "return rotate some points in 2D space";
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String filename = "C:/Users/ahoffer/Documents/GitHub/edu.uwb.opensubspace/edu.uwb.opensubspace/Databases/synth-normal/D02_k03_N60000.arff";
        DataSource source = null;
        Instances data = null;
        Rotate r = new Rotate();
        ArffSaver saver = new ArffSaver();
        MoccaUtils.testFileReadable(filename);
        try {
            source = new DataSource(filename);
            data = source.getDataSet();
            if (data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);
            r.setInputFormat(data);
            Instances newData = Filter.useFilter(data, r);
            saver.setInstances(newData);
            saver.setFile(new File("C:/Users/ahoffer/Desktop/test.arff"));
            saver.writeBatch();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }// method

    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.enableAllAttributes();
        result.enableAllClasses();
        result.enable(Capability.NO_CLASS); // // filter doesn't need class to be set//
        return result;
    }
}
