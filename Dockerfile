FROM inseefrlab/onyxia-python-minimal:latest

ENV JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64"
ENV PATH="${JAVA_HOME}/bin:${PATH}"

USER root

RUN apt-get update && \
        # Install JDK
        apt-get install -y --no-install-recommends \
            ca-certificates-java \
            openjdk-11-jre-headless && \
    mamba install -y -c conda-forge \
        ipywidgets \
        jupyterlab && \
    # Fix permissions
    chown -R ${USERNAME}:${GROUPNAME} ${HOME} ${MAMBA_DIR} && \
    # Clean
    rm -rf "${HOME}/.local" && \
    mamba clean --all -f -y && \
    jupyter lab clean

USER ${USERNAME}

# Allows the kernel to load the Spark and Hadoop config.
ENV CLASSPATH_PREFIX "/opt/hadoop/etc/hadoop:/opt/spark/conf"

COPY target/appassembler/ /usr/local/share/jupyter/kernels/trevas/
COPY kernel.json /usr/local/share/jupyter/kernels/trevas/

EXPOSE 8888

CMD ["jupyter", "lab", "--no-browser", "--ip", "0.0.0.0"]