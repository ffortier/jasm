package jasm.basic.parser;

import static jasm.basic.parser.Parsers.prefix;

public class Main {
    public static void main(String[] args) {
        final var input = new Input("hello world");
        final var parser = prefix("hello")
                .foldLeft(prefix(" ").many())
                .join(prefix("world"));

        final var res = parser.run(input);

        System.out.println(res.unwrap());
    }
}
