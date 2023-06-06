import argparse
import os
import re
from pandas import read_csv, isna, DataFrame
from dataclasses import dataclass
from io import StringIO

instructions_df: DataFrame
signatures_df: DataFrame


@dataclass
class JavaClassMember:
    name: str
    type: str
    optional: bool = False
    final: bool = True

    def __str__(self) -> str:
        return f"private {'final' if self.final else ''} {self.type} {self.name};"

    def getter(self) -> str:
        t = f"Optional<{self.type}>" if self.optional else self.type
        r = f"Optional.ofNullable({self.name})" if self.optional else self.name
        return f"public {t} {self.name}() {{ return {r}; }}"


@dataclass
class JavaEnumVariant:
    name: str
    params: list[str]

    def __str__(self) -> str:
        joined_params = f", ".join(self.params)
        joined_params = f"({joined_params})" if len(
            joined_params) > 0 else joined_params
        return f"{self.name}{joined_params},"


@dataclass
class JavaClassConstructor:
    name: str
    params: list[(str, str)]
    body: list[str]
    modifiers: list[str] | None = None

    def __str__(self) -> str:
        joined_modifiers = " ".join(
            self.modifiers) + " " if self.modifiers is not None else ""
        joined_params = ", ".join([f"{p[1]} {p[0]}" for p in self.params])
        return "\n".join([
            f"{joined_modifiers}{self.name}({joined_params}) {{",
        ] + self.body + [
            f"}}"
        ])


@dataclass
class JavaEnum:
    simple_name: str
    variants: list[JavaEnumVariant]
    members: list[JavaClassMember]

    def __str__(self) -> str:
        constructor = JavaClassConstructor(
            name=self.simple_name,
            params=[(m.name, m.type) for m in self.members if m.final],
            body=[
                f"this.{m.name} = {m.name};" for m in self.members if m.final],
        )

        return "\n".join([
            f"enum {self.simple_name} {{",
        ] + [str(v) for v in self.variants] + [
            f";",
        ] + [str(m) for m in self.members] + [
            f"{constructor}",
        ] + [m.getter() for m in self.members] + [
            f"}}"
        ])


@dataclass
class NotImplemented:
    classname: str
    expr_name: str

    def __str__(self) -> str:
        return "\n".join([
            f"record {self.expr_name}() implements {self.classname} {{",
            f"public static {self.expr_name} from(BinaryReader code) throws IOException {{ throw new UnsupportedOperationException(\"not implemented: {self.expr_name}\"); }}",
            f"@Override public OpCode opCode() {{ return OpCode.{pc_to_upper(self.expr_name)}; }}",
            f"@Override public ProgramState apply(ProgramState ps) {{ throw new UnsupportedOperationException(\"not implemented\"); }}",
            f"}}",
        ])


def pc_to_upper(pc: str) -> list[str]:
    return "".join([f"_{ch}" if ch.isupper() else ch for ch in pc]).upper().lstrip("_")


def to_java_str(val: str) -> str:
    return f"\"{val}\""


def none_if_underscore(val: str):
    return "NONE" if val == "_" else val


# https://webassembly.github.io/spec/core/syntax/instructions.html#numeric-instructions
CONSTANTS = ["const"]
INT_UNARY_OP = ["clz", "ctz", "popcnt"]
INT_BINARY_OP = ["add", "sub", "mul", "div_u", "div_s", "rem_u",
                 "rem_s", "and", "or", "xor", "shl", "shr_u", "shr_s", "rotl", "rotr"]
FLOAT_UNARY_OP = ["abs", "neg", "sqrt", "ceil", "floor", "trunc", "nearest"]
FLOAT_BINARY_OP = ["add", "sub", "mul", "div", "min", "max", "copysign"]
INT_TEST_OP = ["eqz"]
INT_COMPARISON_OP = ["eq", "ne", "lt_s", "lt_u",
                     "gt_s", "gt_u", "le_u", "le_s", "ge_s", "ge_u"]
FLOAT_COMPARISON_OP = ["eq", "ne", "lt", "gt", "le", "ge"]


JAVA_TYPE_MAPPING = {
    "i32": "Int",
    "i32": "Int",
    "u32": "Int",
    "u32": "Int",
    "s32": "Int",
    "s32": "Int",
    "i64": "Long",
    "i64": "Long",
    "u64": "Long",
    "u64": "Long",
    "s64": "Long",
    "s64": "Long",
    "f32": "Float",
    "f64": "Double",
}


@dataclass
class Nop:
    classname: str
    expr_name: str

    def __str__(self) -> str:
        return "\n".join([
            f"record {self.expr_name}() implements {self.classname} {{",
            f"public static {self.expr_name} from(BinaryReader code) throws IOException {{ return new {self.expr_name}(); }}",
            f"@Override public OpCode opCode() {{ return OpCode.{pc_to_upper(self.expr_name)}; }}",
            f"@Override public ProgramState apply(ProgramState ps) {{ return ps; }}",
            f"}}",
        ])


@dataclass
class CallFunction:
    classname: str
    expr_name: str

    def __str__(self) -> str:
        return "\n".join([
            f"record {self.expr_name}(int functionIndex) implements {self.classname} {{",
            f"public static {self.expr_name} from(BinaryReader code) throws IOException {{ return new {self.expr_name}(code.u32().intValue()); }}",
            f"@Override public OpCode opCode() {{ return OpCode.{pc_to_upper(self.expr_name)}; }}",
            f"@Override public ProgramState apply(ProgramState ps) {{ throw new UnsupportedOperationException(\"not implemented\"); }}",
            f"}}",
        ])


