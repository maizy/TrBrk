package com.github.maizy.trbrk.app

import org.knowm.xchart.QuickChart
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChart

import scala.util.Random

final case class BrakePressurePoint(shiftMs: Double, percent: Double)

object Main {

  final private val POINTS = 50

  private var lastPressure = Random.nextInt(100).toDouble
  private var fillPoint = 0

  private val data: Array[Array[Double]] = Array(
    Array.fill(POINTS)(0.0),
    Array.fill(POINTS)(0.0),
  )

  def main(args: Array[String]): Unit = {

    var phase = 0

    // Create Chart
    val chart = QuickChart.getChart(
      "Brake Pressure", "ms", "%", "real", data(0), data(1)
    )

    // Show it
    val sw = new SwingWrapper[XYChart](chart)
    sw.displayChart

    while (true) {
      phase += 1
      Thread.sleep(50)
      updateChartData(phase)
      javax.swing.SwingUtilities.invokeLater { () =>
          chart.updateXYSeries("real", data(0), data(1), null)
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
      data(0) = shiftArray(data(0))
      data(1) = shiftArray(data(1))
    } else {
      fillPoint += 1
    }

    val newData = getData(phase)
    data(0)(fillPoint) = newData.shiftMs
    data(1)(fillPoint) = newData.percent
  }

  private def shiftArray(a: Array[Double], fill: Double = 0.0): Array[Double] = {
    val newArray = new Array[Double](a.length)
    Array.copy(a, 1, newArray, 0, POINTS - 1)
    newArray(a.length - 1) = fill
    newArray
  }

}
