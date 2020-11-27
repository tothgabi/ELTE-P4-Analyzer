package p4analyser.broker;

import com.beust.jcommander.Parameter;

public class BaseCommand {

    @Parameter(names={"--input", "-i"}, description = "<location of P4 file to be analysed>" /* required = true */ )
    String P4_FILEPATH;

    @Parameter(names={"--reset", "-R"}, description = "Reset persistent knowledge graph and run all the analysers again.")
    boolean reset;

    @Parameter(help =true, names = { "-?", "--help"}, description = "Lists available options, commands, and command options." )
    boolean help;
}
