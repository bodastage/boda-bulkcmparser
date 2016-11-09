/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.com.bodastage.boda_bulkcmparser.bulkcmxml;

import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author dev
 */
public class ManagedELementAttributes {
    private String locationName = "";
    private String userLabel = "SiteA";
    private String vendorName = "Bodastage";
    private String swVersion = "V1.0.1";
    private String managedElementType = "SDR";
    private String dnPrefix = "www.bodastage.com";
    
    private VsDataContainer vsDataContainer = new VsDataContainer();

    public String getLocationName() {
        return locationName;
    }

    @XmlElement(name = "locationName", namespace = "http://www.3gpp.org/ftp/specs/archive/32_series/32.625#genericNrm")
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getUserLabel() {
        return userLabel;
    }

    public void setUserLabel(String userLabel) {
        this.userLabel = userLabel;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getSwVersion() {
        return swVersion;
    }

    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    public String getManagedElementType() {
        return managedElementType;
    }

    public void setManagedElementType(String managedElementType) {
        this.managedElementType = managedElementType;
    }

    public String getDnPrefix() {
        return dnPrefix;
    }

    public void setDnPrefix(String dnPrefix) {
        this.dnPrefix = dnPrefix;
    }

    public VsDataContainer getVsDataContainer() {
        return vsDataContainer;
    }

    public void setVsDataContainer(VsDataContainer vsDataContainer) {
        this.vsDataContainer = vsDataContainer;
    }
}
