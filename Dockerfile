FROM inseefrlab/onyxia-jupyter-pyspark:latest

# Allows the kernel to load the Spark and Hadoop config.
ENV CLASSPATH_PREFIX "/opt/hadoop/etc/hadoop:/opt/spark/conf"

COPY target/appassembler/ /usr/local/share/jupyter/kernels/trevas/
COPY kernel.json /usr/local/share/jupyter/kernels/trevas/

CMD ["jupyter", "lab", "--no-browser", "--ip", "0.0.0.0"]
