package jasm.basic.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static jasm.basic.parser.Parsers.*;

public final class BasicParsers {
    private BasicParsers() {
    }

    public static Parser<Tuple<Integer, List<Instruction>>> block() {
        return integer().foldLeft(whitespaces()).join(instructions());
    }

    public static Parser<List<Instruction>> instructions() {
        final var separator = whitespaces().optional().foldRight(prefix(":")).foldRight(whitespaces().optional());

        return instruction().join(separator.bind(wd -> instructions()).optional()).map(t -> {
            final var list = new ArrayList<>(t.right().orElse(Collections.emptyList()));
            list.add(0, t.left());
            return Collections.unmodifiableList(list);
        });
    }

    public static Parser<Instruction> instruction() {
        return oneOf(
                printInstruction(),
                gotoInstruction());
    }

    public static Parser<Instruction.Print> printInstruction() {
        return prefix("PRINT")
                .foldLeft(whitespaces())
                .foldRight(value())
                .map(value -> new Instruction.Print(value));
    }

    public static Parser<Instruction.Goto> gotoInstruction() {
        return prefix("GOTO")
                .foldLeft(whitespaces())
                .foldRight(integer())
                .map(label -> new Instruction.Goto(label));
    }

    public static Parser<Value> value() {
        return oneOf(
                stringLiteral(),
                numberLiteral(),
                functionCall(),
                variable());
    }

    public static Parser<Value.StringLiteral> stringLiteral() {
        return prefix("\"")
                .foldRight(match(ch -> ch != '"').many().map(BasicParsers::joined))
                .foldLeft(prefix("\""))
                .map(str -> new Value.StringLiteral(str));
    }

    public static Parser<Value.NumberLiteral> numberLiteral() {
        return input -> {
            throw new UnsupportedOperationException("not implemented yet");
        };
    }

    public static Parser<Value.FunctionCall> functionCall() {
        return input -> {
            throw new UnsupportedOperationException("not implemented yet");
        };
    }

    public static Parser<Value.Variable> variable() {
        return input -> {
            throw new UnsupportedOperationException("not implemented yet");
        };
    }

    private static String joined(List<Character> chars) {
        final var sb = new StringBuilder(chars.size());
        chars.forEach(sb::append);
        return sb.toString();
    }

}
