package io.github.ffortier.jasm.runtime;

import java.util.List;

public record Store(
        List<FuncInstance> funcs,
        List<TableInstance> tables,
        List<MemInstance> mems,
        List<GlobalInstance> globals,
        List<ElemInstance> elems,
        List<DataInstance> datas
) {

}