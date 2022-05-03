package com.github.maizy.trbrk.app

import com.github.maizy.trbrk.app.graph.GraphData
import com.github.maizy.trbrk.app.training_generator.ExpectedDataGenerator
import com.typesafe.scalalogging.LazyLogging

case class HistoryPoint(shift: TrainingTimeShift, expected: Double, real: Double)

class TrainingSessionData(
  resolution: Int,
  expectedDataGenerator: ExpectedDataGenerator,
  bufferLengthMs: Int = 60 * 1000,
  aheadOfTimeBufferMs: Int = 30 * 1000,
) extends LazyLogging {

  logger.trace("init training session data with generator: {}", expectedDataGenerator.getClass.getSimpleName)
  expectedDataGenerator.init()

  private val bufferSteps = bufferLengthMs / resolution
  private val aheadOfTimeSteps = aheadOfTimeBufferMs / resolution

  private var timeline = Array.fill(bufferSteps)(0.0)
  private var timelineUntil = 0
  fillTimeline(0, bufferSteps)

  private var expectedData = Array.fill(bufferSteps)(0.0)
  private var expectedUntil = 0
  fillExpectedData(0, bufferSteps)

  private var realData = Array.fill(bufferSteps)(0.0)
  private var currentShift = TrainingTimeShift(0)
  private var currentStep = -1

  def addRealValue(shift: TrainingTimeShift, value: Double): Unit = {
    this.synchronized {
      val correctedShift = shift.roundToResolution(resolution)
      val step = correctedShift.toStep(resolution)
      if (step == currentStep + 1) {
        currentStep = step
        currentShift = correctedShift
        realData = expandAndSet(realData, step, value)
      } else if (step <= currentStep) {
        logger.trace("set value in the past for step {}", step)
        realData(step) = value
      } else {
        logger.trace("fill missed real values from {} until {}", currentStep, step)
        // fill missed values with zeros
        for (missedStep <- currentStep until step) {
          val shift = TrainingTimeShift.fromStepAndResolution(missedStep, resolution)
          addRealValue(shift, 0.0)
        }
        addRealValue(correctedShift, value)
      }

      if (currentStep + aheadOfTimeSteps > expectedUntil) {
        fillExpectedData(expectedUntil, expectedUntil + aheadOfTimeSteps)
      }

      if (currentStep + aheadOfTimeSteps > timelineUntil) {
        fillTimeline(timelineUntil, timelineUntil + aheadOfTimeSteps)
      }
    }
  }

  private def fillExpectedData(fromStep: Int, untilStep: Int): Unit = {
    logger.trace("fill expected data from {} until {}", fromStep, untilStep)
    (fromStep until untilStep).foreach { step =>
      val time = TrainingTimeShift.fromStepAndResolution(step, resolution)
      val expected = expectedDataGenerator.generate(step, time)
      expectedData = expandAndSet(expectedData, step, expected)
    }
    expectedUntil = untilStep
  }

  private def fillTimeline(fromStep: Int, untilStep: Int): Unit = {
    logger.trace("fill timeline from {} until {}", fromStep, untilStep)
    (fromStep until untilStep).foreach { step =>
      timeline = expandAndSet(timeline, step, (step * resolution).toDouble)
    }
    timelineUntil = untilStep
  }

  private def expandAndSet(array: Array[Double], step: Int, value: Double): Array[Double] = {
    if (step > array.length - 1) {
      val newArray = new Array[Double](array.length + aheadOfTimeSteps)
      Array.copy(array, 0, newArray, 0, array.length)
      newArray(step) = value
      newArray
    } else {
      array(step) = value
      array
    }
  }

  def lastShift: Option[TrainingTimeShift] = if (isEmpty) None else Some(currentShift)
  def lastStep: Option[Int] = if (isEmpty) None else Some(currentStep)
  def length: Int = currentStep + 1
  def isEmpty: Boolean = length == 0

  def fillGraphData(fromMs: Int, toMs: Int, data: GraphData): Unit = {
    this.synchronized {

      val fromStep = fromMs / resolution
      val toStep = toMs / resolution
      val steps = toStep - fromStep + 1

      require(toStep >= fromStep)
      if (toStep >= expectedUntil) {
        fillExpectedData(expectedUntil, List(toStep, expectedUntil + bufferSteps).max)
      }

      // TODO: change graph X left margin and use abs timeline
      val expectedTimeArray = (0 until steps).map { step =>
        step.toDouble * resolution
      }.toArray
      data.expectedTime = expectedTimeArray

      val expectedArray = if (data.expected.length != steps) {
        new Array[Double](steps)
      } else {
        data.expected
      }
      Array.copy(expectedData, fromStep, expectedArray, 0, steps)
      data.expected = expectedArray

      if (currentStep == -1 || fromStep > currentStep) {
        data.real = Array.fill(steps)(0.0)
        data.realTime = data.expectedTime.clone()
      } else {
        val realDataToStep = math.min(currentStep, toStep)
        val realDataSteps = realDataToStep - fromStep + 1

        val realTimeArray = (0 until realDataSteps).map { step =>
          step.toDouble * resolution
        }.toArray
        data.realTime = realTimeArray

        val realArray = if (data.real.length != realDataSteps) {
          new Array[Double](realDataSteps)
        } else {
          data.real
        }
        Array.copy(realData, fromStep, realArray, 0, realDataSteps)
        data.real = realArray
      }
    }
  }

  def exportHistory: List[HistoryPoint] = {
    (0 to currentStep).map { step =>
      HistoryPoint(
        TrainingTimeShift.fromStepAndResolution(step, resolution),
        expectedData(step),
        realData(step)
      )
    }.toList
  }
}
