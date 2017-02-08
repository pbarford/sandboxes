package sandbox.akka.config

import com.datastax.driver.core.{Cluster, Session}


trait CassandraConfig {

  def cluster: Cluster
  def session: Session

  def cassandraKeyspace: String
  def cassandraReadConsistency: String
  def cassandraWriteConsistency: String

}

trait DefaultCassandraConfig extends CassandraConfig {

  override lazy val cluster:Cluster = Cluster.builder.addContactPoints("192.168.99.100").build()
  override lazy val session:Session = cluster.connect(cassandraKeyspace)
  override def cassandraKeyspace: String = "docker_dev"
  override def cassandraReadConsistency:String = "LOCAL_QUORUM"
  override def cassandraWriteConsistency:String = "LOCAL_QUORUM"

}
