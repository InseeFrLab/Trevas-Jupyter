/* (C)2024 */
package fr.insee.trevas.jupyter;

import fr.insee.vtl.engine.VtlScriptEngine;
import fr.insee.vtl.spark.SparkDataset;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

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
        SparkConf conf = new SparkConf(true);
        String spark_home = System.getenv("SPARK_HOME") + "/conf";
        Path path = Path.of(spark_home, "spark-defaults.conf");
        if (Files.exists(path)) {
            org.apache.spark.util.Utils.loadDefaultSparkProperties(
                    conf, path.normalize().toAbsolutePath().toString());
            if (conf.get("spark.jars", "").isEmpty()) {
                conf.set(
                        "spark.jars",
                        String.join(
                                ",",
                                "/vtl-spark.jar",
                                "/vtl-model.jar",
                                "/vtl-parser.jar",
                                "/vtl-engine.jar"));
            }
        } else {
            sparkBuilder.master("local");
        }
        return sparkBuilder.config(conf).getOrCreate();
    }

    public static SparkDataset readParquetDataset(SparkSession spark, String path)
            throws Exception {
        Dataset<Row> dataset;
        try {
            dataset = spark.read().parquet(path);
        } catch (Exception e) {
            throw new Exception("Bad format", e);
        }
        return new SparkDataset(dataset);
    }

    public static SparkDataset readSasDataset(SparkSession spark, String path) throws Exception {
        Dataset<Row> dataset;
        try {
            dataset = spark.read().format("com.github.saurfang.sas.spark").load(path);
        } catch (Exception e) {
            throw new Exception(e);
        }
        return new SparkDataset(dataset);
    }

    public static SparkDataset readCSVDataset(SparkSession spark, String path) throws Exception {
        Dataset<Row> dataset;
        var uri = Utils.uri(path);
        var params = new Utils.QueryParam(uri);
        try {
            dataset =
                    spark.read()
                            .option("delimiter", params.getValue("delimiter").orElse(";"))
                            .option("quote", params.getValue("quote").orElse("\""))
                            .option("header", params.getValue("header").orElse("true"))
                            .options(params.flatten())
                            .csv(Path.of(Utils.strip(uri)).normalize().toAbsolutePath().toString());
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
        var uri = Utils.uri(location);
        var params = new Utils.QueryParam(uri);
        sparkDataset
                .write()
                .option("delimiter", params.getValue("delimiter").orElse(";"))
                .option("quote", params.getValue("quote").orElse("\""))
                .option("header", params.getValue("header").orElse("true"))
                .options(params.flatten())
                .mode(SaveMode.Overwrite)
                .csv(Path.of(Utils.strip(uri)).normalize().toAbsolutePath().toString());
    }
}
