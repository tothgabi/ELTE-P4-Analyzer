package p4analyser.ontology.providers;

import com.beust.jcommander.Parameter;

public abstract class AppUI {

    abstract public String getCommandName(); 
    abstract public String[] getCommandNameAliases(); 

    @Parameter(description = "<location of P4 file to be analysed>" /* , required = true  */)
    public String p4FilePath;

    @Parameter(names={"--reset", "-R"}, description = "Reset persistent knowledge graph and run all the analysers again.")
    public boolean reset;

    @Parameter(help =true, names = { "-?", "--help"}, description = "Lists available options, commands, and command options." )
    public boolean help;

    @Parameter(names = { "--store", "-s" }, description = "Directory where database is stored. If not specified, in-memory database is launched. If no database exists, one is created.")
    public String databaseLocation;
}
