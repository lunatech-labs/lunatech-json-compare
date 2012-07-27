package com.lunatech.jsoncompare

import play.api.libs.json._

object JsonCompare {

  /**
   * Test whether a test JSON string is a subtree of a reference JSON string
   *
   * A test object is a subtree if each leaf JSON primitive in the test object is identical to the one at the same position in the reference object.
   * @param Reference object
   * @param Test object
   * @return Either a Left with a sequence of path -> msg tuples in case of difference, or a Right in case of no differences.
   */
  def caseInsensitiveSubTree(reference: String, test: String): Either[Seq[Difference], Unit] = caseInsensitiveSubTree(Json.parse(reference), Json.parse(test))

  /**
   * Test whether a test JsValue is a subtree of a reference JsValue
   *
   * A test object is a subtree if each leaf JSON primitive in the test object is identical to the one at the same position in the reference object.
   * @param Reference object
   * @param Test object
   * @return Either a Left with a sequence of path -> msg tuples in case of difference, or a Right in case of no differences.
   */
  def caseInsensitiveSubTree(reference: JsValue, test: JsValue): Either[Seq[Difference], Unit] = checkSubTree(reference, test, "")

  private def checkSubTree(reference: JsValue, test: JsValue, path: String): Either[Seq[Difference], Unit] = {
    // JsUndefined is a subtree of everything by definition
    if (test.isInstanceOf[JsUndefined]) {
      Right()
    } else if (test.getClass != reference.getClass) {
      Left(List(Difference(path, "type differs", Some(typeName(reference.getClass)), Some(typeName(test.getClass)))))
    } else {
      test match {
        case JsNull => if (reference == JsNull) Right() else Left(List(Difference(path, "is null")))
        case b: JsBoolean => {
          val expected = reference.asInstanceOf[JsBoolean].value
          val found = b.value
          if (expected == found) Right() else Left(List(Difference(path, "boolean value differs", Some(expected.toString), Some(found.toString))))
        }
        case n: JsNumber => {
          // Java BigDecimal says 1.0 and 1 are not equal, even though their compareTo is zero.
          // Scala BigDecimals says 1.0 and 1 are equal.
          // We don't want 1.0 and 1 in JSON to be considered equal, because the spec doesn't say we should
          // and Java's distinction is reasonable.
          val expected = reference.asInstanceOf[JsNumber].value
          val found = n.value
          if (expected == found && expected.scale == found.scale) Right() else Left(List(Difference(path, "number value differs", Some(expected.toString), Some(found.toString))))
        }
        case s: JsString => {
          val expected = reference.asInstanceOf[JsString].value
          val found = s.value
          if (expected == found) Right() else Left(List(Difference(path, "string content differs", Some(expected), Some(found))))
        }
        case o: JsObject => flattenLeft(o.fields.map(pair => checkSubTree(reference \ pair._1, pair._2, addSegment(path, pair._1))))
        case a: JsArray => {
          if (a.value.size != reference.asInstanceOf[JsArray].value.size) Left(List(Difference(path, "array size differs"))) else
            flattenLeft(a.value.zipWithIndex.map(pair => checkSubTree(reference(pair._2), pair._1, addIndex(path, pair._2))))
        }
        case u: JsUndefined => Right()
      }
    }
  }

  private def flattenLeft[A](eithers: Seq[Either[Seq[A], Unit]]): Either[Seq[A], Unit] = eithers.filter(_.isLeft).flatMap(_.left.get) match {
    case Nil => Right()
    case values => Left(values)
  }

  private def typeName(clazz: Class[_ <: JsValue]) = clazz.getSimpleName.substring(2).toLowerCase.reverse.dropWhile(_ == '$').reverse

  private def addSegment(path: String, segment: String) = if (path == "") segment else path + "." + segment
  private def addIndex(path: String, index: Int) = path + "[" + index + "]"

  case class Difference(path: String, msg: String, expected: Option[String] = None, found: Option[String] = None)
}