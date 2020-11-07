package p4analyser.broker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.apache.commons.lang3.SystemUtils;


public class App {

    private static ClassLoader loader = Thread.currentThread().getContextClassLoader();

    private static String BROKER_DEFINITION_PATH = loader.getResource("broker.xml").getPath();

    private static String GREMLIN_CLIENT_CONF_PATH = loader.getResource("conf/remote-graph.properties").getPath();

//    NOTE Unlike with YAML, we can use "classpath:" notation in .properties files, so it turned out we don't need this:
//    private static String GREMLIN_CLIENT_CONF_CLUSTERFILE_PATH = loader.getResource("conf/remote-objects.yaml").getPath();

    public static void main(String[] args) {
        App app = new App();
        app.start();
        app.close();
    }

    private LocalGremlinServer server;
    private App() {
        rightPaths();
        server = new LocalGremlinServer();
        server.start();
    }

    private void close(){
        server.close();
    }

    private void start() {
        File buildFile = new File(BROKER_DEFINITION_PATH);
        Project p = new Project();
        badSolutionForDepends("aliases");


        DefaultLogger consoleLogger = new DefaultLogger();
        consoleLogger.setErrorPrintStream(System.err);
        consoleLogger.setOutputPrintStream(System.out);
        consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
        p.addBuildListener(consoleLogger);

        p.setProperty("host", server.host);
        p.setProperty("port", Integer.toString(server.port));
        p.setProperty("remoteTraversalSourceName", server.remoteTraversalSourceName);
        p.setProperty("p4ProgramSourceName", "basic.p4");
        p.setProperty("optinalTestXml", "true");

        p.setUserProperty("ant.file", buildFile.getAbsolutePath());
        p.init();
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        p.addReference("ant.projectHelper", helper);
        helper.parse(p, buildFile);
        p.executeTarget(p.getDefaultTarget());

        rewriteBadSolutionForDepends("aliases");
    }

    private Boolean isWindows = SystemUtils.OS_NAME.contains("Windows");
    private void rightPaths() {
        if (isWindows) {
            BROKER_DEFINITION_PATH = BROKER_DEFINITION_PATH.substring(1);
            GREMLIN_CLIENT_CONF_PATH = GREMLIN_CLIENT_CONF_PATH.substring(1);
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

    private static Configuration loadClientConfig() {
        Configuration c;
        try {
            c = new PropertiesConfiguration(GREMLIN_CLIENT_CONF_PATH);
        } catch (ConfigurationException e) {
            throw new IllegalStateException(
                    String.format(
                        "Error parsing Gremlin client file at %s:", 
                        GREMLIN_CLIENT_CONF_PATH),
                    e);
        }

//      NOTE Unlike with YAML, we can use "classpath:" notation in .properties files, so it turned out we don't need this:
//      c.setProperty("gremlin.remote.driver.clusterFile", GREMLIN_CLIENT_CONF_CLUSTERFILE_PATH);

        return c;
    }
}
