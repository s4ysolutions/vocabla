package solutions.s4y.vocabla.words.app.repo

/** A bit more clear and explicit variant of auto Conversion
  * @tparam DtoID
  *   DTO ID type to convert from
  * @tparam DomainID
  *   Domain ID type to convert to
  */
trait DtoIdToDomainId[DtoID, DomainID]:
  def toDomain(dtoId: DtoID): DomainID

object DtoIdToDomainId:
  extension [DtoID](dtoID: DtoID)
    def toDomain[DomainID](using
        conv: DtoIdToDomainId[DtoID, DomainID]
    ): DomainID =
      conv.toDomain(dtoID)

/*
given DtoIdToDomainId[Int, Int] with
  def toDomain(dtoId: Int): Int = dtoId

given DtoIdToDomainId[Long, Long] with
  def toDomain(dtoId: Long): Long = dtoId

given DtoIdToDomainId[String, String] with
  def toDomain(dtoId: String): String = dtoId
 */
