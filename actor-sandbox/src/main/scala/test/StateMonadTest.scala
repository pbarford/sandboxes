package test

import test.StateMonadTest.StateCache

case class FollowerStats(username:String, numFollowers:Int, numFollowing:Int)
case class Timestamped[A](value: A, timestamp: Long)
case class Cache(stats: Map[String, Timestamped[FollowerStats]],
                 hits: Int,
                 misses: Int) {

  def get(username: String): Option[Timestamped[FollowerStats]] =
    stats.get(username)

  def update(u: String, s: Timestamped[FollowerStats]): Cache =
    Cache(stats + (u -> s), hits, misses)
}

trait State[S, +A] {
  def run(initial: S): (S, A)

  def map[B](f: A => B):State[S, B] = {
    State.apply { s =>
      val (s1, a) = run(s)
      (s1, f(a))
    }
  }
  def flatMap[B](f: A => State[S,B]):State[S, B] = {
    State.apply { s =>
      val (s1, a) = run(s)
      f(a).run(s1)
    }
  }
}

object State {
  def apply[S,A](f: S =>(S,A)): State[S,A] =
    new State[S,A] {
      def run(s: S) = f(s)
    }

  def state[S, A](a: A): State[S, A] =
    State(s => (s, a))
}

trait SocialService {
  def followerStats(u:String): StateCache[FollowerStats]
}

object StateFulSocialService extends SocialService {

  private def stale(ts: Long): Boolean = {
    System.currentTimeMillis - ts > (5 * 60 * 1000L)
  }

  def followerStats(u: String): StateCache[FollowerStats] = {
    for {
      os <- checkCache(u)
      s <- os match {
        case Some(fs) =>
          State { s:Cache => (s, fs) }
        case None =>
          retrieve(u)
      }
    } yield s
 }

  private def checkCache(u:String):StateCache[Option[FollowerStats]] = {
    State { c =>
      c.get(u) match {
        case Some(Timestamped(fs, ts))
          if (!stale(ts)) =>
          (c.copy(hits = c.hits + 1), Some(fs))
        case other =>
          (c.copy(misses = c.misses + 1), None)
      }
    }
  }
  private def retrieve(u:String):StateCache[FollowerStats] =
    State { c =>
      val fs = FollowerStats(u, 10, 10)
      val tfs = Timestamped(fs, System.currentTimeMillis())
      (c.update(u, tfs), fs)
    }
}


object StateMonadTest {
  type StateCache[+A] = State[Cache, A]

  def main(args: Array[String]) {
    val s = StateFulSocialService
    val v1 = s.followerStats("u1").run(Cache(Map.empty, 0, 0))
    println(s"v1 --> hits: ${v1._1.hits} misses : ${v1._1.misses}")
    val v2 = s.followerStats("u1").run(v1._1)
    println(s"v2 --> hits: ${v2._1.hits} misses : ${v2._1.misses}")
    val v3 = s.followerStats("u2").run(v2._1)
    println(s"v3 --> hits: ${v3._1.hits} misses : ${v3._1.misses}")
  }
}
