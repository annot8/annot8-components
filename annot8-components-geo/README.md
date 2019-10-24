# Geo Processors

## Data

This repository contains data derived from other data sources.

### Countries JSON

The `countries.json` file was downloaded from http://github.com/mledoze/countries on the 25/09/2019.

Licensed under the ODC Open Database Licence (ODbL) 1.0 - http://opendatacommons.org/licenses/odbl/1.0/

Any rights in individual contents of the database are licensed under the Database Contents License - http://opendatacommons.org/licenses/dbcl/1.0/

### UK Postcodes CSV

The `ukpostcodes.csv` file is derived from the Ordnance Survey CodePoint Open dataset, using data retrieved on 25/09/2019.

Licensed under the Ordnance Survey Data Licence -  www.ordnancesurvey.co.uk/opendata/licence.

Contains Ordnance Survey data (c) Crown copyright and database right 2019.

Contains Royal Mail data (c) Royal Mail copyright and database right 2019.

Contains National Statistics data (c) Crown copyright and database right 2019.

#### Reproducing data

To recreate the `ukpostcodes.csv` file from the CodePoint Open dataset, run the following commands from the CSV directory.

    cut -d, -f1,3,4 *.csv > ukpostcodes.csv
    sed -i 's/"//g' ukpostcodes.csv
    sed -i 's/ //g' ukpostcodes.csv