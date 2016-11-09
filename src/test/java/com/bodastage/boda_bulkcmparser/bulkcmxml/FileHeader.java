/*
 * File header class.
 */
package test.java.com.bodastage.boda_bulkcmparser.bulkcmxml;

import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @version 1.0.0
 * @author Bodastage<info@bodastage.com>
 */
public class FileHeader {
    private String fileFormatVersion = "FV1.0.0";
    private String vendorName ="Bodastage";

    public String getFileFormatVersion() {
        return fileFormatVersion;
    }

    @XmlAttribute( name = "fileFormatVersion" , namespace = "" )
    public void setFileFormatVersion(String fileFormatVersion) {
        this.fileFormatVersion = fileFormatVersion;
    }

    public String getVendorName() {
        return vendorName;
    }

    @XmlAttribute( name = "vendorName" , namespace = "" )
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }
}
