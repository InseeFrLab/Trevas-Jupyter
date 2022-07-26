FROM inseefrlab/jupyter-datascience:latest

USER root

COPY target/appassembler/ /usr/local/share/jupyter/kernels/trevas/
COPY kernel.json /usr/local/share/jupyter/kernels/trevas/

# Set user back to privileged user.
USER $NB_USER