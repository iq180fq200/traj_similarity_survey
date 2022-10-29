package DISON.Partitioners

/**
 * @author hanxi
 * @date 2022/3/12 19 41
 * @discription to partition the trajectories according to the global index
 */
import DISON.Index.GlobalIndex
import DISON.common.DISONConfigConstants.{NUM_LEVEL_TWO_PARTITION, NUM_PARTITIONS}
import org.apache.spark.Partitioner

class  GlobalPartitioner(globalIndex: GlobalIndex) extends Partitioner {
  override def numPartitions: Int = NUM_PARTITIONS
  override def getPartition(key: Any): Int = {
    val L1GroupNum = globalIndex.indexL1.get(key.asInstanceOf[Tuple2[Int,Int]]._1).getOrElse(-1)
    if(L1GroupNum == -1){
      println("error! the start node key is not in any partition!!")
    }
    val L2GroupNum = globalIndex.indexL2(L1GroupNum.asInstanceOf[Int]).get(key.asInstanceOf[Tuple2[Int,Int]]._2).getOrElse(-1)
    if(L1GroupNum == -1||L2GroupNum == -1){
      println("error! the end node key is not in any partition!!")
    }
    val a: Int = (NUM_LEVEL_TWO_PARTITION * L1GroupNum)
    a + L2GroupNum
  }
//  override def hashCode: Int = numPartitions

}

object GlobalPartitioner{
  def GetPartitionIDWithGroupID(groupNum1:Int,groupNum2:Int):Int={
    (NUM_LEVEL_TWO_PARTITION * groupNum1)+groupNum2
  }
}