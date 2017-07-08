/*
 * SubNetwork.
 */
package com.bodastage.boda_bulkcmparser.bulkcmxml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @version 1.0.0
 * @author Bodastage<info@bodastage.com>
 */
class SubNetwork {
    private SubNetwork2 subnetwork2 = new SubNetwork2();
    private String id = "1";

    public SubNetwork2 getSubnetwork2() {
        return subnetwork2;
    }
    
    @XmlElement(name = "subNetwork", namespace = "http://www.3gpp.org/ftp/specs/archive/32_series/32.625#genericNrm")
    public void setSubnetwork2(SubNetwork2 subnetwork2) {
        this.subnetwork2 = subnetwork2;
    }    

    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id",namespace = "")
    public void setId(String id) {
        this.id = id;
    }
    
}
