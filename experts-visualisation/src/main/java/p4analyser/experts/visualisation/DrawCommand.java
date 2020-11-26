package p4analyser.experts.visualisation;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Launch the graph drawing application")
public class DrawCommand {
    @Parameter(description = "<descriptors of analysers whose results will be included in the output subgraph>")
    private List<String> names;

    @Parameter(names = { "-F", "--format"}, description="Output subgraph file format descriptor (e.g. svg).")
    private String format;

    @Parameter(names = { "-o", "--output"}, description="Preferred location of output subgraph file.")
    private Boolean interactive = false;

    @Parameter(names = { "-?", "--help" }, help =true)
    private boolean help;
}