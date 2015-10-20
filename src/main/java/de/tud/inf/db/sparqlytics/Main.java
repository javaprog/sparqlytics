// SPARQLytics: Multidimensional Analytics for RDF Data.
// Copyright (C) 2015  Michael Rudolf
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package de.tud.inf.db.sparqlytics;

import arq.cmd.CmdException;
import arq.cmd.TerminationException;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdGeneral;
import com.codahale.metrics.MetricRegistry;
import de.tud.inf.db.sparqlytics.parser.ParseException;
import de.tud.inf.db.sparqlytics.parser.SPARQLyticsParser;
import de.tud.inf.db.sparqlytics.parser.TokenMgrError;
import java.io.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

/**
 * The application's main class.
 *
 * @author Michael Rudolf
 */
public class Main extends CmdGeneral {
    private static Main instance;

    /**
     * Registry for collecting METRICS of the program execution.
     */
    public static final MetricRegistry METRICS = new MetricRegistry();

    /**
     * The input argument declaration.
     */
    private final ArgDecl inputDecl =
            new ArgDecl(ArgDecl.HasValue, "--input");

    /**
     * The output argument declaration.
     */
    private final ArgDecl outputDecl =
            new ArgDecl(ArgDecl.HasValue, "--output");

    /**
     * The output format argument declaration.
     */
    private final ArgDecl outputFormatDecl =
            new ArgDecl(ArgDecl.HasValue, "--outputFormat");

    private Reader input;
    private boolean interactive;
    private File output;
    private Lang outputFormat;

    /**
     * Creates a new instance for processing the given command line arguments.
     *
     * @param args the command line arguments to process
     * @throws NullPointerException if the parameter is {@code null}
     */
    private Main(String[] args) {
        super(args);
        add(inputDecl, "--input <file>", "The file to read commands from. " +
                "Reads from standard input if not specified.");
        add(outputDecl, "--output <file>", "The file to write results to. " +
                "Writes to standard output if not specified.");
        add(outputFormatDecl, "--outputFormat <fmt>",
                "The output format to use.");
    }

    @Override
    protected String getSummary() {
        return "java -jar sparqlytics-<version>-dist.jar [--input <file>] [--output <file>]";
    }

    @Override
    protected String getCommandName() {
        return "sparqlytics";
    }

    @Override
    protected void processModulesAndArgs() {
        //Evaluate command line arguments
        if (hasArg(inputDecl)) {
            try {
                input = new BufferedReader(new FileReader(getValue(inputDecl)));
            } catch (FileNotFoundException ex) {
                throw new CmdException(ex.getLocalizedMessage());
            }
        } else {
            input = new InputStreamReader(System.in);
            interactive = true;
        }
        if (hasArg(outputDecl)) {
            output = new File(getValue(outputDecl));
            if (output.isFile() && !output.canWrite()) {
                throw new CmdException("The output file cannot be written to");
            } else if (!output.isDirectory()) {
                outputFormat = RDFLanguages.filenameToLang(output.getName());
            }
        }
        if (hasArg(outputFormatDecl)) {
            String temp = getValue(outputFormatDecl);
            outputFormat = RDFLanguages.shortnameToLang(temp);
            if (outputFormat == null) {
                throw new CmdException("Unsupported output format: " + temp);
            }
        }
    }

    @Override
    protected void exec() {
        //Start processing
        SPARQLyticsParser parser = new SPARQLyticsParser(input);
        parser.setInteractive(interactive);
        parser.getSession().setSink(output);
        parser.getSession().setOutputFormat(outputFormat);
        if (interactive) {
            //Interactive mode
            try {
                parser.CubeDefinition();
            } catch (ParseException | TokenMgrError | RuntimeException ex) {
                System.err.println(ex.getLocalizedMessage());
                throw (TerminationException)
                        new TerminationException(1).initCause(ex);
            }
            boolean keepGoing = true;
            do {
                try {
                    keepGoing = parser.OLAPOperation();
                } catch (ParseException | TokenMgrError ex) {
                    System.err.println(ex.getLocalizedMessage());
                }
            } while (keepGoing);
        } else {
            //Batch mode
            try {
                parser.Start();
            } catch (ParseException | TokenMgrError | RuntimeException ex) {
                System.err.println(ex.getLocalizedMessage());
                throw (TerminationException)
                        new TerminationException(1).initCause(ex);
            }
        }
    }

    @Override
    public boolean isDebug() {
        return super.isDebug();
    }

    /**
     * Returns the instance the program was run with or a dummy instance.
     *
     * @return an instance encapsulating the program settings
     */
    public static Main getInstance() {
        if (instance == null) {
            instance = new Main(new String[0]);
        }
        return instance;
    }

    /**
     * The application's main method.
     *
     * @param args the command line arguments to process
     * @throws NullPointerException if the parameter is {@code null}
     */
    public static void main(String[] args) {
        instance = new Main(args);
        instance.mainRun();
    }
}
