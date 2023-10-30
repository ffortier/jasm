package io.github.ffortier.jasm.binary;

import static io.github.ffortier.jasm.binary.BinaryReader.leb128;
import static java.util.Collections.unmodifiableList;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public sealed interface Section permits
        Section.CustomSection,
        Section.TypeSection,
        Section.ImportSection,
        Section.FunctionSection,
        Section.TableSection,
        Section.MemorySection,
        Section.GlobalSection,
        Section.ExportSection,
        Section.StartSection,
        Section.ElementSection,
        Section.CodeSection,
        Section.DataSection,
        Section.DataCountSection {
    static Optional<Section> read(BufferedInputStream in) throws IOException {
        var sectionId = in.read();

        if (sectionId < 0) {
            return Optional.empty();
        }

        final var size = leb128(in);
        final var bb = ByteBuffer.wrap(in.readNBytes(size));

        final var section = switch (sectionId) {
            case 0 -> CustomSection.read(bb);
            case 1 -> TypeSection.read(bb);
            case 2 -> ImportSection.read(bb);
            case 3 -> FunctionSection.read(bb);
            case 4 -> TableSection.read(bb);
            case 5 -> MemorySection.read(bb);
            case 6 -> GlobalSection.read(bb);
            case 7 -> ExportSection.read(bb);
            case 8 -> StartSection.read(bb);
            case 9 -> ElementSection.read(bb);
            case 10 -> CodeSection.read(bb);
            case 11 -> DataSection.read(bb);
            case 12 -> DataCountSection.read(bb);
            default -> throw new UnsupportedOperationException("Unknown section id %d".formatted(sectionId));
        };

        return Optional.of(section);
    }

    private static <T extends Section> T notImplemented(Class<T> sectionType) {
        throw new UnsupportedOperationException("Not implemented %s".formatted(sectionType.getName()));
    }

    record CustomSection() implements Section {
        public static CustomSection read(ByteBuffer bb) {
            return notImplemented(CustomSection.class);
        }
    }

    record TypeSection(List<FuncType> types) implements Section {
        public static TypeSection read(ByteBuffer bb) {
            final var len = leb128(bb);
            final var types = new ArrayList<FuncType>();

            for (int i = 0; i < len; i++) {
                final var typeId = bb.get();

                if (typeId != 0x60) {
                    throw new UnsupportedOperationException("Unsupported type with id %02x".formatted(typeId));
                }

                types.add(FuncType.read(bb));
            }

            return new TypeSection(unmodifiableList(types));
        }
    }

    record ImportSection(List<Import> imports) implements Section {
        public static ImportSection read(ByteBuffer bb) {
            final var len = leb128(bb);
            final var imports = new ArrayList<Import>();

            for (int i = 0; i < len; i++) {
                imports.add(Import.read(bb));
            }

            return new ImportSection(unmodifiableList(imports));
        }
    }

    record FunctionSection(List<Index.TypeIdx> typeIndices) implements Section {
        public static FunctionSection read(ByteBuffer bb) {
            final var typeIndices = new ArrayList<Index.TypeIdx>();
            int len = leb128(bb);

            for (int i = 0; i < len; i++) {
                typeIndices.add(new Index.TypeIdx(leb128(bb)));
            }

            return new FunctionSection(typeIndices);
        }
    }

    record TableSection() implements Section {
        public static TableSection read(ByteBuffer bb) {
            return notImplemented(TableSection.class);
        }
    }

    record MemorySection() implements Section {
        public static MemorySection read(ByteBuffer bb) {
            return notImplemented(MemorySection.class);
        }
    }

    record GlobalSection() implements Section {
        public static GlobalSection read(ByteBuffer bb) {
            return notImplemented(GlobalSection.class);
        }
    }

    record ExportSection(List<Export> exports) implements Section {
        public static ExportSection read(ByteBuffer bb) {
            final var len = leb128(bb);
            final var exports = new ArrayList<Export>();

            for (int i = 0; i < len; i++) {
                exports.add(Export.read(bb));
            }

            return new ExportSection(unmodifiableList(exports));

        }
    }

    record StartSection() implements Section {
        public static StartSection read(ByteBuffer bb) {
            return notImplemented(StartSection.class);
        }
    }

    record ElementSection() implements Section {
        public static ElementSection read(ByteBuffer bb) {
            return notImplemented(ElementSection.class);
        }
    }

    record CodeSection() implements Section {
        public static CodeSection read(ByteBuffer bb) {
            return notImplemented(CodeSection.class);
        }
    }

    record DataSection() implements Section {
        public static DataSection read(ByteBuffer bb) {
            return notImplemented(DataSection.class);
        }
    }

    record DataCountSection() implements Section {
        public static DataCountSection read(ByteBuffer bb) {
            return notImplemented(DataCountSection.class);
        }
    }
}
