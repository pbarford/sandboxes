package test

class StringOne extends Process[String, String] {
  override def on(msg: String) = {
    if(msg == "drop")
      discard(s"dropping : $msg")
    else
      keep(msg + ":StringOne")
  }
}
