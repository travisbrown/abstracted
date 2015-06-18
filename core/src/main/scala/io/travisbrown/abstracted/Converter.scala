package io.travisbrown.abstracted

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

/**
 * A conversion from a wrapped value to some other type.
 */
trait Converter[A, B] {
  def apply(a: Empty[A]): B
}

private[abstracted] object Converter {
  implicit def materialize[A, B]: Converter[A, B] = macro materializeImpl[A, B]

  def materializeImpl[A, B](c: Context)(implicit
    A: c.WeakTypeTag[A]
  ): c.Expr[Converter[A, B]] = {
    import c.ImplicitCandidate
    import c.universe._

    val view = c.openImplicits.flatMap {
      case ImplicitCandidate(_, _, TypeRef(_, _, _ :: target :: _), _) =>
        val view = c.inferImplicitView(EmptyTree, A.tpe, target)

        if (view.nonEmpty) Some(view) else None
      case _ => None
    }.headOption.getOrElse {
      c.abort(c.enclosingPosition, "Unable to find appropriate view")
    }

    c.Expr[Converter[A, B]](
      q"""
        new Converter[$A, ${view.tpe.finalResultType}] {
          def apply(e: Empty[$A]) = $view(e.a)
        }
      """
    )
  }
}