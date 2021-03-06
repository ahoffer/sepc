package weka.subspaceClusterer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class Consolidator {
    public static void main(String[] args) throws IOException {
        consolidate(args[0], args[1]);
    }

    static void consolidate(String path, String consolidatedFilename) throws IOException {
        File dir = new File(path);
        IOFileFilter fileFilter = new WildcardFileFilter("RSLT*");
        Iterator<File> iterator = FileUtils.iterateFiles(dir, fileFilter, null);
        Scanner scanner;
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(consolidatedFilename)));
        // stream = Files.newDirectoryStream(Paths.get(path), "RSLT*.csv");
        boolean headerIsWritten = false;
        // Iterate over result files
        while (iterator.hasNext()) {
            File file = iterator.next();
            FileInputStream fis = new FileInputStream(file);
            scanner = new Scanner(fis);
            // Each file should have exactly two lines: a header and values. Grab them.
            String header = scanner.nextLine();
            String result = scanner.nextLine();
            if (!headerIsWritten) {
                writer.println(header);
                headerIsWritten = true;
            }// if
             // Write data
            writer.println(result);
            // Shutdown IO
            scanner.close();
        }// for
         // Shutdown IO
        writer.close();
    }// method
}
