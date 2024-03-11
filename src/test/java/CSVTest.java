import fr.insee.trevas.jupyter.SparkUtils;
import fr.insee.vtl.model.Dataset;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CSVTest {

    private SparkSession spark;

    @BeforeEach
    public void setUp() {
        spark = SparkSession.builder()
                .appName("test")
                .master("local")
                .getOrCreate();
    }

    @Test
    public void readCSVDatasetTest() throws Exception {
        Dataset ds1 = SparkUtils.readCSVDataset(spark, "src/test/resources/ds1.csv");
        assertThat(ds1.getDataPoints().get(1).get("name")).isEqualTo("B");
        Dataset ds2 = SparkUtils.readCSVDataset(spark, "src/test/resources/ds2.csv?sep=\"|\"&delimiter=\"\"");
        assertThat(ds2.getDataPoints().get(1).get("name")).isEqualTo("B");
    }
}
