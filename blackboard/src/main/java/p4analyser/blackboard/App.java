package p4analyser.blackboard;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import org.apache.tinkerpop.gremlin.server.GremlinServer;
import org.apache.tinkerpop.gremlin.server.Settings;

public class App {

    public static void main(String[] args) {
        new App(args).start();
    }

    @Parameter(names={"--config", "-c"}, description = "Configuration file for GremlinServer (e.g. gremlin-server.yaml)")
    private String gremlinConfigYaml;
    private Settings gremlinConfig;
    private GremlinServer server = null;

    public App(String[] args){
        JCommander.newBuilder()
            .addObject(this)
            .build()
            .parse(args);

        try {
          this.gremlinConfig = Settings.read(gremlinConfigYaml);
        } catch (Exception e) {
            throw 
                new IllegalArgumentException(
                        String.format(
                            "Error parsing Gremlin server config file at %s:", 
                            gremlinConfigYaml),
                        e);
        }
    }

    public void start(){
        server = new GremlinServer(gremlinConfig);
        try {
            server.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start GremlinServer.", e);
        }
    }

    public void close(){
        server.stop().join();
    }
}
