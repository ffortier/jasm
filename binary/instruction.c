#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <stddef.h>
#include <stdlib.h>
#include <stdbool.h>

#include "jasm/binary/wasm_opcodes.h"

#define FOREACH_JASM_OPCODE(V)  \
    FOREACH_CONTROL_OPCODE(V)   \
    FOREACH_MISC_OPCODE(V)      \
    FOREACH_SIMPLE_OPCODE(V)    \
    FOREACH_STORE_MEM_OPCODE(V) \
    FOREACH_LOAD_MEM_OPCODE(V)  \
    FOREACH_MISC_MEM_OPCODE(V)

void print_record_const(const char *variant, int hex, const char *java_type, const char *wasm_type)
{
    printf("record %s(%s value) implements Instruction {"
           "public static final int OPCODE = 0x%04x;"
           "public static %s read(ByteBuffer bb) { return new %s(%s(bb)); }"
           "}\n",
           variant, java_type, hex, variant, variant, wasm_type);
}

bool is_load_mem_instruction(const char *variant, size_t len)
{
    if (len > 7 && strncmp(variant + len - 7, "LoadMem", 7) == 0)
    {
        return true;
    }
    if (len > 9 && strncmp(variant + len - 9, "LoadMem", 7) == 0)
    {
        return true;
    }

    if (len > 10 && strncmp(variant + len - 10, "LoadMem", 7) == 0)
    {
        return true;
    }

    return false;
}

bool is_store_mem_instruction(const char *variant, size_t len)
{
    if (len > 8 && strncmp(variant + len - 8, "StoreMem", 8) == 0)
    {
        return true;
    }
    if (len > 9 && strncmp(variant + len - 9, "StoreMem", 8) == 0)
    {
        return true;
    }

    if (len > 10 && strncmp(variant + len - 10, "StoreMem", 8) == 0)
    {
        return true;
    }

    return false;
}

void print_record(const char *variant, int hex)
{
    size_t len = strlen(variant);

    if (strncmp(variant + len - 5, "Const", 5) == 0)
    {
        if (strncmp(variant, "I32", 3) == 0)
        {
            print_record_const(variant, hex, "int", "i32");
        }
        else if (strncmp(variant, "I64", 3) == 0)
        {
            print_record_const(variant, hex, "long", "i64");
        }
        else if (strncmp(variant, "F32", 3) == 0)
        {
            print_record_const(variant, hex, "float", "f32");
        }
        else if (strncmp(variant, "F64", 3) == 0)
        {
            print_record_const(variant, hex, "double", "f64");
        }
        else
        {
            fprintf(stderr, "Unknown variant %s 0x%04x", variant, hex);
            exit(1);
        }
    }
    else if (strcmp(variant, "CallFunction") == 0)
    {
        printf("record %s(int x) implements Instruction {"
               "public static final int OPCODE = 0x%04x;"
               "public static %s read(ByteBuffer bb) { return new %s(leb128(bb)); }"
               "}\n",
               variant, hex, variant, variant);
    }
    else if (strcmp(variant, "CallIndirect") == 0)
    {
        printf("record %s(int y, int x) implements Instruction {"
               "public static final int OPCODE = 0x%04x;"
               "public static %s read(ByteBuffer bb) { return new %s(leb128(bb),leb128(bb)); }"
               "}\n",
               variant, hex, variant, variant);
    }
    else if (is_load_mem_instruction(variant, len) || is_store_mem_instruction(variant, len))
    {
        printf("record %s(Memarg arg) implements Instruction {"
               "public static final int OPCODE = 0x%04x;"
               "public static %s read(ByteBuffer bb) { return new %s(Memarg.read(bb)); }"
               "}\n",
               variant, hex, variant, variant);
    }
    else
    {
        printf("record %s() implements Instruction {"
               "public static final int OPCODE = 0x%04x;"
               "public static %s read(ByteBuffer bb) { return new %s(); }"
               "}\n",
               variant, hex, variant, variant);
    }
}

void print_variant(const char *variant, int hex, bool *first)
{
    if (*first)
    {
        printf("Instruction.%s\n", variant);
        *first = false;
    }
    else
    {
        printf(", Instruction.%s\n", variant);
    }
}

int main(void)
{
    printf("package io.github.ffortier.jasm.binary;\n\n");
    printf("import static io.github.ffortier.jasm.binary.BinaryReader.*;\n");
    printf("import java.nio.ByteBuffer;\n\n");
    printf("public sealed interface Instruction permits\n");

    bool first = true;

#define V(variant, hex, ...) \
    print_variant(#variant, hex, &first);
    FOREACH_JASM_OPCODE(V)
#undef V

    printf("{\n");

#define V(variant, hex, ...) \
    print_record(#variant, hex);
    FOREACH_JASM_OPCODE(V)
#undef V

    printf("public static Instruction read(ByteBuffer bb) {\n");
    printf("  final var opcode = leb128(bb);\n");
    printf("    return switch(opcode) {\n");
#define V(variant, hex, ...) \
    printf("      case Instruction." #variant ".OPCODE -> Instruction." #variant ".read(bb);\n");
    FOREACH_JASM_OPCODE(V)
#undef V
    printf("      default -> throw new UnsupportedOperationException(\"Opcode not supported yet %%04x\".formatted(opcode));\n");
    printf("    };\n");
    printf("  }\n");
    printf("}\n");

    return 0;
}