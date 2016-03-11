package test

import java.sql.{DriverManager, ResultSet, Connection}

object DB {

  case class DB[A](x: Connection => A) {
    def apply(c:Connection) = x(c)
    def map[B](f: A => B): DB[B] = {
      new DB(c => f(x(c)))
    }

    def flatMap[B](f: A => DB[B]):DB[B] = {
      new DB(c => f(x(c))(c))
    }
  }

  def pure[A](a: A):DB[A] = DB(c => a)

  implicit def db[A](f: Connection => A):DB[A] = DB(f)

  def setUserPwd(userId:String,
                 pwd:String):Connection => Unit = {
    c => {
      val stmt = c.prepareStatement("update users set pwd = ? where user_id = ?")
      stmt.setString(1, pwd)
      stmt.setString(2, userId)
      stmt.executeUpdate()
      stmt.close()
    }
  }

  def printPwd(userId:String):Connection => Unit = {
    c => {
      val p = getUserPwd(userId)(c)
      println(s"pass = $p")
    }
  }

  def getUserPwd(userId:String):Connection => String = {
    c => {
      val stmt = c.prepareStatement("select pwd from users where user_id = ?")
      stmt.setString(1, userId)
      val res:ResultSet = stmt.executeQuery()
      val pwd = res.next() match {
        case true => res.getString("pwd")
        case false => ""
      }
      stmt.close()
      pwd
    }
  }

  def changePwd(userId:String,
                oldPwd:String,
                newPwd:String):DB[Boolean] =
    for {
      pwd <- getUserPwd(userId)
      eq <- if (pwd == oldPwd)
              for {
                  _ <- setUserPwd(userId, newPwd)
              } yield true
            else pure(false)
    } yield eq


  abstract class ConnProvider {
    def apply[A](f: DB[A]): A
  }

  def mkProvider(driver:String, url:String) = new ConnProvider {
    override def apply[A](f: DB[A]): A = {
      Class.forName(driver)
      val conn = DriverManager.getConnection(url)
      try {
        println("init")
        conn.createStatement().execute("CREATE TABLE users (user_id varchar(255), pwd varchar(255));")
        conn.createStatement().execute("INSERT into users values('paulo', 'test');")
        f(conn)
      } finally {
        conn.close()
      }
    }
  }

  lazy val sqliteTestDB = mkProvider("org.sqlite.JDBC", "jdbc:sqlite::memory:")

  def doDbStuff(userId:String): ConnProvider => Unit = {
    val stuffToDo = for {
      more1 <- printPwd(userId)
      cool <- changePwd(userId, "test", "foo1")
      more2 <- printPwd(userId)
    } yield List(more1, cool, more2)

    println(stuffToDo)
    conn => {
      println("start 1")
      conn(stuffToDo)
      println("start 2")
      conn(stuffToDo)
    }

  }

  def runIntTest[A](f: ConnProvider => A): A = f(sqliteTestDB)

  def main(args: Array[String]) {
    runIntTest(doDbStuff("paulo"))
  }
}
