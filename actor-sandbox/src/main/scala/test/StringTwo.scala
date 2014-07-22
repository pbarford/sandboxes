package test

class StringTwo extends Process[String, String] {
  override def on(msg: String) = {
    if(msg == "drop")
      discard("dropping : $msg")
    else
      keep(msg + ":StringTwo")
  }
}


