from dataclasses import dataclass
from typing import Union


@dataclass
class Field:
    name: str
    typeref: str
    modifiers: list[str]
    initial_value: str | None = None


@dataclass
class Parameter:
    name: str
    typeref: str


@dataclass
class Method:
    name: str
    return_typeref: str
    modifiers: list[str]
    body: list[str]
    params: list[Parameter]


@dataclass
class Class:
    name: str
    modifiers: list[str]
    members: list[Union['Field', 'Method', 'Class', 'Interface', 'Enum']]


@dataclass
class EnumVariant:
    name: str
    param_values: list[str]


@dataclass
class Enum:
    name: str
    variants: list[EnumVariant]
    members: list[Union['Field', 'Method', 'Class', 'Interface', 'Enum']]


@dataclass
class Interface:
    name: str
    members: list[Union['Field', 'Method', 'Class', 'Interface', 'Enum']]


@dataclass
class ClassFile:
    pkg: str
    imports: list[str]
    typedef: Class | Interface | Enum

    def __str__(self) -> str:
        return "\n".join([
            f"package {self.pkg};"
            f"",
        ] + [
            f"import {imp};" for imp in self.imports
        ] + [
            f"",
            str(self.typedef),
        ])
