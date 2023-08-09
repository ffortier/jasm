package jasm.basic.parser;

import java.util.Objects;

public record Input(String origin, String text, int pos) {
    public Input {
        Objects.requireNonNull(text, "text");
    }

    public Input(String text) {
        this(text, text, 0);
    }

    public Input slice(int length) {
        return new Input(origin, text.substring(length), pos + length);
    }
}
