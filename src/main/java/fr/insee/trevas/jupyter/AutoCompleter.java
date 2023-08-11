package fr.insee.trevas.jupyter;

import io.github.spencerpark.jupyter.kernel.ReplacementOptions;

public interface AutoCompleter {
    ReplacementOptions complete(String code, int at);
}
