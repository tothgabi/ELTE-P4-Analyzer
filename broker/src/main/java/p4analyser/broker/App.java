package p4analyser.broker;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import p4analyser.ontology.providers.ApplicationProvider;
import p4analyser.ontology.providers.ApplicationProvider.Application;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.apache.commons.lang3.SystemUtils;

public class App {

//    private static final List<Class<? extends Annotation>> analyses =
//        Arrays.asList(SyntaxTree.class);

    private static final String EXPERTS_PACKAGE = "p4analyser.experts";
    private static final String APPLICATIONS_PACKAGE = "p4analyser.applications";
    private static final String ANALYSES_PACKAGE = "p4analyser.ontology.analyses";

    private static ClassLoader loader = Thread.currentThread().getContextClassLoader();

    private String GREMLIN_CLIENT_CONF_PATH = loader.getResource("conf/remote-graph.properties").getPath();
    // NOTE Unlike with YAML, we can use "classpath:" notation in .properties files,
    // so it turned out we don't need this:
    // private static String GREMLIN_CLIENT_CONF_CLUSTERFILE_PATH =
    // loader.getResource("conf/remote-objects.yaml").getPath();

    {
        rightPaths();
    }

    public static final String CORE_P4 = loader.getResource("core.p4").getPath().toString();
    public static final String V1MODEL_P4 = loader.getResource("v1model.p4").getPath().toString();
    public static final String BASIC_P4 = loader.getResource("basic.p4").getPath().toString();

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException,
            InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
//        Reflections appReflections = new Reflections("p4analyser.experts", new MethodAnnotationsScanner());

        Map<String, ApplicationProvider> apps = discoverApplications();
        Map<String, Object> appCmds = appCommands(apps); // local copies to avoid storing stuff inside the providers

        BaseCommand base = new BaseCommand();

        JCommander.Builder jcb = JCommander.newBuilder();
        jcb.addObject(base);

        for (ApplicationProvider app : apps.values()) {
            String cmdName = app.getUICommandName();
            jcb.addCommand(cmdName, appCmds.get(cmdName), app.getUICommandAliases());
        }
        
        JCommander jc = jcb.build();
        jc.parse(args);

        String commandName = jc.getParsedCommand();
        Object command = appCmds.get(commandName);

        if (base.help) {
            jc.usage();
            System.exit(0);
        }

        System.out.println("command: " + commandName);
        if(commandName == null){
            System.out.println("Please, provide a command argument.");
            jc.usage();
            System.exit(1);
        }


        if (base.P4_FILEPATH == null) {
            System.out.println("warning: no P4 input file argument provided, using basic.p4");
            base.P4_FILEPATH = BASIC_P4;
        }

        P4FileService p4FileService = new P4FileService(base.P4_FILEPATH, CORE_P4, V1MODEL_P4);
        LocalGremlinServer server = new LocalGremlinServer();

        Collection<Object> deps = new ArrayList<>();
        deps.add(p4FileService);
        deps.add(server);
        for (Object analyser : discoverAnalysers()) {
            deps.add(analyser);
        }

        deps.add(command);
        deps.add(apps.get(commandName));

        Feather feather = Feather.with(deps.toArray());

        feather.provider(Key.of(Void.class, Application.class)).get();

        server.close();
        System.exit(0);
    }

    public static Map<String, Object> appCommands(Map<String, ApplicationProvider> apps){
        Map<String, Object> cmds = new HashMap<>();
        for (Map.Entry<String, ApplicationProvider> entry : apps.entrySet()) {
            cmds.put(entry.getKey(), entry.getValue().getUICommand());
        }
        return cmds;
    }

    public static Map<String, ApplicationProvider> discoverApplications()
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        Reflections reflections = new Reflections(APPLICATIONS_PACKAGE, new MethodAnnotationsScanner());

        Set<Method> methods = reflections.getMethodsAnnotatedWith(Application.class);
        if(methods.isEmpty()){
            String msg = 
                String.format("No applications found in " + APPLICATIONS_PACKAGE);
            throw new IllegalStateException(msg);
        }

        Map<String, ApplicationProvider> apps = new HashMap<>();
        for (Method m : methods) {
            Object candid = m.getDeclaringClass().getConstructor().newInstance();
            if(!(candid instanceof ApplicationProvider)){
                throw new IllegalStateException(
                    String.format("Application %s does not implement interface %s", 
                                  ApplicationProvider.class.getSimpleName()));
            }
            ApplicationProvider app = (ApplicationProvider) candid;
            if(apps.get(app.getUICommandName()) != null)
                throw new IllegalStateException("Ambiguous application name " + app.getUICommandName());
            apps.put(app.getUICommandName(), app);
        }

        return apps;
    }

    public static Collection<Object> discoverAnalysers() throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

        Reflections reflections = new Reflections(EXPERTS_PACKAGE, new MethodAnnotationsScanner());

        Collection<Object> analyserImplemms = new ArrayList<>();
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

            analyserImplemms.add(implems.iterator().next().getConstructor().newInstance());
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
