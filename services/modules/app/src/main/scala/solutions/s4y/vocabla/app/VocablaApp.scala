package solutions.s4y.vocabla.app

import solutions.s4y.vocabla.app.ports.{CreateEntryCommand, CreateEntryUseCase}
import solutions.s4y.vocabla.app.repo.EntryRepository
import solutions.s4y.vocabla.app.repo.tx.TransactionManager
import solutions.s4y.vocabla.domain.Entry
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.IO

private final class VocablaApp(
    val transactionManager: TransactionManager,
    val entryRepository: EntryRepository
) extends CreateEntryUseCase:

  override def apply(
      command: CreateEntryCommand
  ): IO[String, Identifier[Entry]] =
    transactionManager.withTransactionZIO { tx =>
      entryRepository.createEntry(command.entry);
    }

end VocablaApp
