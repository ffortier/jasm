package io.github.ffortier.jasm.binary;

import io.github.ffortier.jasm.binary.Instruction;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public record Expr(List<Instruction> instructions) {
    public static Expr read(ByteBuffer bb) {
        final var instructions = new ArrayList<Instruction>();

        var instruction = Instruction.read(bb);

        while (!(instruction instanceof Instruction.End)) {
            instructions.add(instruction);
            instruction = Instruction.read(bb);
        }

        return new Expr(instructions);
    }
}
