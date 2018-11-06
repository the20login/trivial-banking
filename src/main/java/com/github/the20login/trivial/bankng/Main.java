package com.github.the20login.trivial.bankng;

import com.github.the20login.trivial.bankng.processing.ProcessingImpl;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;
import org.rapidoid.http.MediaType;
import org.rapidoid.http.Self;
import org.rapidoid.http.customize.defaults.DefaultErrorHandler;
import org.rapidoid.integrate.GuiceBeans;
import org.rapidoid.integrate.Integrate;
import org.rapidoid.setup.App;
import org.rapidoid.setup.My;
import org.rapidoid.setup.On;
import org.rapidoid.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingImpl.class);

    @Option(name = "--address", aliases = "-a", usage = "bind address")
    private String address = "0.0.0.0";

    @Option(name = "--port", aliases = "-p", usage = "bind port")
    private int port = 8080;

    @Option(name = "--help", aliases = "-h", hidden = true)
    private boolean showHelp = false;

    public Main(String[] args) throws IOException {

        CmdLineParser parser = new CmdLineParser(this);
        parser.getProperties().withShowDefaults(true);
        try {
            parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println();
            parser.printUsage(System.err);
            System.err.println();

            return;
        }

        if (showHelp) {
            parser.printUsage(System.out);
            return;
        }

        On.changes().ignore();

        GuiceBeans beans = Integrate.guice(new ProductionModule());
        DefaultErrorHandler errorHandler = new DefaultErrorHandler();
        My.errorHandler((req, resp, error) -> {
            return errorHandler.handleError(req, resp.contentType(MediaType.JSON), error);
        });
        On.address(address).port(port);
        App.register(beans);
        LOG.info("Running at {}:{}",address, port);
    }

    public static void main(String[] args) {
        try {
            new Main(args);
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }
}
