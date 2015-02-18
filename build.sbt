import scalariform.formatter.preferences._


name := "scala-mapifier"

organization := "com.in-and-win"

version := "0.2.1"

scalaVersion := "2.11.5"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings", "-Xlint", "-language:reflectiveCalls")

/// Scala compiler
libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _)

/// Specs2
resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies += "org.specs2" % "specs2_2.11" % "2.4.16" % "test"

/// Scalariform
scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(IndentLocalDefs, true)


/// Publishing
publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>http://github.com/in-and-win/scala-mapifier</url>
  <licenses>
    <license>
      <name>Apache</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:in-and-win/scala-mapifier.git</url>
    <connection>scm:git@github.com:in-and-win/scala-mapifier.git</connection>
  </scm>
  <developers>
    <developer>
      <id>nremond</id>
      <name>Nicolas Rémond</name>
      <url>http://nremond.github.io</url>
    </developer>
  </developers>
)
