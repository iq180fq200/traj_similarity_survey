Trajectory Similarity Search in Apache Spark
============================================
Build
-----
Call `sbt assembly` and you will get the compiled package at `target/scala-2.11/traj-sim-assembly-1.0.jar`.

Run
---
the main class is **edu.utah.cs.trajectory.DualIndexingSolution**

Run it by feeding the package to spark-submit with parameters <query trajectory file path>  <candidate trajectory file path>.

NOTE: the trajectory length  and sample rate is decided by different  ${candidate trajectory file path}

Contributor
-----------
- Dong Xie: dongx [at] cs [dot] utah [dot] edu