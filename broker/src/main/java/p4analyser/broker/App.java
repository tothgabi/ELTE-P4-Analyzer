package p4analyser.broker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.beust.jcommander.JCommander;
import com.thoughtworks.xstream.XStream;

import org.codejargon.feather.Feather;
import org.codejargon.feather.Key;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.MethodAnnotationsScanner;

import p4analyser.ontology.Status;
import p4analyser.ontology.analyses.AbstractSyntaxTree;
import p4analyser.ontology.analyses.ControlFlow;
import p4analyser.ontology.providers.AppUI;
import p4analyser.ontology.providers.Application;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.apache.commons.lang.SystemUtils;


public class App {

    // private static final List<Class<? extends Annotation>> analyses =
    // Arrays.asList(SyntaxTree.class);

    private static final String EXPERTS_PACKAGE = "p4analyser.experts";
    private static final String APPLICATIONS_PACKAGE = "p4analyser.applications";
    private static final String ANALYSES_PACKAGE = "p4analyser.ontology.analyses";

    // TODO a white list would be better
    // NOTE: before you extend this list with your class, check out the --readonly option
    private static final Collection<Class<?>> DO_NOT_SERIALIZE = Arrays.asList(GraphTraversalSource.class);

    private static final ClassLoader loader = Thread.currentThread().getContextClassLoader();
    private static String GREMLIN_CLIENT_CONF_PATH = loader.getResource("conf/remote-graph.properties").getPath();
    // NOTE Unlike with YAML, we can use "classpath:" notation in .properties files,
    // so it turned out we don't need this:
    // private static String GREMLIN_CLIENT_CONF_CLUSTERFILE_PATH =
    // loader.getResource("conf/remote-objects.yaml").getPath();

    public static final String CORE_P4 = loader.getResource("core.p4").getPath().toString();
    public static final String V1MODEL_P4 = loader.getResource("v1model.p4").getPath().toString();
    public static final String BASIC_P4 = loader.getResource("basic.p4").getPath().toString();
    private static Boolean isWindows = SystemUtils.OS_NAME.contains("Windows");

    static {
        rightPaths();
        loadClientConfig();
    }

    public static void main(String[] args) throws Exception {

        // prepare injector
        App broker = new App(args);  

        // run the experts (except the ones the application lazily initializes)
        broker.feather.injectFields(broker.invokedApp); 

        // run the application
        broker.invokedApp.run(); 

        if (broker.persistingDirectory != null ) {
            if(!broker.readonly){
                App.saveInjector(broker.persistingDirectory, broker.feather);
            } else
                System.out.println("--readonly argument found, modifications are not saved");
        }

        broker.server.close();
        System.exit(0);
    }

    private Feather feather;
    private LocalGremlinServer server;
    private final Map<Class<? extends Annotation>, Object> analysers;
    private final Map<String, Application> apps;
    private final Application invokedApp;
    private String persistingDirectory;
    private final String p4FilePath;
    private boolean readonly;
    private boolean reset;

    private App(String[] args) throws DiscoveryException, IOException, LocalGremlinServerException,
            ClassNotFoundException, ReflectionException {

        analysers = App.discoverAnalysers();
        System.out.println("Analysers discovered: " + analysers);

        apps = App.discoverApplications();
        System.out.println("Applications discovered:" + apps);

        JCommander jc = parseCLIArgs(args);

        String commandName = jc.getParsedCommand();
        if (commandName == null) {
            System.out.println("Please, provide a command argument.");
            jc.usage();
            System.exit(0);
        }

        invokedApp = apps.get(commandName);

        if (invokedApp.getUI().help) {
            jc.usage();
            System.exit(0);
        }

        System.out.println("Command:" + invokedApp.getUI().getCommandName() + " " + invokedApp.getUI());

        reset = invokedApp.getUI().reset;
        readonly = invokedApp.getUI().readonly;

        p4FilePath = this.ensureP4FileOrDefault(invokedApp.getUI().p4FilePath);

        if (invokedApp.getUI().databaseLocation != null) {
            if(reset && readonly){
               throw new IllegalArgumentException("--reset and -- readonly are conflicting options, use at most one.");
            }

            persistingDirectory = 
                App.ensurePersistingDirectoryExists(invokedApp.getUI().databaseLocation, p4FilePath);
        }


        this.initBrokerState();
    }

