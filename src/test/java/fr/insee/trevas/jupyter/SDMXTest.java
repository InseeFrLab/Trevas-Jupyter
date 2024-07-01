package fr.insee.trevas.jupyter;

import fr.insee.vtl.model.Dataset;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SDMXTest {

    @Test
    public void testLoadSDMXSource() {
        Dataset ds = VtlKernel.loadSDMXSource(
                "src/test/resources/sdmx/DSD_BPE_CENSUS.xml",
                "BPE_DETAIL_VTL");
        assertThat(ds.getDataStructure().size()).isEqualTo(6);
    }

    @Test
    public void testLoadSDMXSourceWithData() throws Exception {
        new VtlKernel();
        Dataset ds = VtlKernel.loadSDMXSource(
                "src/test/resources/sdmx/DSD_BPE_CENSUS.xml",
                "BPE_DETAIL_VTL",
                "src/test/resources/sdmx/BPE_DETAIL_SAMPLE.csv");
        assertThat(ds.getDataStructure().size()).isEqualTo(6);
    }

    @Test
    public void testRunSDMXPreview() throws Exception {
        new VtlKernel();
        VtlKernel.runSDMXPreview(
                "src/test/resources/sdmx/DSD_BPE_CENSUS.xml");
    }

    @Test
    public void testRunSDMX() throws Exception {
        new VtlKernel();
        VtlKernel.runSDMX(
                "src/test/resources/sdmx/DSD_BPE_CENSUS.xml",
                Map.of("BPE_DETAIL_VTL", "src/test/resources/sdmx/BPE_DETAIL_SAMPLE.csv",
                        "LEGAL_POP", "src/test/resources/sdmx/LEGAL_POP_NUTS3.csv"
                ));
    }

    @Test
    public void testGetTransformationsVTL() throws Exception {
        new VtlKernel();
        VtlKernel.getTransformationsVTL(
                "src/test/resources/sdmx/DSD_BPE_CENSUS.xml");
    }

    @Test
    public void testGetRulesetsVTL() throws Exception {
        new VtlKernel();
        VtlKernel.getRulesetsVTL(
                "src/test/resources/sdmx/DSD_BPE_CENSUS.xml");
    }
}
