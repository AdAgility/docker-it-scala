package whisk.docker

import org.scalatest.Suite

trait PingContainerKit extends DockerTestKit {
  self: Suite =>

  val pingContainer = DockerContainer("nginx:1.7.11")

  val pongContainer = DockerContainer("nginx:1.7.11")
    .withPorts(80 -> None)
    .withReadyChecker(DockerReadyChecker.HttpResponseCode(port = 80))

  abstract override def dockerContainers = pingContainer :: pongContainer :: super.dockerContainers
}
