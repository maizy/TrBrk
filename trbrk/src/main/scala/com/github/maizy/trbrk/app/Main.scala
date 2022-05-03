package com.github.maizy.trbrk.app

import scala.util.Random
import org.knowm.xchart.QuickChart
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChart
import com.github.maizy.trbrk.app.graph.GraphData
import com.github.maizy.trbrk.app.training_generator.TrailBrake
import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging {

  final private val RESOLUTION = 30
  final private val GRAPH_WIDTH_MS = 15 * 1000

  private val generator = new TrailBrake(RESOLUTION)
  private val trainingData = new TrainingSessionData(RESOLUTION, generator)

  private val data = GraphData.empty

  def main(args: Array[String]): Unit = {

    trainingData.fillGraphData(fromMs = 0, toMs = GRAPH_WIDTH_MS, data)
    // Create Chart
    val chart = QuickChart.getChart(
      "Brake Pressure",
      "ms", "%",
      Array("real", "expected"),
      data.expectedTime,
      Array(data.real, data.expected)
    )

    // Show it
    val sw = new SwingWrapper[XYChart](chart)
    sw.displayChart

    val beginAt = System.currentTimeMillis
    while (true) {
      val shift = TrainingTimeShift((System.currentTimeMillis - beginAt).toInt)
      updateData(shift)
      Thread.sleep(RESOLUTION / 2)
      javax.swing.SwingUtilities.invokeLater { () =>
        val fromMs = if (shift.ms < GRAPH_WIDTH_MS / 2) {
          0
        } else {
          shift.ms - GRAPH_WIDTH_MS / 2
        }

        trainingData.fillGraphData(
          fromMs = fromMs,
          toMs = fromMs + GRAPH_WIDTH_MS - RESOLUTION,
          data
        )
        chart.updateXYSeries("expected", data.expectedTime, data.expected, null)
        chart.updateXYSeries("real", data.realTime, data.real, null)

        sw.repaintChart()
      }
    }
  }

  // random data for test
  // TODO: replace with polling from devices

  private var lastPressure = Random.nextInt(100).toDouble
  private val syncObject = new Object

  private def updateData(shift: TrainingTimeShift): Unit = {
    syncObject.synchronized {
      val sign = if (Random.nextBoolean()) 1.0 else -1.0
      val nextPressure = math.min(math.max(lastPressure + Random.nextInt(10) * sign, 0.0), 100.0)
      lastPressure = nextPressure
      trainingData.addRealValue(shift, nextPressure)
    }
  }
}
