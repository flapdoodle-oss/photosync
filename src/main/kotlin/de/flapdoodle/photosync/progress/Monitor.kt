package de.flapdoodle.photosync.progress

import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

object Monitor {

  private val threadReporter = ThreadLocal<Reporter>()
  private val state = ThreadLocal<StateHolder>()

  fun <T> execute(reporter: Reporter = DeferredReporter(ConsoleReporter()), action: () -> T): T {
    try {
      threadReporter.set(reporter)
      state.set(StateHolder())
      return action()
    } finally {
      reporter.report("")
      state.remove()
      threadReporter.remove()
      if (reporter is AutoCloseable) {
        reporter.close()
      }
    }
  }

  fun <T> scope(key: String, action: () -> T): T {
    val stateHolder = state.get()

    return if (stateHolder!=null) {
      val lastState = stateHolder.start(key)
      try {
        return action()
      } finally {
        stateHolder.end(lastState)
      }
    } else {
      action()
    }

  }

  fun message(message: String) {
    state.get()?.let {
      it.message(message)
      threadReporter.get()?.let { reporter ->
        reporter.report(it.asMessage())
      }
    }
  }

  data class State(
      val key: String,
      val map: Map<String,String> = emptyMap()
  ) {
    fun message(message: String): State {
      return copy(map = map + (key to message))
    }

    fun asMessage(): String {
      return map.map { "${it.key}: ${it.value}" }.joinToString(separator = " | ")
    }
  }

  class StateHolder() {
    private var currentState=State("")

    fun start(key: String): State {
      val ret = currentState
      currentState = currentState.copy(key = key)
      return ret
    }

    fun end(state: State) {
      this.currentState=state
    }

    fun message(message: String) {
      currentState=currentState.message(message)
    }

    fun asMessage(): String {
      return currentState.asMessage()
    }
  }

  interface Reporter {
    fun report(message: String)

    companion object {
      inline operator fun invoke(crossinline delegate: (String) -> Unit): Reporter {
        return object : Reporter {
          override fun report(message: String) {
            delegate(message)
          }
        }
      }
    }
  }

  class ConsoleReporter : Reporter {
    private var clearLastMessage = ""

    override fun report(message: String) {
      print(clearLastMessage)
      print("\r")
      print(message)
      print("\r")
      clearLastMessage = " ".repeat(message.length)
      if (message.isEmpty()) {
        System.out.flush()
      }
    }

  }

  class DeferredReporter(
    private val delegate: Reporter,
    private val interval: Duration = Duration.ofMillis(100)
  ): Reporter, AutoCloseable {

    init {
      require(interval.toMillis() >= 10) {"interval to small: $interval, should >= 10ms"}
    }

    val lastMessage = AtomicReference<String>()
    val stop=AtomicBoolean()
    var runningThread: Thread? = null

    override fun report(message: String) {
      startThread()
      lastMessage.set(message)
    }

    private fun startThread() {
      if (runningThread == null) {
        runningThread = thread(start = true) {
          do {
            pushMessage()
            Thread.sleep(interval.toMillis(), 0)
          } while (!stop.get())
        }
      }
    }

    private fun pushMessage() {
      val message = lastMessage.get()
      if (message!=null) {
        delegate.report(message)
      }
      lastMessage.set(null)
    }

    override fun close() {
      stop.set(true)
      runningThread?.join()
      pushMessage()
      runningThread=null
    }
  }
}