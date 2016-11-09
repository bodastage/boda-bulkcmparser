/*
 * MeContext.
 */
package test.java.com.bodastage.boda_bulkcmparser.bulkcmxml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @version 1.0.0
 * @author Bodastage<info@bodastage.com>
 */
class MeContext {
    private String id = "1";
    private MeContextAttributes attributes = new MeContextAttributes();
    private ManagedElement managedElement = new ManagedElement();
    
    public String getId() {
        return id;
    }

    @XmlAttribute(name = "id", namespace = "")
    public void setId(String id) {
        this.id = id;
    }

    public MeContextAttributes getAttributes() {
        return attributes;
    }

    @XmlElement(name = "attributes", namespace = "http://www.3gpp.org/ftp/specs/archive/32_series/32.625#genericNrm")
    public void setAttributes(MeContextAttributes attributes) {
        this.attributes = attributes;
    }
    
    public ManagedElement getManagedElement() {
        return managedElement;
    }

    @XmlElement(name = "ManagedElement", namespace = "http://www.3gpp.org/ftp/specs/archive/32_series/32.625#genericNrm")
    public void setManagedElement(ManagedElement managedElement) {
        this.managedElement = managedElement;
    }
}
