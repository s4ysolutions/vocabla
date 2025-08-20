package solutions.s4y.infra.pgsql.architecture

class Connection(val id: String) {
  def preparedStatement(sql: String): String =
    s"$id PreparedStatement for: $sql ${Generator.generator}"
  def close(): Unit = ()
  def setAutoCommit(autoCommit: Boolean): Unit = ()
  def commit(): Unit = ()
  def rollback(): Unit = ()
}
