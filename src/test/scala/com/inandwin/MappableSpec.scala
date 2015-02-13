package com.inandwin

import org.specs2.mutable._

case class Foo(a: Int, b: Double, c: String)

class MappableSpec extends Specification {
  "Mappable should" should {
    val foo = Foo(42, 3.14, "Bruce Lee")
    val fooMap = Map(
      "a" -> "42",
      "b" -> "3.14",
      "c" -> "Bruce Lee"
    )

    implicit val intMarshaller = new StringMarshaller[Int] {
      def marshall(i: Int): String = i.toString
      def unMarshall(s: String): Int = s.toInt
    }

    implicit val doubleMarshaller = new StringMarshaller[Double] {
      def marshall(d: Double): String = d.toString
      def unMarshall(s: String): Double = s.toDouble
    }

    implicit val stringMarshaller = new StringMarshaller[String] {
      def marshall(s: String): String = s
      def unMarshall(s: String): String = s
    }

    implicit val fooGen = Mappable.materializeMappable[Foo]

    "be able to convert a 'case class' to a Map" in {
      Mappable.mapify(foo) must beEqualTo(fooMap)
    }

    "be able to instanciate a 'case class' from a Map" in {
      Mappable.materialize(fooMap) must beEqualTo(foo)
    }

  }
}
