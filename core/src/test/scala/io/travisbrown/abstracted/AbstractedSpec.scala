package io.travisbrown.abstracted

import org.scalacheck.{ Arbitrary, Gen, Properties }
import org.scalacheck.Prop._
import org.scalatest.{ Matchers, FlatSpec }
import org.scalatest.prop.Checkers

class AbstractedSpec extends FlatSpec with Matchers with Checkers {
  val numbers = (1 to 10).toList
  val incremented = numbers.map(_ + 1)

  "The abstracted macro" should "provide enrichment methods" in {
    import scalaz.std.list._, scalaz.syntax.functor._

    val fromScalazFunctorOps: List[(Int, Int)] = numbers.abstracted.fpair

    fromScalazFunctorOps shouldBe numbers.map(i => (i, i))
  }

  it should "provide enrichment methods hidden by ordinary methods" in {
    val fromStdLibMonadOps: TraversableOnce[Int] = numbers.abstracted.map(_ + 1)

    fromStdLibMonadOps.toSeq should contain theSameElementsInOrderAs incremented
  }

  it should "hide ordinary methods not provided through enrichment" in {
    import scalaz.std.list._, scalaz.syntax.monad._

    "numbers.abstracted.size" shouldNot compile
  }

  it should "not provided methods that are neither ordinary nor enriched" in {
    import scalaz.std.list._, scalaz.syntax.monad._

    "numbers.abstracted.nonsense" shouldNot compile
  }

  it should "prefer methods from more specific enrichment classes" in {
    import scalaz.std.list._, scalaz.syntax.functor._

    val fromScalazFunctorOps: List[Int] = numbers.abstracted.map(_ + 1)

    fromScalazFunctorOps shouldBe incremented
  }

  it should "work with for-comprehensions" in {
    import scalaz.std.list._, scalaz.syntax.monad._

    val result = for {
      i <- numbers
      j <- numbers
    } yield i + j

    val fromScalazMonadOps: List[Int] = for {
      i <- numbers.abstracted
      j <- numbers.abstracted
    } yield i + j

    fromScalazMonadOps shouldBe result
  }

  it should "work with filtered for-comprehensions" in {
    /**
     * Note that we need `MonadPlusOps` in order to be able to filter. Otherwise
     * we would get the `withFilter` provided by the standard library's
     * `MonadOps`, which doesn't return the appropriate type.
     */
    import scalaz.std.list._, scalaz.syntax.monadPlus._

    val result = for {
      i <- numbers
      j <- numbers
      if i > 8
    } yield i + j

    val fromScalazMonadOps: List[Int] = for {
      i <- numbers.abstracted
      j <- numbers.abstracted
      if i > 8
    } yield i + j

    fromScalazMonadOps shouldBe result
  }

  it should "not compile if not all operations are available" in {
    /**
     * These imports provide `map` and `flatMap`, but not `withFilter`.
     */
    import scalaz.std.list._, scalaz.syntax.monad._

    """
      for {
        i <- numbers.abstracted
        j <- numbers.abstracted
        if i > 8
      } yield i + j
    """ shouldNot compile
  }
}
