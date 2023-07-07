package jasm.binary;

import java.io.IOException;
import java.math.BigInteger;

import jasm.io.BinaryReader;

public sealed interface Import permits Import.MemoryImport, Import.GlobalImport, Import.FuncImport, Import.TableImport {

    static Import from(BinaryReader binaryReader) throws IOException {
        final var module = binaryReader.name();
        final var name = binaryReader.name();
        final var type = binaryReader.u32().intValueExact();

        return switch (type) {
            case 0x00 -> new FuncImport(module, name, binaryReader.u32());
            // case 0x01 -> new TableImport(module, name,
            // binaryReader.u32().intValueExact());
            case 0x02 -> new MemoryImport(module, name, Limits.from(binaryReader));
            // case 0x03 -> new GlobalImport(module, name,
            // binaryReader.u32().intValueExact());
            default -> throw new IllegalArgumentException("unexpected import type 0x%02x".formatted(type));
        };
    }

    String module();

    String name();

    record Limits(BigInteger min, BigInteger max) {
        public static Limits from(BinaryReader reader) throws IOException {
            final var hasMax = !reader.u32().equals(BigInteger.ZERO);
            final var min = reader.u32();
            final var max = hasMax ? reader.u32() : null;

            return new Limits(min, max);
        }
    }

    record MemoryImport(String module, String name, Limits limits) implements Import {
    }

    record GlobalImport(String module, String name, int gt) implements Import {
    }

    record TableImport(String module, String name, int tt) implements Import {
    }

    record FuncImport(String module, String name, BigInteger x) implements Import {
    }
}
