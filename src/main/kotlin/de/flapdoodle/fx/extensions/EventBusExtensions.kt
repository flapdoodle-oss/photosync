package de.flapdoodle.fx.extensions

import javafx.scene.Node
import tornadofx.*
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

inline fun <reified T : FXEvent> Node.subscribeEvent(noinline action: EventContext.(T) -> Unit) {
  EventBusExtensions.subscribe(this, T::class, action)
}

fun Node.fire(event: FXEvent) {
  FX.eventbus.fire(event)
}

inline fun <reified T : FXEvent> UIComponent.subscribeEvent(noinline action: EventContext.(T) -> Unit) {
  EventBusExtensions.subscribe(this.root, T::class, action)
}

fun FXEvent.fire() {
  FX.eventbus.fire(this)
}

object EventBusExtensions {

  private data class ActionRef<T: FXEvent>(
      val action: EventContext.(T) -> Unit
  )

  fun <T: FXEvent> subscribe(node: Node, eventType: KClass<T>, action: EventContext.(T) -> Unit) {
    val actionRef = ActionRef(action)
    val weak = WeakReference(action)

    val delegate: EventContext.(T) -> Unit = {
      val weakAction = weak.get()
      if (weakAction!=null) {
        weakAction.invoke(this,it)
      } else {
        println("action reference was GC'd")
        this.unsubscribe()
      }
    }
    val oldRef = node.property.set(Key.ofType(eventType, ActionRef::class), actionRef)
    require(oldRef==null) { "action ref for $eventType already set to $oldRef"}

    @Suppress("UNCHECKED_CAST")
    val registration = FXEventRegistration(eventType, null, null, delegate as EventContext.(FXEvent) -> Unit)
    FX.eventbus.subscribe(eventType, FX.defaultScope, registration)
  }
}