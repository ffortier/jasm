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
        final var sectionData = ByteBuffer.wrap(in.readNBytes(size));

        final var section = switch (sectionId) {
            case 0 -> CustomSection.read(sectionData);
            case 1 -> TypeSection.read(sectionData);
            case 2 -> ImportSection.read(sectionData);
            case 3 -> FunctionSection.read(sectionData);
            case 4 -> TableSection.read(sectionData);
            case 5 -> MemorySection.read(sectionData);
            case 6 -> GlobalSection.read(sectionData);
            case 7 -> ExportSection.read(sectionData);
            case 8 -> StartSection.read(sectionData);
            case 9 -> ElementSection.read(sectionData);
            case 10 -> CodeSection.read(sectionData);
            case 11 -> DataSection.read(sectionData);
            case 12 -> DataCountSection.read(sectionData);
            default -> throw new UnsupportedOperationException("Unknown section id %d".formatted(sectionId));
        };

        return Optional.of(section);
    }

    record CustomSection() implements Section {
        public static CustomSection read(ByteBuffer sectionData) {
            return new CustomSection();
        }
    }

    record TypeSection(List<FuncType> types) implements Section {
        public static TypeSection read(ByteBuffer sectionData) {
            final var len = leb128(sectionData);
            final var types = new ArrayList<FuncType>();

            for (int i = 0; i < len; i++) {
                final var typeId = sectionData.get();

                if (typeId != 0x60) {
                    throw new UnsupportedOperationException("Unsupported type with id %02x".formatted(typeId));
                }

                types.add(FuncType.read(sectionData));
            }

            return new TypeSection(unmodifiableList(types));
        }
    }

    record ImportSection(List<Import> imports) implements Section {
        public static ImportSection read(ByteBuffer sectionData) {
            final var len = leb128(sectionData);
            final var imports = new ArrayList<Import>();

            for (int i = 0; i < len; i++) {
                imports.add(Import.read(sectionData));
            }

            return new ImportSection(unmodifiableList(imports));
        }
    }

    record FunctionSection() implements Section {
        public static FunctionSection read(ByteBuffer sectionData) {
            return new FunctionSection();
        }
    }

    record TableSection() implements Section {
        public static TableSection read(ByteBuffer sectionData) {
            return new TableSection();
        }
    }

    record MemorySection() implements Section {
        public static MemorySection read(ByteBuffer sectionData) {
            return new MemorySection();
        }
    }

    record GlobalSection() implements Section {
        public static GlobalSection read(ByteBuffer sectionData) {
            return new GlobalSection();
        }
    }

    record ExportSection() implements Section {
        public static ExportSection read(ByteBuffer sectionData) {
            return new ExportSection();
        }
    }

    record StartSection() implements Section {
        public static StartSection read(ByteBuffer sectionData) {
            return new StartSection();
        }
    }

    record ElementSection() implements Section {
        public static ElementSection read(ByteBuffer sectionData) {
            return new ElementSection();
        }
    }

    record CodeSection() implements Section {
        public static CodeSection read(ByteBuffer sectionData) {
            return new CodeSection();
        }
    }

    record DataSection() implements Section {
        public static DataSection read(ByteBuffer sectionData) {
            return new DataSection();
        }
    }

    record DataCountSection() implements Section {
        public static DataCountSection read(ByteBuffer sectionData) {
            return new DataCountSection();
        }
    }
}
