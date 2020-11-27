package p4analyser.applications.visualisation;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.codejargon.feather.Provides;

@Parameters(commandDescription = "Launch the graph drawing application")
public class DrawCommand {
    @Parameter(description = "<descriptors of analysers whose results will be included in the output subgraph>")
    public List<String> names;

    @Parameter(names = { "-F", "--format"}, description="Output subgraph file format descriptor (e.g. svg).")
    public String format;

    @Parameter(names = { "-o", "--output"}, description="Preferred location of output subgraph file.")
    public Boolean output = false;

    @Parameter(names = { "-?", "--help" }, help =true)
    public boolean help;

    @Override
    public String toString() {
        return "DrawCommand [format=" + format + ", help=" + help + ", output=" + output + ", names=" + names
                + "]";
    }

    @Provides
    public DrawCommand provide(){
        return this;
    }
}