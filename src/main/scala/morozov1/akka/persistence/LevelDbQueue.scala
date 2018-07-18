package morozov1.akka.persistence

import java.nio.file.Path
import com.google.common.primitives.Longs
import com.typesafe.scalalogging.LazyLogging
import org.iq80.leveldb.Options
import org.iq80.leveldb.impl.Iq80DBFactory.factory


class LevelDbQueue(name: String, path: Path) extends LazyLogging {

  path.toFile.getParentFile.mkdirs

  private val TAIL = "tail"
  private val HEAD = "head"

  private val options = new Options()

  private val db = factory.open(path.toFile, options)

  private def getKey(name: String): Long = {
    Option(db.get(name.getBytes)).map(v ⇒ Longs.fromByteArray(v)).getOrElse {
      db.put(name.getBytes, Longs.toByteArray(0))
      0
    }
  }

  private def setKey(name: String, v: Long): Unit = {
    db.put(name.getBytes, Longs.toByteArray(v))
  }

  private def headKey(): Long = getKey(HEAD)

  private def tailKey(): Long = getKey(TAIL)

  @volatile private var head = headKey()

  @volatile private var tail = tailKey()

  def offer(data: Array[Byte]): Unit = synchronized {
    db.put(Longs.toByteArray(head), data)
    head = if(head == Long.MaxValue) {
      logger.error(s"Queue db $name max key reached!")
      0
    } else { head + 1}
    setKey(HEAD, head )
  }

  def poll(): Option[PersistentContainer] = synchronized {
    if(tail < head || tail > head) {
      val dataOpt = Option(db.get(Longs.toByteArray(tail)))
      val result = dataOpt.map(data ⇒ PersistentContainer(tail, data))
      tail = if(tail == Long.MaxValue) { 0 } else { tail + 1 }
      setKey(TAIL, tail)
      result
    } else {
      None
    }
  }

  def remove(idx: Long): Unit = {
    db.delete(Longs.toByteArray(idx))
  }

  def close(): Unit = {
    db.close()
  }
}
