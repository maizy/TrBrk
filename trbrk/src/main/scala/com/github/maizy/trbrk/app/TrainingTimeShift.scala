package com.github.maizy.trbrk.app

case class TrainingTimeShift(ms: Int) {
  require(ms >= 0, "time shift should be 0 or positive")

  def roundToResolution(resolution: Int): TrainingTimeShift =
    TrainingTimeShift(ms - ms % resolution)

  def toStep(resolution: Int): Int = {
    ms / resolution
  }

  def +(value: Int): TrainingTimeShift = TrainingTimeShift(ms + value)
}

object TrainingTimeShift {
  def fromStepAndResolution(step: Int, resolution: Int): TrainingTimeShift = {
    TrainingTimeShift(step * resolution)
  }
}
