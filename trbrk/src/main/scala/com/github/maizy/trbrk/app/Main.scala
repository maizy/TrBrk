package com.github.maizy.trbrk.app

import net.java.games.input.Event
import net.java.games.input.ControllerEnvironment

object Main {
  def main(args: Array[String]): Unit = {

    while (true) {
      /* Get the available controllers */
      val controllers = ControllerEnvironment.getDefaultEnvironment.getControllers
      if (controllers.isEmpty) {
        System.out.println("Found no controllers.")
        System.exit(0)
      }
      for (i <- 0 until controllers.length) {
        /* Remember to poll each one */
        controllers(i).poll
        /* Get the controllers event queue */
        val queue = controllers(i).getEventQueue
        /* Create an event object for the underlying plugin to populate */
        val event = new Event

        /* For each object in the queue */
        while (queue.getNextEvent(event)) {
          /*
          * Create a string buffer and put in it, the controller name,
          * the time stamp of the event, the name of the component
          * that changed and the new value.
          *
          * Note that the timestamp is a relative thing, not
          * absolute, we can tell what order events happened in
          * across controllers this way. We can not use it to tell
          * exactly *when* an event happened just the order.
          */
          val buffer = new StringBuffer(controllers(i).getName)
          buffer.append(" at ")
          buffer.append(event.getNanos).append(", ")
          val comp = event.getComponent
          buffer.append(comp.getName).append(" changed to ")
          val value = event.getValue
          /*
           * Check the type of the component and display an
           * appropriate value
           */
          if (comp.isAnalog) {
            buffer.append(value)
          }  else {
            if (value == 1.0f) {
              buffer.append("On")
            } else {
              buffer.append("Off")
            }
          }
          println(buffer.toString)
        }
      }
      /*
      * Sleep for 20 milliseconds, in here only so the example doesn't
      * thrash the system.
      */
      try {
        Thread.sleep(20)
      } catch {
        case e: InterruptedException =>
          e.printStackTrace()
      }
    }
  }
}
