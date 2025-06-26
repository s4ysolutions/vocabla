package solutions.s4y.vocabla.infrastructure.kv

import zio.Task
import zio.stream.ZStream

trait KeyValue[Key, E] {
  def put(key: Key, e: E): Task[Key]
  def get(key: Key): Task[Option[E]]
  def delete(key: Key): Task[Unit]
  // def stream: ZStream[Any, Throwable, E]
}
