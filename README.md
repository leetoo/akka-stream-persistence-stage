Persistence stage for Akka streams with LevelDB on backend.

Usage example:
```

val persistenceId = UUID.randomUUID().toString
val buffer = Persistence.buffer("storage", persistenceId)

Source.actorRef[ByteString](bufferSize, overflowStrategy)
    .via(buffer.async)
    .map(....)
    .to {Sink.foreach { buffer.commitIdx(idx) }}
    .run()
```