from typing import Any
import argparse
import os
import re
from pandas import read_csv, isna, DataFrame
from dataclasses import dataclass
from io import StringIO
import instruction.java.ast

# instruction.java.ast.Class("test")


@dataclass
class Args:
    input: str
    output: str
    classname: str
    java_package: str


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
    params: list[tuple[str, str]]
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


def pc_to_upper(pc: str) -> str:
    return "".join([f"_{ch}" if ch.isupper() else ch for ch in pc]).upper().lstrip("_")


def to_java_str(val: str) -> str:
    return f"\"{val}\""


def none_if_underscore(val: str) -> str:
    return "NONE" if val == "_" else val


CONSTANTS = ["const"]


JAVA_PRIMITIVE_MAPPING = {
    "i32": "int",
    "i32": "int",
    "u32": "int",
    "u32": "int",
    "s32": "int",
    "s32": "int",
    "i64": "long",
    "i64": "long",
    "u64": "long",
    "u64": "long",
    "s64": "long",
    "s64": "long",
    "f32": "float",
    "f64": "double",
}


@dataclass
class DefaultOpCode:
    classname: str
    expr_name: str

    def __str__(self) -> str:
        return "\n".join([
            f"record {self.expr_name}() implements {self.classname} {{",
            f"public static {self.expr_name} from(BinaryReader code) throws IOException {{ return new {self.expr_name}(); }}",
            f"@Override public OpCode opCode() {{ return OpCode.{pc_to_upper(self.expr_name)}; }}",
            f"@Override public void accept(Visitor visitor) {{ visitor.visit(this); }}",
            f"}}",
        ])


@dataclass
class CallFunction:
    classname: str
    expr_name: str

    def __str__(self) -> str:
        return "\n".join([
            f"record {self.expr_name}(int x) implements {self.classname} {{",
            f"public static {self.expr_name} from(BinaryReader code) throws IOException {{ return new {self.expr_name}(code.u32(true)); }}",
            f"@Override public OpCode opCode() {{ return OpCode.{pc_to_upper(self.expr_name)}; }}",
            f"@Override public void accept(Visitor visitor) {{ visitor.visit(this); }}",
            f"}}",
        ])


@dataclass
class NumericConstant:
    classname: str
    expr_name: str
    num_type: str
    num_size: int

    def __str__(self) -> str:
        java_num_primitive = JAVA_PRIMITIVE_MAPPING[f'{self.num_type}{self.num_size}']

        return "\n".join([
            f"record {self.expr_name}({java_num_primitive} value) implements {self.classname} {{",
            f"public static {self.expr_name} from(BinaryReader code) throws IOException {{ return new {self.expr_name}(code.{self.num_type}{self.num_size}()); }}",
            f"@Override public OpCode opCode() {{ return OpCode.{pc_to_upper(self.expr_name)}; }}",
            f"@Override public void accept(Visitor visitor) {{ visitor.visit(this); }}",
            f"}}",
        ])


def make_numeric_instruction(classname: str, expr_name: str, num_type: str, num_size: str, op_type: str) -> Any:
    if op_type in CONSTANTS:
        return NumericConstant(classname, expr_name, num_type, int(num_size))

    return DefaultOpCode(classname, expr_name)


def make_instruction_rec(classname: str, expr_name: str, wat_name: str) -> Any:
    numeric_expr = re.match(
        r"^(?P<num_type>[fi])(?P<num_size>32|64)\.(?P<op_type>\w+)", wat_name)

    if numeric_expr is not None:
        return make_numeric_instruction(classname, expr_name, **numeric_expr.groupdict())

    match wat_name:
        case "call": return CallFunction(classname, expr_name)
        case _: return DefaultOpCode(classname, expr_name)


def parse_args() -> Args:
    parser = argparse.ArgumentParser(prog="instructions_codegen")

    parser.add_argument("-i", "--input", required=True)
    parser.add_argument("-o", "--output", required=True)
    parser.add_argument("-c", "--classname", required=True)
    parser.add_argument("-p", "--java_package", required=True)

    ns = parser.parse_args()

    return Args(
        ns.input,
        ns.output,
        ns.classname,
        ns.java_package,
    )


def parse_input(input: str) -> tuple[DataFrame, DataFrame]:
    with open(input) as f:
        instructions_csv, signatures_csv = f.read().split("\n---\n")

    instructions_df = read_csv(StringIO(instructions_csv), header=None, names=[
        "kExprName", "binary", "sig32", "watName", "sig64"])

    signatures_df = read_csv(StringIO(signatures_csv), header=None, names=[
        "signature", "1", "2", "3", "4", "5"])

    signatures_df["params"] = signatures_df[["1", "2", "3", "4", "5"]] \
        .apply(lambda row: [v for v in row if isna(v) == False], axis=1)

    signatures_df = signatures_df.drop(["1", "2", "3", "4", "5"], axis=1)

    return instructions_df, signatures_df


def generate_op_codes(instructions_df: DataFrame) -> JavaEnum:
    return JavaEnum(
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


def generate_class_file(args: Args, instructions_df: DataFrame, opCode: JavaEnum, sig: JavaEnum) -> str:
    return "\n".join([
        "/**",
        " * AUTOGENERATED",
        " * bazel run //parsers/binary:instructions_codegen",
        " */",
        f"package {args.java_package};",
        f"",
        f"import java.io.IOException;",
        f"import java.math.BigInteger;",
        f"import java.util.List;",
        f"import java.util.Optional;",
        f"import jasm.io.BinaryReader;",
        f"",
        f"public interface {args.classname} {{",
        f"static {args.classname} next(BinaryReader code) throws IOException {{",
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
        f"void accept(Visitor visitor);",
        f"interface Visitor {{"] + [
        f"void visit({instructions_df['kExprName'][i].strip()} instr);" for i in instructions_df.index
    ] + [
        f"}}",
    ] + [
        str(make_instruction_rec(args.classname, instructions_df['kExprName'][i].strip(), instructions_df['watName'][i].strip().strip("\""))) for i in instructions_df.index
    ] + [
        str(opCode),
        f"",
        str(sig),
        f"}}",
    ])


def generate_signatures(signatures_df: DataFrame) -> JavaEnum:
    return JavaEnum(
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


def main() -> None:
    args = parse_args()
    instructions_df, signatures_df = parse_input(args.input)

    opCodes = generate_op_codes(instructions_df)
    sig = generate_signatures(signatures_df)
    class_file = generate_class_file(args, instructions_df, opCodes, sig)

    build_work_dir = os.getenv("BUILD_WORKING_DIRECTORY")

    out_path = os.path.join(
        build_work_dir, args.output) if build_work_dir is not None else args.output

    with open(out_path, mode="w") as output_file:
        output_file.write(class_file)


if __name__ == "__main__":
    main()
