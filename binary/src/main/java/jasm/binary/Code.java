package jasm.binary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jasm.io.BinaryReader;
import jasm.instruction.Instruction;

public sealed interface Code permits Code.Func, Code.Local, Code.Expression {
    static Code construct(BinaryReader binaryReader) throws IOException {
        return binaryReader.slice(Func::from);
    }

    record Func(List<Local> locals, Expression expr) implements Code {
        static Func from(BinaryReader binaryReader) throws IOException {
            final var locals = binaryReader.vec(Local::from);
            final var expr = Expression.from(binaryReader);

            return new Func(locals, expr);
        }
    }

    record Local(int count, Type valueType) implements Code {
        static Local from(BinaryReader binaryReader) throws IOException {
            final var count = binaryReader.u32(true);
            final var valueType = Type.construct(binaryReader);

            return new Local(count, valueType);
        }
    }

    record Expression(List<Instruction> instructions) implements Code {
        public static Expression from(BinaryReader binaryReader) throws IOException {
            final var instructions = new ArrayList<Instruction>();

            var instr = Instruction.next(binaryReader);

            while (instr.opCode() != Instruction.OpCode.END) {
                instructions.add(instr);
                instr = Instruction.next(binaryReader);
            }

            return new Expression(instructions);
        }
    }
}
