package DISON.Index

import DISON.Index.LocalIndex.CreateLocalIndex
import DISON.common._
import DISON.common.EdgeTable
import DISON.common.Segment.getSegLength
import org.apache.spark.sql.SQLContext

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * @author hanxi
 * @date 2022/3/13 13 47
 *       discription
 */
case class LocalIndex(mappedTrajs:Array[Trajectory],sparksc:SQLContext){

  val localInvertedIndex=CreateLocalIndex(mappedTrajs,sparksc)

}

object LocalIndex{
  def CreateLocalIndex(mappedTrajs:Array[Trajectory],sparksc:SQLContext):mutable.HashMap[Int,ArrayBuffer[(BigInt,Double)]]={
    var localInvertedIndex=new mutable.HashMap[Int,ArrayBuffer[(BigInt,Double)]]
    import scala.util.control._
    val loop = new Breaks;
      for (elem <- mappedTrajs) {
        var D:Double=0
        loop.breakable{
          for(elem1<-elem.segments){
            if(D>elem.length*DISONConfigConstants.THRESHOLD_LIMIT)
              loop.break()
            if(localInvertedIndex.contains(elem1.sid)){
              var currentArray=localInvertedIndex.get(elem1.sid)
              currentArray.getOrElse(ArrayBuffer[(BigInt,Double)]()).append((elem.Tid,D))
            }
            else{
              localInvertedIndex.put((elem1.sid),ArrayBuffer[(BigInt,Double)]((elem.Tid,D)))
            }
            D=D+elem1.segLength
          }
        }
      }
    localInvertedIndex
  }
}