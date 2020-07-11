package de.flapdoodle.fx.layout.splitpane

import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.geometry.HPos
import javafx.geometry.Point2D
import javafx.geometry.VPos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.SkinBase
import javafx.scene.input.MouseDragEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import tornadofx.*
import java.lang.Double.max
import java.lang.Double.min

class BetterSplitPane(

) : Control() {

  private val nodes = FXCollections.observableArrayList<Node>()
  private val skin = Skin(this)

  init {
//    style {
//      borderColor += box(Color.GREEN)
//      borderWidth += box(1.px)
//    }
    addClass(Style.betterSplitPane)
  }

  override fun getUserAgentStylesheet() = Style().base64URL.toExternalForm()

  override fun createDefaultSkin() = skin

  fun add(node: Node) {
    nodes.add(node)
  }

  fun nodes() = nodes

  class Skin(
      private val control: BetterSplitPane
  ) : SkinBase<BetterSplitPane>(control) {

    private val handles = FXCollections.observableArrayList<SplitHandle>()

    init {
      control.nodes.addListener(ListChangeListener {
        println("changed: $it")
        handles.setAll(control.nodes.map {
          SplitHandle(this, it)
        })

        children.setAll(
            control.nodes + handles
        )
        control.requestLayout()
      })

      if (true) {
        var dragStarted: DragStart? = null
        control.addEventFilter(MouseEvent.MOUSE_PRESSED) { event ->
//          println("mouse clicked in parent")
          val target = event.target
          if (target is SplitHandle && target.parentSkin == this) {
//            println("start")
            dragStarted = DragStart(Point2D(event.x, event.y), target)

            event.isDragDetect = true
            event.consume()
          } else {
//            println("skip ${event.target}")
          }
        }

        control.addEventFilter(MouseEvent.DRAG_DETECTED) { event ->
//        dragStarted = dragStarted?.copy(pos = Point2D(event.x, event.y))
          control.startFullDrag()
          event.consume()
        }

        control.addEventFilter(MouseDragEvent.MOUSE_DRAGGED) { event ->
          if (dragStarted != null) {
            val start = dragStarted
            require(start != null) { "drag not started.." }
            val current = Point2D(event.x, event.y)

            val localStart = control.sceneToLocal(start.pos)
            val localCurrent = control.sceneToLocal(current)

            val diff = current - start.pos

//            println("${start.handle.node}: from $dragStarted to $current -> $diff (local)")


//            if (diff.x > 20.0 || diff.x < -20.0) {
//              println("###################################")
//              println("# WHAT                            #")
//              println("###################################")
//            }
            start.handle.prefWidthOffset(control.height, start.currentWith + diff.x)
            start.handle.requestLayout()
            //        //node.prefWidth = node.layoutBounds.width + diff.x
            event.consume()
          }
        }
        control.addEventFilter(MouseEvent.MOUSE_RELEASED) { event ->
          if (dragStarted != null) {
            dragStarted = null
            event.consume()
          }
        }
      }
    }

    override fun computePrefWidth(height: Double, topInset: Double, rightInset: Double, bottomInset: Double, leftInset: Double): Double {
//      super.computePrefWidth(height,topInset,rightInset,bottomInset,leftInset)
      val ret = handles.filter { it.isNodeManaged() }
          .map { it.prefWidthOfNode(height) }
          .sumByDouble { it -> snapSizeX(it) }

//      println("pref width -> $ret")
      return ret
    }

    override fun layoutChildren(contentX: Double, contentY: Double, contentWidth: Double, contentHeight: Double) {
      val managedChildren = handles.filter { it.isNodeManaged() }
      val nodeWidthMap = managedChildren.map {
        it to it.prefWidthOfNode(contentHeight)
      }

//      println("-------------------------")
      var currentX = contentX
//      println("start $currentX")
      nodeWidthMap.forEach { (handle, w) ->
//        println("layout ${handle.node} --> $w")
        layoutInArea(handle.node, currentX, contentY, w, contentHeight, -1.0, HPos.CENTER, VPos.CENTER)
        currentX = currentX + w
//        println("... $currentX")
      }
//      println("-------------------------")

      currentX = contentX
      nodeWidthMap.forEach { (it, w) ->
        val pos = currentX + w - it.prefWidth / 2.0
//        println("${it.node} - w=${it.node.layoutBounds.width}, x=${it.node.layoutX} - ${it.changedPrefWidth}")
        layoutInArea(it, pos, contentY, it.prefWidth, contentHeight, -1.0, HPos.CENTER, VPos.CENTER)
        currentX = currentX + w
      }
    }
  }

  private data class DragStart(
          val pos: Point2D,
          val handle: SplitHandle,
          val currentWith: Double = handle.prefWidthOffset()
  )

  private class SplitHandle(
          internal val parentSkin: BetterSplitPane.Skin,
          internal val node: Node
  ) : Control() {
    private val skin = Skin(this)
    var changedPrefWidth: Double? = null

    init {
      isFocusTraversable = false
      addClass(Style.betterSplitPaneHandle)
    }

    override fun createDefaultSkin() = skin

    fun prefWidthOffset(currentHeight: Double, offset: Double) {
      val pref = node.prefWidth(currentHeight)
      val min = node.minWidth(currentHeight)
      val max = node.maxWidth(currentHeight)

      val w = pref + offset
      changedPrefWidth = when  {
        (w < min) -> min - pref
        (w > max) -> max - pref
        else -> offset
      }
    }

    fun prefWidthOffset(): Double {
      return changedPrefWidth ?: 0.0
    }

    fun prefWidthOfNode(height: Double): Double {
      val min = node.minWidth(height)
      val max = node.maxWidth(height)

      val ret = node.prefWidth(height) + (changedPrefWidth ?: 0.0)
      return min(max(min, ret), max)
    }

    fun isNodeManaged(): Boolean {
      return node.isManaged
    }


    internal class Skin(control: SplitHandle) : SkinBase<SplitHandle>(control) {
      init {
        children.add(StackPane().apply {
          isMouseTransparent = true
          addClass(Style.stackPane)
//          minWidth = 1.0
//          style {
//            backgroundColor += Color.BLACK
//          }
        })

//        control.style {
//          borderWidth += box(1.px)
//          borderColor += box(Color.RED)
//          padding = box(0.px, 5.px)
//          cursor = Cursor.H_RESIZE
//        }
      }
    }
  }

  class Style : Stylesheet() {

    companion object {
      val betterSplitPane by cssclass()
      val betterSplitPaneHandle by cssclass()
      val stackPane by cssclass()
    }

    init {
      betterSplitPane {
        backgroundColor += Color.valueOf("#f4f4f4")
      }

      betterSplitPaneHandle {
        padding = box(0.px, 5.px)
        cursor = Cursor.H_RESIZE

        child(stackPane) {
          minWidth = 1.0.px
          backgroundColor += LinearGradient(0.0, 0.0, 0.0, 1.0, true,
              CycleMethod.NO_CYCLE,
              Stop(0.0, Color(0.0,0.0,0.0,0.05)),
              Stop(0.5, Color(0.0,0.0,0.0,0.2)),
              Stop(1.0, Color(0.0,0.0,0.0,0.05))
          )
        }
      }

      if (false) {
        println("-----------------------------")
        println(this.render())
        println("-----------------------------")
      }
    }

  }
}