/**
 * 3GPP Bulk CM XML to CSV Parser.
 *
 * @author Bodastage<info@bodastage.com>
 * @version 1.0.0
 * @see http://github.com/bodastage/boda-bulkcmparsers
 */
package com.bodastage.boda_bulkcmparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class BodaBulkCMParser {

    /**
     * Tracks XML elements.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    Stack xmlTagStack = new Stack();

    /**
     * Tracks how deep a Management Object is in the XML doc hierarchy.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    Integer depth = 0;

    /**
     * Tracks XML attributes per Management Objects.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    Map<Integer, Map<String, String>> xmlAttrStack = new LinkedHashMap<Integer, Map<String, String>>();

    /**
     * Tracks Managed Object specific 3GPP attributes.
     *
     * This tracks every thing within <xn:attributes>...</xn:attributes>.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    Map<Integer, Map<String, String>> threeGPPAttrStack = new LinkedHashMap<Integer, Map<String, String>>();

    /**
     * Marks start of processing per MO attributes.
     *
     * This is set to true when xn:attributes is encountered. It's set to false
     * when the corresponding closing tag is encountered.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    boolean attrMarker = false;

    /**
     * Tracks the depth of VsDataContainer tags in the XML document hierarchy.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    int vsDCDepth = 0;

    /**
     * Maps of vsDataContainer instances to vendor specific data types.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    Map<String, String> vsDataContainerTypeMap = new LinkedHashMap<String, String>();

    /**
     * Tracks current vsDataType if not null
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    String vsDataType = null;

    /**
     * vsDataTypes stack.
     *
     * @since 1.0.0
     * @version 1.1.0
     */
    Map<String, String> vsDataTypeStack = new LinkedHashMap<String, String>();

    /**
     * Real stack to push and pop vsDataType attributes.
     *
     * This is used to track multivalued attributes and attributes with children
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    Stack vsDataTypeRlStack = new Stack();

    /**
     * Real stack to push and pop xn:attributes.
     *
     * This is used to track multivalued attributes and attributes with children
     *
     * @since 1.0.2
     * @version 1.0.0
     */
    Stack xnAttrRlStack = new Stack();

    /**
     * Multi-valued parameter separator.
     *
     * @since 1.0.0
     * @version 1.0.0
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
     * @since 1.0.0
     * @version 1.0.0
     */
    Map<String, PrintWriter> outputFilePW = new LinkedHashMap<String, PrintWriter>();

    /**
     * Output directory.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    String outputDirectory = "/tmp";

    /**
     * Limit the number of iterations for testing.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    int testCounter = 0;

    /**
     * Start element tag.
     *
     * Use in the character event to determine the data parent XML tag.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    String startElementTag = "";

    /**
     * Start element NS prefix.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    String startElementTagPrefix = "";

    /**
     * Tag data.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    String tagData = "";

    /**
     * Tracking parameters with children under vsDataSomeMO.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    Map<String, String> parentChildParameters = new LinkedHashMap<String, String>();

    /**
     * Tracking parameters with children in xn:attributes.
     *
     * @since 1.0.2
     * @version 1.0.0
     */
    Map<String, String> attrParentChildMap = new LinkedHashMap<String, String>();

    /**
     * A map of MO to printwriter.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    Map<String, PrintWriter> outputVsDataTypePWMap = new LinkedHashMap<String, PrintWriter>();

    /**
     * A map of 3GPP MOs to their file print writers.
     *
     * @since 1.0.0
     * @version 1.0.0
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
     *
     * @since 1.0.3
     * @version 1.0.0
     */
    Map<String, Stack> moColumns = new LinkedHashMap<String, Stack>();

    /**
     * Tracks the IDs of the parent elements
     * 
     * @since 1.2.0
     */
    Map<String, Stack> moColumnsParentIds = new LinkedHashMap<String, Stack>();
    
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
    
    /**
     * Parser start time.
     *
     * @since 1.1.0
     * @version 1.1.0
     */
    final long startTime = System.currentTimeMillis();

    private int parserState = ParserStates.EXTRACTING_PARAMETERS;

    /**
     * @param inputFilename
     * @param outputDirectory
     */
    public void parseFile(String inputFilename ) throws FileNotFoundException, XMLStreamException, UnsupportedEncodingException {

        XMLInputFactory factory = XMLInputFactory.newInstance();

        XMLEventReader eventReader = factory.createXMLEventReader(
                new FileReader(inputFilename));
        baseFileName = bulkCMXMLFileBasename =  getFileBasename(inputFilename);
        
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

    /**
     * @param args the command line arguments
     *
     * @since 1.0.0
     * @version 1.0.1
     */
    public static void main(String[] args) {

        try{
            
         BodaBulkCMParser theParser = new BodaBulkCMParser();
            
        //show help
        if (args.length != 2 || (args.length == 1 && args[0] == "-h")) {
            theParser.showHelp();
            System.exit(1);
        }
        
        String outputDirectory = args[1];      
        
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

        //Get bulk CM XML file to parse.
        //bulkCMXMLFile = ;
        //outputDirectory = args[1];

        BodaBulkCMParser cmParser = new BodaBulkCMParser();
        cmParser.setDataSource(args[0]);
        cmParser.setOutputDirectory(outputDirectory);
        cmParser.parse();
        }catch(Exception e){
            System.out.println(e.getMessage());
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
            vsDataType = null;
            vsDataTypeStack.clear();
            vsDataTypeRlStack.clear();
            xmlAttrStack.clear();
            xmlTagStack.clear();
            startElementTag = null; 
            startElementTagPrefix = "";
            attrMarker = false;
            depth = 0;
            
        //Extracting values
        if (parserState == ParserStates.EXTRACTING_VALUES) {
            processFileOrDirectory();
            parserState = ParserStates.EXTRACTING_DONE;
        }
        
        closeMOPWMap();

        printExecutionTime();
    }

    /**
     * Determines if the source data file is a regular file or a directory and 
     * parses it accordingly
     * 
     * @since 1.1.0
     * @version 1.0.0
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
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
            baseFileName =  getFileBasename(this.dataFile);
            if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                System.out.print("Extracting parameters from " + this.baseFileName + "...");
            }else{
                System.out.print("Parsing " + this.baseFileName + "...");
            }
            this.parseFile(this.dataSource);
            if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                 System.out.println("Done.");
            }else{
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
                    baseFileName =  getFileBasename(this.dataFile);
                    if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                        System.out.print("Extracting parameters from " + this.baseFileName + "...");
                    }else{
                        System.out.print("Parsing " + this.baseFileName + "...");
                    }
                    
                    //Parse
                    this.parseFile(f.getAbsolutePath());
                    if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                         System.out.println("Done.");
                    }else{
                        System.out.println("Done.");
                        //System.out.println(this.baseFileName + " successfully parsed.\n");
                    }
                   
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("Skipping file: " + this.baseFileName + "\n");
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

            //Expiry check 
            Date expiryDate = new GregorianCalendar(2017, Calendar.AUGUST, 01).getTime();
            Date todayDate = new Date();
            //System.out.println(todayDate);
            //System.out.println(expiryDate);
            if (todayDate.after(expiryDate)) {
                System.out.println("Parser has expired. Please request new version from www.telecomhall.net");
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
     *
     * @since 1.0.0
     * @version 1.0.0
     *
     */
    public void startElementEvent(XMLEvent xmlEvent) {

        StartElement startElement = xmlEvent.asStartElement();
        String qName = startElement.getName().getLocalPart();
        String prefix = startElement.getName().getPrefix();

        startElementTag = qName;
        startElementTagPrefix = prefix;

        Iterator<Attribute> attributes = startElement.getAttributes();

        if(qName.equals("fileFooter") && ParserStates.EXTRACTING_PARAMETERS == parserState){
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
            xmlAttrStack.remove(depth);
            vsDataContainerTypeMap.remove(vsDCDepth);
            threeGPPAttrStack.remove(depth);
            vsDCDepth--;
            depth--;
            return;
        }

        //3.2 </xn:attributes>
        if (qName.equals("attributes")) {
            attrMarker = false;
            return;
        }

        //E3:3 xx:vsData<VendorSpecificDataType>
        if (qName.startsWith("vsData") && !qName.equalsIgnoreCase("VsDataContainer")
                && !prefix.equals("xn")) { //This skips xn:vsDataType
            
            if(ParserStates.EXTRACTING_PARAMETERS == parserState){
                collectVendorMOColumns();
            }else{
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

            //Handle attributes with children
            if (parentChildParameters.containsKey(qName)) {//End of parent tag

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

                //Store the parent and it's child
                parentChildParameters.put(parentTag, qName);

                //Remove this tag from the tag stack.
                vsDataTypeStack.remove(qName);

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
        //Objects
        if (xmlTagStack.contains(qName)) {
            String theTag = qName;

            //@TODO: This occurences check does not appear to be of any use; test 
            // and remove if not needed.
            int occurences = getXMLTagOccurences(qName);
            if (occurences > 1) {
                theTag = qName + "_" + occurences;
            }

            if( parserState != ParserStates.EXTRACTING_PARAMETERS){
                process3GPPAttributes();
            }

            xmlTagStack.pop();
            xmlAttrStack.remove(depth);
            threeGPPAttrStack.remove(depth);
            depth--;
        }
    }

    /**
     * Get the number of occurrences of an XML tag in the xmlTagStack.
     *
     * This is used to handle cases where XML elements with the same name are
     * nested.
     *
     * @param tagName String The XML tag name
     * @since 1.0.0
     * @version 1.0.0
     * @return Integer Number of tag occurrences.
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

        String paramNames = "FileName,varDateTime";
        String paramValues = bulkCMXMLFileBasename + "," + dateTime;

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
                paramNames = paramNames + "," + pName;
                paramValues = paramValues + "," + toCSVFormat(meMap.getValue());
            }
        }

        //Get 3GPP parameters for the MO at the current depth.
        if (!threeGPPAttrStack.isEmpty() && threeGPPAttrStack.get(depth) != null) {
            Iterator<Map.Entry<String, String>> iter
                    = threeGPPAttrStack.get(depth).entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> meMap = iter.next();
                paramNames = paramNames + "," + meMap.getKey();
                paramValues = paramValues + "," + toCSVFormat(meMap.getValue());
            }
        }

        //Write the 3GPP defined MOs to files.
        PrintWriter pw = null;
        if (!output3GPPMOPWMap.containsKey(mo)) {
            String moFile = outputDirectory + File.separatorChar + mo + ".csv";
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
     * Print vendor specific attributes. The vendor specific attributes start
     * with a vendor specific namespace.
     *
     * @verison 1.0.0
     * @since 1.0.0
     */
    public void processVendorAttributes() {
        String paramNames = "FileName,varDateTime";
        String paramValues = bulkCMXMLFileBasename + "," + dateTime;

        Map<String,String> parentIdValues = new LinkedHashMap<String, String>();
        
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

            Map<String, String> m = xmlAttrStack.get(depthKey);
            if (null == m) {
                continue;
            }
            Iterator<Map.Entry<String, String>> aIter
                    = xmlAttrStack.get(depthKey).entrySet().iterator();
            
            while (aIter.hasNext()) {
                Map.Entry<String, String> meMap = aIter.next();

                String pValue = toCSVFormat(meMap.getValue());
                String pName = parentMO + "_" + meMap.getKey();

                parentIdValues.put(pName, pValue);

            }
        }

        
        Stack parentIds = moColumnsParentIds.get(vsDataType);
        for (int idx = 0; idx < parentIds.size(); idx++) {

            String pName = (String)parentIds.get(idx);

            String pValue= "";
            if( parentIdValues.containsKey(pName)){
                pValue = parentIdValues.get(pName);
            }

            paramNames = paramNames + "," + pName;
            paramValues = paramValues + "," + pValue;
        }
        
        //Make copy of the columns first
        Stack columns = new Stack();

        columns = moColumns.get(vsDataType);
        //Iterate through the columns already collected
        for (int i = 0; i < columns.size(); i++) {
            String pName = columns.get(i).toString();
            String pValue = "";
            if (vsDataTypeStack.containsKey(pName)) {
                pValue = toCSVFormat(vsDataTypeStack.get(pName));
            }

            paramNames = paramNames + "," + pName;
            paramValues = paramValues + "," + pValue;
        }    

        //Write the parameters and values to files.
        PrintWriter pw = null;
        if (!outputVsDataTypePWMap.containsKey(vsDataType)) {
            String moFile = outputDirectory + File.separatorChar + vsDataType + ".csv";
            try {
                outputVsDataTypePWMap.put(vsDataType, new PrintWriter(new File(moFile)));
                outputVsDataTypePWMap.get(vsDataType).println(paramNames);
            } catch (FileNotFoundException e) {
                //@TODO: Add logger
                System.err.println(e.getMessage());
            }
        }

        pw = outputVsDataTypePWMap.get(vsDataType);
        pw.println(paramValues);

    }
    
    /**
     * Collect parameters for vendor specific mo data
     */
    private void collectVendorMOColumns(){
        if (!moColumns.containsKey(vsDataType)) {
            moColumns.put(vsDataType, new Stack());
            moColumnsParentIds.put(vsDataType, new Stack()); //Holds parent element IDs
        }

        Stack s = moColumns.get(vsDataType); 
        Stack parentIDStack = moColumnsParentIds.get(vsDataType); 

        //Get vendor specific attributes
        Iterator<Map.Entry<String, String>> iter
                = vsDataTypeStack.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> me = iter.next();
            String parameter = me.getKey();
            if( !s.contains( parameter ) ){
                s.push(parameter);
            }
        }
        moColumns.replace(vsDataType, s);
        
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
                
                if( parentIDStack.search(pName ) < 0 ){
                    parentIDStack.push(pName);
                }
            }
        }

        moColumnsParentIds.replace(vsDataType, parentIDStack);

    }

    /**
     * Process given string into a format acceptable for CSV format.
     *
     * @since 1.0.0
     * @param s String
     * @return String Formated version of input string
     */
    public String toCSVFormat(String s) {
        String csvValue = s;

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
     * @since 1.0.0
     * @version 1.0.0
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
     * @since 1.0.0
     * @version 1.0.0
     */
    public void showHelp() {
        System.out.println("boda-bulkcmparser 1.2.0 Copyright (c) 2017 Bodastage(http://www.bodastage.com)");
        System.out.println("Parses 3GPP Bulk CM XML to csv.");
        System.out.println("Usage: java -jar boda-bulkcmparser.jar <fileToParse.xml|Directory> <outputDirectory>");
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
     * @since 1.0.1
     * @version 1.0.0
     * @param dataSource
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
     * @since 1.0.0
     * @version 1.0.0
     * @param directoryName
     */
    public void setOutputDirectory(String directoryName) {
        this.outputDirectory = directoryName;
    }

    /**
     * Set name of file to parser.
     *
     * @since 1.0.0
     * @version 1.0.0
     * @param directoryName
     */
    private void setFileName(String filename) {
        this.dataFile = filename;
    }

}