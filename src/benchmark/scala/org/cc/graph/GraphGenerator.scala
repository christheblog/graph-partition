package org.cc.graph

import scala.util.Random

object GraphGenerator {

  import Graph._

  def generate(start: Int = 1, size: Int = 1000, connections: Int = 5000): Graph[Int] = {
    val random = new Random()
    val graph = (start to start+size).foldLeft(Graph.empty[Int]) { case (g,e) => add(g)(e) }
    // Generating random connections
    (1 to connections).foldLeft(graph) { case (g,_) =>
      val a = start + random.nextInt(size) + 1
      val b = start + random.nextInt(size) + 1
      if(a != b) connect(g)(a)(b) else g
    }
  }

}
