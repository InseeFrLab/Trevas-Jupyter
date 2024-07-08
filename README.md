# Trevas Jupyter

Jupyter notebook providing VTL support through Trevas engine

[![Build Status](https://github.com/InseeFrLab/Trevas-Jupyter/actions/workflows/ci.yml/badge.svg)](https://github.com/InseeFrLab/Trevas-Jupyter/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

## Custom functions

Custom functions have been introduced into the Trevas engine.

| Name                  | Arguments                                               | Returned type | Description                                                                             |
| --------------------- | ------------------------------------------------------- | ------------- | --------------------------------------------------------------------------------------- |
| loadParquet           | String url                                              | Dataset       | Load Parquet dataset                                                                    |
| loadCSV (1)           | String url                                              | Dataset       | Load CSV dataset                                                                        |
| loadSas               | String url                                              | Dataset       | Load Sas dataset                                                                        |
| loadSDMXEmptySource   | (String sdmxMesUrl, String structureId)                 | Dataset       | Load SDMX empty source                                                                  |
| loadSDMXSource (2)    | (String sdmxMesUrl, String structureId, String dataUrl) | Dataset       | Load SDMX source                                                                        |
| writeParquet          | (String url, Dataset ds)                                | String        | Write given dataset in Parquet                                                          |
| writeCSV (3)          | (String url, Dataset ds)                                | String        | Write given dataset in CSV                                                              |
| show                  | Dataset ds                                              | DisplayData   | Display firt rows of a given dataset                                                    |
| showMetadata          | Dataset ds                                              | DisplayData   | Display metadata of a given dataset                                                     |
| runSDMXPreview        | String sdmxMesUrl                                       | DisplayData   | Run SDMX VTL transformations, with empty datasets, to obtain Persitent defined datasets |
| runSDMX (4)           | (String sdmxMesUrl, String dataLocations )              | DisplayData   | Run SDMX VTL transformations, with sources, to obtain Persitent defined datasets        |
| getTransformationsVTL | String sdmxMesUrl                                       | String        | Display VTL transformations defined in the SDMX Message file                            |
| getRulesetsVTL        | String sdmxMesUrl                                       | String        | Display VTL rulesets defined in the SDMX Message file                                   |
| size                  | Dataset ds                                              | String        | Display size of a given dataset                                                         |

### (1) loadCSV

Default option values:

|    Name     |  Value  |
|:-----------:|:-------:|
|   header    |  true   |
|  delimiter  |    ;    |
|    quote    |    "    |

Any CSV option can be defined or overridden thanks to url parameters (values have to be encoded).

For instance, to read a CSV content where delimiter is `|` and quote is `'`:

`loadCSV(...?delimiter=%7C&quote=%27)`

### (2) loadSDMXSource

Sources has to be `.csv` files for now.

### (3) writeCSV

Default option values:

|    Name     |  Value  |
|:-----------:|:-------:|
|   header    |  true   |
|  delimiter  |    ;    |
|    quote    |    "    |

Any CSV option can be defined or overridden thanks to url parameters (values have to be encoded).

For instance, to write a CSV with a content delimited by `|` and quoted by `'`:

`writeCSV(...?delimiter=%7C&quote=%27)`

### (4) runSDMX

Sources has to be `.csv` files for now.

Second argument, `dataLocations` has to be a string separated field containing SDMX structure id and source location (ex: `structId1,dataLocation1,structId2,dataLocation2`)

## Launch with demo project

`INIT_PROJECT_URL` docker environment variable enable to load a default project in your Trevas Jupyter instance.

Have a look to [this project definition](https://github.com/Making-Sense-Info/Trevas-Jupyter-Training) for instance.

Fill the `INIT_PROJECT_URL` environment variable with your script adress and run:

```bash
docker pull inseefrlab/trevas-jupyter:latest
docker run -p 8888:8888 -e INIT_PROJECT_URL="https://raw.githubusercontent.com/Making-Sense-Info/Trevas-Jupyter-Training/main/init-notebook.sh" inseefrlab/trevas-jupyter:latest
```
