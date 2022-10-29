package DISON.common

import DISON.common.DistanceTable.DISTANCETABLE
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ArrayBuffer

case class DistanceTable(sparksc:SparkSession,filePath:String){
  DISTANCETABLE=sparksc.sparkContext.textFile(filePath).zipWithIndex()

  def getNodeWithinDistance(nodeID:Int,threshold:Double):Array[Int]={
    val nodeDistances=DISTANCETABLE.filter(x=>x._2==nodeID.toLong).map(_._1).collect()
    val distances=nodeDistances.head.split(",").zipWithIndex
    val result=distances.filter(x=>x._1.toDouble<=threshold).map(x=>x._2)
    result
  }
  def getDistance(nodeID1:Int,nodeID2:Int):Double={
    val nodeDistances=DISTANCETABLE.filter(x=>x._2==nodeID1.toLong).map(_._1).collect()
    val distances=nodeDistances.head.split(",").zipWithIndex
    val result=distances.filter(x=>x._2==nodeID2).map(x=>x._1.toDouble).head
    result
  }
}
object DistanceTable{
  var DISTANCETABLE:RDD[(String,Long)]=null
}
