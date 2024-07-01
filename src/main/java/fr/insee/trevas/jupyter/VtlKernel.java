/* (C)2024 */
package fr.insee.trevas.jupyter;

import fr.insee.vtl.engine.VtlScriptEngine;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.PersistentDataset;
import fr.insee.vtl.model.Structured;
import fr.insee.vtl.sdmx.SDMXVTLWorkflow;
import fr.insee.vtl.sdmx.TrevasSDMXUtils;
import fr.insee.vtl.spark.SparkDataset;
import io.github.spencerpark.jupyter.channels.JupyterConnection;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.kernel.BaseKernel;
import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;
import io.github.spencerpark.jupyter.kernel.LanguageInfo;
import io.github.spencerpark.jupyter.kernel.ReplacementOptions;
import io.github.spencerpark.jupyter.kernel.display.DisplayData;
import io.sdmx.api.io.ReadableDataLocation;
import io.sdmx.utils.core.io.ReadableDataLocationTmp;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import org.apache.spark.sql.SparkSession;

public class VtlKernel extends BaseKernel {

    private static DisplayData displayData = new DisplayData();
    private static SparkSession spark;
    private static VtlScriptEngine engine;
    private final LanguageInfo info;
    private final AutoCompleter autoCompleter;

    public VtlKernel() throws Exception {
        spark = SparkUtils.buildSparkSession();
        engine = SparkUtils.buildSparkEngine(spark);
        System.out.println("Loaded VTL engine " + engine.getFactory().getEngineName());
        ScriptEngineFactory factory = engine.getFactory();
        this.info =
                new LanguageInfo.Builder(factory.getEngineName())
                        .version(factory.getEngineVersion())
                        .build();
        registerGlobalMethods();
        this.autoCompleter = new OranoranCompleter();
    }

    private static Map<String, Dataset.Role> getRoleMap(
            Collection<Structured.Component> components) {
        return components.stream()
                .collect(
                        Collectors.toMap(
                                Structured.Component::getName, Structured.Component::getRole));
    }

    private static Map<String, Dataset.Role> getRoleMap(fr.insee.vtl.model.Dataset dataset) {
        return getRoleMap(dataset.getDataStructure().values());
    }

    private static SparkDataset asSparkDataset(Dataset dataset) {
        if (dataset instanceof SparkDataset) {
            return (SparkDataset) dataset;
        }
        if (dataset instanceof PersistentDataset) {
            fr.insee.vtl.model.Dataset ds = ((PersistentDataset) dataset).getDelegate();
            if (ds instanceof SparkDataset) {
                return (SparkDataset) ds;
            } else {
                return new SparkDataset(ds, getRoleMap(dataset), spark);
            }
        }
        throw new IllegalArgumentException("Unknow dataset type");
    }

    public static SparkDataset loadParquet(String path) throws Exception {
        return SparkUtils.readParquetDataset(spark, path);
    }

    public static SparkDataset loadCSV(String path) throws Exception {
        return SparkUtils.readCSVDataset(spark, path);
    }

    public static SparkDataset loadSas(String path) throws Exception {
        return SparkUtils.readSasDataset(spark, path);
    }

    public static String writeParquet(String path, Dataset ds) {
        SparkUtils.writeParquetDataset(path, asSparkDataset(ds));
        return "Dataset written: '" + path + "'";
    }

    public static String writeCSV(String path, Dataset ds) {
        SparkUtils.writeCSVDataset(path, asSparkDataset(ds));
        return "Dataset written: '" + path + "'";
    }

    public static String getSize(Dataset ds) {
        SparkDataset sparkDataset = asSparkDataset(ds);
        return "Dataset size: " + sparkDataset.getDataPoints().size();
    }

    public static Object show(Object o) {
        if (o instanceof Dataset) {
            SparkDataset dataset = asSparkDataset((Dataset) o);
            var roles =
                    dataset.getDataStructure().entrySet().stream()
                            .collect(
                                    Collectors.toMap(
                                            Map.Entry::getKey, e -> e.getValue().getRole()));
            showDataset(new SparkDataset(dataset.getSparkDataset().limit(50), roles));
        } else {
            displayData.putText(o.toString());
        }
        return o;
    }

    private static void showDataset(Dataset dataset) {
        displayData.putHTML(DatasetUtils.datasetToDisplay(dataset));
    }

    public static Object showMetadata(Object o) {
        if (o instanceof Dataset) {
            displayData.putText(DatasetUtils.datasetMetadataToDisplay((Dataset) o));
        } else {
            displayData.putText(o.toString());
        }
        return o;
    }

    public static Dataset loadSDMXSource(String path, String id) {
        Structured.DataStructure structure = TrevasSDMXUtils.buildStructureFromSDMX3(path, id);
        return new InMemoryDataset(List.of(List.of()), structure);
    }

    public static Dataset loadSDMXSource(String path, String id, String dataPath) {
        Structured.DataStructure structure = TrevasSDMXUtils.buildStructureFromSDMX3(path, id);
        return new SparkDataset(
                spark.read()
                        .option("header", "true")
                        .option("delimiter", ";")
                        .option("quote", "\"")
                        .csv(dataPath),
                structure);
    }

