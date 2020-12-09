/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */
package p4analyser.applications.visualisation;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.codejargon.feather.Provides;

import p4analyser.ontology.providers.AppUI;

@Parameters(commandDescription = "Launch the graph drawing application")
public class DrawCommand extends AppUI {


    @Override
    public String getCommandName() {
        return "draw";
    }

    @Override
    public String[] getCommandNameAliases() {
        return new String[]{};
    }

    @Parameter(names = { "-A", "--analysers"}, description = "<descriptors of analysers whose results will be included in the output subgraph>", validateWith = OptionCannotBeValueValidator.class, variableArity = true)
    public List<String> names;

    @Parameter(names = { "-F", "--format"}, description="Output subgraph file format descriptors (e.g. svg).", validateWith = OptionCannotBeValueValidator.class)
    public List<String> format;

    @Parameter(names = { "-o", "--output"}, description="Preferred location of output subgraph file.", validateWith = OptionCannotBeValueValidator.class)
    public Boolean output = false;

    @Override
    public String toString() {
        return "DrawCommand [super= " + super.toString()+ ", format=" + format + ", help=" + help + ", output=" + output + ", names=" + names
                + "]";
    }
}