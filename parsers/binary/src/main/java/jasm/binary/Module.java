package jasm.binary;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jasm.common.Records;
import jasm.io.BinaryReader;
import jasm.runtime.Store;

public record Module(List<Section> sections) {
    private static final byte[] WASM_HEADER = { 0x00, 0x61, 0x73, 0x6d, 0x01, 0x00, 0x00, 0x00 };

    public static void main(String[] args) throws Exception {
        final var moduleFile = Path.of(args[0]);

        if (!moduleFile.toString().endsWith(".wasm")) {
            throw new UnsupportedOperationException("this file is not supported yet: %s".formatted(moduleFile));
        }

        try (final var in = Files.newInputStream(moduleFile)) {
            System.out.println(Records.toTextBlock(Module.from(in)));
        }
    }

    public static Module from(InputStream in) throws IOException {
        final var reader = new BinaryReader(in);
        readWasmHeader(reader);

        List<Section> sections = new ArrayList<>();

        while (!reader.eof()) {
            sections.add(Section.construct(reader));
        }

        return new Module(Collections.unmodifiableList(sections));
    }

    public void populateStore(Store store) {

    }

    private static void readWasmHeader(BinaryReader reader) throws IOException {
        byte[] buf = reader.bytes(WASM_HEADER.length);

        if (!Arrays.equals(WASM_HEADER, buf)) {
            throw new IOException("invalid wasm header");
        }
    }

    interface SectionConstructor<T extends Section> {
        T from(BinaryReader binaryReader) throws IOException;
    }

}
