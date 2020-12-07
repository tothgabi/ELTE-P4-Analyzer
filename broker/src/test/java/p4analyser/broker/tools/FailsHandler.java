/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */
package p4analyser.broker.tools;

import java.util.List;
import java.io.IOException;
import org.junit.runner.notification.Failure;

import java.nio.file.StandardOpenOption;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.File;
import java.io.FileNotFoundException; 
import java.util.Scanner;


//todo do not use file, but List
public class FailsHandler {
    
    public static void initFails () {
        try {
            File failsFile = new File("fails");
            failsFile.createNewFile();
        } catch (IOException e) {

        }
    }

    public static void deleteFails ()  {
        File failsFile = new File("fails");
        failsFile.delete();
    }

    public static void writeFails (String fileName, String testType, List<Failure> failures) {
        for (Failure failure : failures){
            try {
                Files.write(Paths.get("fails"), (fileName + " - " + testType + ": " + failure.toString() + "\n").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
             }
        }
    }

    public static void reportFails() {
        System.out.println("===========\nTest Failures:\n===========");

        try {
            File failsFile = new File("fails");
            Scanner myReader = new Scanner(failsFile);
            int sum = 0;
            while (myReader.hasNextLine()) {
                String oneFail = myReader.nextLine();
                System.out.println(oneFail);
                sum = sum + 1;
            }
            myReader.close();
            System.out.println("==============\nSum failures during tests: " + sum + "\n==============");
        } catch (FileNotFoundException e) {
            //skip
        }
    }
}
