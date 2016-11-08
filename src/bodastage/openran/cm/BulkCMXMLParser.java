/**
 * 3GPP Bulk CM XML to CSV Parser.
 *
 * @author Bodastage<info@bodastage.com>
 * @version 1.0.0
 * @see http://github.com/bodastage/openran-cm-bulkcmparsers
 */
package bodastage.openran.cm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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

public class BulkCMXMLParser {

    /**
     * Tracks XML elements.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static Stack xmlTagStack = new Stack();

    /**
     * Tracks how deep a Management Object is in the XML doc hierarchy.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static Integer depth = 0;

    /**
     * Tracks XML attributes per Management Objects.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static Map<Integer, Map<String, String>> xmlAttrStack = new HashMap<>();

    /**
     * Tracks Managed Object specific 3GPP attributes.
     *
     * This tracks every thing within <xn:attributes>...</xn:attributes>.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static Map<Integer, Map<String, String>> threeGPPAttrStack = new HashMap<>();

    /**
     * Marks start of processing per MO attributes.
     *
     * This is set to true when xn:attributes is encountered. It's set to false
     * when the corresponding closing tag is encountered.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static boolean attrMarker = false;

    /**
     * Tracks the depth of VsDataContainer tags in the XML document hierarchy.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static int vsDCDepth = 0;

    /**
     * Maps of vsDataContainer instances to vendor specific data types.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static Map<String, String> vsDataContainerTypeMap = new HashMap<>();

    /**
     * Tracks current vsDataType if not null
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static String vsDataType = null;

    /**
     * vsDataTypes stack.
     *
     * @since 1.0.0
     * @version 1.1.0
     */
    static Map<String, String> vsDataTypeStack = new HashMap<>();

    /**
     * Real stack to push and pop vsDataType attributes.
     *
     * This is used to track multivalued attributes and attributes with children
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static Stack vsDataTypeRlStack = new Stack();

    /**
     * Multi-valued parameter separator.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static String multiValueSeparetor = ",";

    /**
     * For attributes with children, define parameter-child separator
     *
     * @since 1.0.0
     */
    static String parentChildAttrSeperator = "_";

    /**
     * Output file print writers
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static Map<String, PrintWriter> outputFilePW = new HashMap<>();

    /**
     * Output directory.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static String outputDirectory = "/tmp";

    /**
     * Limit the number of iterations for testing.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static int testCounter = 0;

    /**
     * Start element tag.
     *
     * Use in the character event to determine the data parent XML tag.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static String startElementTag = "";

    /**
     * Start element NS prefix.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static String startElementTagPrefix = "";

    /**
     * Tag data.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static String tagData = "";

    /**
     * Tracking parameters with children.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static Map<String, String> parentChildParameters = new HashMap<>();

    /**
     * A map of MO to printwriter.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    static Map<String, PrintWriter> outputVsDataTypePWMap = new HashMap<>();

    /**
     * A map of 3GPP MOs to their file print writers.
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    static Map<String, PrintWriter> output3GPPMOPWMap = new HashMap<>();
    
    /**
     * Bulk CM XML file name. The file we are parsing.
     */
    static String bulkCMXMLFile;
    
    static String bulkCMXMLFileBasename;
    
