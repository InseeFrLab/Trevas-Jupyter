package fr.insee.trevas.jupyter;

import fr.insee.vtl.engine.VtlScriptEngine;
import fr.insee.vtl.spark.SparkDataset;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.nio.file.Path;

public class SparkUtils {

    public static VtlScriptEngine buildSparkEngine(SparkSession spark) {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByExtension("vtl");
        engine.put("$vtl.engine.processing_engine_names", "spark");
        engine.put("$vtl.spark.session", spark);
        return (VtlScriptEngine) engine;
    }

    public static SparkSession buildSparkSession() {
        SparkSession.Builder sparkBuilder = SparkSession.builder();
//                .master("local");
        SparkConf conf = new SparkConf(true);
        String spark_home = System.getenv("SPARK_HOME") + "/conf";
        Path path = Path.of(spark_home, "spark-defaults.conf");
        org.apache.spark.util.Utils.loadDefaultSparkProperties(conf, path.normalize().toAbsolutePath().toString());
        conf.set("spark.jars", String.join(",",
                "/vtl-spark.jar",
                "/vtl-model.jar",
                "/vtl-parser.jar",
                "/vtl-engine.jar",
                "/hadoop-aws.jar"
        ));
        conf.set("spark.kubernetes.container.image", "inseefrlab/spark-hadoop:trevas-0.4.8-spark-3.2.1-hadoop-3.3.1-postgresql-42.3.3-postgis-2021.1.0");
        conf.set("spark.kubernetes.container.pullPolicy", "IfNotPresent");
        return sparkBuilder.config(conf).getOrCreate();
    }

    public static SparkSession buildLocalSparkSession() {
        SparkSession.Builder sparkBuilder = SparkSession.builder()
                .master("local");
        return sparkBuilder.getOrCreate();
    }

    public static SparkDataset readParquetDataset(SparkSession spark, String path) throws Exception {
        Dataset<Row> dataset;
        try {
            dataset = spark.read().parquet(path);
        } catch (Exception e) {
            throw new Exception("Bad format", e);

        }
        return new SparkDataset(dataset);
    }

    public static SparkDataset readCSVDataset(SparkSession spark, String path) throws Exception {
        Dataset<Row> dataset;
        try {
            dataset = spark.read()
                    .option("sep", ";")
                    .option("header", "true")
                    .csv(path);
        } catch (Exception e) {
            throw new Exception(e);

        }
        return new SparkDataset(dataset);
    }

    public static void writeParquetDataset(String location, SparkDataset dataset) {
        org.apache.spark.sql.Dataset<Row> sparkDataset = dataset.getSparkDataset();
        sparkDataset.write().mode(SaveMode.Overwrite).parquet(location);
    }

    public static void writeCSVDataset(String location, SparkDataset dataset) {
        org.apache.spark.sql.Dataset<Row> sparkDataset = dataset.getSparkDataset();
        sparkDataset.write().mode(SaveMode.Overwrite).csv(location);
    }
}
