package whisk.docker

import org.scalatest.concurrent.{ PatienceConfiguration, ScalaFutures }
import org.scalatest.time._
import org.scalatest.{ BeforeAndAfterAll, Suite }

import scala.concurrent.{ Future, ExecutionContext }

trait DockerTestKit extends BeforeAndAfterAll with ScalaFutures {
  self: Suite with DockerClientConfig =>

  // we need ExecutionContext in order to run docker.init() / docker.stop() there
  implicit def dockerExecutionContext: ExecutionContext = ExecutionContext.global

  def dockerContainers: List[DockerContainer] = Nil

  def dockerInitPatienceInterval = PatienceConfiguration.Interval(Span(10, Seconds))

  private def stopRmAll(): Seq[DockerContainer] = {
    Future
      .sequence(
        dockerContainers
          .map(_.remove(force = true))
      ).futureValue(dockerInitPatienceInterval)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()

    val allRunning = Future.sequence(
      dockerContainers
        .map(_.init())
        .map(_.flatMap(_.isReady()))
        .map(_ recover {
          case e =>
            System.err.println(e.getMessage)
            e.printStackTrace(System.err)
            false
        })
    ).map(_.forall(_ == true)).futureValue(dockerInitPatienceInterval)
    if (!allRunning) {
      stopRmAll()
      throw new RuntimeException("Cannot run all required containers")
    }
  }

  override def afterAll(): Unit = {
    super.afterAll()
    stopRmAll()
  }
}

