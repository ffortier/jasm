package jasm.basic.parser;

import java.util.List;

public record Tuple<L, R>(L left, R right) {

    public static <L, R> Tuple<L, R> of(L left, R right) {
        return new Tuple<L, R>(left, right);
    }

    public List<Object> toList() {
        return List.of(left, right);
    }
}
