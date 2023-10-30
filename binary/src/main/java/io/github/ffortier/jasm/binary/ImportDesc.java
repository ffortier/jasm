package io.github.ffortier.jasm.binary;

public sealed interface ImportDesc permits ImportDesc.TypeIdx {
    record TypeIdx(int idx) implements ImportDesc {
    }

}
