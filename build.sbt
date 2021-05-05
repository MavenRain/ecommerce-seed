import BuildEnvPlugin.autoImport
import BuildEnvPlugin.autoImport.BuildEnv
import com.typesafe.sbt.packager.docker.DockerAlias
import com.typesafe.sbt.packager.docker.ExecCmd

scalaVersion in ThisBuild := "2.13.5"

resolvers in ThisBuild ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

lazy val akkaVersion              = "2.6.10"
lazy val akkaHttpVersion          = "10.2.0"
lazy val akkaGrpcVersion          = "1.1.1"
lazy val h2Version = "1.4.200"
lazy val hikariVersion = "3.4.5"
lazy val logbackVersion           = "1.2.3"
lazy val scalaTestPlusPlayVersion = "5.0.0"
lazy val scalaJavaTime = "2.0.0"
lazy val scalaJsDomVersion        = "1.1.0"
lazy val scalaJsScriptsVersion    = "1.1.4"
lazy val scalaTestVersion = "3.2.8"
lazy val shapelessVersion = "2.3.4"
lazy val slinkyVersion            = "0.6.7"
lazy val squerylVersion = "0.9.16"
lazy val reactVersion             = "16.12.0"
lazy val reactProxyVersion        = "1.1.8"
lazy val zioVersion = "1.0.5"
lazy val zioPreludeVersion = "1.0.0-RC3"

lazy val `ecommerce-seed` = (project in file("."))
  .aggregate(
    client,
    server
  )

lazy val proto =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("proto"))
    .enablePlugins(AkkaGrpcPlugin)
    .settings(
      PB.protoSources in Compile := Seq(
        (baseDirectory in ThisBuild).value / "proto" / "src" / "main" / "protobuf"
      )
    )
    .jsSettings(
      libraryDependencies += "com.thesamet.scalapb"         %%% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
      libraryDependencies += "com.thesamet.scalapb.grpcweb" %%% "scalapb-grpcweb" % scalapb.grpcweb.BuildInfo.version,
      PB.targets in Compile := Seq(
        scalapb.gen(grpc = false)            -> (sourceManaged in Compile).value,
        scalapb.grpcweb.GrpcWebCodeGenerator -> (sourceManaged in Compile).value
      )
    )

lazy val protoJs  = proto.js
lazy val protoJVM = proto.jvm

lazy val client =
  project
    .in(file("client"))
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(
      libraryDependencies += "dev.zio" %%% "zio" % zioVersion,
      // https://github.com/zio/zio/issues/3139
      libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % scalaJavaTime,
      libraryDependencies += "me.shadaj" %%% "slinky-web" % slinkyVersion,
      libraryDependencies += "me.shadaj" %%% "slinky-hot" % slinkyVersion,
      libraryDependencies += "com.chuusai" %%% "shapeless" % shapelessVersion,
      libraryDependencies += "dev.zio" %%% "zio-prelude" % zioPreludeVersion,
      scalacOptions += "-Ymacro-annotations",
      scalacOptions += "-Xfatal-warnings",
      scalacOptions += "-feature",
      scalacOptions += "-deprecation",
      scalacOptions += "-Wunused:imports,privates,locals,implicits",
      npmDependencies in Compile += "react"                  -> reactVersion,
      npmDependencies in Compile += "react-dom"              -> reactVersion,
      npmDependencies in Compile += "react-proxy"            -> reactProxyVersion,
      npmDevDependencies in Compile += "file-loader"         -> "6.2.0",
      npmDevDependencies in Compile += "style-loader"        -> "2.0.0",
      npmDevDependencies in Compile += "css-loader"          -> "5.0.1",
      npmDevDependencies in Compile += "html-webpack-plugin" -> "4.3.0",
      npmDevDependencies in Compile += "webpack-merge"       -> "5.7.3",
      scalaJSStage := {
        autoImport.buildEnv.value match {
          case BuildEnv.Development =>
            FastOptStage
          case _ =>
            FullOptStage
        }
      },
      webpackResources := baseDirectory.value / "webpack" * "*",
      webpackConfigFile in fastOptJS := Some(baseDirectory.value / "webpack" / "webpack-fastopt.config.js"),
      webpackConfigFile in fullOptJS := Some(baseDirectory.value / "webpack" / "webpack-opt.config.js"),
      webpackConfigFile in Test := Some(baseDirectory.value / "webpack" / "webpack-core.config.js"),
      webpackDevServerExtraArgs in fastOptJS := Seq("--inline", "--hot"),
      webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
      requireJsDomEnv in Test := true
    )
    .dependsOn(protoJs)

lazy val server =
  project
    .enablePlugins(AkkaGrpcPlugin, WebScalaJSBundlerPlugin, JavaAppPackaging, DockerPlugin, BuildInfoPlugin)
    .in(file("server"))
    .settings(
      scalaJSProjects := {
        autoImport.buildEnv.value match {
          case BuildEnv.Production =>
            Seq(client)
          case _ =>
            Seq.empty
        }
      },
      pipelineStages in Assets := {
        autoImport.buildEnv.value match {
          case BuildEnv.Production =>
            Seq(scalaJSPipeline)
          case _ =>
            Seq.empty
        }
      },
      compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
      WebKeys.packagePrefix in Assets := "public/",
      managedClasspath in Runtime += (packageBin in Assets).value,
      libraryDependencies ++= Seq(
        "com.chuusai" %% "shapeless" % shapelessVersion,
        "com.h2database" % "h2" % h2Version,
        "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
        "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
        "com.typesafe.akka" %% "akka-discovery"           % akkaVersion,
        "com.typesafe.akka" %% "akka-pki"                 % akkaVersion,
        "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-http2-support"       % akkaHttpVersion,
        "com.zaxxer" % "HikariCP" % hikariVersion,
        "ch.megard"         %% "akka-http-cors"           % "0.4.2",
        "ch.qos.logback"    % "logback-classic"           % logbackVersion,
        "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
        "com.typesafe.akka" %% "akka-stream-testkit"      % akkaVersion % Test,
        "dev.zio" %% "zio" % zioVersion,
        "dev.zio" %% "zio-prelude" % zioPreludeVersion,
        "org.scalatest"     %% "scalatest"                % scalaTestVersion % Test,
        "org.squeryl" %% "squeryl" % squerylVersion
      ),
      scalacOptions ++= Seq(
        "-Xfatal-warnings",
        "-feature",
        "-deprecation",
        "-Wunused:imports,privates,locals,implicits"
      ),
      Compile / mainClass := Some("com.example.server.Server"),
      buildInfoKeys ++= Seq[BuildInfoKey]("environmentMode" -> autoImport.buildEnv.value),
      buildInfoPackage := "com.example"
    )
    .settings(
      dockerAliases in Docker += DockerAlias(None, None, "ecommerce-seed", None),
      packageName in Docker := "ecommerce-seed",
      dockerBaseImage := "openjdk:8-alpine",
      dockerCommands := {
        val (stage0, stage1)           = dockerCommands.value.splitAt(8)
        val (stage1part1, stage1part2) = stage1.splitAt(5)
        stage0 ++ stage1part1 ++ Seq(ExecCmd("RUN", "apk", "add", "--no-cache", "bash")) ++ stage1part2
      },
      dockerExposedPorts ++= Seq(9000)
    )
    .dependsOn(protoJVM)

addCommandAlias("serverDev", "~server/reStart")
addCommandAlias("clientDev", "client/fastOptJS::startWebpackDevServer;~client/fastOptJS")
