FROM inseefrlab/onyxia-jupyter-pyspark:latest

# Allows the kernel to load the Spark and Hadoop config.
ENV CLASSPATH_PREFIX "/opt/hadoop/etc/hadoop:/opt/spark/conf"

COPY target/appassembler/ /usr/local/share/jupyter/kernels/trevas/
COPY kernel.json /usr/local/share/jupyter/kernels/trevas/

COPY target/appassembler/repo/fr/insee/trevas/*/vtl-spark-*.jar /vtl-spark.jar
COPY target/appassembler/repo/fr/insee/trevas/*/vtl-model-*.jar /vtl-model.jar
COPY target/appassembler/repo/fr/insee/trevas/v*/tl-engine-*.jar /vtl-engine.jar
COPY target/appassembler/repo/fr/insee/trevas/vt*/l-jackson-*.jar /vtl-jackson.jar
COPY target/appassembler/repo/fr/insee/trevas/v*/tl-parser-*.jar /vtl-parser.jar

RUN mamba install -y -c conda-forge "elyra[all]"

CMD ["jupyter", "lab", "--no-browser", "--ip", "0.0.0.0"]
