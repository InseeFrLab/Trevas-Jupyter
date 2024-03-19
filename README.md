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
