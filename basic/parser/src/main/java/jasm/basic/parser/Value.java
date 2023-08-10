package jasm.basic.parser;

public sealed interface Value permits Value.StringLiteral, Value.NumberLiteral, Value.FunctionCall, Value.Variable {
    record StringLiteral(String str) implements Value {
    }

    record NumberLiteral() implements Value {
    }

    record FunctionCall() implements Value {
    }

    record Variable() implements Value {
    }
}
