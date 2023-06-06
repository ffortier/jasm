load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_file")

def establish_v8_deps():
    http_file(
        name = "v8_wasm_opcodes",
        url = "https://raw.githubusercontent.com/v8/v8/e9f288a56c0d4377cb73d13d97cb11a505de51be/src/wasm/wasm-opcodes.h",
        sha256 = "989a7211778d795c0352006e8e99212cb36b3ee4fa74d6171243d62fecae4311",
    )
