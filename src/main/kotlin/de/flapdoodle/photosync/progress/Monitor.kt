package de.flapdoodle.photosync.progress

object Monitor {

  private val threadReporter = ThreadLocal<Reporter>()
  private val scopes = ThreadLocal<Scopes>()

  fun <T> execute(reporter: Reporter = ConsoleReporter(), action: () -> T): T {
    try {
      threadReporter.set(reporter)
      scopes.set(Scopes())
      return action()
    } finally {
      scopes.remove()
      threadReporter.remove()
    }
  }

  fun <T> scope(key: String, action: () -> T): T {
    val currentScopes = scopes.get()

    try {
      currentScopes?.let {
        it.open(key)
      }
      return action()
    } finally {
      currentScopes?.let {
        it.close(key)
      }
    }
  }

  fun message(message: String) {
    scopes.get()?.let {
      it.message(message)
      threadReporter.get()?.let { reporter ->
        reporter.report(it.asMessage())
      }
    }
  }


  class Scopes() {
    private var messages = emptyMap<String, String>()
    private var currentScope: String? = null

    fun open(key: String) {

    }

    fun close(key: String) {

    }

    fun message(message: String) {

    }

    fun asMessage(): String {
      return "test"
    }
  }

  interface Reporter {
    fun report(message: String)
  }

  class ConsoleReporter : Reporter {
    private var clearLastMessage = ""

    override fun report(message: String) {
      print(clearLastMessage)
      print("\r")
      print(message)
      print("\r")
      clearLastMessage = " ".repeat(message.length)
    }

  }

  @Deprecated("remove")
  fun report(key: String, message: String?) {
  }

  @Deprecated("remove")
  fun reset() {
  }


}