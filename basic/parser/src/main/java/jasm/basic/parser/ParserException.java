package jasm.basic.parser;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParserException extends RuntimeException {
    public ParserException(Input input, String message) {
        super(("Parser error: \n%s\n%s^\n%s").formatted(input.origin(),
                IntStream.range(0, input.pos()).mapToObj(num -> " ").collect(Collectors.joining("")), message));
    }
}
