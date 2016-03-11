package test

class StringTwo extends AProcess[String, String] {
  override def on(msg: String) = {
    if(msg == "drop")
      discard("dropping : $msg")
    else
      keep(msg + ":StringTwo")
  }
}