@dataclass
class NumericConstant:
    classname: str
    expr_name: str
    num_type: str
    num_size: int

    def __str__(self) -> str:
        java_num_type = JAVA_TYPE_MAPPING[f'{self.num_type}{self.num_size}']
        java_num_primitive = java_num_type.lower()

        return "\n".join([
            f"record {self.expr_name}({java_num_primitive} value) implements {self.classname} {{",
            f"public static {self.expr_name} from(BinaryReader code) throws IOException {{ return new {self.expr_name}(code.{self.num_type}{self.num_size}()); }}",
            f"@Override public OpCode opCode() {{ return OpCode.{pc_to_upper(self.expr_name)}; }}",
            f"@Override public ProgramState apply(ProgramState ps) {{ throw new UnsupportedOperationException(\"not implemented\"); }}",
            f"}}",
        ])


def make_numeric_instruction(classname: str, expr_name: str, num_type: str, num_size: str, op_type: str):
    if op_type in CONSTANTS:
        return NumericConstant(classname, expr_name, num_type, int(num_size))

    return NotImplemented(classname, expr_name)


def make_instruction_rec(classname: str, expr_name: str, wat_name: str) -> any:
    numeric_expr = re.match(
        r"^(?P<num_type>[fi])(?P<num_size>32|64)\.(?P<op_type>\w+)", wat_name)

    if numeric_expr is not None:
        return make_numeric_instruction(classname, expr_name, **numeric_expr.groupdict())

    match wat_name:
        case "nop" | "nop_for_testing" | "end": return Nop(classname, expr_name)
        case "call": return CallFunction(classname, expr_name)
        case _: return NotImplemented(classname, expr_name)


def main(args: dict[str, str]):
    global instructions_df
    global signatures_df

    with open(args['input']) as input_file:
        instructions_csv, signatures_csv = input_file.read().split("\n---\n")

    instructions_df = read_csv(StringIO(instructions_csv), header=None, names=[
        "kExprName", "binary", "sig32", "watName", "sig64"])
    signatures_df = read_csv(StringIO(signatures_csv), header=None, names=[
        "signature", "1", "2", "3", "4", "5"])

    signatures_df["params"] = signatures_df[["1", "2", "3", "4", "5"]] \
        .apply(lambda row: [v for v in row if isna(v) == False], axis=1)

    signatures_df = signatures_df.drop(["1", "2", "3", "4", "5"], axis=1)

    opCode = JavaEnum(
        simple_name="OpCode",
        variants=[
            JavaEnumVariant(
                name=pc_to_upper(instructions_df["kExprName"][i]),
                params=[
                    to_java_str(instructions_df["kExprName"][i].strip()),
                    instructions_df["binary"][i].strip(),
                    f"Signature.{none_if_underscore(str(instructions_df['sig32'][i]).strip().upper())}",
                    instructions_df["watName"][i].strip(),
                    "null" if isna(
                        instructions_df['sig64'][i]) else f"Signature.{str(instructions_df['sig64'][i]).strip().upper()}"
                ],
            ) for i in instructions_df.index
        ],
        members=[
            JavaClassMember("kExprName", "String"),
            JavaClassMember("binary", "int"),
            JavaClassMember("sig32", "Signature"),
            JavaClassMember("watName", "String"),
            JavaClassMember("sig64", "Signature", optional=True),
        ],
    )

    sig = JavaEnum(
        simple_name="Signature",
        variants=[
            JavaEnumVariant(
                name=signatures_df["signature"][i].strip().upper(),
                params=[
                    f"List.of({', '.join([to_java_str(p) for p in signatures_df['params'][i]])})"
                ]
            ) for i in signatures_df.index
        ] + [
            JavaEnumVariant(
                name="NONE",
                params=["List.of()"],
            )
        ],
        members=[
            JavaClassMember("params", "List<String>")
        ]
    )

    out_path = os.path.join(os.getenv("BUILD_WORKING_DIRECTORY"), args['output']) if os.getenv(
        "BUILD_WORKING_DIRECTORY") is not None else args['output']

    with open(out_path, mode="w") as output_file:
        output_file.write("\n".join([
            "/**",
            " * AUTOGENERATED",
            " * bazel run //parsers/binary:instructions_codegen",
            " */",
            f"package {args['java_package']};",
            f"",
            f"import java.io.IOException;"
            f"import java.util.List;",
            f"import java.util.Optional;",
            f"import java.util.function.UnaryOperator;",
            f"",
            f"public interface {args['classname']} extends UnaryOperator<ProgramState> {{",
            f"static {args['classname']} next(BinaryReader code) throws IOException {{",
            f"    int binary = code.u8();",
            f"",
            f"    if (binary >= 0xfb) {{",
            f"        binary = (binary << 8) + code.u8();",
            f"    }}",
            f"",
            f"    return switch (binary) {{"] + [f"case {instructions_df['binary'][i].strip()} -> {instructions_df['kExprName'][i].strip()}.from(code);" for i in instructions_df.index] + [
            f"        default -> throw new IllegalStateException(\"Unexpected opcode %04x\".formatted(binary));",
            f"    }};",
            f"}}",
            f"OpCode opCode();",
        ]+[
            str(make_instruction_rec(args['classname'], instructions_df['kExprName'][i].strip(), instructions_df['watName'][i].strip().strip("\""))) for i in instructions_df.index
        ]+[
            str(opCode),
            f"",
            str(sig),
            f"}}",
        ]))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()

    parser.add_argument("-i", "--input")
    parser.add_argument("-o", "--output")
    parser.add_argument("-c", "--classname")
    parser.add_argument("-p", "--java_package")

    args = parser.parse_args()

    main(vars(args))
