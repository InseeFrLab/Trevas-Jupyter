package fr.insee.trevas.jupyter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.vtl.model.Structured;
import fr.insee.vtl.spark.SparkDataset;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static SparkDataset readParquetDataset(SparkSession spark, String path) throws Exception {
        Dataset<Row> dataset;
        Dataset<Row> json;
        try {
            dataset = spark.read().parquet(path + "/data");
        } catch (Exception e) {
            try {
                dataset = spark.read()
                        .option("sep", ";")
                        .option("header", "true")
                        .csv(path + "/data");
            } catch (Exception ee) {
                throw new Exception("Bad format. parquet & csv are supported");
            }
        }
        try {
            json = spark.read()
                    .option("multiLine", "true")
                    .json(path + "/structure");
        } catch (Exception e) {
            return new SparkDataset(dataset, Map.of());
        }
        Map<String, fr.insee.vtl.model.Dataset.Role> components = json.collectAsList().stream().map(r -> {
                            String name = r.getAs("name");
                            Class type = r.getAs("type").getClass();
                            fr.insee.vtl.model.Dataset.Role role = fr.insee.vtl.model.Dataset.Role.valueOf(r.getAs("role"));
                            return new Structured.Component(name, type, role);
                        }
                ).collect(Collectors.toList())
                .stream()
                .collect(Collectors.toMap(Structured.Component::getName, Structured.Component::getRole));
        return new SparkDataset(dataset, components);
    }

    public static SparkDataset readCSVDatasetFromHttp(SparkSession spark, String path) throws Exception {
        Dataset<Row> dataset;
        /*Dataset<Row> json;*/
        try {
            dataset = spark.read()
                    .option("delimiter", ";")
                    .option("header", "true")
                    .csv(path);
        } catch (Exception e) {
            throw new Exception("Error while loading data");
        }
        /*try {
            json = spark.read()
                    .option("multiLine", "true")
                    .json(path + "/structure");
        } catch (Exception e) {
            throw new Exception("An error has occurred while loading structure for: " + path);
        }
        Map<String, fr.insee.vtl.model.Dataset.Role> components = json.collectAsList().stream().map(r -> {
                            String name = r.getAs("name");
                            Class type = r.getAs("type").getClass();
                            fr.insee.vtl.model.Dataset.Role role = fr.insee.vtl.model.Dataset.Role.valueOf(r.getAs("role"));
                            return new Structured.Component(name, type, role);
                        }
                ).collect(Collectors.toList())
                .stream()
                .collect(Collectors.toMap(Structured.Component::getName, Structured.Component::getRole));*/
        return new SparkDataset(dataset, Map.of());
    }

    public static void writeParquetDataset(SparkSession spark, String location, SparkDataset dataset) {
        org.apache.spark.sql.Dataset<Row> sparkDataset = dataset.getSparkDataset();
        sparkDataset.write().mode(SaveMode.Overwrite).parquet(location + "/data");
        // Trick to write json thanks to spark
        String json = "";
        try {
            json = objectMapper.writeValueAsString(dataset.getDataStructure().values());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        JavaSparkContext.fromSparkContext(spark.sparkContext())
                .parallelize(List.of(json))
                .coalesce(1)
                .saveAsTextFile(location + "/structure");
    }
}
