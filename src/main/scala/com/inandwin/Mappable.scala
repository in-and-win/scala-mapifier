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

trait Mappable[T] {
  def toMap(t: T): Map[String, String]
  def fromMap(map: Map[String, String]): T
}

object Mappable {

  def mapify[T: Mappable](t: T): Map[String, String] = implicitly[Mappable[T]].toMap(t)
  def materialize[T: Mappable](map: Map[String, String]): T = implicitly[Mappable[T]].fromMap(map)

  implicit def materializeMappable[T]: Mappable[T] = macro materializeMappableImpl[T]

  def materializeMappableImpl[T: c.WeakTypeTag](c: Context): c.Expr[Mappable[T]] = {
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

    c.Expr[Mappable[T]] {
      q"""
      new Mappable[$tpe] {
        def toMap(t: $tpe): Map[String, String] = Map(..$toMapParams)
        def fromMap(map: Map[String, String]): $tpe = $companion(..$fromMapParams)
      }
    """
    }
  }
}
