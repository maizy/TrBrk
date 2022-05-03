package com.github.maizy.trbrk.app.training_generator

import com.github.maizy.trbrk.app.TrainingTimeShift

trait ExpectedDataGenerator {
  val resolution: Int
  def init(): Unit = {}
  def generate(step: Int, shift: TrainingTimeShift): Double
}

trait DiscreteUtils extends ExpectedDataGenerator {
  protected def msToSteps(ms: Int): Int = ms / resolution
}
