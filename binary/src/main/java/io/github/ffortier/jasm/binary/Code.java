package io.github.ffortier.jasm.binary;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static io.github.ffortier.jasm.binary.BinaryReader.leb128;

public record Code(Code.Func func) {
    public static Code read(ByteBuffer bb) {
        final var size = leb128(bb);
        final var bytes = new byte[size];

        bb.get(bytes);

        final var func = Func.read(ByteBuffer.wrap(bytes));

        return new Code(func);
    }

    public record Func(List<Locals> locals, Expr e) {
        public static Func read(ByteBuffer bb) {
            final var len = leb128(bb);
            final var locals = new ArrayList<Locals>();

            for (int i = 0; i < len; i++) {
                locals.add(Locals.read(bb));
            }

            return new Func(locals, Expr.read(bb));
        }
    }

    public record Locals(int n, ValType t) {
        public static Locals read(ByteBuffer bb) {
            final var n = leb128(bb);
            final var t = ValType.get(bb.get());

            return new Locals(n, t);
        }
    }

    public record Expr(byte[] instr) {
        public static Expr read(ByteBuffer bb) {
            final var instr = new byte[bb.remaining()];
            bb.get(instr);
            return new Expr(instr);
        }
    }
}
