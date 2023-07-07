load("@rules_jvm_external//:defs.bzl", "maven_install")

def establish_java_deps():
    maven_install(
        artifacts = [
            "org.ow2.asm:asm:9.5",
        ],
        repositories = [
            "https://repo1.maven.org/maven2",
        ],
        maven_install_json = "@//third_party/java:maven_install.json",
        fetch_sources = True,
        fetch_javadoc = True,
    )
