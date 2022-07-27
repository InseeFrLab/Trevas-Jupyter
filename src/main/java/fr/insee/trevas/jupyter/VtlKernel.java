package fr.insee.trevas.jupyter;

import fr.insee.vtl.engine.VtlScriptEngine;
import fr.insee.vtl.model.Structured;
import fr.insee.vtl.spark.SparkDataset;
import io.github.spencerpark.jupyter.channels.JupyterConnection;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.kernel.BaseKernel;
import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;
import io.github.spencerpark.jupyter.kernel.LanguageInfo;
import io.github.spencerpark.jupyter.kernel.display.DisplayData;
import org.apache.spark.sql.SparkSession;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

public class VtlKernel extends BaseKernel {

    private static SparkSession spark;
    private final VtlScriptEngine engine;
    private final LanguageInfo info;

    public VtlKernel() throws Exception {
        spark = buildSparkSession();
        this.engine = buildSparkEngine(spark);
        System.out.println("Loaded VTL engine " + engine.getFactory().getEngineVersion());
        ScriptEngineFactory factory = engine.getFactory();
        this.info = new LanguageInfo.Builder(factory.getEngineName())
                .version(factory.getEngineVersion())
                .build();
        registerMethods();
    }

    public static SparkDataset loadS3(String path) throws Exception {
        return Utils.readParquetDataset(spark, path);
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
        // TODO: insert print
        // TODO: insert printMetadata
        // TODO: insert writes3
        this.engine.registerMethod("loadS3", VtlKernel.class.getMethod("loadS3", String.class));
    }

    public VtlScriptEngine buildSparkEngine(SparkSession spark) throws Exception {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByExtension("vtl");
        engine.put("$vtl.engine.processing_engine_names", "spark");
        engine.put("$vtl.spark.session", spark);
        VtlScriptEngine vtlEngine = (VtlScriptEngine) engine;
        return vtlEngine;
    }

    private SparkSession buildSparkSession() {
        SparkSession.Builder sparkBuilder = SparkSession.builder()
                .appName("trevas-jupyter")
                .master("local");
        SparkSession spark = sparkBuilder.getOrCreate();
        return spark;
    }

    @Override
    public DisplayData eval(String expr) throws Exception {
        this.engine.eval(expr);
        // TODO: get varID from expr
        // TODO: loop on them into bindings
        SparkDataset res = (SparkDataset) this.engine.get("res");
        DisplayData displayData = new DisplayData();
        // TODO: build "beautiful" html output for each varID (Structure + X lines)
        StringBuilder sb = new StringBuilder();
        Structured.DataStructure dataStructure = res.getDataStructure();
        dataStructure.entrySet().forEach(entry -> {
            sb.append(entry.getKey()).append("\n");
        });
        displayData.putText("Columns: \n" + sb);
        return displayData;
    }

    @Override
    public LanguageInfo getLanguageInfo() {
        return this.info;
    }
}
