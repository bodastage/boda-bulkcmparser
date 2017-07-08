/*
 * File footer.
 */
package com.bodastage.boda_bulkcmparser.bulkcmxml;

import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @version 1.0.0
 * @author Bodastage<info@bodastage.com>
 */
public class ConfigData {
    private SubNetwork subNetwork = new SubNetwork();

    public SubNetwork getSubNetwork() {
        return subNetwork;
    }

    @XmlElement(name = "subNetwork", namespace = "http://www.3gpp.org/ftp/specs/archive/32_series/32.625#genericNrm")
    public void setSubNetwork(SubNetwork subNetwork) {
        this.subNetwork = subNetwork;
    }
    
}
