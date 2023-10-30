package io.github.ffortier.jasm.binary;

import static io.github.ffortier.jasm.binary.BinaryReader.leb128;
import static io.github.ffortier.jasm.binary.BinaryReader.name;

import java.nio.ByteBuffer;

public record Import(String mod, String nm, ImportDesc desc) {

    public static Import read(ByteBuffer sectionData) {
        final var mod = name(sectionData);
        final var nm = name(sectionData);
        final var type = sectionData.get();

        final var desc = switch (type) {
            case 0x00 -> new ImportDesc.TypeIdx(leb128(sectionData));
            default -> throw new UnsupportedOperationException("Unsupported import desc %02x".formatted(type));
        };

        return new Import(mod, nm, desc);
    }

}
