/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.com.bodastage.boda_bulkcmparser.bulkcmxml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @version 1.0.0
 * @author Bodastage<info@bodastage.com>
 */
class VsDataContainer {
    private VsDataContainerAttributes vsDataContainerAttributes 
            = new VsDataContainerAttributes();
    private String id = "1";

    public VsDataContainerAttributes getVsDataContainerAttributes() {
        return vsDataContainerAttributes;
    }

    @XmlElement(name = "attributes", namespace = "http://www.3gpp.org/ftp/specs/archive/32_series/32.625#genericNrm")
    public void setVsDataContainerAttributes(VsDataContainerAttributes vsDataContainerAttributes) {
        this.vsDataContainerAttributes = vsDataContainerAttributes;
    }

    public String getId() {
        return id;
    }

    @XmlAttribute(name="id", namespace = "")
    public void setId(String id) {
        this.id = id;
    }
}
