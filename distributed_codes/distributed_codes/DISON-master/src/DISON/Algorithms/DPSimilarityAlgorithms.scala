package DISON.Algorithms

import DISON.common.{DISONConfigConstants, Segment, Trajectory}
import org.apache.spark.sql.SQLContext

import scala.collection.mutable.ArrayBuffer


/**
 * @author hanxi
 * @date 2022/3/12 20 36
 *       discription
 */
object DPSimilarityAlgorithms {
  private def FillIn(commonLength:Array[Array[Double]],x:Int,y:Int,QueryTraj:Array[Segment],CandidateTraj:Array[Segment],SQLContext: SQLContext): Double ={
    if(x==0||y==0)
      commonLength(x)(y)=0
    else if(QueryTraj(x-1).equals(CandidateTraj(y-1)))
      commonLength(x)(y)=commonLength(x-1)(y-1)+QueryTraj(x-1).segLength
    else {
    commonLength(x)(y)=math.max(commonLength(x-1)(y),commonLength(x)(y-1))
    }
    commonLength(x)(y)
  }
  def DPSimilarityValue(QueryTraj:Array[Segment],CandidateTraj:Array[Segment],SQLContext: SQLContext):Double={
    val segNumQ=QueryTraj.length
    val segNumT=CandidateTraj.length
    val lengthT=CandidateTraj.map(x=>x.segLength).sum
    val lengthQ=QueryTraj.map(x=>x.segLength).sum
    var commonLength=Array.ofDim[Double](segNumQ+1,segNumT+1)
    //Q is row, T is column; x is row, y is column
    var x=0;var y=0
    var startx=0;var starty=0
    import scala.util.control._
    val loop = new Breaks;
    var nowCommon=0.0;
    loop.breakable{
      while(true){
        nowCommon=FillIn(commonLength,x,y,QueryTraj,CandidateTraj,SQLContext)
//        if(nowCommon/(lengthQ+lengthT-nowCommon)>=DISONConfigConstants.THRESHOLD_LIMIT)
//          return true
        if(y-1>=0&&x+1<=segNumQ){
          y=y-1
          x=x+1
        }
        else{
          x=startx
          y=starty
          if(y+1<=segNumT)
            y=y+1
          else if(x+1<=segNumQ)
            x=x+1
          else
            loop.break
          startx=x
          starty=y
        }
      }
    }
    return nowCommon/(lengthQ+lengthT-nowCommon)
  }
  def DPJudgeSimilar(QueryTraj:Array[Segment],CandidateTraj:Array[Segment],SQLContext: SQLContext):Boolean={
    val segNumQ=QueryTraj.length
    val segNumT=CandidateTraj.length
    val lengthT=CandidateTraj.map(x=>x.segLength).sum
    val lengthQ=QueryTraj.map(x=>x.segLength).sum
    var commonLength=Array.ofDim[Double](segNumQ+1,segNumT+1)
    //Q is row, T is column; x is row, y is column
    var x=0;var y=0
    var startx=0;var starty=0
    import scala.util.control._
    val loop = new Breaks;
    loop.breakable{
      while(true){
        val nowCommon=FillIn(commonLength,x,y,QueryTraj,CandidateTraj,SQLContext)
        if(nowCommon/(lengthQ+lengthT-nowCommon)>=DISONConfigConstants.THRESHOLD_LIMIT)
          return true
        if(y-1>=0&&x+1<=segNumQ){
          y=y-1
          x=x+1
        }
        else{
          x=startx
          y=starty
          if(y+1<=segNumT)
            y=y+1
          else if(x+1<=segNumQ)
            x=x+1
          else
            loop.break
          startx=x
          starty=y
        }
      }
    }
    return false
  }
}
