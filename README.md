![Build status](https://travis-ci.org/bodastage/boda-bulkcmparser.svg?branch=master)

# boda-bulkcmparser
XML to csv parser for 3GPP Bulk CM data files as defined by ETSI TS 132 615.

The parser will convert from XML to csv any XML file that is complaint with TS 132 615 such as Ericsson and ZTE. The parser creates a separate csv file for each Managed Object in the XML file.

#Usage
java -jar  bulkcmparser.jar bulkcm.xml outputDirectory

#Download and installation
The lastest compiled jar file is availabled in the dist directory or get it [here](https://github.com/boda-stage/boda-bulkcmparser/blob/master/dist/boda-bulkcmparser.jar).

#Requirements
To run the jar file, you need Java version 1.6 and above.

#Getting help
To report issues with the application or request new features use the issue [tracker](https://github.com/boda-stage/boda-bulkcmparser/issues). For help and customizations send an email to info@bodastage.com.

#Credits
[Bodastage](http://www.bodastage.com) - info@bodastage.co

#Contact
For any other concerns apart from issues and feature requests, send an email to info@bodastage.com.

#Licence
This project is licensed under Apache 2.0. See LICENCE file for details.


