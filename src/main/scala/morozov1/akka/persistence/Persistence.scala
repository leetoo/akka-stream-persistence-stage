package morozov1.akka.persistence

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.util.ByteString



object Persistence {

  def buffer(basePath: String, persistenceId: String)(implicit system: ActorSystem): PersistenceBuffer = {
    val path = Paths.get(s"$basePath/persistence/$persistenceId")
    val buffer = new PersistenceBuffer(persistenceId, path)
    buffer
  }
}
