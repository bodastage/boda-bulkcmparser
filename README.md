# boda-bulkcmparser
XML to csv parser for 3GPP Bulk CM data files as defined by ETSI TS 132 615.

The parser will convert from XML to csv any XML file that is complaint with TS 132 615 such as Ericsson , ZTE and Nokia Siemens. The parser creates a separate csv file for each Managed Object in the XML file.

#Usage
java -jar  bulkcmparser.jar bulkcm.xml outputDirectory

#Download
The lastest compiled jar file is availabled in the dist directory or get it [here](https://github.com/boda-stage/boda-bulkcmparser/blob/master/dist/boda-bulkcmparser.jar).

