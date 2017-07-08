@javax.xml.bind.annotation.XmlSchema(
    namespace = "http://www.3gpp.org/ftp/specs/archive/32_series/32.615#configData",
    elementFormDefault = XmlNsForm.QUALIFIED,
	xmlns = {	
	@XmlNs(prefix="en",  namespaceURI="http://www.3gpp.org/ftp/specs/archive/32_series/32.765#eutranNrm"),
	@XmlNs(prefix="un",  namespaceURI="http://www.3gpp.org/ftp/specs/archive/32_series/32.765#utranNrm"),
	@XmlNs(prefix="gn",  namespaceURI="http://www.3gpp.org/ftp/specs/archive/32_series/32.765#gsmNrm"),
	@XmlNs(prefix="xn",  namespaceURI="http://www.3gpp.org/ftp/specs/archive/32_series/32.625#genericNrm"),
	@XmlNs(prefix="bs",  namespaceURI="http://BodastageSpecificAttributes#BodastageSpecificAttributes"),
	@XmlNs(prefix="xsi", namespaceURI="http://www.w3.org/2001/XMLSchema-instance")
    }
  )
package com.bodastage.boda_bulkcmparser.bulkcmxml;
import javax.xml.bind.annotation.*;
