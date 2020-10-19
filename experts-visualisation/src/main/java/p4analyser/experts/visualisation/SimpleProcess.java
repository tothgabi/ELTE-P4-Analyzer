package p4analyser.experts.visualisation;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SimpleProcess {

    final Logger logger = LoggerFactory.getLogger(SimpleProcess.class);

    private final CommandLine cmdLine;

    public SimpleProcess(final Path cmd, final String... cmdArgs) {
        this.cmdLine = new CommandLine(cmd.toString());
        cmdLine.addArguments(cmdArgs);
    }

    public void addArguments(final String... cmdArgs) {
        cmdLine.addArguments(cmdArgs);
    }

    public int run() throws ExecuteException, IOException, InterruptedException {
        final DefaultExecutor executor = new DefaultExecutor();
        executor.setWatchdog(new ExecuteWatchdog(100000));
        executor.setExitValue(1);

        final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        System.out.print("External process is running... ");
        executor.execute(cmdLine, resultHandler);
        logger.info(cmdLine.toString());
        resultHandler.waitFor();
        System.out.println("Done.");
        logger.info("DONE");

        return resultHandler.getExitValue();
    }
}