package kafka

/*
import kafka.producer.Partitioner

class TestPartitioner extends Partitioner{
  override def partition(key: Any, numPartitions: Int): Int = {
    val organizationId = key.asInstanceOf[Long]
    if (numPartitions < organizationId) {
      numPartitions - 1
    } else
      key.asInstanceOf[Int]
  }
}
*/