    /**
     * @param args the command line arguments
     *
     * @since 1.0.0
     * @version 1.0.0
     */    
    public static void main(String[] args) throws UnsupportedEncodingException {

        try {
            
            if(args.length != 2 ){
                showHelp();
                System.exit(1);
            }
            //Get bulk CM XML file to parse.
            bulkCMXMLFile = args[0];
            outputDirectory = args[1];
            
            //Confirm that the output directory is a directory and has write 
            //privileges
            File fOutputDir = new File(outputDirectory);
            if(!fOutputDir.isDirectory()) {
                System.out.println("The specified output directory is not a directory!.");
                System.exit(1);
            }
            
            if(!fOutputDir.canWrite()){
                System.out.println("Cannot write to output directory!");
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
                    case XMLStreamConstants.CHARACTERS:
                        characterEvent(event);
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        endELementEvent(event);
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        closeMOPWMap();
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
    public static void startElementEvent(XMLEvent xmlEvent) {

        StartElement startElement = xmlEvent.asStartElement();
        String qName = startElement.getName().getLocalPart();
        String prefix = startElement.getName().getPrefix();

        startElementTag = qName;
        startElementTagPrefix = prefix;

        Iterator<Attribute> attributes = startElement.getAttributes();

        //E1:0
        if (qName.equalsIgnoreCase("VsDataContainer")) {
            vsDCDepth++;
            depth++;

            String vsDCTagWithDepth = "VsDataContainer_" + vsDCDepth;
            xmlTagStack.push(vsDCTagWithDepth);

            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                if (attribute.getName().toString().equals("id")) {
                    Map<String, String> m = new HashMap<>();
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
                    xmlAttrStack.replace(depth, mm);
                } else {
                    Map<String, String> m = new HashMap<>();
                    m.put(attribute.getName().getLocalPart(), attribute.getValue());
                    xmlAttrStack.put(depth, m);
                }
            }

            return;
        }

        //E1.5
        if (attrMarker == true) {

            Map<String, String> m = new HashMap<>();
            if (threeGPPAttrStack.containsKey(depth)) {
                m = threeGPPAttrStack.get(depth);
                m.put(qName, null);
                threeGPPAttrStack.replace(depth, m);
            } else {
                m.put(qName, null); //Initial value null
                threeGPPAttrStack.put(depth, m);
            }
            return;
        }

        //E1.6
        depth++;
        xmlTagStack.push(qName);
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();

            if (xmlAttrStack.containsKey(depth)) {
                Map<String, String> mm = xmlAttrStack.get(depth);
                mm.put(attribute.getName().getLocalPart(), attribute.getValue());
                xmlAttrStack.replace(depth, mm);
            } else {
                Map<String, String> m = new HashMap<>();
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
    public static void characterEvent(XMLEvent xmlEvent) {
        Characters characters = xmlEvent.asCharacters();
        tagData = characters.getData();
    }

    public static void endELementEvent(XMLEvent xmlEvent)
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
            processVendorAttributes();
            vsDataType = null;
            vsDataTypeStack.clear();
            return;
        }

        //E3:4
        if (vsDataType != null && attrMarker == true) {//We are processing vsData<DataType> attributes
            //E2:3:1 @TODO: later
            //E2:3:2 @TODO: later

            if (parentChildParameters.containsKey(qName)) {
                return;
            }

            String newTag = qName;
            String newValue = tagData;

            //@TODO: Handle cases of multi values parameters and parameters with children
            //For now continue as if they do not exist
            vsDataTypeStack.put(newTag, newValue);
        }

        //E3.5
        if (attrMarker == true && vsDataType == null) {
            Map<String, String> m = new HashMap<>();
            m = threeGPPAttrStack.get(depth);
            if (m.containsKey(qName)) {
                m.replace(qName, tagData);
            } else {
                m.put(qName, tagData);
            }
            threeGPPAttrStack.replace(depth, m);
            tagData = "";
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

            process3GPPAttributes();

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
    public static Integer getXMLTagOccurences(String tagName) {
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
    public static void process3GPPAttributes()
            throws FileNotFoundException, UnsupportedEncodingException {

        String mo = xmlTagStack.peek().toString();

        String paramNames = "bulkCMFileName";
        String paramValues = bulkCMXMLFileBasename;

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
                paramValues = paramValues + "," + meMap.getValue();
            }
        }

        //Get 3GPP parameters for the MO at the current depth.
        if (!threeGPPAttrStack.isEmpty() && threeGPPAttrStack.get(depth) != null) {
            Iterator<Map.Entry<String, String>> iter
                    = threeGPPAttrStack.get(depth).entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> meMap = iter.next();
                paramNames = paramNames + "," + meMap.getKey();
                paramValues = paramValues + "," + meMap.getValue();
            }
        }
        
        //Write the 3GPP defined MOs to files.
        PrintWriter pw = null;
        if(!output3GPPMOPWMap.containsKey(mo)){
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
    public static void processVendorAttributes() {
        String paramNames = "bulkCMFileName";
        String paramValues = bulkCMXMLFileBasename;

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

                String pValue = meMap.getValue();
                String pName = parentMO + "_" + meMap.getKey();

                paramNames = paramNames + "," + pName;
                paramValues = paramValues + "," + pValue;
            }
        }

        //Get vendor specific attributes
        Iterator<Map.Entry<String, String>> iter
                = vsDataTypeStack.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> me = iter.next();

            String pValue = toCSVFormat(me.getValue());
            String pName = me.getKey();

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
     * Process given string into a format acceptable for CSV format.
     *
     * @since 1.0.0
     * @param s String
     * @return String Formated version of input string
     */
    public static String toCSVFormat(String s) {
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
    public static void closeMOPWMap() {
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
    static public void showHelp(){
        System.out.println("3GPP Bulk CM XML to csv parser.");
        System.out.println("Usage: parser <fileToParser.xml> <outputDirectory>");
    }
    
    /**
     * Get file base name.
     * 
     * @since 1.0.0
     */
    static public String getFileBasename(String filename){
        String[] splitArray;
        splitArray = filename.split(File.separatorChar + "");
        return splitArray[splitArray.length-1];
    }
}
