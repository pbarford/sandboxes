package com.pjb

package object sandbox {
    case class Msg(offset:Long, content:String)
    case class Ack(offset:Long)
}
