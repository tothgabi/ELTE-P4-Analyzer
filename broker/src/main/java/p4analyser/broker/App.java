package p4analyser.broker;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        p4analyser.blackboard.App.main(args);
        p4analyser.experts.demo.App.main(args);

//        System.out.println(System.getProperty("user.dir"));
//        System.out.println(System.getProperty("java.class.path"));
//
//        String[] cps = System.getProperty("java.class.path").split(":");
//        for (String cp : cps) {
//            File f = new File(cp);
//            if(f.list() != null) 
//                System.out.println(Arrays.asList(f.list()));
//        }


        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        File buildFile = new File(loader.getResource("broker.xml").getPath());
        Project p = new Project();

        DefaultLogger consoleLogger = new DefaultLogger();
        consoleLogger.setErrorPrintStream(System.err);
        consoleLogger.setOutputPrintStream(System.out);
        consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
        p.addBuildListener(consoleLogger);

        p.setProperty("myProp", "alma");

        p.setUserProperty("ant.file", buildFile.getAbsolutePath());
        p.init();
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        p.addReference("ant.projectHelper", helper);
        helper.parse(p, buildFile);
        p.executeTarget(p.getDefaultTarget());

    }
}
