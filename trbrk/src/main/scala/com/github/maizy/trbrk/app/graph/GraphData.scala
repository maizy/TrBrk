package com.github.maizy.trbrk.app.graph

class GraphData(
   var realTime: Array[Double],
   var real: Array[Double],
   var expectedTime: Array[Double],
   var expected: Array[Double]
)

object GraphData {
  def empty: GraphData = new GraphData(Array.empty, Array.empty, Array.empty, Array.empty)
}
