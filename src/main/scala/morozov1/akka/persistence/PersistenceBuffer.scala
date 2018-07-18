package morozov1.akka.persistence

import java.nio.file.Path

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging

import scala.util.Try


class PersistenceBuffer(name: String, path: Path) extends GraphStage[FlowShape[ByteString, PersistentContainer]] with LazyLogging {

  type T = ByteString
  type S = PersistentContainer

  val queue = new LevelDbQueue(name, path)

  private val in = Inlet[T]("PersistentBuffer.in")
  private val out = Outlet[S]("PersistentBuffer.out")
  val shape: FlowShape[T, S] = FlowShape.of(in, out)

  protected var upstreamFailed = false
  protected var upstreamFinished = false

  def remove(idx: Long): Unit = queue.remove(idx)

  def close(): Unit = queue.close()

  def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    var downstreamWaiting = false

    override def preStart(): Unit = {
      // Start upstream demand
      pull(in)
    }

    override def postStop(): Unit = {
      Try { queue.close() }
    }

    setHandler(in, new InHandler {

      override def onPush(): Unit = {
        val element = grab(in)
        queue.offer(element.toArray)

        if (downstreamWaiting) {
          queue.poll().foreach { element ⇒
            push(out, elementOut(element))
            downstreamWaiting = false
          }
        }
        pull(in)
      }

      override def onUpstreamFinish(): Unit = {
        upstreamFinished = true
        queue.close()
        if (downstreamWaiting) {
          completeStage()
        }
      }

      override def onUpstreamFailure(ex: Throwable): Unit = {
        logger.error("Received upstream failure signal: " + ex)
        upstreamFailed = true
        queue.close()
        completeStage()
      }
    })

    setHandler(out, new OutHandler {

      override def onPull(): Unit = {
        queue.poll() match {
          case Some(element) ⇒
            push(out, elementOut(element))
          case None ⇒
            if (upstreamFinished) {
              completeStage()
            } else downstreamWaiting = true
        }
      }
    })
  }

  private def elementOut(element: S) = element
}
