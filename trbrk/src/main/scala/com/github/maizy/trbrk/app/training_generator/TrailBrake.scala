package com.github.maizy.trbrk.app.training_generator

class TrailBrake(
    override val resolution: Int,
    prepareMs: Int = 5000,
    topPressureMs: Int = 2000,
    topToBottomMs: Int = 3000,
    pauseMs: Int = 1000
) extends ExpectedDataGenerator with RepeatedShapeGenerator with DiscreteUtils {

  override protected def genShape(): Array[Double] = {
    val prepare = Array.fill(msToSteps(prepareMs))(0.0)
    val topPressure = Array.fill(msToSteps(topPressureMs))(100.0)

    val topToBottomSteps = msToSteps(topToBottomMs)
    val step = 100.0 / topToBottomSteps
    val topToBottom =  (0 until topToBottomSteps).map { i =>
      100.0 - i * step
    }.toArray

    val profile = prepare ++ topPressure ++ topToBottom
    val totalLength = msToSteps(prepareMs + topPressureMs + topToBottomMs + pauseMs)

    profile ++ Array.fill(totalLength - profile.length)(0.0)
  }
}
