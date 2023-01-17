package fr.insee.trevas.jupyter;

import fr.insee.vtl.spark.SparkDataset;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import java.util.Map;

public class Utils {

    public static SparkDataset readParquetDataset(SparkSession spark, String path) throws Exception {
        Dataset<Row> dataset;
        try {
            dataset = spark.read().parquet(path);
        } catch (Exception e) {
            try {
                dataset = spark.read()
                        .option("sep", ";")
                        .option("header", "true")
                        .csv(path);
            } catch (Exception ee) {
                throw new Exception("Bad format. parquet & csv are supported");
            }
        }
        return new SparkDataset(dataset, Map.of());
    }

    public static void writeParquetDataset(SparkSession spark, String location, SparkDataset dataset) {
        org.apache.spark.sql.Dataset<Row> sparkDataset = dataset.getSparkDataset();
        sparkDataset.write().mode(SaveMode.Overwrite).parquet(location);
    }
}
