package io.github.ffortier.jasm.binary;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;

@RecordBuilder
public record Module(
        List<FuncType> types,
        List<Func> funcs,
        List<Table> tables,
        List<Memory> memories,
        List<Global> globals,
        List<Element> elements,
        List<Data> data,
        Start start,
        List<Import> imports,
        List<Export> exports
) {

}
