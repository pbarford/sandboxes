package test

import scalaz.Reader
/*
case class User(id:Int,
                firstName:String,
                lastName:String,
                email:String,
                supervisorId:Int)

trait UserRepository {
  def get(id:Int):User
  def find(name:String):User
}

trait Users {
  import scalaz.Reader

  def getUser(id: Int) = Reader((userRepository: UserRepository) =>
    userRepository.get(id)
  )

  def findUser(username: String) = Reader((userRepository: UserRepository) =>
    userRepository.find(username)
  )
}

object UserInfo extends Users {

  def userEmail(id: Int) = {
    getUser(id) map (_.email)
  }

  def userInfo(username: String) =
    for {
      user <- findUser(username)
      boss <- getUser(user.supervisorId)
    } yield Map(
      "fullName" -> s"${user.firstName} ${user.lastName}",
      "email" -> s"${user.email}",
      "boss" -> s"${boss.firstName} ${boss.lastName}"
    )
}

object UserRepositoryImpl extends UserRepository {
  override def get(id: Int): User = User(id, "test", "tester", "test.tester@test.com", id + 2)

  override def find(name: String): User = User(10, name, "tester", s"$name.tester@test.com", 12)
}

trait TestUserRepository extends UserRepository {
  override def get(id: Int): User = User(id, "big", "tester", "big.tester@test.com", id + 2)

  override def find(name: String): User = User(10, name, "tester", s"$name.tester@test.com", 12)
}

object TestApplication extends Application(UserRepositoryImpl)

class Application(userRepository: UserRepository) extends Users {
  def userEmail(id: Int) = {
    run(UserInfo.userEmail(id))
  }

  def userInfo(username: String) =  {
    run(UserInfo.userInfo(username))
  }

  private def run[A](reader: Reader[UserRepository, A]): String = {
    String.valueOf(reader(userRepository))
  }
}

trait Application2 extends Users with TestUserRepository {
  this: UserRepository =>
  def userEmail(id: Int) = {
    run(UserInfo.userEmail(id))
  }

  def userInfo(username: String) =  {
    run(UserInfo.userInfo(username))
  }

  private def run[A](reader: Reader[UserRepository, A]): String = {
    String.valueOf(reader(this))
  }
}

object DITest extends Application2 {

  def main(args: Array[String]) {

    println(TestApplication.userEmail(12))
    println(TestApplication.userInfo("paulo"))

    println(userEmail(34))
    println(userInfo("alexandra"))

    /*
    val triple = Reader((i: Int) => i * 3)
    println(triple(3))   // => 9
    val thricePlus2 = triple map (i => i + 2)
    println(thricePlus2(3))  // => 11
    val f = for (i <- thricePlus2) yield i.toString
    println(f(3))
    */
  }


}
  */
