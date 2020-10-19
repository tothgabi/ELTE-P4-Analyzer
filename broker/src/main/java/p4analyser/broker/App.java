package p4analyser.broker;

import java.io.File;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


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
        server = new LocalGremlinServer();
        server.start();
    }

    private void close(){
        server.close();

    }

    private void start() {
        File buildFile = new File(BROKER_DEFINITION_PATH);
        Project p = new Project();

        DefaultLogger consoleLogger = new DefaultLogger();
        consoleLogger.setErrorPrintStream(System.err);
        consoleLogger.setOutputPrintStream(System.out);
        consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
        p.addBuildListener(consoleLogger);

        p.setProperty("host", server.host);
        p.setProperty("port", Integer.toString(server.port));
        p.setProperty("remoteTraversalSourceName", server.remoteTraversalSourceName);

        p.setUserProperty("ant.file", buildFile.getAbsolutePath());
        p.init();
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        p.addReference("ant.projectHelper", helper);
        helper.parse(p, buildFile);
        p.executeTarget(p.getDefaultTarget());

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
