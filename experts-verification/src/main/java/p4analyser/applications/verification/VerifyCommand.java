/**
 * Copyright 2020, Eötvös Loránd University.
 * All rights reserved.
 */

package p4analyser.applications.verification;

import com.beust.jcommander.Parameters;

import p4analyser.ontology.providers.AppUI;

@Parameters(commandDescription = "Launch the verification application with the input file")
public class VerifyCommand extends AppUI {


    @Override
    public String getCommandName() {
        return "verify";
    }

    @Override
    public String[] getCommandNameAliases() {
        return new String[]{"v"};
    }
}