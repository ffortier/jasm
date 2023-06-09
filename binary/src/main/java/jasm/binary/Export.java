package jasm.binary;

import java.io.IOException;

import jasm.io.BinaryReader;

public sealed interface Export permits Export.MemoryExport, Export.GlobalExport, Export.TableExport, Export.FuncExport {

    static Export from(BinaryReader binaryReader) throws IOException {
        final var name = binaryReader.name();
        final var type = binaryReader.u32(true);

        return switch (type) {
            case 0x00 -> new FuncExport(name, binaryReader.u32(true));
            case 0x01 -> new TableExport(name, binaryReader.u32(true));
            case 0x02 -> new MemoryExport(name, binaryReader.u32(true));
            case 0x03 -> new GlobalExport(name, binaryReader.u32(true));
            default -> throw new IllegalArgumentException("unexpected import type 0x%02x".formatted(type));
        };
    }

    String name();

    record MemoryExport(String name, int x) implements Export {
    }

    record GlobalExport(String name, int x) implements Export {
    }

    record TableExport(String name, int x) implements Export {
    }

    record FuncExport(String name, int x) implements Export {
    }
}
