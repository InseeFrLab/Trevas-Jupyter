FROM inseefrlab/jupyter-datascience:latest

USER root

RUN apt-get update --yes && \
    apt-get install --yes --no-install-recommends \
    openjdk-11-jre && \
    apt-get clean

COPY target/appassembler/ /usr/local/share/jupyter/kernels/trevas/
COPY kernel.json /usr/local/share/jupyter/kernels/trevas/

# Set user back to privileged user.
USER $NB_USER