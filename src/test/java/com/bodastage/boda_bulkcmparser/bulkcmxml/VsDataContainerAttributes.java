/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.com.bodastage.boda_bulkcmparser.bulkcmxml;

import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @version 1.0.0
 * @author Bodastage<info@bodastage.com>
 */
class VsDataContainerAttributes {
    private String vsDataType = "vsDataSomeMO";
    private String vsDataFormatVersion = "V1.0.0";
    private VsDataSomeMO vsDataSomeMO = new VsDataSomeMO();

    public String getVsDataType() {
        return vsDataType;
    }

    @XmlElement(name = "vsDataType", namespace = "http://www.3gpp.org/ftp/specs/archive/32_series/32.625#genericNrm")
    public void setVsDataType(String vsDataType) {
        this.vsDataType = vsDataType;
    }

    public String getVsDataFormatVersion() {
        return vsDataFormatVersion;
    }

    @XmlElement(name = "vsDataFormatVersion", namespace = "http://www.3gpp.org/ftp/specs/archive/32_series/32.625#genericNrm")
    public void setVsDataFormatVersion(String vsDataFormatVersion) {
        this.vsDataFormatVersion = vsDataFormatVersion;
    }

    public VsDataSomeMO getVsDataSomeMO() {
        return vsDataSomeMO;
    }

    @XmlElement(name = "vsDataSomeMO", namespace = "http://BodastageSpecificAttributes#BodastageSpecificAttributes")
    public void setVsDataSomeMO(VsDataSomeMO vsDataSomeMO) {
        this.vsDataSomeMO = vsDataSomeMO;
    }
    
    
    
}
