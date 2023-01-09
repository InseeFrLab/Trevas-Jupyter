# Trevas Jupyter

Jupyter notebook providing VTL support through Trevas engine

[![Build Status](https://github.com/InseeFrLab/Trevas-Jupyter/actions/workflows/ci.yml/badge.svg)](https://github.com/InseeFrLab/Trevas-Jupyter/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

## Custom functions

| name         | arguments        | Description                          |
| ------------ | ---------------- | ------------------------------------ |
| loadS3       | s3 URL           | Load Parquet, CSV Dataset            |
| writeS3      | s3 URL - dataset | Write given dataset in Parquet       |
| show         | dataset          | Display firt rows of a given dataset |
| showMetadata | dataset          | Display metadata of a given dataset  |
| size         | dataset          | Display size of a given dataset      |
