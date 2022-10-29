name := "traj-sim"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.1.0"// % "provided"
libraryDependencies += "org.roaringbitmap" % "RoaringBitmap" % "0.6.28"
// https://mvnrepository.com/artifact/org.geotools/gt-geojson
libraryDependencies += "org.geotools" % "gt-geojson" % "24.0"
//libraryDependencies += "org.apache.hadoop" % "hadoop-hdfs" % "2.6.0"
//libraryDependencies+="com.vividsolutions.jts"
resolvers ++= Seq(
//  "geosolution" at "https://maven.geo-solutions.it",
  "osgeo" at "https://repo.osgeo.org/repository/release/"
)


assemblyMergeStrategy  in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("org", "slf4j", xs @ _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "axiom.xml" => MergeStrategy.filterDistinctLines
  case PathList(ps @ _*) if ps.last endsWith "Log$Logger.class" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "ILoggerFactory.class" => MergeStrategy.first
  case x => MergeStrategy.first
  //case x => old(x)
}