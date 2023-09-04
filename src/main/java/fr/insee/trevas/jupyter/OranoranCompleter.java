package fr.insee.trevas.jupyter;

import com.intigua.antlr4.autosuggest.AutoSuggester;
import com.intigua.antlr4.autosuggest.LexerAndParserFactory;
import com.intigua.antlr4.autosuggest.ReflectionLexerAndParserFactory;
import fr.insee.vtl.parser.VtlLexer;
import fr.insee.vtl.parser.VtlParser;
import io.github.spencerpark.jupyter.kernel.ReplacementOptions;

import javax.script.Bindings;
import java.util.ArrayList;
import java.util.Collection;

public class OranoranCompleter implements AutoCompleter {

    private final static LexerAndParserFactory lexerAndParserFactory =
            new ReflectionLexerAndParserFactory(
                    VtlLexer.class, VtlParser.class
            );

    public OranoranCompleter() {
    }

    @Override
    public ReplacementOptions complete(String code, int at) {
        Collection<String> suggestions =
                new AutoSuggester(lexerAndParserFactory, code).suggestCompletions();
        return new ReplacementOptions(new ArrayList<>(suggestions), at, at);
    }
}
