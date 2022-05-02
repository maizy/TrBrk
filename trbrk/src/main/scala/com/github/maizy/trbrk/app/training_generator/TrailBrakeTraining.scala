package com.github.maizy.trbrk.app.training_generator
import com.github.maizy.trbrk.app.TrainingTimeShift

class TrailBrakeTraining(
    prepareMs: Int = 3000,
    topPressureMs: Int = 1500,
    topToBottomMs: Int = 2500,
    pauseMs: Int = 1000
) extends ExpectedDataGenerator {
  override def generate(step: Int, shift: TrainingTimeShift): Double = ???
}
