/**
 * 3GPP Bulk CM XML to CSV Parser.
 *
 * @author Bodastage<info @ bodastage.com>
 * @version 1.0.0
 * @see http://github.com/bodastage/boda-bulkcmparsers
 */
package com.bodastage.boda_bulkcmparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BodaBulkCMParser {

    BodaBulkCMParser() {
        MoToFileNameMap.put("EutranFreqRelation", "EutranFreqRelation_UtranCell");
        MoToFileNameMap.put("vsDataEutranFreqRelation", "vsDataEutranFreqRelation_UtranCell");
    }

    /**
     * Current release version
     * <p>
     * Since 1.3.0
     */
    final static String VERSION = "2.2.5";


    private static final Logger LOGGER = LoggerFactory.getLogger(BodaBulkCMParser.class);

    /**
     * Tracks XML elements.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    Stack xmlTagStack = new Stack();


    /**
     * Tracks how deep a Management Object is in the XML doc hierarchy.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    Integer depth = 0;

    /**
     * Tracks XML attributes per Management Objects.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    Map<Integer, Map<String, String>> xmlAttrStack = new LinkedHashMap<Integer, Map<String, String>>();

    /**
     * Tracks Managed Object specific 3GPP attributes.
     * <p>
     * This tracks every thing within <xn:attributes>...</xn:attributes>.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    Map<Integer, Map<String, String>> threeGPPAttrStack = new LinkedHashMap<Integer, Map<String, String>>();

    /**
     * Marks start of processing per MO attributes.
     * <p>
     * This is set to true when xn:attributes is encountered. It's set to false
     * when the corresponding closing tag is encountered.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    boolean attrMarker = false;

    /**
     * Tracks the depth of VsDataContainer tags in the XML document hierarchy.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    int vsDCDepth = 0;

    /**
     * Maps of vsDataContainer instances to vendor specific data types.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    Map<String, String> vsDataContainerTypeMap = new LinkedHashMap<String, String>();

    /**
     * Tracks current vsDataType if not null
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    String vsDataType = null;

    /**
     * vsDataTypes stack.
     *
     * @version 1.1.0
     * @since 1.0.0
     */
    Map<String, String> vsDataTypeStack = new LinkedHashMap<String, String>();

    /**
     * Real stack to push and pop vsDataType attributes.
     * <p>
     * This is used to track multivalued attributes and attributes with children
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    Stack vsDataTypeRlStack = new Stack();

    /**
     * Real stack to push and pop xn:attributes.
     * <p>
     * This is used to track multivalued attributes and attributes with children
     *
     * @version 1.0.0
     * @since 1.0.2
     */
    Stack xnAttrRlStack = new Stack();

    /**
     * Multi-valued parameter separator.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    String multiValueSeparetor = ";";

    /**
     * For attributes with children, define parameter-child separator
     *
     * @since 1.0.0
     */
    String parentChildAttrSeperator = "_";

    /**
     * Output file print writers
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    Map<String, PrintWriter> outputFilePW = new LinkedHashMap<String, PrintWriter>();

    /**
     * Output directory.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    String outputDirectory = "/tmp";

    /**
     * Limit the number of iterations for testing.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    int testCounter = 0;

    /**
     * Start element tag.
     * <p>
     * Use in the character event to determine the data parent XML tag.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    String startElementTag = "";

    /**
     * Start element NS prefix.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    String startElementTagPrefix = "";

    /**
     * Tag data.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    String tagData = "";

    /**
     * Tracking parameters with children under vsDataSomeMO.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    Map<String, String> parentChildParameters = new LinkedHashMap<String, String>();

    /**
     * Tracking parameters with children in xn:attributes.
     *
     * @version 1.0.0
     * @since 1.0.2
     */
    Map<String, String> attrParentChildMap = new LinkedHashMap<String, String>();

    /**
     * A map of MO to printwriter.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    Map<String, PrintWriter> outputVsDataTypePWMap = new LinkedHashMap<String, PrintWriter>();

    /**
     * A map of 3GPP MOs to their file print writers.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    Map<String, PrintWriter> output3GPPMOPWMap = new LinkedHashMap<String, PrintWriter>();

    /**
     * Bulk CM XML file name. The file we are parsing.
     */
    String bulkCMXMLFile;

    String bulkCMXMLFileBasename;

    /**
     * Tracks Managed Object attributes to write to file. This is dictated by
     * the first instance of the MO found.
     *
     * @TODO: Handle this better.
     * @version 1.0.0
     * @since 1.0.3
     */
    Map<String, Stack> moColumns = new LinkedHashMap<String, Stack>();

    /**
     * Tracks the IDs of the parent elements
     *
     * @since 1.2.0
     */
    Map<String, Stack> moColumnsParentIds = new LinkedHashMap<String, Stack>();

    /**
     * A map of 3GPP attributes to the 3GPP MOs
     *
     * @since 1.3.0
     */
    Map<String, Stack> moThreeGPPAttrMap = new LinkedHashMap<String, Stack>();

    /**
     * This stores the values of the 3GPP MO attributes to be used when combining
     * a 3GPP MO with a vendor specific MO (i.e. vsData...).
     *
     * @since 2.1.0
     */
    Map<String, String> threeGPPAttrValues = new LinkedHashMap<String, String>();

    /**
     * This is used to renamed some of the generated csv files to prevent name
     * conflict on windows where paths are case insensitive.
     */
    Map<String, String> MoToFileNameMap = new LinkedHashMap<String, String>();

    /**
     * The file/directory to be parsed.
     *
     * @since 1.1.0
     */
    private String dataSource;

    /**
     * The file being parsed.
     *
     * @since 1.1.0
     */
    private String dataFile;

    /**
     * The base file name of the file being parsed.
     *
     * @since 1.1.0
     */
    private String baseFileName = "";

    private String dateTime = "";

    private Boolean separateVendorAttributes = true;

    /**
     * Parser start time.
     *
     * @version 1.1.0
     * @since 1.1.0
     */
    final long startTime = System.currentTimeMillis();

    private int parserState = ParserStates.EXTRACTING_PARAMETERS;

    /**
     * Extract managed objects and their parameters
     */
    private Boolean extractParametersOnly = false;

    /**
     * Add meta fields to each MO.
     * FILENAME,DATETIME,NE_TECHNOLOGY,NE_VENDOR,NE_VERSION,NE_TYPE
     */
    private Boolean extractMetaFields = false;

    /**
     * parameter selection file
     */
    private String parameterFile = null;


    /**
     * This is used to mark when processing is still inside the children of a
     * a parameter - child scenario. It is useful when one of the children has
     * the same name as the parent.
     * <moname>
     * <chid1>someValue</child1>
     * ...
     * <moname>someValue</moname>
     * ...
     * <child/>someValue<childN>
     * </moName>
     */
    private Boolean inParentChildTag = false;

    public void setExtractParametersOnly(Boolean bool) {
        extractParametersOnly = bool;
    }

    public void setExtractMetaFields(Boolean bool) {
        extractMetaFields = bool;
    }

    /**
     * Get the date
     *
     * @param inputFilename
     */
    public void getDateTime(String inputFilename) {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();

            XMLEventReader eventReader = factory.createXMLEventReader(
                    new FileReader(inputFilename));
            baseFileName = bulkCMXMLFileBasename = getFileBasename(inputFilename);

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        StartElement startElement = event.asStartElement();
                        String qName = startElement.getName().getLocalPart();
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        if (qName.equals("fileFooter")) {
                            while (attributes.hasNext()) {
                                Attribute attribute = attributes.next();
                                if (attribute.getName().toString().equals("dateTime")) {
                                    dateTime = attribute.getValue();
                                }
                            }
                        }

                        break;
                }


            }
        } catch (Exception e) {

        }
    }

    /**
     * Extract parameter list from  parameter file
     *
     * @param filename
     */
    public void getParametersToExtract(String filename) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        for (String line; (line = br.readLine()) != null; ) {
            String[] moAndParameters = line.split(":");
            String mo = moAndParameters[0];
            String[] parameters = moAndParameters[1].split(",");

            Stack parameterStack = new Stack();
            for (int i = 0; i < parameters.length; i++) {
                parameterStack.push(parameters[i]);
            }

            if (mo.startsWith("vsData")) {
                moColumns.put(mo, parameterStack);
                moColumnsParentIds.put(mo, new Stack());
            } else {
                moThreeGPPAttrMap.put(mo, parameterStack);
            }

        }

        //Move to the parameter value extraction stage
        //parserState = ParserStates.EXTRACTING_VALUES;
    }

    /**
     * @param inputFilename
     */
    public void parseFile(String inputFilename) throws FileNotFoundException, XMLStreamException, UnsupportedEncodingException {

        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);

        XMLEventReader eventReader = factory.createXMLEventReader(
                new FileReader(inputFilename));
        baseFileName = bulkCMXMLFileBasename = getFileBasename(inputFilename);

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    startElementEvent(event);
                    break;
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.CHARACTERS:
                    characterEvent(event);
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    endELementEvent(event);
                    break;
            }
        }

    }

    public void setParameterFile(String filename) {
        parameterFile = filename;
    }

    /**
     * Set multi-value separator
     *
     * @since 2.2.0
     */
    public void setMultiValueSeparator(String mvSeparator) {
        multiValueSeparetor = mvSeparator;
    }

    /**
     * Separate vendor specifiic attributes from 3GPP attributes
     *
     * @since 2.1.0
     */
    public void setSeparateVendorAttributes(Boolean separate) {
        separateVendorAttributes = separate;
    }

    /**
     * @param args the command line arguments
     * @version 1.0.1
     * @since 1.0.0
     */
    public static void main(String[] args) {

        //Define
        Options options = new Options();
        CommandLine cmd = null;
        String outputDirectory = null;
        String inputFile = null;
        String parameterConfigFile = null;
        Boolean onlyExtractParameters = false;
        Boolean showHelpMessage = false;
        Boolean showVersion = false;
        Boolean separateVsData = false; //separaete 3GPP standard attributes and vendor specific attr
        Boolean attachMetaFields = false; //Attach mattachMetaFields FILENAME,DATETIME,NE_TECHNOLOGY,NE_VENDOR,NE_VERSION,NE_TYPE

        //Multi-valued separator
        String mvSeparator = ";";

        try {
            options.addOption("p", "extract-parameters", false, "extract only the managed objects and parameters");
            options.addOption("v", "version", false, "display version");
            options.addOption("m", "meta-fields", false, "add meta fields to extracted parameters. FILENAME,DATETIME");
            options.addOption(Option.builder("i")
                    .longOpt("input-file")
                    .desc("input file or directory name")
                    .hasArg()
                    .argName("INPUT_FILE").build());
            options.addOption(Option.builder("o")
                    .longOpt("output-directory")
                    .desc("output directory name")
                    .hasArg()
                    .argName("OUTPUT_DIRECTORY").build());
            options.addOption(Option.builder("c")
                    .longOpt("parameter-config")
                    .desc("parameter configuration file")
                    .hasArg()
                    .argName("PARAMETER_CONFIG").build());
            options.addOption(Option.builder("d")
                    .longOpt("multivalue-separator")
                    .desc("Specify multi value separator. Default is \";\"")
                    .hasArg()
                    .argName("MV_SEPARATOR").build());
            options.addOption("s", "separate-vsdata", false, "Separate vendor specific data");
            options.addOption("h", "help", false, "show help");

            //Parse command line arguments
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                showHelpMessage = true;
            }

            if (cmd.hasOption("s")) {
                separateVsData = true;
            }

            if (cmd.hasOption("v")) {
                showVersion = true;
            }

            if (cmd.hasOption("d")) {
                mvSeparator = cmd.getOptionValue("d");
            }


            if (cmd.hasOption('o')) {
                outputDirectory = cmd.getOptionValue("o");
            }

            if (cmd.hasOption('i')) {
                inputFile = cmd.getOptionValue("i");
            }

            if (cmd.hasOption('c')) {
                parameterConfigFile = cmd.getOptionValue("c");
            }

            if (cmd.hasOption('p')) {
                onlyExtractParameters = true;
            }

            if (cmd.hasOption('m')) {
                attachMetaFields = true;
            }

        } catch (IllegalArgumentException e) {

        } catch (ParseException ex) {
//            java.util.logging.Logger.getLogger(HuaweiCMObjectParser.class.getName()).log(Level.SEVERE, null, ex);
        }


        try {

            if (showVersion == true) {
                System.out.println(VERSION);
                System.out.println("Copyright (c) 2019 Bodastage Solutions(https://www.bodastage.com)");
                System.exit(0);
            }

            //show help
            if (showHelpMessage == true ||
                    inputFile == null ||
                    (outputDirectory == null && onlyExtractParameters == false)) {
                HelpFormatter formatter = new HelpFormatter();
                String header = "Parses BulkCM configuration data file XML to csv\n\n";
                String footer = "\n";
                footer += "Examples: \n";
                footer += "java -jar boda-bulkcmparser.jar -i bulkcm_dump.xml -o out_folder\n";
                footer += "java -jar boda-bulkcmparser.jar -i input_folder -o out_folder\n";
                footer += "java -jar boda-bulkcmparser.jar -i input_folder -p\n";
                footer += "java -jar boda-bulkcmparser.jar -i input_folder -p -m\n";
                footer += "\nCopyright (c) 2019 Bodastage Solutions(https://www.bodastage.com)";
                formatter.printHelp("java -jar boda-bulkcmparser.jar", header, options, footer);
                System.exit(0);
            }

            //Confirm that the output directory is a directory and has write 
            //privileges
            if (outputDirectory != null) {
                File fOutputDir = new File(outputDirectory);
                if (!fOutputDir.isDirectory()) {
                    System.err.println("ERROR: The specified output directory is not a directory!.");
                    System.exit(1);
                }

                if (!fOutputDir.canWrite()) {
                    System.err.println("ERROR: Cannot write to output directory!");
                    System.exit(1);
                }
            }


            //Get parser instance
            BodaBulkCMParser cmParser = new BodaBulkCMParser();

            cmParser.setSeparateVendorAttributes(separateVsData);

            if (onlyExtractParameters == true) {
                cmParser.setExtractParametersOnly(true);
            }


            if (attachMetaFields == true) {
                cmParser.setExtractMetaFields(true);
            }

            if (parameterConfigFile != null) {
                File f = new File(parameterConfigFile);
                if (f.isFile()) {
                    cmParser.setParameterFile(parameterConfigFile);
                    cmParser.getParametersToExtract(parameterConfigFile);
                    cmParser.parserState = ParserStates.EXTRACTING_VALUES;
                }
            }

            cmParser.setMultiValueSeparator(mvSeparator);
            cmParser.setDataSource(inputFile);
            if (outputDirectory != null) cmParser.setOutputDirectory(outputDirectory);

            cmParser.parse();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Parser entry point
     *
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public void parse() throws XMLStreamException, FileNotFoundException, UnsupportedEncodingException {
        //Extract parameters
        if (parserState == ParserStates.EXTRACTING_PARAMETERS) {
            processFileOrDirectory();

            parserState = ParserStates.EXTRACTING_VALUES;
        }

        //Reset variables
        resetVariables();

        //Extracting values
        if (parserState == ParserStates.EXTRACTING_VALUES) {
            processFileOrDirectory();
            parserState = ParserStates.EXTRACTING_DONE;
        }

        closeMOPWMap();

        printExecutionTime();
    }

    /**
     * Reset parser variables before next file
     */
    public void resetVariables() {
        //Reset variables
        vsDataType = null;
        vsDataTypeStack.clear();
        vsDataTypeRlStack.clear();
        xmlAttrStack.clear();
        xmlTagStack.clear();
        startElementTag = null;
        startElementTagPrefix = "";
        attrMarker = false;
        depth = 0;
    }

    /**
     * Determines if the source data file is a regular file or a directory and
     * parses it accordingly
     *
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @version 1.0.0
     * @since 1.1.0
     */
    public void processFileOrDirectory()
            throws XMLStreamException, FileNotFoundException, UnsupportedEncodingException {
        //this.dataFILe;
        Path file = Paths.get(this.dataSource);
        boolean isRegularExecutableFile = Files.isRegularFile(file)
                & Files.isReadable(file);

        boolean isReadableDirectory = Files.isDirectory(file)
                & Files.isReadable(file);

        if (isRegularExecutableFile) {
            this.setFileName(this.dataSource);
            baseFileName = getFileBasename(this.dataFile);
            if (parserState == ParserStates.EXTRACTING_PARAMETERS) {
                System.out.print("Extracting parameters from " + this.baseFileName + "...");
            } else {
                System.out.print("Parsing " + this.baseFileName + "...");
            }

            //Get date time 
            if (parameterFile != null) {
                getDateTime(this.dataSource);
            }

            //Parse file
            this.parseFile(this.dataSource);

            if (parserState == ParserStates.EXTRACTING_PARAMETERS) {
                System.out.println("Done.");
            } else {
                System.out.println("Done.");
                //System.out.println(this.baseFileName + " successfully parsed.\n");
            }
        }

        if (isReadableDirectory) {
            File directory = new File(this.dataSource);

            //get all the files from a directory
            File[] fList = directory.listFiles();

            for (File f : fList) {
                this.setFileName(f.getAbsolutePath());
                try {
                    baseFileName = getFileBasename(this.dataFile);
                    if (parserState == ParserStates.EXTRACTING_PARAMETERS) {
                        System.out.print("Extracting parameters from " + this.baseFileName + "...");
                    } else {
                        System.out.print("Parsing " + this.baseFileName + "...");
                    }

                    //Get date time 
                    if (parameterFile != null) {
                        getDateTime(f.getAbsolutePath());
                    }

                    //Parse dump file 
                    this.parseFile(f.getAbsolutePath());

                    if (parserState == ParserStates.EXTRACTING_PARAMETERS) {
                        System.out.println("Done.");
                    } else {
                        System.out.println("Done.");
                        //System.out.println(this.baseFileName + " successfully parsed.\n");
                    }

                } catch (Exception e) {
                    //LOGGER.info("depth:" + depth);
                    //LOGGER.info("xmlTagStack:" + xmlTagStack.toString());
                    //LOGGER.info("xmlAttrStack:" + xmlAttrStack.toString());
                    System.out.println(e.getMessage());
                    System.out.println("Skipping file: " + this.baseFileName + "\n");

                    //Reset variables if a file is skipped
                    resetVariables();
                }
            }
        }

    }

    /**
     * Collect MO Parameters
     *
     * @param inputFile
     * @param outputDirectory
     */
    private void collectMOParameters(String inputFile, String outputDirectory) {

        try {
            //Confirm that the output directory is a directory and has write 
            //privileges
            File fOutputDir = new File(outputDirectory);
            if (!fOutputDir.isDirectory()) {
                System.err.println("ERROR: The specified output directory is not a directory!.");
                System.exit(1);
            }

            if (!fOutputDir.canWrite()) {
                System.err.println("ERROR: Cannot write to output directory!");
                System.exit(1);
            }


            XMLInputFactory factory = XMLInputFactory.newInstance();

            XMLEventReader eventReader = factory.createXMLEventReader(
                    new FileReader(bulkCMXMLFile));
            bulkCMXMLFileBasename = getFileBasename(bulkCMXMLFile);

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        startElementEvent(event);
                        break;
                    case XMLStreamConstants.SPACE:
                    case XMLStreamConstants.CHARACTERS:
                        characterEvent(event);
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        endELementEvent(event);
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("ERROR:" + e.getMessage());
            System.exit(1);
        } catch (XMLStreamException e) {
            System.err.println("ERROR:" + e.getMessage());
            System.exit(1);
        } catch (UnsupportedEncodingException e) {
            System.err.println("ERROR:" + e.getMessage());
            System.exit(1);
        }

    }

    /**
     * Handle start element event.
     *
     * @param xmlEvent
     * @version 1.0.0
     * @since 1.0.0
     */
    public void startElementEvent(XMLEvent xmlEvent) {

        StartElement startElement = xmlEvent.asStartElement();
        String qName = startElement.getName().getLocalPart();
        String prefix = startElement.getName().getPrefix();

        startElementTag = qName;
        startElementTagPrefix = prefix;

        Iterator<Attribute> attributes = startElement.getAttributes();

        if (qName.equals("fileFooter") && ParserStates.EXTRACTING_PARAMETERS == parserState) {
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().toString().equals("dateTime")) {
                    dateTime = attribute.getValue();
                }
            }
        }

        //E1:0. xn:VsDataContainer encountered
        //Push vendor speicific MOs to the xmlTagStack
        if (qName.equalsIgnoreCase("VsDataContainer")) {
            vsDCDepth++;
            depth++;

            String vsDCTagWithDepth = "VsDataContainer_" + vsDCDepth;
            xmlTagStack.push(vsDCTagWithDepth);

            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().toString().equals("id")) {
                    Map<String, String> m = new LinkedHashMap<String, String>();
                    m.put("id", attribute.getValue());
                    xmlAttrStack.put(depth, m);
                }
            }

            vsDataType = null;
            vsDataTypeStack.clear();
            vsDataTypeRlStack.clear();
            return;
        }

        //E1:1 
        if (!prefix.equals("xn") && qName.startsWith("vsData")) {
            vsDataType = qName;

            String vsDCTagWithDepth = "VsDataContainer_" + vsDCDepth;
            vsDataContainerTypeMap.put(vsDCTagWithDepth, qName);

            return;
        }

        //E1.2
        if (null != vsDataType) {

            //Handle parameters with children
            //Update vsDataTypeStack and vsDataTypeRlStack
            if (vsDataTypeRlStack.size() > 0) {
                String parentParameter = vsDataTypeRlStack.get(0).toString();
                String childParameter = qName;
                String param = parentParameter + "_" + childParameter;
                if (!vsDataTypeStack.containsKey(param)) {
                    vsDataTypeStack.put(param, null);
                }
                vsDataTypeRlStack.push(qName);

                return;
            }

            //Handle parameters with no children
            if (!vsDataTypeStack.containsKey(qName)) {
                vsDataTypeStack.put(qName, null);
                vsDataTypeRlStack.push(qName);
            }
            return;
        }

        //E1.3
        if (qName.equals("attributes")) {
            attrMarker = true;
            return;
        }

        //E1.4
        if (xmlTagStack.contains(qName)) {
            depth++;
            Integer occurences = getXMLTagOccurences(qName) + 1;
            String newTagName = qName + "_" + occurences;
            xmlTagStack.push(newTagName);

            //Add XML attributes to the XML Attribute Stack.
            //@TODO: This while block is repeated below. The 2 should be combined
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();

                if (xmlAttrStack.containsKey(depth)) {
                    Map<String, String> mm = xmlAttrStack.get(depth);
                    mm.put(attribute.getName().getLocalPart(), attribute.getValue());
                    xmlAttrStack.put(depth, mm);
                } else {
                    Map<String, String> m = new LinkedHashMap<String, String>();
                    m.put(attribute.getName().getLocalPart(), attribute.getValue());
                    xmlAttrStack.put(depth, m);
                }
            }

            return;
        }

        //E1.5
        if (attrMarker == true && vsDataType == null) {

            //Tracks hierachy of tags under xn:attributes.
            xnAttrRlStack.push(qName);

            Map<String, String> m = new LinkedHashMap<String, String>();
            if (threeGPPAttrStack.containsKey(depth)) {
                m = threeGPPAttrStack.get(depth);

                //Check if the parameter is already in the stack so that we dont
                //over write it.
                if (!m.containsKey(qName)) {
                    m.put(qName, null);
                    threeGPPAttrStack.put(depth, m);
                }
            } else {
                m.put(qName, null); //Initial value null
                threeGPPAttrStack.put(depth, m);
            }
            return;
        }

        //E1.6
        //Push 3GPP Defined MOs to the xmlTagStack
        depth++;
        xmlTagStack.push(qName);
        xmlAttrStack.put(depth, new LinkedHashMap<String, String>());
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();

            if (xmlAttrStack.containsKey(depth)) {
                Map<String, String> mm = xmlAttrStack.get(depth);
                mm.put(attribute.getName().getLocalPart(), attribute.getValue());
                xmlAttrStack.put(depth, mm);
            } else {
                Map<String, String> m = new LinkedHashMap<String, String>();
                m.put(attribute.getName().getLocalPart(), attribute.getValue());
                xmlAttrStack.put(depth, m);
            }
        }
    }

    /**
     * Handle character events.
     *
     * @param xmlEvent
     * @version 1.0.0
     * @since 1.0.0
     */
    public void characterEvent(XMLEvent xmlEvent) {
        Characters characters = xmlEvent.asCharacters();
        if (!characters.isWhiteSpace()) {
            tagData = characters.getData();
        }
    }

    public void endELementEvent(XMLEvent xmlEvent)
            throws FileNotFoundException, UnsupportedEncodingException {

        EndElement endElement = xmlEvent.asEndElement();
        String prefix = endElement.getName().getPrefix();
        String qName = endElement.getName().getLocalPart();

        startElementTag = "";

        //E3:1 </xn:VsDataContainer>
        if (qName.equalsIgnoreCase("VsDataContainer")) {
            String vsDCTag = "VsDataContainer_" + vsDCDepth;
            xmlTagStack.pop();
            if (xmlAttrStack.containsKey(depth)) {
                xmlAttrStack.remove(depth);
                threeGPPAttrStack.remove(depth);
            }
            if (vsDataContainerTypeMap.containsKey(vsDCDepth)) vsDataContainerTypeMap.remove(vsDCDepth);
            vsDCDepth--;
            depth--;
            return;
        }

        //We are at the end of </attributes> in 3GPP tag
        if (qName.equals("attributes") && !xmlTagStack.peek().toString().startsWith("VsDataContainer")) {
            //Collect values for use when separateVsData is false
            if (parserState == ParserStates.EXTRACTING_VALUES &&
                    separateVendorAttributes == false &&
                    vsDataType == null) {
                int xmlTagStackSize = xmlTagStack.size();
                //@TODO: Keep copy of attribute values
                if (xmlTagStackSize > 1) {

                    String tagBeforeCurrentVsContainer = xmlTagStack.get(xmlTagStackSize - 1).toString();
                    saveThreeGPPAttrValues(tagBeforeCurrentVsContainer);
                }
            }
        }

        //3.2 </xn:attributes>
        if (qName.equals("attributes")) {
            attrMarker = false;

            if (parserState == ParserStates.EXTRACTING_PARAMETERS && vsDataType == null) {
                updateThreeGPPAttrMap();
            }
            return;
        }

        //E3:3 xx:vsData<VendorSpecificDataType>
        if (qName.startsWith("vsData") && !qName.equalsIgnoreCase("VsDataContainer")
                && !prefix.equals("xn")) { //This skips xn:vsDataType

            if (ParserStates.EXTRACTING_PARAMETERS == parserState) {
                collectVendorMOColumns();
            } else {
                processVendorAttributes();
            }

            vsDataType = null;
            vsDataTypeStack.clear();
            return;
        }

        //E3:4
        //Process parameters under <bs:vsDataSomeMO>..</bs:vsDataSomeMo>
        if (vsDataType != null && attrMarker == true) {//We are processing vsData<DataType> attributes
            String newTag = qName;
            String newValue = tagData;

            //Note end of the parent-child
            if (vsDataTypeRlStack.size() == 1 && inParentChildTag == true) {
                inParentChildTag = false;
            }

            //Handle attributes with children
            //inParentChildTag== false, means we have completed processing the children
            if (parentChildParameters.containsKey(qName) && inParentChildTag == false) {//End of parent tag

                //Ware at the end of the parent tag so we remove the mapping
                //as the child values have already been collected in 
                //vsDataTypeStack.
                parentChildParameters.remove(qName);

                //The top most value on the stack should be qName
                if (vsDataTypeRlStack.size() > 0) {
                    vsDataTypeRlStack.pop();
                }

                //Remove the parent tag from the stack so that we don't output 
                //data for it. It's values are taked care of by its children.
                vsDataTypeStack.remove(qName);
                return;
            }

            //If size is greater than 1, then there is parent with chidren
            if (vsDataTypeRlStack.size() > 1) {
                int len = vsDataTypeRlStack.size();
                String parentTag = vsDataTypeRlStack.get(len - 2).toString();
                newTag = parentTag + parentChildAttrSeperator + qName;
                inParentChildTag = true;

                //Store the parent and it's child
                parentChildParameters.put(parentTag, qName);

            }

            //Handle multivalued paramenters
            if (vsDataTypeStack.containsKey(newTag)) {
                if (vsDataTypeStack.get(newTag) != null) {
                    newValue = vsDataTypeStack.get(newTag) + multiValueSeparetor + tagData;
                }
            }

            //@TODO: Handle cases of multi values parameters and parameters with children
            //For now continue as if they do not exist
            vsDataTypeStack.put(newTag, newValue);
            tagData = "";
            if (vsDataTypeRlStack.size() > 0) {
                vsDataTypeRlStack.pop();
            }
        }

        //E3.5
        //Process tags under xn:attributes.
        if (attrMarker == true && vsDataType == null) {
            String newValue = tagData;
            String newTag = qName;

            //Handle attributes with children.Do this when parent end tag is 
            //encountered.
            if (attrParentChildMap.containsKey(qName)) { //End of parent tag
                //Remove parent child map
                attrParentChildMap.remove(qName);

                //Remove the top most value from the stack.
                xnAttrRlStack.pop();

                //Remove the parent from the threeGPPAttrStack so that we 
                //don't output data for it.
                Map<String, String> treMap = threeGPPAttrStack.get(depth);
                treMap.remove(qName);
                threeGPPAttrStack.put(depth, treMap);

                return;
            }

            //Handle parent child attributes. Get the child value
            int xnAttrRlStackLen = xnAttrRlStack.size();
            if (xnAttrRlStackLen > 1) {
                String parentXnAttr
                        = xnAttrRlStack.get(xnAttrRlStackLen - 2).toString();
                newTag = parentXnAttr + parentChildAttrSeperator + qName;

                //Store parent child map
                attrParentChildMap.put(parentXnAttr, qName);

                //Remove the child tag from the 3gpp xnAttribute stack
                Map<String, String> cMap = threeGPPAttrStack.get(depth);
                if (cMap.containsKey(qName)) {
                    cMap.remove(qName);
                    threeGPPAttrStack.put(depth, cMap);
                }
            }

            Map<String, String> m = new LinkedHashMap<String, String>();
            m = threeGPPAttrStack.get(depth);

            //For multivaluted attributes , first check that the tag already 
            //exits.
            if (m.containsKey(newTag) && m.get(newTag) != null) {
                String oldValue = m.get(newTag);
                String val = oldValue + multiValueSeparetor + newValue;
                m.put(newTag, val);
            } else {
                m.put(newTag, newValue);
            }

            threeGPPAttrStack.put(depth, m);
            tagData = "";
            xnAttrRlStack.pop();
            return;
        }

        //E3:6 
        //At this point, the remaining XML elements are 3GPP defined Managed 
        //Objects. 
        if (xmlTagStack.contains(qName)) {
            String theTag = qName;

            //@TODO: This occurences check does not appear to be of any use; test 
            // and remove if not needed.
            int occurences = getXMLTagOccurences(qName);
            if (occurences > 1) {
                theTag = qName + "_" + occurences;
            }

            //Extracting parameter value stage.
            //Printout values ifthere is no matching vsDataMO  and separateVsData is true
            String vsDataMO = "vsData" + qName; //This create vsDataMO
            if (parserState != ParserStates.EXTRACTING_PARAMETERS &&
                    (separateVendorAttributes == true ||
                            (!moColumns.containsKey(vsDataMO) && separateVendorAttributes == false)
                    )
            ) {
                process3GPPAttributes();
            }

            threeGPPAttrValues.clear();
            xmlTagStack.pop();
            xmlAttrStack.remove(depth);
            threeGPPAttrStack.remove(depth);
            depth--;
        }
        //parentChildParameters.clear();


    }

    /**
     * Get the number of occurrences of an XML tag in the xmlTagStack.
     * <p>
     * This is used to handle cases where XML elements with the same name are
     * nested.
     *
     * @param tagName String The XML tag name
     * @return Integer Number of tag occurrences.
     * @version 1.0.0
     * @since 1.0.0
     */
    public Integer getXMLTagOccurences(String tagName) {
        int tagOccurences = 0;
        Iterator<String> iter = xmlTagStack.iterator();
        while (iter.hasNext()) {
            String tag = iter.next();
            String regex = "^(" + tagName + "|" + tagName + "_\\d+)$";

            if (tag.matches(regex)) {
                tagOccurences++;
            }
        }
        return tagOccurences;
    }

    /**
     * Returns 3GPP defined Managed Objects(MOs) and their attribute values.
     * This method is called at the end of processing 3GPP attributes.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    public void process3GPPAttributes()
            throws FileNotFoundException, UnsupportedEncodingException {

        String mo = xmlTagStack.peek().toString();

        //Holds parameter-value map before printing
        Map<String, String> xmlTagValues = new LinkedHashMap<String, String>();

        if (parameterFile != null && !moThreeGPPAttrMap.containsKey(mo)) {
            return;
        }

        String paramNames = "FILENAME,DATETIME";
        String paramValues = bulkCMXMLFileBasename + "," + dateTime;

        Stack<String> ignoreInParameterFile = new Stack();

        //Parent IDs
        for (int i = 0; i < xmlTagStack.size(); i++) {
            String parentMO = xmlTagStack.get(i).toString();

            //The depth at each xml tag index is  index+1 
            int depthKey = i + 1;

            //Iterate through the XML attribute tags for the element.
            if (xmlAttrStack.get(depthKey) == null) {
                continue; //Skip null values
            }

            Iterator<Map.Entry<String, String>> mIter
                    = xmlAttrStack.get(depthKey).entrySet().iterator();

            while (mIter.hasNext()) {
                Map.Entry<String, String> meMap = mIter.next();
                String pName = parentMO + "_" + meMap.getKey();
                String pValue = toCSVFormat(meMap.getValue());

                xmlTagValues.put(pName, pValue);

            }
        }

        //Some MOs dont have 3GPP attributes e.g. the fileHeader 
        //and the fileFooter
        if (moThreeGPPAttrMap.get(mo) != null) {
            //Get 3GPP attributes for MO at the current depth
            Stack a3GPPAtrr = moThreeGPPAttrMap.get(mo);
            Map<String, String> current3GPPAttrs = new LinkedHashMap<String, String>();

            if (!threeGPPAttrStack.isEmpty() && threeGPPAttrStack.get(depth) != null) {
                current3GPPAttrs = threeGPPAttrStack.get(depth);
            }

            for (int i = 0; i < a3GPPAtrr.size(); i++) {
                String aAttr = (String) a3GPPAtrr.get(i);

                //Skip parameters listed in the parameter file that are in the xmlTagList already
//                  if(ignoreInParameterFile.contains(aAttr)) continue;

                //Skip fileName, and dateTime in the parameter file as they are added by default
                if (aAttr.toLowerCase().equals("filename") ||
                        aAttr.toLowerCase().equals("datetime")) continue;

                String aValue = "";

                if (xmlTagValues.containsKey(aAttr)) {
                    aValue = xmlTagValues.get(aAttr);
                }

                if (current3GPPAttrs != null && current3GPPAttrs.containsKey(aAttr)) {
                    aValue = toCSVFormat(current3GPPAttrs.get(aAttr));
                }

                paramNames = paramNames + "," + aAttr;
                paramValues = paramValues + "," + aValue;
            }
        } else {
            //if there are not 3GPP Attributes(ie moThreeGPPAttrMap is empty), collect the XML attributes 
            Iterator<Map.Entry<String, String>> mIter
                    = xmlTagValues.entrySet().iterator();
            while (mIter.hasNext()) {
                Map.Entry<String, String> meMap = mIter.next();
                paramNames = paramNames + "," + meMap.getKey();
                paramValues = paramValues + "," + toCSVFormat(meMap.getValue());
            }

        }

        //Write the 3GPP defined MOs to files.
        PrintWriter pw = null;
        if (!output3GPPMOPWMap.containsKey(mo)) {

            //Rename conflicting csv files on windows
            String renamedFileName = mo;
            if (System.getProperty("os.name").startsWith("Windows")) {
                if (MoToFileNameMap.containsKey(mo)) renamedFileName = MoToFileNameMap.get(mo);
            }

            String moFile = outputDirectory + File.separatorChar + renamedFileName + ".csv";
            try {
                output3GPPMOPWMap.put(mo, new PrintWriter(new File(moFile)));
                output3GPPMOPWMap.get(mo).println(paramNames);
            } catch (FileNotFoundException e) {
                //@TODO: Add logger
                System.err.println(e.getMessage());
            }
        }

        pw = output3GPPMOPWMap.get(mo);
        pw.println(paramValues);
    }

    /**
     * Save a values for Three GPP attribute values .
     * <p>
     * This should be called at the end of </attributes>
     *
     * @param mo
     */
    private void saveThreeGPPAttrValues(String mo) {

        threeGPPAttrValues.clear();

        //Some MOs dont have 3GPP attributes e.g. the fileHeader 
        //and the fileFooter
        if (moThreeGPPAttrMap.get(mo) != null) {
            //Get 3GPP attributes for MO at the current depth
            Stack a3GPPAtrr = moThreeGPPAttrMap.get(mo);
            Map<String, String> current3GPPAttrs = null;

            //We are assuming the vsDataSomeMO is an immediate child of SomeMO
//              if (!threeGPPAttrStack.isEmpty() && threeGPPAttrStack.get(depth-2) != null) {
//                  current3GPPAttrs = threeGPPAttrStack.get(depth);
//              }

            if (!threeGPPAttrStack.isEmpty() && threeGPPAttrStack.containsKey(depth)) {
                current3GPPAttrs = threeGPPAttrStack.get(depth);
            }

            for (int i = 0; i < a3GPPAtrr.size(); i++) {
                String aAttr = (String) a3GPPAtrr.get(i);


                //Skip parameters listed in the parameter file that are in the xmlTagList already
//                  if(ignoreInParameterFile.contains(aAttr)) continue;

                //Skip fileName, and dateTime in the parameter file as they are added by default
                if (aAttr.toLowerCase().equals("filename") ||
                        aAttr.toLowerCase().equals("datetime")) continue;

                String aValue = "";

                if (current3GPPAttrs != null && current3GPPAttrs.containsKey(aAttr)) {
                    aValue = toCSVFormat(current3GPPAttrs.get(aAttr));
                } else {
                    //Only take the current Attri but maitain the order in 
                    //a3GPPAtrr i.e. moThreeGPPAttrMap
                    continue;
                }
                threeGPPAttrValues.put(aAttr, aValue);
            }
        }
    }

    /**
     * Print vendor specific attributes. The vendor specific attributes start
     * with a vendor specific namespace.
     *
     * @verison 2.0.0
     * @since 1.0.0
     */
    public void processVendorAttributes() {

        //Skip if the mo is not in the parameterFile
        if (parameterFile != null && !moColumns.containsKey(vsDataType)) {
            return;
        }

        String paramNames = "FILENAME,DATETIME";
        String paramValues = bulkCMXMLFileBasename + "," + dateTime;

        Map<String, String> parentIdValues = new LinkedHashMap<String, String>();

        //Parent MO IDs
        for (int i = 0; i < xmlTagStack.size(); i++) {

            //Get parent tag from the stack
            String parentMO = xmlTagStack.get(i).toString();

            //The depth at each XML tag in xmlTagStack is given by index+1. 
            int depthKey = i + 1;

            //If the parent tag is VsDataContainer, look for the 
            //vendor specific MO in the vsDataContainer-to-vsDataType map.
            if (parentMO.startsWith("VsDataContainer")) {
                parentMO = vsDataContainerTypeMap.get(parentMO);
            }

            Map<String, String> m = null;
            if (xmlAttrStack.containsKey(depthKey)) m = xmlAttrStack.get(depthKey);

            if (null == m || m.isEmpty()) {
                continue;
            }
            Iterator<Map.Entry<String, String>> aIter
                    = xmlAttrStack.get(depthKey).entrySet().iterator();

            //If we dont't want to separate the vsDataMo from the 3GPP mos
            //strip vsData From the MOs Ids ie.e vsDataSomeMO_id becomes SomeMO_id
            if (separateVendorAttributes == false) {
                parentMO = parentMO.replace("vsData", "vs");
            }

            while (aIter.hasNext()) {
                Map.Entry<String, String> meMap = aIter.next();

                String pValue = toCSVFormat(meMap.getValue());
                String pName = parentMO + "_" + meMap.getKey();

                parentIdValues.put(pName, pValue);

            }
        }

        //Make copy of the columns first
        Stack columns = new Stack();

        columns = moColumns.get(vsDataType);

        //Iterate through the columns already collected
        for (int i = 0; i < columns.size(); i++) {
            String pName = columns.get(i).toString();

            //This strips vsData from vsDataSomeMO_Attribute e.g
            //vsDataGsmCell_id becaomes GsmCell_id
            if (separateVendorAttributes == false) {
                //Skip vsDataSomeMO_Id
                //if( pName.equals(vsDataType + "_id") ) continue;

                //Remove vsData from vsDataSomeMO_id to vsSomeMO_id
                pName = pName.replace("vsData", "vs");
            }


            //Skip parent parameters/ parentIds listed in the parameter file
//            if( parameterFile != null && moColumnsParentIds.get(vsDataType).contains(pName)) continue;

            if (pName.equals("FILENAME") || pName.equals("DATETIME")) continue;

            String pValue = "";

            //Check parameter ids fro parameter name
            if (parentIdValues.containsKey(pName)) {
                pValue = parentIdValues.get(pName);
            }

            //
            if (vsDataTypeStack.containsKey(pName)) {
                pValue = toCSVFormat(vsDataTypeStack.get(pName));
            }

            paramNames = paramNames + "," + pName;
            paramValues = paramValues + "," + pValue;
        }

        //If we dont't want to separate the vsDataMo from the 3GPP mos
        //strip vsData From the MOs, we must print the 3GPP mos here .
        //Get the parameter names and values of the 3GPP MOs
        //@TODO: Handle parameter file
        String threeGGPMo = vsDataType.replace("vsData", "");
        if (separateVendorAttributes == false && xmlTagStack.contains(threeGGPMo)) {
            Stack _3gppAttr = new Stack();

            if (!moThreeGPPAttrMap.isEmpty() && moThreeGPPAttrMap.containsKey(threeGGPMo))
                _3gppAttr = moThreeGPPAttrMap.get(threeGGPMo);

            for (int idx = 0; idx < _3gppAttr.size(); idx++) {
                String pName = _3gppAttr.get(idx).toString();
                String pValue = "";

                //Skip _id fileds
                if (pName.endsWith("_id")) continue;

                if (threeGPPAttrValues.containsKey(pName)) pValue = threeGPPAttrValues.get(pName);

                paramNames = paramNames + "," + pName;
                paramValues = paramValues + "," + pValue;
            }
        }

        String csvFileName = vsDataType;

        //Remove vsData if we don't want to separate the 3GPP and vendor attributes
        if (separateVendorAttributes == false) csvFileName = csvFileName.replace("vsData", "");

        //Write the parameters and values to files.
        PrintWriter pw = null;
        if (!outputVsDataTypePWMap.containsKey(csvFileName)) {

            String renamedFileName = csvFileName;
            if (System.getProperty("os.name").startsWith("Windows")) {
                if (MoToFileNameMap.containsKey(csvFileName)) renamedFileName = MoToFileNameMap.get(csvFileName);
            }

            String moFile = outputDirectory + File.separatorChar + renamedFileName + ".csv";
            try {
                outputVsDataTypePWMap.put(csvFileName, new PrintWriter(new File(moFile)));
                outputVsDataTypePWMap.get(csvFileName).println(paramNames);
            } catch (FileNotFoundException e) {
                //@TODO: Add logger
                System.err.println(e.getMessage());
            }
        }

        pw = outputVsDataTypePWMap.get(csvFileName);
        pw.println(paramValues);

    }


    /**
     * Update the map of 3GPP MOs to attributes.
     * <p>
     * This is necessary to ensure the final output in the csv is aligned.
     *
     * @since 1.3.0
     */
    private void updateThreeGPPAttrMap() {
        if (xmlTagStack == null || xmlTagStack.isEmpty()) return;


        String mo = xmlTagStack.peek().toString();

        //Skip 3GPP MO if it is not in the parameter file
        if (parameterFile != null && !moThreeGPPAttrMap.containsKey(mo)) return;

        //Hold the current 3GPP attributes
        HashMap<String, String> tgppAttrs = null;

        Stack attrs = new Stack();

        //Initialize if the MO does not exist
        if (!moThreeGPPAttrMap.containsKey(mo)) {
            moThreeGPPAttrMap.put(mo, new Stack());
        }


        //The attributes stack can be empty if the MO has no 3GPP attributes
        if (threeGPPAttrStack.isEmpty() || threeGPPAttrStack.get(depth) == null) {
            return;
        }
        tgppAttrs = (LinkedHashMap<String, String>) threeGPPAttrStack.get(depth);


        attrs = moThreeGPPAttrMap.get(mo);


        //Add Parent IDs as parameters
        for (int i = 0; i < xmlTagStack.size(); i++) {
            String parentMO = xmlTagStack.get(i).toString();

            //The depth at each xml tag index is  index+1 
            int depthKey = i + 1;

            //Iterate through the XML attribute tags for the element.
            if (xmlAttrStack.get(depthKey) == null) {
                continue; //Skip null values
            }

            Iterator<Map.Entry<String, String>> mIter
                    = xmlAttrStack.get(depthKey).entrySet().iterator();

            while (mIter.hasNext()) {
                Map.Entry<String, String> meMap = mIter.next();
                String pName = parentMO + "_" + meMap.getKey();

                if (!attrs.contains(pName) && parameterFile == null) {
                    attrs.push(pName);
                }
            }
        }


        if (tgppAttrs != null) {
            //Get vendor specific attributes
            Iterator<Map.Entry<String, String>> iter
                    = tgppAttrs.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> me = iter.next();
                String parameter = me.getKey();

                //Only add missing parameter is a paramterFile was not specified.
                //The parameter file parameter list is our only interest in this 
                //case
                if (!attrs.contains(parameter) && parameterFile == null) {
                    attrs.push(parameter);
                }
            }
            moThreeGPPAttrMap.replace(mo, attrs);
        }
    }

    /**
     * Collect parameters for vendor specific MO data
     */
    private void collectVendorMOColumns() {

        //If MO is not in the parameter list, then don't continue
        if (parameterFile != null && !moColumns.containsKey(vsDataType)) return;

        if (!moColumns.containsKey(vsDataType)) {
            moColumns.put(vsDataType, new Stack());
            moColumnsParentIds.put(vsDataType, new Stack()); //Holds parent element IDs
        }

        Stack s = moColumns.get(vsDataType);
        Stack parentIDStack = moColumnsParentIds.get(vsDataType);

        //
        //Parent IDs
        for (int i = 0; i < xmlTagStack.size(); i++) {
            String parentMO = xmlTagStack.get(i).toString();

            //If the parent tag is VsDataContainer, look for the 
            //vendor specific MO in the vsDataContainer-to-vsDataType map.
            if (parentMO.startsWith("VsDataContainer")) {
                parentMO = vsDataContainerTypeMap.get(parentMO);
            }

            //The depth at each xml tag index is  index+1 
            int depthKey = i + 1;

            //Iterate through the XML attribute tags for the element.
            if (xmlAttrStack.get(depthKey) == null) {
                continue; //Skip null values
            }

            Iterator<Map.Entry<String, String>> mIter
                    = xmlAttrStack.get(depthKey).entrySet().iterator();

            while (mIter.hasNext()) {
                Map.Entry<String, String> meMap = mIter.next();
                //String pName = meMap.getKey();
                String pName = parentMO + "_" + meMap.getKey();

                if (parentIDStack.search(pName) < 0) {
                    parentIDStack.push(pName);
                }

                if (parameterFile == null && !s.contains(pName)) {
                    s.push(pName);
                }
            }
        }

        moColumnsParentIds.replace(vsDataType, parentIDStack);

        //Only update hte moColumns list if the parameterFile is not set
        //else use the list provided in the parameterFile
        if (parameterFile == null) {
            //Get vendor specific attributes
            Iterator<Map.Entry<String, String>> iter
                    = vsDataTypeStack.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> me = iter.next();
                String parameter = me.getKey();
                if (!s.contains(parameter)) {
                    s.push(parameter);
                }
            }
            moColumns.replace(vsDataType, s);
        }

    }

    /**
     * Process given string into a format acceptable for CSV format.
     *
     * @param s String
     * @return String Formated version of input string
     * @since 1.0.0
     */
    public String toCSVFormat(String s) {
        String csvValue = s;

        if (s == null) {
            csvValue = "\"\"";
            return csvValue;
        }

        //Check if value contains comma
        if (s.contains(",")) {
            csvValue = "\"" + s + "\"";
        }

        if (s.contains("\"")) {
            csvValue = "\"" + s.replace("\"", "\"\"") + "\"";
        }

        return csvValue;
    }

    /**
     * Close file print writers.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    public void closeMOPWMap() {
        Iterator<Map.Entry<String, PrintWriter>> iter
                = outputVsDataTypePWMap.entrySet().iterator();
        while (iter.hasNext()) {
            iter.next().getValue().close();
        }
        outputVsDataTypePWMap.clear();

        //Close 3GPP MO files.
        Iterator<Map.Entry<String, PrintWriter>> mIter
                = output3GPPMOPWMap.entrySet().iterator();
        while (mIter.hasNext()) {
            mIter.next().getValue().close();
        }
        output3GPPMOPWMap.clear();
    }

    /**
     * Show parser help.
     *
     * @version 1.0.0
     * @since 1.0.0
     */
    public void showHelp() {
        System.out.println("boda-bulkcmparser " + VERSION + " Copyright (c) 2019 Bodastage(http://www.bodastage.com)");
        System.out.println("Parses 3GPP Bulk CM XML to csv.");
        System.out.println("Usage: java -jar boda-bulkcmparser.jar <fileToParse.xml|Directory> <outputDirectory> [parameter.conf]");
    }

    /**
     * Get file base name.
     *
     * @since 1.0.0
     */
    public String getFileBasename(String filename) {
        try {
            return new File(filename).getName();
        } catch (Exception e) {
            return filename;
        }
    }

    /**
     * Set name of file to parser.
     *
     * @param dataSource
     * @version 1.0.0
     * @since 1.0.1
     */
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Print program's execution time.
     *
     * @since 1.0.0
     */
    public void printExecutionTime() {
        float runningTime = System.currentTimeMillis() - startTime;

        String s = "Parsing completed.\n";
        s = s + "Total time:";

        //Get hours
        if (runningTime > 1000 * 60 * 60) {
            int hrs = (int) Math.floor(runningTime / (1000 * 60 * 60));
            s = s + hrs + " hours ";
            runningTime = runningTime - (hrs * 1000 * 60 * 60);
        }

        //Get minutes
        if (runningTime > 1000 * 60) {
            int mins = (int) Math.floor(runningTime / (1000 * 60));
            s = s + mins + " minutes ";
            runningTime = runningTime - (mins * 1000 * 60);
        }

        //Get seconds
        if (runningTime > 1000) {
            int secs = (int) Math.floor(runningTime / (1000));
            s = s + secs + " seconds ";
            runningTime = runningTime - (secs / 1000);
        }

        //Get milliseconds
        if (runningTime > 0) {
            int msecs = (int) Math.floor(runningTime / (1000));
            s = s + msecs + " milliseconds ";
            runningTime = runningTime - (msecs / 1000);
        }

        System.out.println(s);
    }

    /**
     * Set the output directory.
     *
     * @param directoryName
     * @version 1.0.0
     * @since 1.0.0
     */
    public void setOutputDirectory(String directoryName) {
        this.outputDirectory = directoryName;
    }

    /**
     * Set name of file to parser.
     *
     * @param filename
     * @version 1.0.0
     * @since 1.0.0
     */
    private void setFileName(String filename) {
        this.dataFile = filename;
    }
}
