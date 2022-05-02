package com.github.maizy.trbrk.app

import org.knowm.xchart.QuickChart
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChart

import scala.util.Random

final case class BrakePressurePoint(shiftMs: Double, percent: Double)
final case class GraphData(var time: Array[Double], var realPercents: Array[Double], var expectedPercents: Array[Double])

object GraphData {
  // TODO: expected data should slide with real data
  def fill(points: Int, stepMs: Int, expected: Array[Double], realFill: Double = 0.0): GraphData = {
    require(expected.length == points)
    val time = (0 until points).map { i => (i * stepMs).toDouble }.toArray
    val real = Array.fill(points)(realFill)
    GraphData(time, real, expected)
  }
}

object Main {

  final private val POINTS = 50
  final private val STEP_MS = 20

  private var lastPressure = Random.nextInt(100).toDouble
  private var fillPoint = 0

  private val expected: Array[Double] = {

    val before = Array.fill(5)(0.0)
    val top = Array.fill(10)(100.0)

    val step = 100.0 / 20
    val topToBottom = (1 to 20).map { i =>
      100.0 - i * step
    }.toArray

    val profile = before ++ top ++ topToBottom
    profile ++ Array.fill(POINTS - profile.length)(0.0)
  }

  private val data = GraphData.fill(POINTS, STEP_MS, expected)

  def main(args: Array[String]): Unit = {

    var phase = 0

    // Create Chart
    val chart = QuickChart.getChart(
      "Brake Pressure",
      "ms", "%",
      Array("real", "expected"),
      data.time,
      Array(data.realPercents, data.expectedPercents)
    )

    // Show it
    val sw = new SwingWrapper[XYChart](chart)
    sw.displayChart

    while (true) {
      phase += 1
      Thread.sleep(50)
      updateChartData(phase)
      javax.swing.SwingUtilities.invokeLater { () =>
          chart.updateXYSeries("real", data.time, data.realPercents, null)
          sw.repaintChart()
      }
    }
  }

  private def getData(i: Int): BrakePressurePoint = {
    val sign = if (Random.nextBoolean()) 1.0 else -1.0

    val nextPressure = math.min(math.max(lastPressure + Random.nextInt(10) * sign, 0.0), 100.0)
    lastPressure = nextPressure
    BrakePressurePoint((i * 20).toDouble, nextPressure)
  }

  private def updateChartData(phase: Int): Unit = {
    if (fillPoint >= POINTS - 1) {
      data.time = shiftArray(data.time)
      data.realPercents = shiftArray(data.realPercents)
    } else {
      fillPoint += 1
    }

    val newData = getData(phase)
    data.time(fillPoint) = newData.shiftMs
    data.realPercents(fillPoint) = newData.percent
  }

  private def shiftArray(a: Array[Double], fill: Double = 0.0): Array[Double] = {
    val newArray = new Array[Double](a.length)
    Array.copy(a, 1, newArray, 0, POINTS - 1)
    newArray(a.length - 1) = fill
    newArray
  }

}
