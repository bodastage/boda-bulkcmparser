package com.bodastage.boda_bulkcmparser;

import com.bodastage.boda_bulkcmparser.bulkcmxml.BulkCmConfigDataFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for boda-bulkcmparser.
 */
public class    BodaBulkCMParserTest
    extends TestCase
{
    String sampleBulkCMFile;
    
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
            
            sampleBulkCMFile = System.getProperty("java.io.tmpdir") +  File.separator  + "bulkcmdata.xml";
            
            File file = new File(sampleBulkCMFile);
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
//    public void testApp()
//    {
//        try {
//            BodaBulkCMParser parser = new BodaBulkCMParser();
//            String[] args = { sampleBulkCMFile, System.getProperty("java.io.tmpdir")};
//            parser.main(args);
//
//            for(int i=0; i<expectedFiles.length;i++){
//                boolean fileExists 
//                        = new File( expectedFiles[i]).exists();
//                assertTrue(fileExists);
//            }
//
//        } catch (Exception ex) {
//            assertTrue(false);
//        }
//    }
    
    public void testParentChildAttrbutesWithSameName(){
        ClassLoader classLoader = getClass().getClassLoader();
        File inFile = new File(classLoader.getResource("bulkcm_parent_child_same_name.xml").getFile());
        
        BodaBulkCMParser parser = new BodaBulkCMParser();
        String inputFile = inFile.getAbsolutePath();
        
        System.out.println(inputFile);
        String outputFolder = System.getProperty("java.io.tmpdir");
        
        Logger.getLogger(BodaBulkCMParserTest.class.getName()).log(Level.INFO, "outputFolder:" + outputFolder );
        
        String[] args = { "-i", inputFile, "-o", outputFolder};
        
        parser.main(args);
        
        String expectedResult [] = {
            "FILENAME,DATETIME,bulkCmConfigDataFile_schemaLocation,SubNetwork_id,SubNetwork_2_id,meContext_id,ManagedElement_id,vsDataSomeMO_id,SomeAttr_SomeAttrChild1,SomeAttr_SomeAttr,SomeAttr_SomeAttr2,AnotherAttr_AnotherChild1,AnotherAttr_AnotherAttr",
            "bulkcm_parent_child_same_name.xml,2019-04-16T00:05:00+03:00,http://www.3gpp.org/ftp/specs/archive/32_series/32.615#configData configData.xsd,BS_NRM_ROOT,101,4698,4698,Q0001,Val1,SomeAttrChildVal,SomeAttrChildVal2,1234,777"};
        
        try {
            String csvFile = outputFolder + File.separator + "vsDataSomeMO.csv";
            
            BufferedReader br = new BufferedReader(new FileReader(csvFile)); 
            String csvResult [] = new String[2];
            
            int i = 0;
            String st; 
            while ((st = br.readLine()) != null) {
                csvResult[i] = st;
                i++;
            }
            
            
            assertTrue(Arrays.equals(expectedResult, csvResult));
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BodaBulkCMParserTest.class.getName()).log(Level.SEVERE, null, ex);
            assert(false);
        } catch (IOException ex) {
            Logger.getLogger(BodaBulkCMParserTest.class.getName()).log(Level.SEVERE, null, ex);
            assert(false);
        }
        
        
        
    }
    
    /**
     * Test parsing of bulkcm without separating vsData MOs
     */
    public void testSeparatingVsData(){
        ClassLoader classLoader = getClass().getClassLoader();
        File inFile = new File(classLoader.getResource("bulkcm3.xml").getFile());
        
        BodaBulkCMParser parser = new BodaBulkCMParser();
        String inputFile = inFile.getAbsolutePath();
        
        System.out.println(inputFile);
        String outputFolder = System.getProperty("java.io.tmpdir");
        
        Logger.getLogger(BodaBulkCMParserTest.class.getName()).log(Level.INFO, "outputFolder:" + outputFolder );
        
        String[] args = { "-i", inputFile, "-o", outputFolder, "-s"};
        
        parser.main(args);
        
        String expectedResult [] = {
            "FILENAME,DATETIME,bulkCmConfigDataFile_schemaLocation,SubNetwork_id,SubNetwork_2_id,meContext_id,ManagedElement_id,GsmCell_id,vsGsmCell_id,height,latitude,longitude,bcc,ncc",
            "bulkcm.xml,2019-04-16T00:05:00+03:00,http://www.3gpp.org/ftp/specs/archive/32_series/32.615#configData configData.xsd,BS_NRM_ROOT,101,4698,4698,1A,11,Q0001,0001,9,1,2"};
        
        try {
            String csvFile = outputFolder + File.separator + "GsmCell.csv";
            
            BufferedReader br = new BufferedReader(new FileReader(csvFile)); 
            String csvResult [] = new String[2];
            
            int i = 0;
            String st; 
            while ((st = br.readLine()) != null) {
                csvResult[i] = st;
                i++;
            }
            
            
            assertTrue(Arrays.equals(expectedResult, csvResult));
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BodaBulkCMParserTest.class.getName()).log(Level.SEVERE, null, ex);
            assert(false);
        } catch (IOException ex) {
            Logger.getLogger(BodaBulkCMParserTest.class.getName()).log(Level.SEVERE, null, ex);
            assert(false);
        }
    }
    
    /**
     * Test parsing of bulkcm without separating vsData MOs
     */
    public void testNotSeparatingVsData(){
        ClassLoader classLoader = getClass().getClassLoader();
        File inFile = new File(classLoader.getResource("bulkcm3.xml").getFile());
        
        BodaBulkCMParser parser = new BodaBulkCMParser();
        String inputFile = inFile.getAbsolutePath();
        
        String outputFolder = System.getProperty("java.io.tmpdir");
        
        Logger.getLogger(BodaBulkCMParserTest.class.getName()).log(Level.INFO, "outputFolder:" + outputFolder );
        
        String[] args = { "-i", inputFile, "-o", outputFolder};
        
        parser.main(args);
        
        String expectedResult [] = {
            "FILENAME,DATETIME,bulkCmConfigDataFile_schemaLocation,SubNetwork_id,SubNetwork_2_id,meContext_id,ManagedElement_id,GsmCell_id,vsDataGsmCell_id,height,latitude,longitude,bcc,ncc",
            "bulkcm.xml,2019-04-16T00:05:00+03:00,http://www.3gpp.org/ftp/specs/archive/32_series/32.615#configData configData.xsd,BS_NRM_ROOT,101,4698,4698,A1,11,Q0001,0001,9,1,2"};
        
        try {
            String csvFile = outputFolder + File.separator + "GsmCell.csv";
            
            BufferedReader br = new BufferedReader(new FileReader(csvFile)); 
            String csvResult [] = new String[2];
            
            int i = 0;
            String st; 
            while ((st = br.readLine()) != null) {
                csvResult[i] = st;
                i++;
            }

            assertTrue(Arrays.equals(expectedResult, csvResult));
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BodaBulkCMParserTest.class.getName()).log(Level.SEVERE, null, ex);
            assert(false);
        } catch (IOException ex) {
            Logger.getLogger(BodaBulkCMParserTest.class.getName()).log(Level.SEVERE, null, ex);
            assert(false);
        }

    }
}
