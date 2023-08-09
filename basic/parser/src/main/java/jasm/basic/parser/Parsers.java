package jasm.basic.parser;

import static jasm.basic.parser.Result.Error;
import static jasm.basic.parser.Result.Ok;

public class Parsers {
    private Parsers() {
    }

    public static Parser<String> prefix(String prefix) {
        return input -> input.text().startsWith(prefix)
                ? Ok(input.slice(prefix.length()), prefix)
                : Error(input, "expected `%s`".formatted(prefix));
    }
}
