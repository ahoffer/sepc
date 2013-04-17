package weka.subspaceClusterer;

import i9.subspace.base.Cluster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

public class ResultsWriter {
    static String clustererExpKey = "experiment_ID";
    static String clustererNameKey = "algorithm";
    static String dataNameKey = "dataset";
    static String extension = ".csv";
    static int keyFieldWidth = 6;
    static char separator = ',';
    static char spacer = ',';

    static public String separatedPath(String string) {
        if (string.charAt(string.length() - 1) != File.separatorChar) {
            string += File.separator;
        }
        return string;

    }

    /* The name of a measure or parameter (key) and either the measurement or the value of the parameter */
    TreeMap<String, String> output = new TreeMap<String, String>();

    String path;

    File getFile(String name) {

        return new File(getPath() + name + "_" + padZeroesLeft(getKey(), keyFieldWidth) + extension);
    }

    String getKey() {

        return output.get(clustererExpKey);
    }

    CsvListWriter getListWriter(String name) throws IOException {

        FileWriter fw = new FileWriter(getFile(name));
        return new CsvListWriter(fw, new CsvPreference.Builder('"', separator, "\n").build());
    }

    String getPath() {
        return separatedPath(path);
    }

    TreeMap<String, String> getRecord(Cluster cluster) {
        TreeMap<String, String> map = new TreeMap<String, String>();
        StringBuffer subspace = new StringBuffer();
        StringBuffer objs = new StringBuffer();

        // Subspace
        // subspace.append('[');
        for (boolean each : cluster.m_subspace) {
            subspace.append(each ? '1' : '0');
            subspace.append(spacer);
        }
        // Remove last punctuation mark
        subspace.deleteCharAt(subspace.length() - 1);
        // subspace.append(']');
        map.put("SUBSPACE", subspace.toString());

        // Cardinality of object set
        map.put("CARDINALITY", ntoa(cluster.m_objects.size()));

        // Indexes of the object set
        objs.append('[');
        for (Integer each : cluster.m_objects) {
            objs.append(each);
            objs.append(spacer);
        }
        // Remove last punctuation mark
        objs.deleteCharAt(objs.length() - 1);
        objs.append(']');
        map.put("OBJECTS", (objs.toString()));

        return map;
    }

    TreeMap<String, String> getRecord(MoccaCluster cluster) {
        TreeMap<String, String> map = new TreeMap<String, String>();

        // If MOCCA cluster, add in quality metric for the cluster
        map.put("QUALITY", (ntoa(cluster.quality)));

        // If MOCCA cluster, add in number of congregating dimensions
        map.put("NUM_CONGR_DIMS", ntoa(cluster.getNumCongregatingDims()));

        // Add generic cluster information
        map.putAll(getRecord((Cluster) cluster));
        return map;
    }

    String ntoa(Double val) {
        return String.format("%f", val);
    }

    String ntoa(int val) {
        return String.format("%d", val);
    }

    String padZeroesLeft(String str, int fieldWidth) {

        StringBuilder sb = new StringBuilder();

        for (int toPrepend = fieldWidth - str.length(); toPrepend > 0; toPrepend--) {
            sb.append('0');
        }

        sb.append(str);
        return sb.toString();
    }

    public void put(String name, Double value) {
        output.put(name, ntoa(value));
    }

    public void setClusterer(SubspaceClusterer clusterer) {

        // Set name of algorithm/subspace clusterer
        output.put(clustererNameKey, clusterer.getName());

        StringTokenizer st = new StringTokenizer(clusterer.getParameterString(), ";");
        while (st.hasMoreTokens()) {
            // paraters in form "param_name=param_value"
            String[] strings = st.nextToken().split("=");
            output.put(strings[0], strings[1]);
        }

    }

    public void setDataName(String name) {
        output.put(dataNameKey, name);
    }

    public void setKey(String key) {
        output.put(clustererExpKey, key);
    }

    public void setPath(String path) {
        verifyPath(path);
        this.path = path;
    }

    void verifyPath(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (!dir.exists()) {
            Exception e = new Exception();
            System.err.printf("Could not access directory %s\n", path);
            e.printStackTrace();
            System.exit(-1);
        }
    }// method

    public void writeClusters(ArrayList<Cluster> clusters) throws Exception {

        /*
         * Screw you Java. You're dispatch model is a griefer.
         * 
         * TODO: When news algorithms are exlored, the clusters will not be MoccaCluster. Wrap the cast in try/catch to
         * abort illegal attempt to cast a Cluster to a MoccaCluster
         */

        // Protect against empty clusters
        if (clusters.isEmpty()) {
            return;
        }

        MoccaCluster first = (MoccaCluster) clusters.get(0);
        CsvListWriter writer = getListWriter("CLSTR");

        // Go through some silly hoops to get the header
        TreeMap<String, String> map = getRecord(first);
        String[] header = map.navigableKeySet().toArray(new String[0]);
        writer.writeHeader(header);

        // Write the data
        for (Cluster each : clusters) {
            map = getRecord((MoccaCluster) each);
            writer.write(map.values().toArray(new String[0]));
        }

        // Shutdown IO
        writer.close();
    }

    public void writeResults() throws Exception {

        CsvListWriter writer = getListWriter("RSLT");
        String[] header = output.navigableKeySet().toArray(new String[0]);
        writer.writeHeader(header);
        writer.write(output.values().toArray(new String[0]));
        writer.close();
    }

    public void put(String name, int value) {

        // Convenience wrapper
        put(name, (double) value);

    }// method

}// class
