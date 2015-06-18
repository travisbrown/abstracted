lazy val sharedSettings = Seq(
  organization := "io.travisbrown",
  scalaVersion := "2.11.6",
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion.value
  )
)

lazy val root = project.in(file("."))
  .settings(sharedSettings)
  .aggregate(core, demo)
  .dependsOn(core)

lazy val core = project
  .settings(moduleName := "abstracted")
  .settings(sharedSettings)

lazy val demo = project
  .settings(moduleName := "abstracted-demo")
  .settings(sharedSettings)
  .settings(resolvers += Resolver.sonatypeRepo("snapshots"))
  .dependsOn(
    ProjectRef(uri("git://github.com/travisbrown/catbird.git"), "finagle"),
    core
  )
