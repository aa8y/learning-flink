name := "learning-flink"

version := "0.1.0-SNAPSHOT"

organization := "co.aa8y"

scalaVersion in ThisBuild := "2.11.11"

val flinkVersion = settingKey[String]("flinkVersion")
flinkVersion := sys.env.get("FLINK_VERSION").getOrElse("1.3.2")

libraryDependencies ++= Seq(
  "org.apache.flink" % "flink-core" % flinkVersion.value % "provided",
  "org.apache.flink" %% "flink-connector-twitter" % flinkVersion.value,
  "org.apache.flink" %% "flink-scala" % flinkVersion.value % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion.value % "provided",
  "org.apache.flink" %% "flink-test-utils" % flinkVersion.value % "test",
  "org.json4s" %% "json4s-jackson" % "3.5.3",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

// Disable running tests while assembling the JAR as we depend on Docker to start services
// the tests use.
test in assembly := {}
