![Build status](https://travis-ci.org/bodastage/boda-bulkcmparser.svg?branch=master)

# boda-bulkcmparser
XML to csv parser for 3GPP Bulk CM data files as defined by ETSI TS 132 615.

The parser will convert from XML to csv any XML file that is complaint with TS 132 615 such as Ericsson and ZTE. The parser creates a separate csv file for each Managed Object in the XML file.

# Usage
```
usage: java -jar boda-bulkcmparser.jar
Parses BulkCM configuration data file XML to csv

 -c,--parameter-config <PARAMETER_CONFIG>   parameter configuration file
 -d,--multivalue-separator <MV_SEPARATOR>   Specify multi value separator.
                                            Default is ";"
 -h,--help                                  show help
 -i,--input-file <INPUT_FILE>               input file or directory name
 -m,--meta-fields                           add meta fields to extracted
                                            parameters. FILENAME,DATETIME
 -o,--output-directory <OUTPUT_DIRECTORY>   output directory name
 -p,--extract-parameters                    extract only the managed
                                            objects and parameters
 -s,--separate-vsdata                       Separate vendor specific data
 -v,--version                               display version

Examples:
java -jar boda-bulkcmparser.jar -i bulkcm_dump.xml -o out_folder
java -jar boda-bulkcmparser.jar -i input_folder -o out_folder
java -jar boda-bulkcmparser.jar -i input_folder -p
java -jar boda-bulkcmparser.jar -i input_folder -p -m

Copyright (c) 2019 Bodastage Solutions(https://www.bodastage.com)
```

# Requirements
To run the jar file, you need Java version 1.8 and above.

# Getting help
To report issues with the application or request new features use the issue [tracker](https://github.com/boda-stage/boda-bulkcmparser/issues). For help and customizations send an email to info@bodastage.com.

# Credits
[Bodastage Solutions](http://www.bodastage.com) - info@bodastage.com

# Contact
For any other concerns apart from issues and feature requests, send an email to info@bodastage.com.

# Licence
This project is licensed under Apache 2.0. See LICENCE file for details.