    public static void runSDMXPreview(String path) {
        ReadableDataLocation rdl = new ReadableDataLocationTmp(path);

        SDMXVTLWorkflow sdmxVtlWorkflow = new SDMXVTLWorkflow(engine, rdl, Map.of());

        Map<String, Dataset> emptyDatasets = sdmxVtlWorkflow.getEmptyDatasets();
        engine.getBindings(ScriptContext.ENGINE_SCOPE).putAll(emptyDatasets);

        Map<String, PersistentDataset> results = sdmxVtlWorkflow.run();

        var result = new StringBuilder();

        results.forEach(
                (k, v) -> {
                    result.append("<h2>")
                            .append(k)
                            .append("</h2>")
                            .append(DatasetUtils.datasetMetadataToDisplay(v));
                });

        displayData.putText(result.toString());
    }

    public static void runSDMX(String path, Map<String, String> data) {

        Map<String, Dataset> inputs =
                data.entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> {
                                            Structured.DataStructure structure =
                                                    TrevasSDMXUtils.buildStructureFromSDMX3(
                                                            path, e.getKey());
                                            return new SparkDataset(
                                                    spark.read()
                                                            .option("header", "true")
                                                            .option("delimiter", ";")
                                                            .option("quote", "\"")
                                                            .csv(e.getValue()),
                                                    structure);
                                        }));

        ReadableDataLocation rdl = new ReadableDataLocationTmp(path);
        SDMXVTLWorkflow sdmxVtlWorkflow = new SDMXVTLWorkflow(engine, rdl, inputs);
        Map<String, PersistentDataset> results = sdmxVtlWorkflow.run();

        var result = new StringBuilder();

        results.forEach(
                (k, v) -> {
                    result.append("<h2>")
                            .append(k)
                            .append("</h2>")
                            .append(DatasetUtils.datasetToDisplay(v));
                });

        displayData.putText(result.toString());
    }

    public static void getTransformationsVTL(String path) {
        ReadableDataLocation rdl = new ReadableDataLocationTmp(path);
        SDMXVTLWorkflow sdmxVtlWorkflow = new SDMXVTLWorkflow(engine, rdl, Map.of());
        String vtl = sdmxVtlWorkflow.getTransformationsVTL();

        displayData.putText(vtl);
    }

    public static void getRulesetsVTL(String path) {
        ReadableDataLocation rdl = new ReadableDataLocationTmp(path);
        SDMXVTLWorkflow sdmxVtlWorkflow = new SDMXVTLWorkflow(engine, rdl, Map.of());
        String dprs = sdmxVtlWorkflow.getRulesetsVTL();

        displayData.putText(dprs);
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) throw new IllegalArgumentException("Missing connection file argument");

        Path connectionFile = Paths.get(args[0]);

        if (!Files.isRegularFile(connectionFile))
            throw new IllegalArgumentException(
                    "Connection file '" + connectionFile + "' isn't a file.");

        String contents = new String(Files.readAllBytes(connectionFile));

        JupyterSocket.JUPYTER_LOGGER.setLevel(Level.WARNING);

        KernelConnectionProperties connProps = KernelConnectionProperties.parse(contents);
        JupyterConnection connection = new JupyterConnection(connProps);

        VtlKernel kernel = new VtlKernel();

        kernel.becomeHandlerForConnection(connection);

        connection.connect();
        connection.waitUntilClose();
    }

    private void registerGlobalMethods() throws NoSuchMethodException {
        this.engine.registerGlobalMethod(
                "loadParquet", VtlKernel.class.getMethod("loadParquet", String.class));
        this.engine.registerGlobalMethod(
                "loadCSV", VtlKernel.class.getMethod("loadCSV", String.class));
        this.engine.registerGlobalMethod(
                "loadSas", VtlKernel.class.getMethod("loadSas", String.class));
        this.engine.registerGlobalMethod(
                "writeParquet",
                VtlKernel.class.getMethod("writeParquet", String.class, Dataset.class));
        this.engine.registerGlobalMethod(
                "writeCSV", VtlKernel.class.getMethod("writeCSV", String.class, Dataset.class));
        this.engine.registerGlobalMethod("show", VtlKernel.class.getMethod("show", Object.class));
        this.engine.registerGlobalMethod(
                "showMetadata", VtlKernel.class.getMethod("showMetadata", Object.class));
        this.engine.registerGlobalMethod(
                "size", VtlKernel.class.getMethod("getSize", Dataset.class));

        // SDMX
        this.engine.registerGlobalMethod(
                "loadSDMXSource",
                VtlKernel.class.getMethod("loadSDMXSource", String.class, String.class));
        this.engine.registerGlobalMethod(
                "loadSDMXSource",
                VtlKernel.class.getMethod(
                        "loadSDMXSource", String.class, String.class, String.class));
        this.engine.registerGlobalMethod(
                "runSDMXPreview", VtlKernel.class.getMethod("runSDMXPreview", String.class));
        // this.engine.registerGlobalMethod(
        //        "runSDMX", VtlKernel.class.getMethod("runSDMX", String.class, Map<String, String>)
        // );
        this.engine.registerGlobalMethod(
                "getTransformationsVTL",
                VtlKernel.class.getMethod("getTransformationsVTL", String.class));
        this.engine.registerGlobalMethod(
                "getRulesetsVTL", VtlKernel.class.getMethod("getRulesetsVTL", String.class));
    }

    @Override
    public synchronized DisplayData eval(String expr) throws Exception {
        displayData = new DisplayData();
        this.engine.eval(expr);
        return displayData;
    }

    @Override
    public ReplacementOptions complete(String code, int at) {
        return this.autoCompleter.complete(code, at);
    }

    @Override
    public LanguageInfo getLanguageInfo() {
        return this.info;
    }
}
