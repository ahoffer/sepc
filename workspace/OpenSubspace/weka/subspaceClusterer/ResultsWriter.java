package weka.subspaceClusterer;

import i9.subspace.base.Cluster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

public class ResultsWriter {
    static String clustererExpKey = "EXP";
    static String clustererNameKey = "ALGO";
    static String dataNameKey = "DATA";
    static String extension = ".csv";
    static char nonseparator = ',';
    static char separator = ',';

    String path;

    /* The name of a measure or parameter (key) and either the measurement or the value of the parameter */
    TreeMap<String, String> output = new TreeMap<String, String>();

    public void put(String name, Double value) {
        output.put(name, ntoa(value));
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void writeResults() throws Exception {

        CsvListWriter writer = getListWriter("RSLT");
        String[] header = output.navigableKeySet().toArray(new String[0]);
        writer.writeHeader(header);
        writer.write(output.values().toArray(new String[0]));
        writer.close();
    }

    public void writeClusters(ArrayList<Cluster> clusters) throws Exception {

        /*
         * Screw you Java. You're dispatch model is a griefer.
         * 
         * TODO: When news algorithms are exlored, the clusters will not be MoccaCluster. Wrap the cast in try/catch to
         * abort illegal attempt to cast a Cluster to a MoccaCluster
         */
        MoccaCluster first = (MoccaCluster) clusters.get(0);

        CsvListWriter writer = getListWriter("CLSTR");
        String[] header = getClusterHeader(first).toArray(new String[0]);
        writer.writeHeader(header);

        for (Cluster each : clusters) {
            writer.write(getRecord((MoccaCluster) each));
        }

        writer.close();
    }

    File getFile(String name) {
        return new File(getPath() + getKey() + "_" + name + extension);
    }

    CsvListWriter getListWriter(String name) {

        File file = getFile(name);
        CsvListWriter temp = null;
        try {
            FileWriter fw = new FileWriter(file.getCanonicalFile());
            temp = new CsvListWriter(fw, new CsvPreference.Builder('"', separator, "\n").build());
        } catch (IOException e) {
            System.err.println(e.getMessage());

        }
        return temp;
    }

    public String getKey() {
        return output.get(clustererExpKey);
    }

    public String getPath() {
        return path;
    }

    public void setKey(String key) {
        output.put(clustererExpKey, key);
    }

    public void setClustererName(String name) {
        output.put(clustererNameKey, name);
    }

    public void setDataName(String name) {
        output.put(dataNameKey, name);
    }

    List<String> getRecord(MoccaCluster cluster) {
        ArrayList<String> list = new ArrayList<String>();

        // If MOCCA cluster, add in quality metric for the cluster
        list.add(ntoa(cluster.quality));
        list.addAll(getRecord((Cluster) cluster));
        return list;
    }

    private List<String> getRecord(Cluster cluster) {
        ArrayList<String> list = new ArrayList<String>();
        StringBuffer subspace = new StringBuffer();
        StringBuffer objs = new StringBuffer();

        // Subspace
        // subspace.append('[');
        for (boolean each : cluster.m_subspace) {
            subspace.append(each ? '1' : '0');
            subspace.append(nonseparator);
        }
        // Remove last punctuation mark
        subspace.deleteCharAt(subspace.length() - 1);
        // subspace.append(']');
        list.add(subspace.toString());

        // Cardinality of object set
        list.add(ntoa(cluster.m_objects.size()));

        // Indexes of the object set
        objs.append('[');
        for (Integer each : cluster.m_objects) {
            objs.append(each);
            objs.append(nonseparator);
        }
        // Remove last punctuation mark
        objs.deleteCharAt(objs.length() - 1);
        objs.append(']');
        list.add(objs.toString());

        return list;
    }

    String ntoa(Double val) {
        return String.format("%f", val);
    }

    String ntoa(int val) {
        return String.format("%d", val);
    }

    List<String> getClusterHeader(Cluster cluster) {
        ArrayList<String> list = new ArrayList<String>();
        list.add("SUBSPACE");
        list.add("CARDINALITY");
        list.add("OBJECTS");
        return list;
    }

    List<String> getClusterHeader(MoccaCluster cluster) {
        ArrayList<String> list = new ArrayList<String>();

        list.add("QUALITY");
        list.addAll(getClusterHeader((Cluster) cluster));
        return list;
    }

}// class

