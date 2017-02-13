package com.pjb

package object sandbox {
    case class Msg(offset:Long, content:String)
    case class Result(offset:Long, content:Seq[String])
}
