# Trevas Jupyter

Jupyter notebook providing VTL support through Trevas engine

[![Build Status](https://github.com/InseeFrLab/Trevas-Jupyter/actions/workflows/ci.yml/badge.svg)](https://github.com/InseeFrLab/Trevas-Jupyter/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

## Custom functions

Custom functions have been introduced into the Trevas engine.

| Name         | Arguments                | Description                          |
| ------------ | ------------------------ | ------------------------------------ |
| loadParquet  | String url               | Load Parquet dataset                 |
| loadCSV      | String url               | Load CSV dataset                     |
| loadSas      | String url               | Load Sas dataset                     |
| writeParquet | (String url, Dataset ds) | Write given dataset in Parquet       |
| writeCSV     | (String url, Dataset ds) | Write given dataset in CSV           |
| show         | Dataset ds               | Display firt rows of a given dataset |
| showMetadata | Dataset ds               | Display metadata of a given dataset  |
| size         | Dataset ds               | Display size of a given dataset      |
