package p4analyser.broker;

import p4analyser.broker.tools.JsonHandler;
import p4analyser.broker.tools.FailsHandler;

import java.io.File;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.SystemUtils;

public class ControllerIT {

    private static ClassLoader loader = Thread.currentThread().getContextClassLoader();

    private static String BROKER_DEFINITION_PATH = loader.getResource("broker.xml").getPath();

    private LocalGremlinServer server;

    public static JsonHandler jsonHandler;

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException,
            IOException {
        System.out.println("ControllerIT - has started");
        rightPaths();

        jsonHandler = new JsonHandler("tests.json");
        FailsHandler.initFails();

        HashMap<String, ArrayList<String>> fileInfo = jsonHandler.getFileTestInfo();
        for (String fileName : fileInfo.keySet()) {
            ControllerIT app = new ControllerIT();
            app.start(fileName, fileInfo.get(fileName));
            app.close();
        }

        FailsHandler.reportFails();
        FailsHandler.deleteFails();
        System.out.println("ControllerIT - has finished");

        System.exit(0);
    }

    private ControllerIT() throws IOException {
        server = new LocalGremlinServer();
    }

    private void close() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        server.close();
    }

    /*
    private void runUnitTests() {
        System.out.println("==============\nRun Unit Tests\n==============");
        FailsHandler.writeFails("UnitTest", "aliases", JUnitCore.runClasses(AliasesTest.class).getFailures());
        FailsHandler.writeFails("UnitTest", "call-graph", JUnitCore.runClasses(CallGraphGeneratorTest.class).getFailures());
        FailsHandler.writeFails("UnitTest", "symbol-table", JUnitCore.runClasses(SymbolTableTest.class).getFailures());
        System.out.println("==============\nFinish Unit Tests\n==============");
    }*/

    private void start (String fileName, ArrayList<String> propertyList) {
        System.out.println("****************\nTest - has started a new run with " + fileName + "\n****************");

        System.out.println(propertyList.toString());
        String propertyListString = propertyList.toString();
        propertyListString = propertyListString.substring(1, propertyListString.length()-1);
        badSolutionForDepends(propertyListString);

        File buildFile = new File(BROKER_DEFINITION_PATH);
        Project p = new Project();

        DefaultLogger consoleLogger = new DefaultLogger();
        consoleLogger.setErrorPrintStream(System.err);
        consoleLogger.setOutputPrintStream(System.out);
        consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
        p.addBuildListener(consoleLogger);

        p.setProperty("host", "localhost");
        p.setProperty("port", Integer.toString(8182));
        p.setProperty("remoteTraversalSourceName", "g");
        p.setProperty("p4ProgramSourceName", fileName);
        p.setProperty("optinalTestXml", "false");
        

        p.setUserProperty("ant.file", buildFile.getAbsolutePath());
        p.init();
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        p.addReference("ant.projectHelper", helper);
        helper.parse(p, buildFile);
        
        p.executeTarget("test");
        rewriteBadSolutionForDepends(propertyListString);
    }

    private static Boolean isWindows = SystemUtils.OS_NAME.contains("Windows");
    private static void rightPaths() {
        if (isWindows) {
            BROKER_DEFINITION_PATH = BROKER_DEFINITION_PATH.substring(1);
        }
    }

    private void badSolutionForDepends (String depends) {
        Path path = Paths.get(BROKER_DEFINITION_PATH);
        Charset charset = StandardCharsets.UTF_8;

        String content;
        try {
            content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll("TEST-DEPENDS", depends);
            Files.write(path, content.getBytes(charset));
        } catch (IOException e1) {
            throw new IllegalStateException("Failed to edit config file " + BROKER_DEFINITION_PATH);
        }
    }

    private void rewriteBadSolutionForDepends (String depends) {
        Path path = Paths.get(BROKER_DEFINITION_PATH);
        Charset charset = StandardCharsets.UTF_8;

        String content;
        try {
            content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll(depends, "TEST-DEPENDS");
            
            Files.write(path, content.getBytes(charset));
        } catch (IOException e1) {
            throw new IllegalStateException("Failed to edit config file " + BROKER_DEFINITION_PATH);
        }
    }

}
