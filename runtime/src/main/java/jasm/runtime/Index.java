package jasm.runtime;

import java.math.BigInteger;

public sealed interface Index<T> extends Comparable<Index<T>> permits Index.FuncIndex {
    BigInteger x();

    default int compareTo(Index<T> o) {
        return x().compareTo(o.x());
    }

    record FuncIndex(BigInteger x) implements Index<FuncInstance> {
    }
}
