/* (C)2024 */
import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.trevas.jupyter.SparkUtils;
import fr.insee.trevas.jupyter.Utils;
import fr.insee.vtl.model.Dataset;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;

public class CSVTest {
    @Test
    void testURI() throws URISyntaxException {
        URI uri;

        uri = Utils.uri("src/test/resources/ds1.csv");
        URI expected = Path.of("src/test/resources/ds1.csv").toUri();
        assertThat(uri).isEqualTo(expected);

        uri = Utils.uri("src/test/resources/ds1.csv#fragment");
        assertThat(uri.getHost()).isEqualTo(expected.getHost());
        assertThat(uri.getAuthority()).isEqualTo(expected.getAuthority());
        assertThat(uri.getFragment()).isEqualTo("fragment");

        uri = Utils.uri("src/test/resources/ds1.csv?foo=bar&foo=baz");
        assertThat(uri.getHost()).isEqualTo(expected.getHost());
        assertThat(uri.getAuthority()).isEqualTo(expected.getAuthority());
        assertThat(uri.getQuery()).isEqualTo("foo=bar&foo=baz");

        uri = Utils.uri("src/test/resources/ds1.csv?sep=%7C&del=%3B");
        assertThat(uri.getHost()).isEqualTo(expected.getHost());
        assertThat(uri.getAuthority()).isEqualTo(expected.getAuthority());
        assertThat(uri.getQuery()).isEqualTo("sep=|&del=;");
    }

    @Test
    public void readCSVDatasetTest() throws Exception {
        try (SparkSession spark =
                SparkSession.builder().appName("test").master("local").getOrCreate()) {
            Dataset ds1 = SparkUtils.readCSVDataset(spark, "src/test/resources/ds1.csv");
            assertThat(ds1.getDataPoints().get(1).get("name")).isEqualTo("B");
            Dataset ds2 = SparkUtils.readCSVDataset(spark, "src/test/resources/ds2.csv?sep=%7C");
            assertThat(ds2.getDataPoints().get(1).get("name")).isEqualTo("G");
        }
    }
}
