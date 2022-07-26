package fr.insee.trevas.jupyter;

import fr.insee.vtl.model.Structured;
import fr.insee.vtl.spark.SparkDataset;
import io.github.spencerpark.jupyter.channels.JupyterConnection;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.kernel.BaseKernel;
import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;
import io.github.spencerpark.jupyter.kernel.LanguageInfo;
import io.github.spencerpark.jupyter.kernel.display.DisplayData;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;

import static fr.insee.trevas.jupyter.Utils.buildSparkEngine;

public class VtlKernel extends BaseKernel {

    private final ScriptEngine engine;
    private final LanguageInfo info;

    public VtlKernel(ScriptEngine engine) {
        this.engine = Objects.requireNonNull(engine);
        ScriptEngineFactory factory = engine.getFactory();
        this.info = new LanguageInfo.Builder(factory.getEngineName())
                .version(factory.getEngineVersion())
                .build();
    }

    public static void main(String[] args) throws Exception {

        ScriptEngine engine = buildSparkEngine();

        System.out.println("Loaded VTL engine " + engine.getFactory().getEngineVersion());

        if (args.length < 1)
            throw new IllegalArgumentException("Missing connection file argument");

        Path connectionFile = Paths.get(args[0]);

        if (!Files.isRegularFile(connectionFile))
            throw new IllegalArgumentException("Connection file '" + connectionFile + "' isn't a file.");

        String contents = new String(Files.readAllBytes(connectionFile));

        JupyterSocket.JUPYTER_LOGGER.setLevel(Level.WARNING);

        KernelConnectionProperties connProps = KernelConnectionProperties.parse(contents);
        JupyterConnection connection = new JupyterConnection(connProps);


        VtlKernel kernel = new VtlKernel(engine);

        kernel.becomeHandlerForConnection(connection);

        connection.connect();
        connection.waitUntilClose();
    }


    @Override
    public DisplayData eval(String expr) throws Exception {
        this.engine.eval(expr);
        SparkDataset res = (SparkDataset) this.engine.get("res");
        DisplayData displayData = new DisplayData();
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
