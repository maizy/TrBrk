package com.github.maizy.trbrk.app

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TrainingTimeShiftSpec extends AnyFlatSpec with Matchers {
  "TrainingTimeShift" should "round time to resolution" in {
    TrainingTimeShift(0).roundToResolution(5) shouldBe TrainingTimeShift(0)
    TrainingTimeShift(7).roundToResolution(3) shouldBe TrainingTimeShift(6)
    TrainingTimeShift(9).roundToResolution(2) shouldBe TrainingTimeShift(8)
    TrainingTimeShift(10).roundToResolution(5) shouldBe TrainingTimeShift(10)
  }

  it should "convert to step" in {
    TrainingTimeShift(100).toStep(20) shouldBe 5
    TrainingTimeShift(111).toStep(20) shouldBe 5
    TrainingTimeShift(119).toStep(20) shouldBe 5
    TrainingTimeShift(120).toStep(20) shouldBe 6
    TrainingTimeShift(0).toStep(20) shouldBe 0
    TrainingTimeShift(5).toStep(20) shouldBe 0
  }
}
