package whisk.docker

import net.ceedubs.ficus.readers.ValueReader
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.Ficus._
import scala.concurrent.duration._

object DockerTypesafeConfig extends DockerKit {
  val EmptyPortBindings: Map[Int, Option[Int]] = Map.empty
  val AlwaysReady = DockerReadyChecker.Always

  case class DockerConfigPortMap(internal: Int, external: Option[Int]) {
    def asTuple = (internal, external)
  }

  case class DockerConfigReadyCheckerLooped(attempts: Int, delay: Int)

  case class DockerConfigHttpResponseReady (
    port: Int, path: String = "/",
    host: Option[String], code: Int = 200,
    within: Option[Int], looped: Option[DockerConfigReadyCheckerLooped])

  case class DockerConfigReadyChecker(
    `log-line`: Option[String],
    `http-response-code`: Option[DockerConfigHttpResponseReady]) {

    def httpResponseCodeReadyChecker(rr: DockerConfigHttpResponseReady) = {
      val codeChecker: DockerReadyChecker =
        DockerReadyChecker.HttpResponseCode(rr.port, rr.path, rr.host, rr.code)
      val within = rr.within.fold(codeChecker)(w => codeChecker.within(w.millis))
      rr.looped.fold(within)(l => within.looped(l.attempts, l.delay.millis))
    }

    // log line checker takes priority
    def toReadyChecker = {
      (`log-line`, `http-response-code`) match {
        case (None, None) => DockerReadyChecker.Always
        case (None, Some(rr)) => httpResponseCodeReadyChecker(rr)
        case (Some(ll), _) => DockerReadyChecker.LogLine(_.contains(ll))
      }
    }
  }

  case class DockerConfig (
    `image-name`: String, command: Option[Seq[String]],
    `environmental-variables`: Seq[String] = Seq.empty,
    `port-maps`: Option[Map[String, DockerConfigPortMap]],
    `ready-checker`: Option[DockerConfigReadyChecker]) {

    def toDockerContainer() = {
      val bindPorts =
        `port-maps`
          .fold(EmptyPortBindings) { _.values.map(_.asTuple).toMap }

      val readyChecker =
        `ready-checker`
          .fold[DockerReadyChecker](AlwaysReady) { _.toReadyChecker }

      DockerContainer(
        image = `image-name`,
        command = command,
        bindPorts = bindPorts,
        env = `environmental-variables`,
        readyChecker = readyChecker
      )
    }
  }

  // case class DockerConfig (
  //   imageName: String,
  //   command: Option[Seq[String]],
  //   envVars: Option[Seq[String]],
  //   portMaps: Option[Map[String, DockerConfigPortMap]],
  //   // logLineChecker: Option[String],
  //   readyChecker: Option[DockerConfigReadyChecker]
  // ) {
  //   def toDockerContainer() = {
  //     val bindPorts = portMaps.fold(EmptyPortBindings) {
  //       _.values.map(_.asTuple).toMap
  //     }
  //     val env = envVars getOrElse Seq.empty

  //     val rc: DockerReadyChecker =
  //       readyChecker
  //         .fold[DockerReadyChecker](AlwaysReady) { _.toReadyChecker }

  //     // val readyChecker =
  //     //   logLineChecker.fold[DockerReadyChecker](DockerReadyChecker.Always) {
  //     //     llc => DockerReadyChecker.LogLine(_.contains(llc))
  //     //   }

  //     DockerContainer(
  //       image = imageName,
  //       command = command,
  //       bindPorts = bindPorts,
  //       env = env,
  //       readyChecker = rc
  //     )
  //   }
  // }

  // implicit val dockerConfigReader: ValueReader[DockerConfig] =
  //   ValueReader.relative { dockerConfig =>
  //     DockerConfig(
  //       imageName = dockerConfig.as[String]("image-name"),
  //       command = dockerConfig.as[Option[Seq[String]]]("command"),
  //       envVars = dockerConfig.as[Option[Seq[String]]]("environmental-variables"),
  //       portMaps = dockerConfig.as[Option[Map[String, DockerConfigPortMap]]]("port-maps"),
  //       // logLineChecker = dockerConfig.as[Option[String]]("log-line-checker"),
  //       readyChecker = dockerConfig.as[Option[DockerConfigReadyChecker]]("ready-checker")
  //     )
  //   }
}