    public static Map<String, Application> discoverApplications() throws DiscoveryException {
        Reflections reflections = new Reflections(APPLICATIONS_PACKAGE);

        Set<Class<? extends Application>> appImpls = reflections.getSubTypesOf(Application.class);
        if (appImpls.isEmpty()) {
            String msg = String.format("No applications found in " + APPLICATIONS_PACKAGE);
            throw new IllegalStateException(msg);
        }

        Map<String, Application> apps = new HashMap<>();
        for (Class<? extends Application> m : appImpls) {
            Object candid;
            try {
                candid = m.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException 
                    | InvocationTargetException | SecurityException | IllegalArgumentException
                    | NoSuchMethodException e) {
                        throw new DiscoveryException(e);
            }
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

    public static Map<Class<? extends Annotation>, Object> discoverAnalysers() throws DiscoveryException {

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

            try {
                analyserImplemms.put(analysis, implems.iterator().next().getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException 
                    | InvocationTargetException | SecurityException | IllegalArgumentException
                    | NoSuchMethodException e) {
                        throw new DiscoveryException(e);
            }

        }
        return analyserImplemms;
    }

    public static Collection<Class<? extends Annotation>> discoverAnalyses() {
        Reflections reflections = new Reflections(ANALYSES_PACKAGE);

        Set<Class<? extends Annotation>> analyses = 
            reflections.getSubTypesOf(Annotation.class);

        return analyses;

    }

    private JCommander parseCLIArgs(String[] args) {

        JCommander.Builder jcb = JCommander.newBuilder();

        for (Application app : apps.values()) {
            AppUI cmd = app.getUI();
            String cmdName = cmd.getCommandName();
            String[] aliases = cmd.getCommandNameAliases();

            jcb.addCommand(cmdName, cmd, aliases);
        }

        JCommander jc = jcb.build();
        jc.parse(args);
        return jc;
    }

    private String ensureP4FileOrDefault(String inputFile) throws IOException {
        String p4FilePath; 
        if (inputFile == null) {
            System.out.println("warning: no P4 input file argument provided, using basic.p4");
            p4FilePath = BASIC_P4;
            // app.getUI().p4FilePath is left on null. this is consistent with not having user input.
        } else {
            p4FilePath = App.absolutePath(inputFile);
        }

        File p4File = new File(p4FilePath);

        if (!p4File.exists()) {
            throw new IllegalArgumentException("No file exists at " + p4FilePath);
        }
        if (!p4File.isFile()) {
            throw new IllegalArgumentException(p4FilePath + " is not a file.");
        }

        return p4FilePath;
    }


    private static String ensurePersistingDirectoryExists(String databaseLocation, String p4FilePath) {
        File f = new File(databaseLocation);
        if (!f.exists()) {
            throw new IllegalArgumentException("No directory found at " + absolutePath(databaseLocation));
        }
        if (!f.isDirectory()) {
            throw new IllegalArgumentException(databaseLocation + " is not a directory");
        }

        String psd = Paths.get(databaseLocation, p4FilePath).toString();
        File psdf = new File(psd);

        if (!psdf.exists() && !psdf.mkdirs()) { // short-circuit
            throw new IllegalStateException("Unable to create directory at " + absolutePath(psd));
        }

        return psd;
    }

    private void initBrokerState() throws LocalGremlinServerException, ClassNotFoundException, IOException,
            ReflectionException {

        LocalGremlinServer server0;
        Feather feather0;
        if (persistingDirectory == null) {
            // Start the server in in-memory mode.
            server0 = new LocalGremlinServer();
        } else {
            server0 = new LocalGremlinServer(persistingDirectory, reset, readonly);
        }

        server0.init();
        feather0 = createInjector(p4FilePath, analysers, server0);

        try {
            // Updates the injector object with the status of the completed dependencies.
            if(!reset){
                App.loadInjector(persistingDirectory, feather0);
            } else {
                System.out.println("--reset argument found, not going to load existing database");
            }
        } catch (FileNotFoundException e) {
            // There is no file at the location yet.
            // This means nothing was done before, no need to update the injector.
        }
        this.server = server0;
        this.feather = feather0;
    }


    static String absolutePath(String relativePath)  {
        File b = new File(relativePath);
        
        try {
            return b.getCanonicalPath();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Feather createInjector(String p4FilePath, Map<Class<? extends Annotation>, Object> analysers,
            LocalGremlinServer server)  {
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

	public static void loadInjector(String persistentStatePath, Feather feather)
			throws IOException, ReflectionException, ClassNotFoundException {
	
		XStream xstream = new XStream();
		ObjectInputStream in = xstream
				.createObjectInputStream(new FileInputStream(Paths.get(persistentStatePath, "state.xml").toString()));
		Map<Key, Object> singletons = (Map) in.readObject();
		in.close();
	
		try {
			Field f = Feather.class.getDeclaredField("singletons");
			f.setAccessible(true);
	
			f.set(feather, singletons);
	
			System.out.println("Deserialized the injector state: " + singletons);
	
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new ReflectionException(e);
		}
	}

    // NOTE: this is very hacky. 
    //   for serialization, we tear the inner state of the injector from a private field and serialize that.  
    //   for deserialization, we squeeze the inner state back to the private field of the injector. 
	public static void saveInjector(String persistentStatePath, Feather feather) throws IOException,
			ReflectionException {
	
		Map<Key, Object> singletons;
		try {
			Field f = Feather.class.getDeclaredField("singletons");
			f.setAccessible(true);
			singletons = (Map) f.get(feather);
			singletons.remove(Key.of(GraphTraversalSource.class));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new ReflectionException(e);
		}
	
	
        Path statePath = Paths.get(persistentStatePath, "state.xml");
        Files.deleteIfExists(statePath);
		XStream xstream = new XStream();
        ObjectOutputStream out = xstream.createObjectOutputStream(
											new FileOutputStream(
                                                    statePath.toString()));
		out.writeObject(singletons);
		out.close();
		System.out.println("Serialized the injector state: " + singletons);
	}

    private static void rightPaths() {
        if (isWindows) {
            GREMLIN_CLIENT_CONF_PATH = GREMLIN_CLIENT_CONF_PATH.substring(1);
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
