package com.paddypower.destmgmt

import com.datastax.driver.core.querybuilder.{QueryBuilder, BuiltStatement}

trait CassandraBatch {
  type Statements[+A] = State[List[BuiltStatement], A]
  def add(bs:BuiltStatement): Statements[Int]
}

object CassandraBatchState extends CassandraBatch {

  override def add(bs: BuiltStatement): Statements[Int] = {
    State { s:List[BuiltStatement] =>
      val f = s.++(List(bs))
      (f, f.length)
    }
  }

  def dummy(id:Int):BuiltStatement = {
    QueryBuilder.insertInto("inboundmessages").value("eventid", id)
                                              .value("seqno", 1)
                                              .value("headers", "testing")
                                              .value("data", "testing")
                                              .value("received_timestamp", java.lang.Long.valueOf(System.currentTimeMillis))
                                              .value("version", 1)
  }
}