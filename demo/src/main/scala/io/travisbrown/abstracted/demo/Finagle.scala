package io.travisbrown.abstracted.demo

import cats.syntax.compose._
import com.twitter.util.Future
import com.twitter.finagle.Service
import io.catbird.finagle._
import io.travisbrown.abstracted._

object Finagle {
  val i2s = Service.mk[Int, String](i => Future.value(i.toString))
  val s2i = Service.mk[String, Int](s => Future(s.toInt))

  val s2s = s2i.abstracted.andThen(i2s)
}
