package com.github.maizy.trbrk.app.training_generator

import com.github.maizy.trbrk.app.TrainingTimeShift

trait ExpectedDataGenerator {
  def generate(step: Int, shift: TrainingTimeShift): Double
}
