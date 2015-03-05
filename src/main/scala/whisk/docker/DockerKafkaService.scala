package whisk.docker

trait DockerKafkaService extends DockerKit with DockerZookeeperService {

  def KafkaAdvertisedPort = 9092

  val kafkaContainer = DockerContainer("wurstmeister/kafka:0.8.2.0")
    .withPorts(9092 -> Some(KafkaAdvertisedPort))
    .withEnv(s"KAFKA_ADVERTISED_PORT=$KafkaAdvertisedPort", s"KAFKA_ADVERTISED_HOST_NAME=${docker.host}")
    .withReadyChecker(DockerReadyChecker.LogLine(_.contains("started (kafka.server.KafkaServer)")))
    .withLinks(zookeeperContainer -> "zk")

  abstract override def dockerContainers: List[DockerContainer] = kafkaContainer :: super.dockerContainers
}
