package jasm.binary;

import java.io.IOException;

import jasm.binary.Code.Expression;
import jasm.instruction.BinaryReader;
import jasm.instruction.Instruction;

public interface DataSegment {
    static DataSegment construct(BinaryReader binaryReader) throws IOException {
        final var type = binaryReader.u32().intValueExact();

        return switch (type) {
            case 0 -> Active.from(0, binaryReader);
            case 1 -> Passive.from(binaryReader);
            case 3 -> Active.from(binaryReader.u32().intValueExact(), binaryReader);
            default -> throw new IllegalArgumentException("Unexpected data segment type 0x%02x".formatted(type));
        };
    }

    record Active(int memory, int offset, byte[] data) implements DataSegment {
        static Active from(int memory, BinaryReader binaryReader) throws IOException {
            final var expr = Expression.from(binaryReader);

            if (expr.instructions().size() != 1
                    || expr.instructions().get(0).opCode() != Instruction.OpCode.I32_CONST) {
                throw new IllegalStateException("expected i32 expression");
            }

            final var offset = ((Instruction.I32Const) expr.instructions().get(0)).value();
            final var data = binaryReader.bytes(binaryReader.u32().intValueExact());

            return new Active(memory, offset, data);
        }
    }

    record Passive(byte[] data) implements DataSegment {
        static Passive from(BinaryReader binaryReader) throws IOException {
            final var data = binaryReader.slice(BinaryReader::readToEnd);

            return new Passive(data);
        }
    }
}
