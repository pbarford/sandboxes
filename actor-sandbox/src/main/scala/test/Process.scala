package test

trait AProcess [A, B] {
  def on(in:A): Either[String, B]

  def ->[C](process: AProcess[B, C]): AProcess[A, C] = {
    val chain = on _
    new AProcess[A, C] {
      override def on(in: A): Either[String, C] = (chain andThen {
        case Right(result) => process.on(result)
        case Left(error) => Left(error)
      })(in)
    }
  }

  def keep(result:B):Either[String,B] = Right(result)
  def discard(reason:String):Either[String,B] = Left(reason)
}
