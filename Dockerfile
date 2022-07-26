FROM inseefrlab/jupyter-datascience:latest

USER root

RUN apt-get update --yes && \
    apt-get install --yes --no-install-recommends \
    openjdk-11-jre && \
    apt-get clean

COPY target/lib /usr/local/share/jupyter/kernels/trevas/lib/
COPY target/trevas-jupyter-*.jar /usr/local/share/jupyter/kernels/trevas/
COPY target/vtl-engine-*.jar target/vtl-engine.jar
COPY target/vtl-spark-*.jar target/vtl-spark.jar
COPY kernel.json /usr/local/share/jupyter/kernels/trevas

# Set user back to priviledged user.
USER $NB_USER