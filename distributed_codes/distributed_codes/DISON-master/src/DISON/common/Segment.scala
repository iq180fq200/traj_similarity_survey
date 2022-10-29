package DISON.common

import DISON.common.Segment.getSegLength
import org.apache.spark.sql.SQLContext


case class Segment(val sid:Int) {
  val segLength=getSegLength(sid)

  override def equals(obj:Any):Boolean ={
    if(!obj.isInstanceOf[Segment]){
      false
    }else{
      val x=obj.asInstanceOf[Segment]
      if(x.sid==sid)
        true
      else
        false
    }
  }

  override def hashCode():Int = {segLength.toInt}
}

object Segment{
  def getSegLength(sid:Int):Double={
    EdgeTable.hashEdgeTable.value.get(sid).getOrElse((-1,-1,-1.0))._3
  }
}