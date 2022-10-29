package DISON.common

import org.apache.spark.sql.SQLContext
import DISON.common.Segment.getSegLength
case class Trajectory(segments:Array[Segment],Tid:BigInt){
  val sNode:Int=EdgeTable.hashEdgeTable.value.get(segments(0).sid).getOrElse((-1,-1,-1))._1
  val eNode:Int=EdgeTable.hashEdgeTable.value.get(segments.last.sid).getOrElse((-1,-1,-1))._1
  val length:Double=segments.map(x=>x.segLength).sum

}
