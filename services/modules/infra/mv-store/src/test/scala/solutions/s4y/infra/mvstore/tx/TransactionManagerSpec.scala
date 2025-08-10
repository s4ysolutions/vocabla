package solutions.s4y.infra.mvstore.tx

import org.h2.mvstore.MVStore
import org.h2.mvstore.tx.TransactionStore
import solutions.s4y.infra.mvstore.tx.TransactionManagerSpec.test
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{Spec, ZIOSpecDefault, assert}

object TransactionManagerSpec extends ZIOSpecDefault {
  override def spec = suite("TransactionManagerSpec")(
    test(
      "should commit a transaction for a map derived from a transaction and return result"
    ) {
      val zio = ZIO.attempt {
        val mvStore = new MVStore.Builder().open()
        val map0 = mvStore.openMap[String, String]("testMap")
        val transactionStore = new TransactionStore(mvStore)
        transactionStore.init()
        val transaction = transactionStore.begin()
        val map = transaction.openMap[String, String]("testMap")
        map.put("key", "value")
        transaction.commit()
        val value0 = "value" // TODO: !!!!
        val value1 = map.get("key")
        (value0, value1)
      }
      zio.map(result => assert(result)(equalTo(("value","value"))))
    },
    test(
      "should rollback a transaction for a map derived from a transaction and not persist changes"
    ) {
      val zio = ZIO.attempt {
        val mvStore = new MVStore.Builder().open()
        val transactionStore = new TransactionStore(mvStore)
        transactionStore.init()
        val transaction = transactionStore.begin()
        val map = transaction.openMap[String, String]("testMap")
        map.put("key", "value")
        transaction.rollback()
        map.get("key")
      }
      zio.map(result => assert(result)(equalTo(null)))
    },
    test(
      "should commit a transaction for a map derived from a store and return result"
    ) {
      val zio = ZIO.attempt {
        val mvStore = new MVStore.Builder().open()
        val transactionStore = new TransactionStore(mvStore)
        transactionStore.init()
        val transaction = transactionStore.begin()
        val map = mvStore.openMap[String, String]("testMap")
        map.put("key", "value")
        transaction.commit()
        map.get("key")
      }
      zio.map(result => assert(result)(equalTo("value")))
    },
    test(
      "should not rollback a transaction for a map derived from a store and not persist changes"
    ) {
      val zio = ZIO.attempt {
        val mvStore = new MVStore.Builder().open()
        val transactionStore = new TransactionStore(mvStore)
        transactionStore.init()
        val transaction = transactionStore.begin()
        val map = mvStore.openMap[String, String]("testMap")
        map.put("key", "value")
        transaction.rollback()
        map.get("key")
      }
      zio.map(result => assert(result)(equalTo("value")))
    },
    test(
      "should rollback a transaction for a map derived from a transaction beyond `begin` and not persist changes"
    ) {
      val zio = ZIO.attempt {
        val mvStore = new MVStore.Builder().open()
        val transactionStore = new TransactionStore(mvStore)
        transactionStore.init()
        val transaction = transactionStore.begin()
        val map = transaction.openMap[String, String]("testMap")
        map.put("key", "value")
        transaction.rollback()
        map.get("key")
      }
      zio.map(result => assert(result)(equalTo(null)))
    },/*
    test(
      "should commit nested transactions"
    ) {
      val zio = ZIO.attempt {
        val mvStore = new MVStore.Builder().open()
        val transactionStore = new TransactionStore(mvStore)
        transactionStore.init()
        val transaction1 = transactionStore.begin()
        val map1 = mvStore.openMap[String, String]("testMap")
        map1.put("key1", "value1")
        val transaction2 = transactionStore.begin()
        val map2 = transaction2.openMap[String, String]("testMap")
        map2.put("key2", "value2")
        transaction2.commit()
        transaction1.commit()
        val r1 = map1.get("key")
      }
      zio.map(result => assert(result)(equalTo("value")))
    })*/
  )
}
