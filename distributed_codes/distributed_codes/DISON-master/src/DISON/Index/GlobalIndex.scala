package DISON.Index

import DISON.Algorithms.PartitionNodeAlgorithms.GetPartition
import DISON.Partitioners.GlobalSTRPartitioner
import DISON.common.DISONConfigConstants.{NUM_LEVEL_ONE_PARTITION, NUM_LEVEL_TWO_PARTITION, NUM_PARTITIONS}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions.col
//import org.apache.spark.sql.hive.thriftserver.HiveThriftServer2.LOG
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.log4j.{Level, Logger}

import scala.collection.mutable

case class GlobalIndex(seTuples:RDD[(Int, Int)], spark:SQLContext){
    /*
   * @param L1 groupNum
   * @return sNodes array in L1 groupNum
   * @Description:get all nodes index who are in the level-1 index of groupNum
   */
    def GetsNodesInL1Group(groupNum:Int):Array[Int]={
      indexL1.filter({x=>x._2==groupNum}).keys.toArray
    }

    def GeteNodesInL2Group(groupNum1:Int,groupNum2:Int): Array[Int]={
      val allNodesInGroupNum1=indexL2(groupNum1)
      allNodesInGroupNum1.filter(x=>x._2==groupNum2)
      allNodesInGroupNum1.keys.toArray
    }




  //the first level partition(head node) of the data. the partitionedRDDL1 only contains the last node of every trajectory
  var start=System.currentTimeMillis()
  val numPartitionsL1=NUM_LEVEL_ONE_PARTITION
  private val (partitionedRDDL1, partitionerL1) = GlobalSTRPartitioner.PartitionPairedRDD(seTuples,
    numPartitionsL1,spark)
  //a tuple of (start-node,
  val indexL1: mutable.HashMap[Int, Int]=partitionerL1.index

  //the second level partition(tail node) of the data
  val numPartitionsL2=NUM_LEVEL_TWO_PARTITION
  val indexL2:Array[mutable.HashMap[Int, Int]]=(0 until partitionerL1.numPartitions).map(i => {
    val childRDD = partitionedRDDL1.mapPartitionsWithIndex(
      (idx, iter) => if (idx == i) iter else Iterator())
    GlobalSTRPartitioner.PartitionRDD(childRDD, numPartitionsL2,spark).index
  }).toArray

  var end=System.currentTimeMillis()
  Logger.getLogger("program").warn(s"global indexing Time: ${end - start} ms")

}














//case class SEtable(sNode: Long, eNode: Long)
//case class GlobalIndex(seTuples:RDD[(Long, Long)], spark:SQLContext){
//  /*
//   * @param null
//   * @return DataFrame
//   * @Description: Get the start end node table, each row represents a trajectory
//   */
//  private def GetTraSETable():DataFrame={
//    import spark.implicits._
//    val SETabledf=seTuples.map(seTuple=>SEtable(seTuple._1,seTuple._2)).toDF("sNode","eNode")
//    SETabledf
//  }
//
//  /*
//   * @param null
//   * @return DataFrame
//   * @Description: Get start node weight table, each row represents a node
//   */
//  private def GetSNodeWeightTable():DataFrame={
//    traSETable.createOrReplaceTempView("dff")
//    val df1=spark.sql("SELECT sNode,count(*) as weight from dff group by sNode")
//    val df2=df1.withColumn("weight",col("weight").cast(IntegerType))
//    //print(df2.count())
//    df2
//  }
//
//  /*
//   * @param null
//   * @return DataFrame
//   * @Description: Get End node weight table list, each table represents a first-level group
//   */
//  private def GetENodeWeightTable():Array[DataFrame]={
//    //select nodes according to the group number. start, end, group_num table
//    val L1groupNums=Range(0,NUM_LEVEL_ONE_PARTITION).toArray
//    import spark.implicits._
//    val eNodeTable=L1groupNums.map({num=>
//    val _startNodeInThisGroup=L1Index.filter(x=>x._2==num).keys.toSeq.toDF()
//    _startNodeInThisGroup.createOrReplaceTempView("startNodeInThisGroup")
//      traSETable.createOrReplaceTempView("SETable")
//      val eNodeWeightTable1=spark.sql("SELECT eNode,count(*) AS weight FROM SETable INNER join startNodeInThisGroup on SETable.sNode=startNodeInThisGroup.value GROUP BY eNode")
////val eNodeWeightTable1=spark.sql("SELECT eNode,count(*) AS weight FROM SETable GROUP BY eNode")
//      val eNodeWeightTable=eNodeWeightTable1.withColumn("weight",col("weight").cast(IntegerType))
//      eNodeWeightTable
//    })
//    eNodeTable
//  }
//
//  /*
// * @param L1 groupNum
// * @return sNodes array in L1 groupNum
// * @Description:get all nodes index who are in the level-1 index of groupNum
// */
//  def GetsNodesInL1Group(groupNum:Int):Array[Long]={
//    L1Index.filter({x=>x._2==groupNum}).keys.toArray
//  }
//  def GeteNodesInL2Group(groupNum1:Int,groupNum2:Int): Array[Long]={
//    val allNodesInGroupNum1=L2Index(groupNum1)
//    allNodesInGroupNum1.filter(x=>x._2==groupNum2)
//    allNodesInGroupNum1.keys.toArray
//  }
//
//  val start = System.currentTimeMillis()
//  private val traSETable=GetTraSETable()
//  private val sNodeWeightTable=GetSNodeWeightTable()
//  val L1Index: mutable.HashMap[Long, Int]=GetPartition(sNodeWeightTable,spark,NUM_LEVEL_ONE_PARTITION,"sNode")
//  //println(L1Index.size)
//  private val eNodeWeightTables=GetENodeWeightTable()
//  val L2Index:Array[mutable.HashMap[Long, Int]]=eNodeWeightTables.map(eNodeWeightTable=>GetPartition(eNodeWeightTable,spark,NUM_LEVEL_TWO_PARTITION,"eNode"))
//  val end = System.currentTimeMillis()
//  LOG.warn(s"Global Index Building Time: ${end - start} ms")
//}


