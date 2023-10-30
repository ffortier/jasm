package io.github.ffortier.jasm.binary;

public enum ValType {
    I32(0x7f),
    I64(0x7e),
    F32(0x7d),
    F64(0x7c),
    V128(0x7b),
    FUNC_REF(0x70),
    EXTERN_REF(0x6f),
    ;
    private final int id;

    ValType(int id) {
        this.id = id;
    }

    public static ValType read(int id) {
        for (final var valType : ValType.values()) {
            if (valType.id() == id) {
                return valType;
            }
        }

        throw new IllegalArgumentException("Unknown ValType id %02x".formatted(id));
    }

    public int id() {
        return id;
    }
}
