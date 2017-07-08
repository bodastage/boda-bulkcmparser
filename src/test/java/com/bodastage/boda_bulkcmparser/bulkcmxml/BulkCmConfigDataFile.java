/*
 *@author Bodastage<info@bodastage.com>
 */
package com.bodastage.boda_bulkcmparser.bulkcmxml;

import javax.xml.bind.annotation.*;
import com.bodastage.boda_bulkcmparser.bulkcmxml.FileFooter;
import com.bodastage.boda_bulkcmparser.bulkcmxml.ConfigData;

 @XmlRootElement(name = "bulkCmConfigDataFile", namespace = "")
public class BulkCmConfigDataFile {
     private FileHeader fileHeader = new FileHeader();
     private ConfigData configData = new ConfigData();
     private FileFooter fileFooter = new FileFooter();

    public FileHeader getFileHeader() {
        return fileHeader;
    }

    @XmlElement(name = "fileHeader", namespace = "")
    public void setFileHeader(FileHeader fileHeader) {
        this.fileHeader = fileHeader;
    }

    public ConfigData getConfigData() {
        return configData;
    }

    @XmlElement(name = "configData", namespace = "")
    public void setConfigData(ConfigData configData) {
        this.configData = configData;
    }

    public FileFooter getFileFooter() {
        return fileFooter;
    }

    @XmlElement(name = "fileFooter", namespace = "")
    public void setFileFooter(FileFooter fileFooter) {
        this.fileFooter = fileFooter;
    }
    
     
}
