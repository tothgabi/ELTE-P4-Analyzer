package p4analyser.broker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.beust.jcommander.JCommander;

import org.codejargon.feather.Feather;
import org.codejargon.feather.Key;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import p4analyser.ontology.Status;
import p4analyser.ontology.analyses.AbstractSyntaxTree;
import p4analyser.ontology.analyses.ControlFlow;
import p4analyser.ontology.providers.AppUI;
import p4analyser.ontology.providers.Application;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.apache.commons.lang3.SystemUtils;

public class App {

    // private static final List<Class<? extends Annotation>> analyses =
    // Arrays.asList(SyntaxTree.class);

    private static final String STATE_NAME = "injector.gryo";

    private static final String EXPERTS_PACKAGE = "p4analyser.experts";
    private static final String APPLICATIONS_PACKAGE = "p4analyser.applications";
    private static final String ANALYSES_PACKAGE = "p4analyser.ontology.analyses";

    private static ClassLoader loader = Thread.currentThread().getContextClassLoader();

    private String GREMLIN_CLIENT_CONF_PATH = loader.getResource("conf/remote-graph.properties").getPath();
    // NOTE Unlike with YAML, we can use "classpath:" notation in .properties files,
    // so it turned out we don't need this:
    // private static String GREMLIN_CLIENT_CONF_CLUSTERFILE_PATH =
    // loader.getResource("conf/remote-objects.yaml").getPath();

    public static final String CORE_P4 = loader.getResource("core.p4").getPath().toString();
    public static final String V1MODEL_P4 = loader.getResource("v1model.p4").getPath().toString();
    public static final String BASIC_P4 = loader.getResource("basic.p4").getPath().toString();

    {
        rightPaths();
    }


    public static void main(String[] args) throws Exception {
//        Reflections appReflections = new Reflections("p4analyser.experts", new MethodAnnotationsScanner());

        Map<String, Application> apps = discoverApplications();

        System.out.println("Applications discovered:" + apps);

        JCommander.Builder jcb = JCommander.newBuilder();

        for (Application app : apps.values()) {
            AppUI cmd = app.getUI();
            String cmdName = cmd.getCommandName();
            String[] aliases = cmd.getCommandNameAliases();

            jcb.addCommand(cmdName, cmd, aliases);
        }
        
        JCommander jc = jcb.build();
        jc.parse(args);

        String commandName = jc.getParsedCommand();
        if(commandName == null){
            jc.usage();
            System.exit(0);
        }

        Application app = apps.get(commandName);

        if (app.getUI().help) {
            jc.usage();
            System.exit(0);
        }

        if(commandName == null){
            System.out.println("Please, provide a command argument.");
            jc.usage();
            System.exit(1);
        }

        System.out.println("Command:" + app.getUI().getCommandName() + " " + app.getUI());

        if (app.getUI().p4FilePath == null) {
            System.out.println("warning: no P4 input file argument provided, using basic.p4");
            app.getUI().p4FilePath = BASIC_P4;
        }

        Map<Class<? extends Annotation>, Object> analysers = discoverAnalysers();
        System.out.println("Analysers discovered: " + analysers);


        String psp = 
            app.getUI().databaseLocation == null 
                ? null 
                : persistentStatePath(app.getUI().databaseLocation, app.getUI().p4FilePath);

        Feather feather; 
        LocalGremlinServer server;
        if(psp == null ){
            // start the server in in-memory mode
            LocalGremlinServer server0 = new LocalGremlinServer();
            server0.init();
            server = server0;
            feather = createInjector(app.getUI().p4FilePath, analysers, server);
        } else {
            try {
                // all dependencies are serialized. the server object will store the 
                PersistentState state = loadInjector(psp);
                feather = state.injector;
                server = state.server;
            } catch(FileNotFoundException e){
                // there is no file at the location yet
                // start the server in persistent mode and point it here
                LocalGremlinServer server0 = new LocalGremlinServer(psp);
                server0.init();
                server = server0;
                feather = createInjector(app.getUI().p4FilePath, analysers, server);
            }
        }
    
        feather.injectFields(app);
        app.run();

        server.close();

        if(psp != null){
            saveInjector(psp);
        }

        System.exit(0);
    }


