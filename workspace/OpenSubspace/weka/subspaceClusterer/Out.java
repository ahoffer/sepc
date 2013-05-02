package weka.subspaceClusterer;

import weka.core.Instances;

public class Out {

    public static void print(Instances data) {
        Instances copy = new Instances(data);

        System.out.println(copy.relationName());

        for (int i = 0; i < copy.numClasses(); ++i) {

            System.out.printf("class %d  [", i);

            for (int j = 0; j < copy.numInstances(); ++j) {

                if (i == (int) copy.instance(j).classValue()) {

                    System.out.printf("%d, ", j);

                }// if

            }// for j

            System.out.printf("]\n");

        } // for i
    }// method
} // class