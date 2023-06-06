#include <stdio.h>
#include "wasm-opcodes.h"

#define PRINT_CSV(...) printf("%s\n", #__VA_ARGS__);

int main(int argc, const char **argv)
{
    FOREACH_OPCODE(PRINT_CSV);
    printf("---\n");
    FOREACH_SIGNATURE(PRINT_CSV);

    return 0;
}
