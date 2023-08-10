package jasm.basic.parser;

public sealed interface Instruction permits Instruction.Print, Instruction.Goto {
    record Print(Value value) implements Instruction {
    }

    record Goto(int label) implements Instruction {
    }
}
