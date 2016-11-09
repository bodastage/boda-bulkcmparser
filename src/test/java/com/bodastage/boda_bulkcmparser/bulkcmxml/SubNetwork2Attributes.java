/*
 * Subnetwork attributes.
 */
package test.java.com.bodastage.boda_bulkcmparser.bulkcmxml;

import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @version 1.0.0
 * @author Bodastage<info@bodastage.com>
 */
class SubNetwork2Attributes {
    private String setOfMcc = "111";
    private String userLabel = "SubNetwork UserLabel";
    private String userDefinedNetworkType = "NetworkType";
    private String dnPrefix = "www.bodastage.com";
    

    public String getSetOfMcc() {
        return setOfMcc;
    }

    @XmlElement(name = "setOfMcc", namespace = "http://www.3gpp.org/ftp/specs/archive/32_series/32.625#genericNrm")
    public void setSetOfMcc(String setOfMcc) {
        this.setOfMcc = setOfMcc;
    }

    public String getUserLabel() {
        return userLabel;
    }

    @XmlElement(name = "userLabel", namespace = "http://www.3gpp.org/ftp/specs/archive/32_series/32.625#genericNrm")
    public void setUserLabel(String userLabel) {
        this.userLabel = userLabel;
    }

    public String getUserDefinedNetworkType() {
        return userDefinedNetworkType;
    }

    @XmlElement(name = "userDefinedNetworkType", namespace = "http://www.3gpp.org/ftp/specs/archive/32_series/32.625#genericNrm")
    public void setUserDefinedNetworkType(String userDefinedNetworkType) {
        this.userDefinedNetworkType = userDefinedNetworkType;
    }

    public String getDnPrefix() {
        return dnPrefix;
    }

    @XmlElement(name = "dnPrefix", namespace = "http://www.3gpp.org/ftp/specs/archive/32_series/32.625#genericNrm")
    public void setDnPrefix(String dnPrefix) {
        this.dnPrefix = dnPrefix;
    }

}
