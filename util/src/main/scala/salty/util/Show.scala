package salty
package util

trait Show[T] { def apply(t: T): Show.Result }
object Show {
  sealed abstract class Result {
    def build = {
      val sb = new StringBuilder
      var indentation = 0
      def nl(res: Result) = {
        sb.append("\n")
        sb.append("  " * indentation)
        loop(res)
      }
      def loop(result: Result): Unit = result match {
        case None => ()
        case Str(value) => sb.append(value)
        case Sequence(xs @ _*) => xs.foreach(loop)
        case Repeat(xs, _, _, _) if xs.isEmpty => ()
        case Repeat(xs, sep, pre, post) =>
          loop(pre)
          xs.init.foreach { x =>
            loop(x)
            loop(sep)
          }
          loop(xs.last)
          loop(post)
        case Indent(res) =>
          indentation += 1
          nl(res)
          indentation -= 1
        case Newline(res) =>
          nl(res)
      }
      loop(this)
      sb.toString
    }
  }
  final case object None extends Result
  final case class Str(value: String) extends Result
  final case class Sequence(xs: Result*) extends Result
  final case class Repeat(xs: Seq[Result], sep: Result = None,
                          pre: Result = None, post: Result = None) extends Result
  final case class Indent(res: Result) extends Result
  final case class Newline(res: Result) extends Result

  def apply[T](f: T => Result): Show[T] =
    new Show[T] { def apply(input: T): Result = f(input) }

  implicit def showResult[R <: Result]: Show[R] = apply(identity)
  implicit def showString[T <: String]: Show[T] = apply(Show.Str(_))
  implicit def toShow[T: Show](t: T): Result =
    implicitly[Show[T]].apply(t)
  implicit def seqToShow[T: Show](ts: Seq[T]): Seq[Result] =
    ts.map { t => implicitly[Show[T]].apply(t) }
}
