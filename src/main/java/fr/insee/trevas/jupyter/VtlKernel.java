package fr.insee.trevas.jupyter;

import fr.insee.vtl.engine.VtlScriptEngine;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Structured;
import fr.insee.vtl.spark.SparkDataset;
import io.github.spencerpark.jupyter.channels.JupyterConnection;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.kernel.BaseKernel;
import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;
import io.github.spencerpark.jupyter.kernel.LanguageInfo;
import io.github.spencerpark.jupyter.kernel.ReplacementOptions;
import io.github.spencerpark.jupyter.kernel.display.DisplayData;
import org.apache.spark.sql.SparkSession;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class VtlKernel extends BaseKernel {

    private static DisplayData displayData = new DisplayData();
    private static SparkSession spark;
    private final VtlScriptEngine engine;
    private final LanguageInfo info;
    private final AutoCompleter autoCompleter;

    public VtlKernel() throws Exception {
        spark = buildSparkSession();
        this.engine = buildSparkEngine(spark);
        System.out.println("Loaded VTL engine " + engine.getFactory().getEngineVersion());
        ScriptEngineFactory factory = engine.getFactory();
        this.info = new LanguageInfo.Builder(factory.getEngineName())
                .version(factory.getEngineVersion())
                .build();
        registerMethods();
        this.autoCompleter = new AutoCompleter(this.engine.getBindings(ScriptContext.ENGINE_SCOPE));
    }

    public static SparkDataset loadS3(String path) throws Exception {
        return Utils.readParquetDataset(spark, path);
    }

    private static Map<String, Dataset.Role> getRoleMap(Collection<Structured.Component> components) {
        return components.stream()
                .collect(Collectors.toMap(
                        Structured.Component::getName,
                        Structured.Component::getRole
                ));
    }

    private static Map<String, Dataset.Role> getRoleMap(fr.insee.vtl.model.Dataset dataset) {
        return getRoleMap(dataset.getDataStructure().values());
    }

    private static SparkDataset asSparkDataset(Dataset dataset) {
        if (dataset instanceof SparkDataset) {
            return (SparkDataset) dataset;
        } else {
            return new SparkDataset(dataset, getRoleMap(dataset), spark);
        }
    }

    public static String writeS3(String path, Dataset ds) {
        // TODO: replace with SparkDataset constructor when available in Trevas
        Utils.writeParquetDataset(spark, path, asSparkDataset(ds));
        return "Dataset written: '" + path + "'";
    }

    public static String getSize(Dataset ds) {
        SparkDataset sparkDataset = asSparkDataset(ds);
        return "Dataset size: " + sparkDataset.getDataPoints().size();
    }

    public static Object show(Object o) {
        if (o instanceof Dataset) {
            SparkDataset dataset = asSparkDataset((Dataset) o);
            var roles = dataset.getDataStructure().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getRole()));
            showDataset(new SparkDataset(dataset.getSparkDataset().limit(50), roles));
        } else {
            displayData.putText(o.toString());
        }
        return o;
    }

    private static void showDataset(Dataset dataset) {
        var b = new StringBuilder();
        b.append("<table id='dataset_").append(dataset.hashCode()).append("' class='display'>");
        b.append("<thead>");
        b.append("<tr>");
        dataset.getDataStructure().forEach((name, component) -> {
            b.append("<th>").append(name).append("</th>");
        });
        b.append("</tr>");
        b.append("</thead>");
        b.append("<tbody>");
        dataset.getDataPoints().forEach(row -> {
            b.append("<tr>");
            dataset.getDataStructure().keySet().forEach(name -> {
                b.append("<td>").append(row.get(name)).append("</td>");
            });
            b.append("</tr>");
        });
        b.append("</tbody>");
        b.append("</table>");
        b.append("<script\n" +
                "  src=\"https://code.jquery.com/jquery-3.6.0.slim.min.js\"\n" +
                "  integrity=\"sha256-u7e5khyithlIdTpu22PHhENmPcRdFiHRjhAuHcs05RI=\"\n" +
                "  crossorigin=\"anonymous\"></script>");
        b.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.datatables.net/1.12.1/css/jquery.dataTables.css\">\n" +
                "  \n" +
                "<script type=\"text/javascript\" charset=\"utf8\" src=\"https://cdn.datatables.net/1.12.1/js/jquery.dataTables.js\"></script>\n");
        b.append("<script type=\"text/javascript\">" +
                "$(document).ready( function () {\n" +
                "    $('#dataset_" + dataset.hashCode() + "').DataTable();\n" +
                "} );" +
                "</script>");
        displayData.putHTML(b.toString());
    }

    public static Object showMetadata(Object o) {
        if (o instanceof Dataset) {
            SparkDataset ds = (SparkDataset) o;
            StringBuilder sb = new StringBuilder();
            Structured.DataStructure dataStructure = ds.getDataStructure();
            dataStructure.forEach((key, value) -> {
                sb.append(key)
                        .append(" (")
                        .append(value.getRole().name())
                        .append(" - ")
                        .append(value.getType().getSimpleName())
                        .append(")")
                        .append("\n");
            });
            displayData.putText(sb.toString());
        } else {
            displayData.putText(o.toString());
        }
        return o;
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1)
            throw new IllegalArgumentException("Missing connection file argument");

        Path connectionFile = Paths.get(args[0]);

        if (!Files.isRegularFile(connectionFile))
            throw new IllegalArgumentException("Connection file '" + connectionFile + "' isn't a file.");

        String contents = new String(Files.readAllBytes(connectionFile));

        JupyterSocket.JUPYTER_LOGGER.setLevel(Level.WARNING);

        KernelConnectionProperties connProps = KernelConnectionProperties.parse(contents);
        JupyterConnection connection = new JupyterConnection(connProps);


        VtlKernel kernel = new VtlKernel();

        kernel.becomeHandlerForConnection(connection);

        connection.connect();
        connection.waitUntilClose();
    }

    private void registerMethods() throws NoSuchMethodException {
        this.engine.registerMethod("loadS3", VtlKernel.class.getMethod("loadS3", String.class));
        this.engine.registerMethod("writeS3", VtlKernel.class.getMethod("writeS3", String.class, Dataset.class));
        this.engine.registerMethod("show", VtlKernel.class.getMethod("show", Object.class));
        this.engine.registerMethod("showMetadata", VtlKernel.class.getMethod("showMetadata", Object.class));
        this.engine.registerMethod("size", VtlKernel.class.getMethod("getSize", Dataset.class));
    }

    public VtlScriptEngine buildSparkEngine(SparkSession spark) throws Exception {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByExtension("vtl");
        engine.put("$vtl.engine.processing_engine_names", "spark");
        engine.put("$vtl.spark.session", spark);

        /*engine.getBindings(ScriptContext.ENGINE_SCOPE).put("test", new InMemoryDataset(
                List.of(
                        List.of("a", 1L, 2L),
                        List.of("b", 3L, 4L),
                        List.of("c", 5L, 6L),
                        List.of("d", 7L, 8L)
                ),
                List.of(
                        new Structured.Component("name", String.class, Dataset.Role.IDENTIFIER),
                        new Structured.Component("age", Long.class, Dataset.Role.MEASURE),
                        new Structured.Component("weight", Long.class, Dataset.Role.MEASURE)
                )
        ));*/
        return (VtlScriptEngine) engine;
    }

    private SparkSession buildSparkSession() {
        SparkSession.Builder sparkBuilder = SparkSession.builder()
                .appName("trevas-jupyter")
                .master("local");
        return sparkBuilder.getOrCreate();
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
