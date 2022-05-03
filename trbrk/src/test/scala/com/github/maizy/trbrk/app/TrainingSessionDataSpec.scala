package com.github.maizy.trbrk.app

import com.github.maizy.trbrk.app.training_generator.ExpectedDataGenerator
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TrainingSessionDataSpec extends AnyFlatSpec with Matchers {

  trait TestSessionData {
    val testResolution = 10
    val testBufferMs = 200
    val testAheadOfTimeBuffer = 100

    val testGenerator = new ExpectedDataGenerator {
      override val resolution: Int = testResolution

      override def generate(step: Int, shift: TrainingTimeShift): Double = {
        step.toDouble * resolution
      }
    }

    val testTrainingSessionData = new TrainingSessionData(
      testResolution, testGenerator,
      bufferLengthMs = testBufferMs, aheadOfTimeBufferMs = testAheadOfTimeBuffer
    )
  }

  behavior of "TrainingSessionData"

  it should "do proper init" in new TestSessionData {
    testTrainingSessionData.length shouldBe 0
    testTrainingSessionData.isEmpty shouldBe true
    testTrainingSessionData.lastShift shouldBe 'empty
    testTrainingSessionData.lastStep shouldBe 'empty
    testTrainingSessionData.exportHistory shouldBe 'empty
  }

  it should "add real data for first point" in new TestSessionData {
    testTrainingSessionData.addRealValue(TrainingTimeShift(0), 55.0)

    testTrainingSessionData.length shouldBe 1
    testTrainingSessionData.isEmpty shouldBe false
    testTrainingSessionData.lastShift shouldBe Some(TrainingTimeShift(0))
    testTrainingSessionData.lastStep shouldBe Some(0)
    testTrainingSessionData.exportHistory shouldBe List(
      HistoryPoint(TrainingTimeShift(0), expected = 0.0, real = 55.0)
    )
  }

  it should "add real data outside of init buffer" in new TestSessionData {
    for (step <- 0 until 20) {
      val shift = TrainingTimeShift.fromStepAndResolution(step, testResolution)
      testTrainingSessionData.addRealValue(shift, (step * 2).toDouble)
    }
    private val preLastShift = TrainingTimeShift.fromStepAndResolution(20, testResolution)
    testTrainingSessionData.addRealValue(
      preLastShift,
      99.9
    )
    private val lastShift = TrainingTimeShift.fromStepAndResolution(21, testResolution)
    testTrainingSessionData.addRealValue(
      lastShift,
      100.0
    )

    testTrainingSessionData.length shouldBe 22
    testTrainingSessionData.lastStep shouldBe Some(21)
    testTrainingSessionData.lastShift shouldBe Some(lastShift)

    val expectedHistory = (0 until 20).toList.map { step =>
      HistoryPoint(
        TrainingTimeShift.fromStepAndResolution(step, testResolution),
        step.toDouble * testResolution,
        (step * 2).toDouble
      )
    } ::: List(
      HistoryPoint(preLastShift, 20.0 * testResolution, 99.9),
      HistoryPoint(lastShift, 21.0 * testResolution, 100.0),
    )

    testTrainingSessionData.exportHistory shouldBe expectedHistory
  }
}
