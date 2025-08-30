package solutions.s4y.infra.pgsql.architecture

import zio.{ULayer, ZLayer}

class DataSource:
  def getConnection: Connection = new Connection(Generator.generatorS.next)

object DataSource:
  val live: ULayer[DataSource] = ZLayer.succeed(new DataSource)
