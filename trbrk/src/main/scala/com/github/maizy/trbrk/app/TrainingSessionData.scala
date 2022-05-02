package com.github.maizy.trbrk.app

import com.github.maizy.trbrk.app.training_generator.ExpectedDataGenerator


class TrainingSessionData(resolution: Int, expectedDataGenerator: ExpectedDataGenerator) {

  private val minBuffer = 60 * 1000 / resolution

  private var timeline = Array.fill(minBuffer)(0.0)
  private var timelineUntil = 0
  fillTimeline(0, minBuffer)

  private var expectedData = Array.fill(minBuffer)(0.0)
  private var expectedUntil = 0
  fillExpectedData(0, minBuffer)

  private var realData = Array.fill(minBuffer)(0.0)
  private var currentShift = TrainingTimeShift(0)
  private var currentStep = 0

  def addRealValue(shift: TrainingTimeShift, value: Double): Unit = {
    val correctedShift = shift.roundToResolution(resolution)
    val step = correctedShift.toStep(resolution)
    if (step == currentStep + 1) {
      currentStep = step
      currentShift = correctedShift
      realData = expandAndSet(realData, step, value)
    } else if (step <= currentStep) {
      realData(step) = value
    } else {
      // fill missed values with zeros
      for (missedStep <- currentStep until step) {
        val shift = TrainingTimeShift.fromStepAndResolution(missedStep, resolution)
        addRealValue(shift, 0.0)
      }
      addRealValue(correctedShift, value)
    }

    if (currentStep + (minBuffer / 2) > expectedUntil) {
      fillExpectedData(expectedUntil, expectedUntil + minBuffer / 2)
    }

    if (currentStep + (minBuffer / 2) > timelineUntil) {
      fillTimeline(timelineUntil, timelineUntil + minBuffer / 2)
    }
  }

  private def fillExpectedData(fromStep: Int, untilStep: Int): Unit = {
    (fromStep until untilStep).foreach { step =>
      val time = TrainingTimeShift.fromStepAndResolution(step, resolution)
      val expected = expectedDataGenerator.generate(step, time)
      expectedData = expandAndSet(expectedData, step, expected)
    }
    expectedUntil = untilStep
  }

  private def fillTimeline(fromStep: Int, untilStep: Int): Unit = {
    (fromStep until untilStep).foreach { step =>
      timeline = expandAndSet(timeline, step, (step * resolution).toDouble)
    }
    timelineUntil = untilStep
  }

  private def expandAndSet(array: Array[Double], step: Int, value: Double): Array[Double] = {
    if (step > array.length - 1) {
      val newArray = new Array[Double](array.length + minBuffer / 2)
      Array.copy(array, 0, newArray, 0, array.length)
      newArray(step) = value
      newArray
    } else {
      array(step) = value
      array
    }
  }

  def shift: TrainingTimeShift = currentShift
}
