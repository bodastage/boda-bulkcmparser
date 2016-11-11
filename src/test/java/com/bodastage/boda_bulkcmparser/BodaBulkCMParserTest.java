package test.java.com.bodastage.boda_bulkcmparser;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import main.java.com.bodastage.boda_bulkcmparser.BodaBulkCMParser;
import test.java.com.bodastage.boda_bulkcmparser.bulkcmxml.BulkCmConfigDataFile;
import test.java.com.bodastage.boda_bulkcmparser.bulkcmxml.FileHeader;
import test.java.com.bodastage.boda_bulkcmparser.bulkcmxml.FileFooter;

/**
 * Unit test for boda-bulkcmparser.
 */
public class BodaBulkCMParserTest 
    extends TestCase
{
    String sampleBulkCMFile = "/tmp/bulkcmdata.xml";
    
    String [] expectedFiles = {
        "/tmp/bulkCmConfigDataFile.csv",
        "/tmp/configData.csv",
        "/tmp/fileFooter.csv",
        "/tmp/fileHeader.csv",
        "/tmp/ManagedElement.csv",
        "/tmp/meContext.csv",
        "/tmp/subNetwork.csv",
        "/tmp/subNetwork_2.csv",
        "/tmp/vsDataSomeMO.csv"                
    };
    
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public BodaBulkCMParserTest( String testName )
    {
        super( testName );
        
       
    }

    /**
     * Setup tests. 
     * 
     * Create the a sample bulk cm data file.
     * 
     */
    public void setUp() {
        javax.xml.bind.JAXBContext jaxbCtx;
        try {

            BulkCmConfigDataFile bulkCMConfigData = new BulkCmConfigDataFile();
            
            jaxbCtx = javax.xml.bind.JAXBContext.newInstance(bulkCMConfigData.getClass());
            javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            File file = new File("/tmp/bulkcmdata.xml");
            marshaller.marshal(bulkCMConfigData, file);
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
    
    
    /**
     * Tear down tests.
     * 
     */
    public void tearDown(){
        try{
            new File(sampleBulkCMFile).delete();
            
            for(int i=0; i<expectedFiles.length;i++){
                new File( expectedFiles[i]).delete();
            }
            
        }catch(Exception e){
            System.err.println(e.getMessage());
            assertTrue(false);
        }
    }
    
    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( BodaBulkCMParserTest.class );
    }

    /**
     * Run test.
     */
    public void testApp()
    {
        try {
            BodaBulkCMParser parser = new BodaBulkCMParser();
            String[] args = { sampleBulkCMFile, "/tmp"};
            parser.main(args);

            for(int i=0; i<expectedFiles.length;i++){
                boolean fileExists 
                        = new File( expectedFiles[i]).exists();
                assertTrue(fileExists);
            }

        } catch (Exception ex) {
            assertTrue(false);
        }
    }
}
