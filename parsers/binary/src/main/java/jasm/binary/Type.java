package jasm.binary;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jasm.io.BinaryReader;

public sealed interface Type permits Type.I32Type, Type.FunctionType {
    static Type construct(BinaryReader binaryReader) throws IOException {
        final var index = binaryReader.u32().intValueExact();
        final var constructor = CONSTRUCTORS.get(index);

        if (constructor == null) {
            throw new IllegalArgumentException("Unknown type index 0x%02x".formatted(index));
        }

        return constructor.from(binaryReader);
    }

    interface TypeConstructor<T extends Type> {
        T from(BinaryReader binaryReader) throws IOException;
    }

    record I32Type() implements Type {
        public static I32Type from(BinaryReader reader) {
            return new I32Type();
        }
    }

    record FunctionType(List<Type> params, List<Type> results) implements Type {
        public static FunctionType from(BinaryReader reader) throws IOException {
            final var params = reader.vec(Type::construct);
            final var results = reader.vec(Type::construct);

            return new FunctionType(params, results);
        }
    }

    Map<Integer, TypeConstructor<?>> CONSTRUCTORS = Map.ofEntries(
            Map.entry(0x7F, I32Type::from),
            Map.entry(0x60, FunctionType::from));
}
