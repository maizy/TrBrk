package com.github.maizy.trbrk.app

import com.github.strikerx3.jxinput.XInputDevice14
import com.github.strikerx3.jxinput.enums.XInputDeviceType

object Main {
  def main(args: Array[String]): Unit = {

    val firstGamepad = XInputDevice14.getAllDevices.find { device =>
      val capabilities = device.getCapabilities
      if (capabilities != null) {
        device.isConnected && capabilities.getType == XInputDeviceType.GAMEPAD
      } else {
        false
      }
    }

    if (firstGamepad.isEmpty) {
      println("no gamepad")
      sys.exit(1)
    }

    firstGamepad.foreach { device =>
      try {
        while (device.poll()) {
          val components = device.getComponents
          if (components != null) {
            val axes = components.getAxes
            if (axes != null) {
              print(s"lt: ${axes.lt} ")
              print(s"lx: ${axes.lx } ")
              print(s"ly: ${axes.ly} ")
              print(s"rt: ${axes.rt} ")
              print(s"rx: ${axes.rx} ")
              print(s"ry: ${axes.ry} ")
              print(s"dpad: ${axes.dpad}")
              println()
            }
          }
          Thread.sleep(500)
        }
      } catch {
        case _: InterruptedException =>
          sys.exit(0)
      }
    }
//    for (device <- devices) {
//      println(device)
//      println(s"is connected: ${device.isConnected}")
//      val capabilities = device.getCapabilities
//      if (capabilities != null) {
//        println(s"type: ${capabilities.getType}")
//        println(s"subtype: ${capabilities.getSubType}")
//        println(s"wireless: ${capabilities.isWireless}")
//        println(s"ff: ${capabilities.isForceFeedbackSupported}")
//      }
//
//      val components = device.getComponents
//
//      if (components != null) {
//        val axes = components.getAxes
//        if (axes != null) {
//          println(s"lt: ${axes.lt}")
//          println(s"lx: ${axes.lx}")
//          println(s"ly: ${axes.ly}")
//          println(s"rt: ${axes.rt}")
//          println(s"rx: ${axes.rx}")
//          println(s"ry: ${axes.ry}")
//          println(s"dpad: ${axes.dpad}")
//        }
//      }
//      println()
//    }
  }
}
