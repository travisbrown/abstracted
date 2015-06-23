# Abstracted

[![Build status](https://img.shields.io/travis/travisbrown/abstracted/master.svg)](http://travis-ci.org/travisbrown/abstracted)

This is a small proof of concept that demonstrates how to implement a Scala
macro that allows us to "forget" all of a value's methods and only use
enrichment methods (which will usually be provided via a type class).

The idea behind `abstracted` was originally (as far as I know) suggested by
[Michael Pilquist](https://twitter.com/mpilquist) in the
[cats](https://github.com/non/cats) room on
[Gitter](https://gitter.im/non/cats?at=5565ecf27a71f1612c266c8d), although the
approach he suggests is different from the one I've used here.

## Simple example

As an example, suppose we've got a `Box` type:

```scala
case class Box[A](val a: A) {
  def map[B](f: A => B): Box[B] = {
    println("Box's map")
    Box(f(a))
  }

  def flatMap[B](f: A => Box[B]): Box[B] = {
    println("Box's flatMap")
    f(a)
  }
}
```

And a monad instance for it:

```scala
import cats.Monad

implicit val boxMonad: Monad[Box] = new Monad[Box] {
  override def map[A, B](fa: Box[A])(f: A => B): Box[B] = {
    println("Box's functor's map")
    Box(f(fa.a))
  }

  def flatMap[A, B](fa: Box[A])(f: A => Box[B]): Box[B] = {
    println("Box's monad's flatMap")
    f(fa.a)
  }

  def pure[A](a: A): Box[A] = Box(a)
}
```

Now if we use `Box` in a `for`-comprehension, for example, the monad instance
won't get used:

```scala
scala> import io.travisbrown.abstracted.demo._
import io.travisbrown.abstracted.demo._

scala> import cats.syntax.all._
import cats.syntax.all._

scala> for { foo <- Box("foo"); howMany <- Box(3) } yield foo * howMany
Box's flatMap
Box's map
res0: io.travisbrown.abstracted.demo.Box[String] = Box(foofoofoo)
```

Our `abstracted` macro allows us to change this:

```scala
scala> import io.travisbrown.abstracted._
import io.travisbrown.abstracted._

scala> for { foo <- Box("foo").abstracted; howMany <- Box(3) } yield foo * howMany
Box's monad's flatMap
Box's map
res1: io.travisbrown.abstracted.demo.Box[String] = Box(foofoofoo)
```

## Finagle services

I decided to take a stab at implementing `abstracted` tonight because of
[a conversation about how Finagle services compose](https://github.com/twitter/finagle/issues/385)
this afternoon. Finagle services are morally more or less Kleisli arrows over
Twitter futures, but for whatever reason `Service` extends `I => Future[O]`,
which means that they have totally useless `compose` and `andThen` methods. In
another [project](https://github.com/travisbrown/catbird) I provide category
and profunctor instances for `Service`, but the `compose` and `andThen`
enrichment methods provided by cats for things with `Compose` instances are
blocked by the stupid methods that `Service` inherits from `Function1`.

For example, if we've got these services:

```scala
import cats.arrow.Compose, cats.syntax.all._
import com.twitter.util.Future
import com.twitter.finagle.Service
import io.catbird.finagle._

val is = Service.mk[Int, String](i => Future.value(i.toString))
val si = Service.mk[String, Int](s => Future(s.toInt))
```

We get an error when we try to compose them:

```scala
scala> val ss = si andThen is
<console>:22: error: type mismatch;
 found   : com.twitter.finagle.Service[Int,String]
 required: com.twitter.util.Future[Int] => ?
              si andThen is
                         ^
```

Our `abstracted` macro fixes this problem:

```scala
scala> import io.travisbrown.abstracted._
import io.travisbrown.abstracted._

scala> val ss = si.abstracted andThen is
ss: com.twitter.finagle.Service[String,String] = <function1>
```

## How it works

The implementation is pretty straightforward. First we've got an implicit class
that provides a `def abstracted: Empty[A]` method for any `A`, where our `Empty`
type is a case class that wraps an `A` and provides access to the wrapped value,
but doesn't have any other methods.

We also have a `Converter[A, B]` type that represents a conversion from
`Empty[A]` to `B` (I ran into problems trying to use `Empty[A] => B` directly),
and an implicit method that will apply the conversion automatically to any
`Empty[A]` for any appropriately-typed `Converter` instance.

The interesting part is how we make `Converter` instances. We use the Scala
macro system's [fundep materialization](http://docs.scala-lang.org/overviews/macros/implicits.html), which allows us to determine in the body of the macro what the output
type of the `Converter` will be. We look at the open implicits and find one that
looks like the compiler is fishing for a `WhateverOps` enrichment class for our
`Empty[A]`. We then ask for a view from `A` (our real type) to the target of
that view. We read the return type off the view from `A`, and from there the
implementation is pretty trivial.

## Status

It seems like it works. The examples above can be run by opening up a REPL with
`sbt demo/console`. If other people think it looks useful I guess it could end
up in cats, although there's nothing cats-specific about the macro itself or the
surrounding machinery.

