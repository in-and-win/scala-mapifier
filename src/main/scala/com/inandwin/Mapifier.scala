package com.inandwin

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

// Scala macro to convert between a case class instance and a Map of constructor parameters.
// http://blog.echo.sh/post/65955606729/exploring-scala-macros-map-to-case-class-conversion
// https://github.com/echojc/scala-macro-template

trait StringMarshaller[T] {
  def marshall(t: T): String
  def unMarshall(s: String): T
}

object StringMarshaller {
  def marshall[T: StringMarshaller](t: T): String = implicitly[StringMarshaller[T]].marshall(t)
  def unMarshall[T: StringMarshaller](s: String): T = implicitly[StringMarshaller[T]].unMarshall(s)

}

trait Mapifier[T] {
  def toMap(t: T): Map[String, String]
  def fromMap(map: Map[String, String]): T
}

object Mapifier {

  def mapify[T: Mapifier](t: T): Map[String, String] = implicitly[Mapifier[T]].toMap(t)
  def materialize[T: Mapifier](map: Map[String, String]): T = implicitly[Mapifier[T]].fromMap(map)

  implicit def apply[T]: Mapifier[T] = macro applyImpl[T]

  def applyImpl[T: c.WeakTypeTag](c: Context): c.Expr[Mapifier[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val companion = tpe.typeSymbol.companion

    val fields = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.get.paramLists.head

    val (toMapParams, fromMapParams) = fields.map { field =>
      val name = field.asTerm.name
      val key = name.decodedName.toString
      val returnType = tpe.decl(name).typeSignature

      (q"$key -> com.inandwin.StringMarshaller.marshall(t.$name)", q"com.inandwin.StringMarshaller.unMarshall[$returnType](map($key))")
    }.unzip

    c.Expr[Mapifier[T]] {
      q"""
      new Mapifier[$tpe] {
        def toMap(t: $tpe): Map[String, String] = Map(..$toMapParams)
        def fromMap(map: Map[String, String]): $tpe = $companion(..$fromMapParams)
      }
    """
    }
  }
}
