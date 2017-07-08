/*
 * SubNetwork2.
 */
package com.bodastage.boda_bulkcmparser.bulkcmxml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @version 1.0.0
 * @author Bodastage<info@bodastage.com>
 */
class SubNetwork2 {
    private String id = "1";
    private SubNetwork2Attributes attributes = new SubNetwork2Attributes();
    private MeContext meContext = new MeContext();
    
    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id",namespace = "")
    public void setId(String id) {
        this.id = id;
    }

    public SubNetwork2Attributes getAttributes() {
        return attributes;
    }

    @XmlElement(name = "attributes", namespace = "http://www.3gpp.org/ftp/specs/archive/32_series/32.625#genericNrm")
    public void setAttributes(SubNetwork2Attributes attributes) {
        this.attributes = attributes;
    }

    public MeContext getMeContext() {
        return meContext;
    }

    @XmlElement(name = "meContext", namespace = "http://www.3gpp.org/ftp/specs/archive/32_series/32.625#genericNrm")
    public void setMeContext(MeContext meContext) {
        this.meContext = meContext;
    }
    
}
