package p4analyser.broker;

import java.util.HashMap;
import java.util.Map;

import com.beust.jcommander.JCommander;

import org.codejargon.feather.Feather;
import org.codejargon.feather.Key;

import p4analyser.experts.syntaxtree.AntlrP4;
import p4analyser.ontology.injectable.SyntaxTreeAnalysis;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.apache.commons.lang3.SystemUtils;


public class App {

    private static ClassLoader loader = Thread.currentThread().getContextClassLoader();

    private String GREMLIN_CLIENT_CONF_PATH = loader.getResource("conf/remote-graph.properties").getPath();
//    NOTE Unlike with YAML, we can use "classpath:" notation in .properties files, so it turned out we don't need this:
//    private static String GREMLIN_CLIENT_CONF_CLUSTERFILE_PATH = loader.getResource("conf/remote-objects.yaml").getPath();

    {
        rightPaths();
    }

    public static final String CORE_P4 = loader.getResource("core.p4").getPath().toString();
    public static final String V1MODEL_P4 = loader.getResource("v1model.p4").getPath().toString();
    public static final String BASIC_P4 = loader.getResource("basic.p4").getPath().toString();
    
    private static final String CMD_DRAW = "draw";
    private static final String CMD_NOOP = "noop";


    public static void main(String[] args) {
        new App(args);
    }
    public App(String[] args){

        Map<String, Object> cmds = new HashMap<>();
        cmds.put(CMD_DRAW, new DrawCommand());
        cmds.put(CMD_NOOP, new NoopCommand());

        BaseCommand base = new BaseCommand();

        JCommander jc = 
            JCommander.newBuilder()
                      .addObject(base)
                      .addCommand(CMD_DRAW, cmds.get(CMD_DRAW))
                      .addCommand(CMD_NOOP, cmds.get(CMD_NOOP))
                      .build();
        jc.parse(args);

        String commandName = jc.getParsedCommand();
        Object command = cmds.get(commandName);

        if(base.help){
            jc.usage();
            System.exit(0);
        }

        if(base.P4_FILEPATH == null){
            System.out.println("warning: no P4 input file argument provided, using basic.p4");
            base.P4_FILEPATH = BASIC_P4;
        }

        P4FileService p4FileService = new P4FileService(base.P4_FILEPATH, CORE_P4, V1MODEL_P4);
        LocalGremlinServer server = new LocalGremlinServer();

        Feather feather = Feather.with(p4FileService, server, new AntlrP4());

        feather.provider(Key.of(Boolean.class, SyntaxTreeAnalysis.class)).get();

        server.close();
    }

    private static Boolean isWindows = SystemUtils.OS_NAME.contains("Windows");
    private void rightPaths() {
        if (isWindows) {
            GREMLIN_CLIENT_CONF_PATH = GREMLIN_CLIENT_CONF_PATH.substring(1);
        }
    }

    private Configuration loadClientConfig() {
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
