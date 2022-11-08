package fr.insee.trevas.jupyter;

import akovari.antlr4.autocomplete.Antlr4Completer;
import akovari.antlr4.autocomplete.CompletionResult;
import akovari.antlr4.autocomplete.DefaultLexerAndParserFactory;
import fr.insee.vtl.parser.VtlLexer;
import fr.insee.vtl.parser.VtlParser;
import io.github.spencerpark.jupyter.kernel.ReplacementOptions;

import javax.script.Bindings;
import java.util.ArrayList;

public class AutoCompleter {
    private final DefaultLexerAndParserFactory<VtlLexer, VtlParser> factory;
    private final Bindings bindings;

    public AutoCompleter(Bindings bindings) {
        this.bindings = bindings;
        this.factory = new DefaultLexerAndParserFactory<>(VtlLexer::new, VtlParser::new);
    }

    public ReplacementOptions complete(String code, int at) {
        var completions = new Antlr4Completer(factory, code.substring(0, at));
        CompletionResult completionResult = completions.complete();
        // TODO: Handle replacement ranges (remove invalid input from and to)
        // TODO: Suggest variables from binding if variableID token.
        // TODO: Handle type of variables.
        // TODO: Handle customer methods.
        var suggestions = new ArrayList<>(completionResult.getSuggestions());
        return new ReplacementOptions(suggestions, at, at);
    }
}
