package fr.insee.trevas.jupyter;

import io.github.spencerpark.jupyter.kernel.ReplacementOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.script.SimpleBindings;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class AutoCompleterTest {

    private SimpleBindings bindings;
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
        this.completer = new AutoCompleter(this.bindings);
    }

    public ReplacementOptions complete(String code, int at) {
        ReplacementOptions replacements = this.completer.complete(code, at);
        System.out.println("Completing '" + code + "' at " + at);
        System.out.println(replacements.getReplacements());
        return replacements;
    }

//    @Test
//    @Disabled
//    public void testSimpleCompletion() {
//        ReplacementOptions replacements = complete("foo", 2);
//        assertThat(replacements.getReplacements())
//                .contains(":=", "<-");
//    }

//    @Test
//    @Disabled
//    public void testAssignement() {
//        ReplacementOptions replacements = complete("foo := ", 6);
//        assertThat(replacements.getReplacements())
//                .contains("full_join");
//    }
}