/*
 * File footer.
 */
package test.java.com.bodastage.boda_bulkcmparser.bulkcmxml;

import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @version 1.0.0
 * @author Bodastage<info@bodastage.com>
 */
public class FileFooter {
    private String dateTime = "2015-11-11T09:04:20+02:00";

    public String getDateTime() {
        return dateTime;
    }

    @XmlAttribute( name = "dateTime" , namespace = "" )
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
    
    
}
