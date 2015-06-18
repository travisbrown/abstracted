package io.travisbrown.abstracted.demo

import cats.Monad

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

object Box {
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
}
