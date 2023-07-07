package jasm.binary;

import java.io.IOException;
import java.util.List;

import jasm.io.BinaryReader;

public sealed interface Section
        permits Section.CustomSection, Section.TypeSection, Section.ImportSection, Section.FunctionSection,
        Section.TableSection, Section.MemorySection,
        Section.GlobalSection, Section.ExportSection, Section.StartSection, Section.ElementSection, Section.CodeSection,
        Section.DataSection,
        Section.ExtraSection {
    List<Module.SectionConstructor<?>> CONSTRUCTORS = List.of(
            CustomSection::from,
            TypeSection::from,
            ImportSection::from,
            FunctionSection::from,
            TableSection::from,
            MemorySection::from,
            GlobalSection::from,
            ExportSection::from,
            StartSection::from,
            ElementSection::from,
            CodeSection::from,
            DataSection::from);

    static Section construct(BinaryReader reader) throws IOException {
        final var sectionType = reader.u32(true);
        final var constructor = sectionType < Section.CONSTRUCTORS.size()
                ? CONSTRUCTORS.get(sectionType)
                : (Module.SectionConstructor<?>) ExtraSection::from;

        return reader.slice(constructor::from);
    }

    default String sectionType() {
        return getClass().getSimpleName();
    }

    record CustomSection(String name, byte[] payload) implements Section {
        static CustomSection from(BinaryReader binaryReader) throws IOException {
            final var name = binaryReader.name();
            final var payload = binaryReader.readToEnd();

            return new CustomSection(name, payload);
        }
    }

    record TypeSection(List<Type> types) implements Section {
        static TypeSection from(BinaryReader binaryReader) throws IOException {
            final var types = binaryReader.vec(Type::construct);

            return new TypeSection(types);
        }
    }

    record ImportSection(List<Import> imports) implements Section {
        static ImportSection from(BinaryReader binaryReader) throws IOException {
            final var imports = binaryReader.vec(Import::from);

            return new ImportSection(imports);
        }
    }

    record FunctionSection(int[] typeIndices) implements Section {
        static FunctionSection from(BinaryReader binaryReader) throws IOException {
            final var typeIndices = binaryReader.vec(BinaryReader::u32).stream().mapToInt(i -> i).toArray();

            return new FunctionSection(typeIndices);
        }
    }

    record TableSection() implements Section {
        static TableSection from(BinaryReader binaryReader) throws IOException {
            throw new UnsupportedOperationException(
                    "not implemented yet: %s".formatted(TableSection.class.getSimpleName()));
        }
    }

    record MemorySection() implements Section {
        static MemorySection from(BinaryReader binaryReader) throws IOException {
            throw new UnsupportedOperationException(
                    "not implemented yet: %s".formatted(MemorySection.class.getSimpleName()));
        }
    }

    record GlobalSection() implements Section {
        static GlobalSection from(BinaryReader binaryReader) throws IOException {
            throw new UnsupportedOperationException(
                    "not implemented yet: %s".formatted(GlobalSection.class.getSimpleName()));
        }
    }

    record ExportSection(List<Export> exports) implements Section {
        static ExportSection from(BinaryReader binaryReader) throws IOException {
            final var exports = binaryReader.vec(Export::from);

            return new ExportSection(exports);
        }
    }

    record StartSection(int functionIndex) implements Section {
        static StartSection from(BinaryReader binaryReader) throws IOException {
            final var functionIndex = binaryReader.u32(true);

            return new StartSection(functionIndex);
        }
    }

    record ElementSection() implements Section {
        static ElementSection from(BinaryReader binaryReader) throws IOException {
            throw new UnsupportedOperationException(
                    "not implemented yet: %s".formatted(ElementSection.class.getSimpleName()));
        }
    }

    record CodeSection(List<Code> code) implements Section {
        static CodeSection from(BinaryReader binaryReader) throws IOException {
            final var code = binaryReader.vec(Code::construct);

            return new CodeSection(code);
        }
    }

    record DataSection(List<DataSegment> data) implements Section {
        static DataSection from(BinaryReader binaryReader) throws IOException {
            final var data = binaryReader.vec(DataSegment::construct);

            return new DataSection(data);
        }
    }

    record ExtraSection(byte[] payload) implements Section {
        static ExtraSection from(BinaryReader binaryReader) throws IOException {
            return new ExtraSection(binaryReader.readToEnd());
        }
    }

}
