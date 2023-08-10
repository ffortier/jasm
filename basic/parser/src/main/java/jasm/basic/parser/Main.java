package jasm.basic.parser;

import static jasm.basic.parser.BasicParsers.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        final var parser = block().many();

        for (final var line : Files.readAllLines(Path.of(args[0]))) {
            final var res = parser.run(new Input(line));

            System.out.println(res.unwrap());
        }

    }
}
