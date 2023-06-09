with (import <nixpkgs> {});
mkShell {
  buildInputs = [
    bazelisk
    buildifier
    wabt
    python311
    gnupatch
  ];

  shellHook = ''
    create_venv() {
      python3 -m venv .venv
    }

    [[ -f .venv ]] || create_venv

    source .venv/bin/activate

    pip install -r third_party/python/requirements.in
  '';
}
