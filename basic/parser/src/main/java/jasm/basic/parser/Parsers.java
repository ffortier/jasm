package jasm.basic.parser;

import static jasm.basic.parser.Result.error;
import static jasm.basic.parser.Result.ok;

import java.util.List;
import java.util.function.Predicate;

public final class Parsers {
    private Parsers() {
    }

    public static Parser<String> prefix(String prefix) {
        return input -> input.text().startsWith(prefix)
                ? ok(input.slice(prefix.length()), prefix)
                : error(input, "expected `%s`".formatted(prefix));
    }

    public static Parser<Character> match(Predicate<Character> predicate) {
        return input -> input.text().length() > 0 && predicate.test(input.text().charAt(0))
                ? ok(input.slice(1), input.text().charAt(0))
                : error(input, "expected char to match predicate");
    }

    public static Parser<Integer> integer() {
        return match(Character::isDigit)
                .many()
                .check(chars -> !chars.isEmpty(), chars -> "expected non-empty string")
                .map(Parsers::joined)
                .map(str -> Integer.parseInt(str));
    }

    public static Parser<String> whitespaces() {
        return match(Character::isWhitespace)
                .many()
                .check(chars -> !chars.isEmpty(), chars -> "expected non-empty string")
                .map(Parsers::joined);
    }

    @SafeVarargs
    public static <T> Parser<T> oneOf(Parser<? extends T>... parsers) {
        return input -> {
            for (final var parser : parsers) {
                if (parser.run(input) instanceof Result.Ok<? extends T> ok) {
                    return Result.ok(ok.input(), ok.value());
                }
            }

            return Result.error(input, "could not find any parser to match input");
        };
    }

    private static String joined(List<Character> chars) {
        final var sb = new StringBuilder(chars.size());
        chars.forEach(sb::append);
        return sb.toString();
    }
}
