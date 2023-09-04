package fr.insee.trevas.jupyter;

import io.github.spencerpark.jupyter.kernel.ReplacementOptions;
import org.junit.jupiter.api.Test;

import javax.script.SimpleBindings;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class AutoCompleterTest {

    @Test
    public void testSimpleCompletion() {
        AutoCompleter completer = new OranoranCompleter();
        for (int i = 0; i < 10; i++) {
            Instant start = Instant.now();
            ReplacementOptions replacements = completer.complete("foo", 2);
            Instant end = Instant.now();
            Duration time = Duration.between(start, end);
            System.out.println(time);
            assertThat(replacements.getReplacements()).contains(":=", "<-");
        }
    }

    @Test
    public void testAssignment() {
        AutoCompleter completer = new OranoranCompleter();
        for (int i = 0; i < 10; i++) {
            Instant start = Instant.now();
            ReplacementOptions replacements = completer.complete("foo := union(a,b", 16);
            Instant end = Instant.now();
            Duration time = Duration.between(start, end);
            System.out.println(time);
            assertThat(replacements.getReplacements()).contains(")");
        }
    }
}