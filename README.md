# Trevas Jupyter

Jupyter notebook providing VTL support through Trevas engine

[![Build Status](https://github.com/InseeFrLab/Trevas-Jupyter/actions/workflows/ci.yml/badge.svg)](https://github.com/InseeFrLab/Trevas-Jupyter/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

## Custom functions

Custom functions have been introduced into the Trevas engine.

| Name         | Arguments                | Description                          |
|--------------|--------------------------|--------------------------------------|
| loadParquet  | String url               | Load Parquet dataset                 |
| loadCSV (1)  | String url               | Load CSV dataset                     |
| loadSas      | String url               | Load Sas dataset                     |
| writeParquet | (String url, Dataset ds) | Write given dataset in Parquet       |
| writeCSV (2) | (String url, Dataset ds) | Write given dataset in CSV           |
| show         | Dataset ds               | Display firt rows of a given dataset |
| showMetadata | Dataset ds               | Display metadata of a given dataset  |
| size         | Dataset ds               | Display size of a given dataset      |

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

### (2) writeCSV

Default option values:

|    Name     |  Value  |
|:-----------:|:-------:|
|   header    |  true   |
|  delimiter  |    ;    |
|    quote    |    "    |

Any CSV option can be defined or overridden thanks to url parameters (values have to be encoded).

For instance, to write a CSV with a content delimited by `|` and quoted by `'`:

`writeCSV(...?delimiter=%7C&quote=%27)`

## Launch with demo project

`INIT_PROJECT_URL` docker environment variable enable to load a default project in your Trevas Jupyter instance.

Have a look to [this project definition](https://github.com/Making-Sense-Info/Trevas-Jupyter-Training) for instance.

Fill the `INIT_PROJECT_URL` environment variable with your script adress and run:

```bash
docker run -p 8888:8888 -e INIT_PROJECT_URL="https://raw.githubusercontent.com/Making-Sense-Info/Trevas-Jupyter-Training/main/init-notebook.sh" inseefrlab/trevas-jupyter:latest
```
