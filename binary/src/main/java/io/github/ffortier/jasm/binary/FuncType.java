package io.github.ffortier.jasm.binary;

import static io.github.ffortier.jasm.binary.BinaryReader.leb128;
import static java.util.Collections.unmodifiableList;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public record FuncType(List<ValType> args, List<ValType> rets) {
    public static FuncType read(ByteBuffer data) {
        final var argLen = leb128(data);
        final var args = new ArrayList<ValType>();

        for (int i = 0; i < argLen; i++) {
            args.add(ValType.read(data.get()));
        }

        final var retLen = leb128(data);
        final var rets = new ArrayList<ValType>();

        for (int i = 0; i < retLen; i++) {
            rets.add(ValType.read(data.get()));
        }

        return new FuncType(unmodifiableList(args), unmodifiableList(rets));
    }
}