    private static String persistentStatePath(String databaseLocation, String p4FilePath) {
        return Paths.get(databaseLocation, p4FilePath).toString();
    }

    private static PersistentState loadInjector(String persistentStatePath) throws FileNotFoundException {
        throw new RuntimeException("persistance not implemented yet");
    }
    private static void saveInjector(String persistentStatePath) {
        throw new RuntimeException("persistance not implemented yet");
    }

    private static Feather createInjector(String p4FilePath, Map<Class<? extends Annotation>, Object> analysers,
            LocalGremlinServer server) {
        P4FileService p4FileService = new P4FileService(p4FilePath, CORE_P4, V1MODEL_P4);

        Collection<Object> deps = new ArrayList<>();
        deps.add(p4FileService);
        deps.add(server);

        for (Object analyser : analysers.values()) {
            deps.add(analyser);
        }

        Feather feather = Feather.with(deps.toArray());

        return feather;
    }

    public static Map<String, Object> appCommands(Map<String, Application> apps)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        Map<String, Object> cmds = new HashMap<>();
        for (Map.Entry<String, Application> entry : apps.entrySet()) {
            AppUI command = entry.getValue().getUI();
            cmds.put(entry.getKey(), command);
        }
        return cmds;
    }

    public static Map<String, Application> discoverApplications()
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        Reflections reflections = new Reflections(APPLICATIONS_PACKAGE);

 //        Set<Method> methods = reflections.getMethodsAnnotatedWith(Application.class);
        Set<Class<? extends Application>> appImpls = reflections.getSubTypesOf(Application.class);
        if(appImpls.isEmpty()){
            String msg = 
                String.format("No applications found in " + APPLICATIONS_PACKAGE);
            throw new IllegalStateException(msg);
        }

        Map<String, Application> apps = new HashMap<>();
        for (Class<? extends Application> m : appImpls) {
            Object candid = m.getConstructor().newInstance();
            if(!(candid instanceof Application)){
                throw new IllegalStateException(
                    String.format("Application %s does not implement interface %s", 
                                  Application.class.getSimpleName()));
            }
            Application app = (Application) candid;
            if(apps.get(app.getUI().getCommandName()) != null)
                throw new IllegalStateException("Ambiguous application name " + app.getUI().getCommandName());
            apps.put(app.getUI().getCommandName(), app);
        }

        return apps;
    }

    public static Map<Class<? extends Annotation>, Object> discoverAnalysers() throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

        Reflections reflections = new Reflections(EXPERTS_PACKAGE, new MethodAnnotationsScanner());

         Map<Class<? extends Annotation>, Object> analyserImplemms = new HashMap<>();
        for (Class<? extends Annotation> analysis : discoverAnalyses()) {
            Set<Method> methods = reflections.getMethodsAnnotatedWith(analysis);
            if(methods.isEmpty()){
                String msg = 
                    String.format("No implementation found in %s for analysis %s",
                    EXPERTS_PACKAGE,
                    analysis.getSimpleName());
                throw new IllegalStateException(msg);
            }

            Collection<Class<?>> implems = methods.stream().map(m -> m.getDeclaringClass()).collect(Collectors.toList());
            if(implems.size() > 1){
                String msg = 
                    String.format("Ambigous implementations found in %s for analysis %s: %s",
                                EXPERTS_PACKAGE,
                                analysis.getSimpleName(),
                                implems.stream().map(c -> c.getSimpleName()).toArray());
                throw new IllegalStateException(msg);
            }

            analyserImplemms.put(analysis, implems.iterator().next().getConstructor().newInstance());
        }
        return analyserImplemms;
    }

    public static Collection<Class<? extends Annotation>> discoverAnalyses() {
        Reflections reflections = new Reflections(ANALYSES_PACKAGE);

        Set<Class<? extends Annotation>> analyses = 
            reflections.getSubTypesOf(Annotation.class);

        return analyses;

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
