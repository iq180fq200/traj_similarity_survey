package DISON.Partitioners

import DISON.Algorithms.PartitionNodeAlgorithms
import DISON.common.NodeTable

import scala.collection.mutable
import org.apache.spark.Partitioner
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.storage.StorageLevel
/**
 * @author hanxi
 * @date 2022/3/26 13 01
 *       discription
 */
case class GlobalSTRPartitioner(data:RDD[Node],_numPartitions:Int)extends Partitioner{

  override def numPartitions: Int = _numPartitions
  override def getPartition(key: Any): Int = {
    val groupNum = index.get(key.asInstanceOf[Int]).getOrElse(-1)
    groupNum
  }

  var index:mutable.HashMap[Int, Int]=PartitionNodeAlgorithms.GetPartition(data,numPartitions)

}


case class Node(id:Int,lon:Double,lat:Double)
object GlobalSTRPartitioner{

  def PartitionPairedRDD(dataRDD: RDD[(Int,Int)], numPartitions:Int,spark:SQLContext):(RDD[Int], GlobalSTRPartitioner)={
    //1. get the coords of the keys
//    var start=System.currentTimeMillis()
    val keysWithCoords=dataRDD.map({item=>
      val s1= item._1
      val s2=NodeTable.hashNodeTable.value.get(s1).get
      Node(s1,s2._1,s2._2)})

    //2.get the partitioner
    val partitioner=new GlobalSTRPartitioner(keysWithCoords,numPartitions)//job4
    val partitionedData1=dataRDD.partitionBy(partitioner)
    val partitionedData=partitionedData1.map(x=>x._2)
    (partitionedData,partitioner)
  }

  def PartitionRDD(dataRDD:RDD[Int],numPartitions:Int,spark:SQLContext):GlobalSTRPartitioner={
    //1. get the coords of the keys
    import spark.implicits._
    var start=System.currentTimeMillis()
    val keysWithCoords=dataRDD.map({item=>
      val s1= item
      val s2=NodeTable.hashNodeTable.value.get(s1).get
      Node(s1,s2._1,s2._2)})
    val partitioner=new GlobalSTRPartitioner(keysWithCoords,numPartitions)//job6
    partitioner
  }
}

