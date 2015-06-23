package io.travisbrown.abstracted

import scala.language.experimental.macros

/**
 * A conversion from a wrapped value to some other type.
 */
trait Converter[A, B] {
  def apply(a: Empty[A]): B
}

private[abstracted] object Converter extends internal.MacrosCompat {
  implicit def materialize[A, B]: Converter[A, B] = macro materializeImpl[A, B]

  def materializeImpl[A, B](c: Context)(implicit
    A: c.WeakTypeTag[A]
  ): c.Expr[Converter[A, B]] = {
    import c.universe._

    val view = implicitViews(c)(A.tpe) match {
      case List(unique) => unique
      case _ =>
        c.abort(c.enclosingPosition, s"Unable to find appropriate view for $A")
    }

    c.Expr[Converter[A, B]](
      q"""
        new Converter[$A, ${ resultType(c)(view.tpe) }] {
          def apply(e: Empty[$A]) = $view(e.a)
        }
      """
    )
  }
}