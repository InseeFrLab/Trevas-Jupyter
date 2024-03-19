package fr.insee.trevas.jupyter;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest {

    @Test
    void testURI() throws URISyntaxException {
        var bases = List.of(
                "http://example.com/foo/bar/file.csv",
                "https://example.com/foo/bar/file.csv",
                "https://example.com:8080/foo/bar/file.csv",
                "file://example.com/foo/bar/file.csv",
                "file://example.com:8080/foo/bar/file.csv",
                "file:///foo/bar/file.csv",
                "/foo/bar/file.csv"
        );
        var queries = List.of(
                "",
                "?sep=,&=del=;",
                "?sep=%2C&del=%3B"
        );
        var fragments = List.of(
                "",
                "#fragment"
        );
        for (String base : bases) {
            for (String query : queries) {
                for (String fragment : fragments) {
                    URI uri = URI.create(base + query + fragment);

                    assertThat(
                            Optional.ofNullable(uri.getQuery())
                                    .map(f -> "?" + f)
                                    .orElse("")
                    ).isEqualTo(URLDecoder.decode(query, StandardCharsets.UTF_8));
                    assertThat(uri.getPath()).isEqualTo("/foo/bar/file.csv");
                    assertThat(Optional.ofNullable(uri.getFragment()).map(f -> "#" + f).orElse("")).isEqualTo(fragment);

                    System.out.println("===============================================================");
                    System.out.println(Utils.uri(base + query + fragment));
                    System.out.println("===============================================================");
                    System.out.println(uri);
                    System.out.printf("Host: %s, Path: %s%n", uri.getHost(), uri.getPath());
                    System.out.printf("Auth: %s, Schm: %s%n", uri.getAuthority(), uri.getScheme());
                    System.out.printf("Query: %s, Fragment: %s%n", uri.getQuery(), uri.getFragment());
                }
            }
        }
    }
}