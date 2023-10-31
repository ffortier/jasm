#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <stddef.h>
#include <stdlib.h>

#include "jasm/binary/wasm_opcodes.h"

void print_record(const char *variant, int hex)
{
    if (hex > 0x00ff)
    {
        return; // Skipping extended instructions (vector, tables, struct)
    }

    size_t len = strlen(variant);

    if (strncmp(variant + len - 5, "Const", 5) == 0)
    {
        if (strncmp(variant, "I32", 3) == 0)
        {
            printf("record %s(int value) implements OpCode { public static final int HEX = 0x%04x; }\n", variant, hex);
        }
        else if (strncmp(variant, "I64", 3) == 0)
        {
            printf("record %s(long value) implements OpCode { public static final int HEX = 0x%04x; }\n", variant, hex);
        }
        else if (strncmp(variant, "F32", 3) == 0)
        {
            printf("record %s(float value) implements OpCode { public static final int HEX = 0x%04x; }\n", variant, hex);
        }
        else if (strncmp(variant, "F64", 3) == 0)
        {
            printf("record %s(double value) implements OpCode { public static final int HEX = 0x%04x; }\n", variant, hex);
        }
        else
        {
            fprintf(stderr, "Unknown variant %s 0x%04x", variant, hex);
            exit(1);
        }
    }
    else if (strncmp(variant + len - 4, "Load", 4) == 0)
    {
        printf("record %s(Memarg arg) implements OpCode { public static final int HEX = 0x%04x; }\n", variant, hex);
    }
    else if (strncmp(variant + len - 5, "Store", 5) == 0)
    {
        printf("record %s(Memarg arg) implements OpCode { public static final int HEX = 0x%04x; }\n", variant, hex);
    }
    else
    {
        printf("record %s() implements OpCode { public static final int HEX = 0x%04x; }\n", variant, hex);
    }
}

int main(void)
{
#define V(variant, hex, ...) print_record(#variant, hex);
    FOREACH_OPCODE(V)
#undef V
    return 0;
}