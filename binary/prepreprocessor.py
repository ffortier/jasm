# Keep only the x-macros
from argparse import ArgumentParser

def extract_x_macros(input: str, output: str):
    keep: list[str] = []

    with open(input, "r") as f:
        reading_x_macro = False
        line = f.readline()

        while line:
            if reading_x_macro:
                keep.append(line)

                if not line.rstrip().endswith('\\'):
                    reading_x_macro = False

            elif line.startswith("#define FOREACH"):
                keep.append(line)
                
                if line.rstrip().endswith('\\'):
                    reading_x_macro = True

            line = f.readline()

    with open(output, "w") as f:
        f.writelines(keep)


if __name__ == "__main__":
    parser = ArgumentParser()
    parser.add_argument("--output","-o", required=True)
    parser.add_argument("input", nargs=1)
    args = parser.parse_args()

    extract_x_macros(args.input[0], args.output)