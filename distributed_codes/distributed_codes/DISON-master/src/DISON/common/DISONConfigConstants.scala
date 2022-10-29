package DISON.common

object DISONConfigConstants {
  // basic
//  var THRESHOLD_LIMIT = 0.9//64
  var THRESHOLD_LIMIT = 0.2//57

  // global
  var NUM_PARTITIONS = 64




  var NUM_LEVEL_ONE_PARTITION=scala.math.pow(NUM_PARTITIONS,0.5).toInt
  var NUM_LEVEL_TWO_PARTITION=NUM_PARTITIONS/NUM_LEVEL_ONE_PARTITION.toInt
}
