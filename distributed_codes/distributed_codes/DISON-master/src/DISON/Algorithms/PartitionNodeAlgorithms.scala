package DISON.Algorithms

import DISON.Partitioners.Node
import DISON.common.NodeTable.NODETABLE
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row, SQLContext}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object PartitionNodeAlgorithms {
  /*
   * @param a node weight table
   * @return a hashmap with key:node,value:groupID
   * @Description: partition nodes into groups logically. nodeWeightTable:sNode,weight.
   */
//  case class Node(nodeID:Long, weight:Int,lon:Double,lat:Double)
  private def getDimension2parts(groupMap:mutable.HashMap[Int,Int],t1Array:ArrayBuffer[(Node,Int)],t1GroupNum:Int,partNum:Int)={
    var currentGroup=0
    var currentWeight=0
    val sorted=t1Array.sortWith(_._1.lat<_._1.lat)
    val totalWeight:Long=sorted.map(x=>x._2).sum
    val partweight=(totalWeight)*1.0/partNum
    var t2Array=ArrayBuffer[Int]()
    for(itemNode<-sorted){
      currentWeight+=itemNode._2
      t2Array.append(itemNode._1.id)
      if(currentWeight>=partweight){
        //deal with this array with lat
       t2Array.map(x=>groupMap.put(x,currentGroup+t1GroupNum*partNum))
        t2Array.clear()
        currentGroup+=1
        currentWeight=0
      }
    }
  if(t2Array.nonEmpty){
    t2Array.map(x=>groupMap.put(x,currentGroup+t1GroupNum*partNum))
  }
    groupMap
  }



  def GetPartition(data:RDD[Node],numPartitions:Int):mutable.HashMap[Int,Int]={
    var groupMap=new mutable.HashMap[Int,Int]()//the result
    val totalWeight=data.count().toInt
    //get weight for every node
    val pairedRDD=data.map(x=>(x.id,x))
    val pairedRDDWithWeight=pairedRDD.countByKey().map(x=>(x._1,x._2.toInt))
    //get the partition parameters
    val partitionNumD1=Math.ceil(Math.pow(numPartitions, 1.0 / 2)).toInt
    val partitionNumD2=Math.floor(numPartitions*1.0/partitionNumD1).toInt
    val partWeight=Math.ceil(totalWeight*1.0/partitionNumD1).toInt
    //iterate every node to decide where it belongs
    var currentGroup=0
    var currentWeight=0
    val sorted=data.collect().sortWith(_.lon<_.lon).distinct.map(x=>(x,pairedRDDWithWeight.get(x.id).getOrElse(-1)))
    var t1Array=new ArrayBuffer[(Node,Int)]()
    for(itemNode<-sorted) {
      currentWeight += itemNode._2
      t1Array.append(itemNode)
      if (currentWeight >= partWeight) {
        getDimension2parts(groupMap, t1Array, currentGroup, partitionNumD2)
        t1Array.clear()
        currentGroup+=1
        currentWeight = 0
      }
    }
    if(t1Array.nonEmpty){
      getDimension2parts(groupMap,t1Array,currentGroup,partitionNumD2)
    }
    groupMap
  }

}
