package p4analyser.broker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.beust.jcommander.JCommander;

import org.codejargon.feather.Feather;
import org.codejargon.feather.Key;

import p4analyser.ontology.providers.Application;
import p4analyser.ontology.providers.SyntaxTreeAnalysis;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.apache.commons.lang3.SystemUtils;

public class App {

    private static final Class<?>[] providerIfaces = { SyntaxTreeAnalysis.class };

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

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        new App(args);
    }

    public App(String[] args) throws InterruptedException, ExecutionException, TimeoutException {

        Map<String, Application> apps = discoverApplications();
        Map<String, Object> appCmds = appCommands(apps); // local copies to avoid storing stuff inside the providers

        BaseCommand base = new BaseCommand();

        JCommander.Builder jcb = JCommander.newBuilder();
        jcb.addObject(base);

        for (Application app : apps.values()) {
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

        List<Object> deps = new ArrayList<>();
        deps.add(p4FileService);
        deps.add(server);
        for (Class<?> providerIface : providerIfaces) {
            deps.add(discoverUniqueProvider(providerIface));
        }
        deps.add(command);
        deps.add(apps.get(commandName));


        Feather feather = Feather.with(deps.toArray());

        feather.provider(Application.class).get();
//        feather.provider(Key.of(Object.class, ApplicationProvider.class)).get();

        server.close();
        System.exit(0);
    }

    public static Map<String, Object> appCommands(Map<String, Application> apps){
        Map<String, Object> cmds = new HashMap<>();
        for (Map.Entry<String, Application> entry : apps.entrySet()) {
            cmds.put(entry.getKey(), entry.getValue().getUICommand());
        }
        return cmds;
    }

    public static Map<String, Application> discoverApplications(){
        List<Application> applications = discoverAllProviders(Application.class);
        if(applications.isEmpty())
            throw new IllegalStateException("No application providers found");
        Map<String, Application> apps = new HashMap<>();
        for (Application app : applications) {
            if(apps.get(app.getUICommandName()) != null)
                throw new IllegalStateException("Ambiguous application name " + app.getUICommandName());
            apps.put(app.getUICommandName(), app);
        }

        return apps;
    }

    public static <T> List<T> discoverAllProviders(Class<T> providerInterface){
        ServiceLoader<T> loader = ServiceLoader.load(providerInterface);
        return loader.stream().map(p -> p.get()).collect(Collectors.toList());
    }

    public static <T> T discoverUniqueProvider(Class<T> providerInterface){
        ServiceLoader<T> loader = ServiceLoader.load(providerInterface);
        Iterator<T> it = loader.iterator();

        if( !(it.hasNext())) 
            throw new IllegalStateException("No provider found for provider interface " + providerInterface.getSimpleName());

        T providerImplem = it.next();

        if(it.hasNext()) {
            T providerImplem2 = it.next();
            String msg = 
                String.format("Ambigous implemententations found for provider interface %s. Both %s and %s implement this interface.",
                              providerInterface.getSimpleName(),
                              providerImplem.getClass().getSimpleName(),
                              providerImplem2.getClass().getSimpleName());
            throw new IllegalStateException(msg); 
        }

        return providerImplem;

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
