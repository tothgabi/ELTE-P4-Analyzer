package p4analyser.ontology.providers;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public abstract class AppUI {

    abstract public String getCommandName(); 
    abstract public String[] getCommandNameAliases(); 

    @Parameter(description = "<location of P4 file to be analysed>" /* , required = true  */)
    public String p4FilePath;

    @Parameter(names={"--reset"}, description = "To be used together with the --store option. Run all the analysers again from scratch and overwrite the existing database.")
    public boolean reset;

    @Parameter(help =true, names = { "-?", "--help"}, description = "Lists available options, commands, and command options." )
    public boolean help;

    @Parameter(names={"--readonly", "-r"}, description = "To be used together with the --store option. Data will be loaded from persistent storage, but modifications will not be saved.")
    public boolean readonly;

    @Parameter(names = { "--store", "-s" }, description = "Directory where database is stored. If not specified, in-memory database is launched. If no database exists, one is created.", validateWith = OptionCannotBeValueValidator.class)
    public String databaseLocation;

    public static class OptionCannotBeValueValidator implements IParameterValidator {
        public void validate(String name, String value)
            throws ParameterException {
            if (value.startsWith("--") || value.startsWith("-")) {
                throw new ParameterException(value + " is not a valid argument for option " + name); 
            }
        }
    }

    @Override
    public String toString() {
        return "AppUI [databaseLocation=" + databaseLocation + ", help=" + help + ", p4FilePath=" + p4FilePath
                + ", readonly=" + readonly + ", reset=" + reset + "]";
    }

}
