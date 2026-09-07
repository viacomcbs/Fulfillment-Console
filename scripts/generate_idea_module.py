from pathlib import Path

module_dir = Path(__file__).resolve().parents[1]
idea_dir = module_dir.parent / ".idea"
cp_file = module_dir / "target/idea-test-classpath.txt"
jars = [p.strip() for p in cp_file.read_text(encoding="utf-8").split(";") if p.strip()]


def to_url(path: Path) -> str:
    return "file://" + path.as_posix()


def to_jar_url(path: str) -> str:
    return "jar://" + Path(path).as_posix() + "!/"


roots = [
    f'      <root url="{to_url(module_dir / "target/classes")}" />',
    f'      <root url="{to_url(module_dir / "target/test-classes")}" />',
]
roots.extend(f'      <root url="{to_jar_url(jar)}" />' for jar in jars)

lib_path = idea_dir / "libraries" / "Maven_Fulfillment_Console_Test_Dependencies.xml"
lib_path.parent.mkdir(parents=True, exist_ok=True)
lib_path.write_text(
    """<?xml version="1.0" encoding="UTF-8"?>
<component name="libraryTable">
  <library name="Maven Fulfillment Console Test Dependencies">
    <CLASSES>
"""
    + "\n".join(roots)
    + """
    </CLASSES>
    <JAVADOC />
    <SOURCES />
  </library>
</component>
""",
    encoding="utf-8",
)

iml = """<?xml version="1.0" encoding="UTF-8"?>
<module org.jetbrains.idea.maven.project.MavenProjectsManager.isMavenModule="true" type="JAVA_MODULE" version="4">
  <component name="NewModuleRootManager" LANGUAGE_LEVEL="JDK_11">
    <output url="file://$MODULE_DIR$/target/classes" />
    <output-test url="file://$MODULE_DIR$/target/test-classes" />
    <content url="file://$MODULE_DIR$">
      <sourceFolder url="file://$MODULE_DIR$/src/main/java" isTestSource="false" />
      <sourceFolder url="file://$MODULE_DIR$/src/test/java" isTestSource="true" />
      <sourceFolder url="file://$MODULE_DIR$/src/test/resources" type="java-test-resource" />
      <excludeFolder url="file://$MODULE_DIR$/target" />
      <excludeFolder url="file://$MODULE_DIR$/allure-report" />
      <excludeFolder url="file://$MODULE_DIR$/allure-results" />
      <excludeFolder url="file://$MODULE_DIR$/test-output" />
    </content>
    <orderEntry type="inheritedJdk" />
    <orderEntry type="sourceFolder" forTests="false" />
    <orderEntry type="library" name="Maven Fulfillment Console Test Dependencies" level="project" />
  </component>
</module>
"""
(module_dir / "fulfillment-console-automation.iml").write_text(iml, encoding="utf-8")
print(f"Generated iml and library with {len(jars)} jars")
