package com.github.maizy.trbrk.app.training_generator
import com.github.maizy.trbrk.app.TrainingTimeShift

trait RepeatedShapeGenerator extends ExpectedDataGenerator {

  protected def genShape(): Array[Double]

  private var shape = Array.empty[Double]

  override def init(): Unit = {
    shape = genShape()
  }

  override def generate(step: Int, shift: TrainingTimeShift): Double = {
    val shapeStep = step % shape.length
    shape(shapeStep)
  }
}
