package fr.insee.trevas.jupyter;

import io.github.spencerpark.jupyter.kernel.ReplacementOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class AutoCompleterTest {

    private Bindings bindings;
    private AutoCompleter completer;

    public static void setLevel(Level targetLevel) {
        Logger root = Logger.getLogger("");
        root.setLevel(targetLevel);
        for (Handler handler : root.getHandlers()) {
            handler.setLevel(targetLevel);
        }
        System.out.println("level set: " + targetLevel.getName());
    }

    @BeforeEach
    void setUp() {
        setLevel(Level.ALL);
        this.bindings = new SimpleBindings();
        this.completer = new OranoranCompleter(this.bindings);
    }

    @Test
    public void testSimpleCompletion() {
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
        for (int i = 0; i < 10; i++) {
            Instant start = Instant.now();
            ReplacementOptions replacements = completer.complete("foo := ", 6);
            Instant end = Instant.now();
            Duration time = Duration.between(start, end);
            System.out.println(time);
            assertThat(replacements.getReplacements()).contains("full_join");
        }
    }
}