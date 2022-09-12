FROM inseefrlab/onyxia-jupyter-python:latest

USER root

# Allows the kernel to load the Spark and Hadoop config.
ENV CLASSPATH_PREFIX "/opt/hadoop/etc/hadoop:/opt/spark/conf"

COPY target/appassembler/ /usr/local/share/jupyter/kernels/trevas/
COPY kernel.json /usr/local/share/jupyter/kernels/trevas/