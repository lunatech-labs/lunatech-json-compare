package com.lunatech.jsoncompare

import play.api.libs.json._

object JsonCompare {
  type DL = Seq[(String, String)] // Differences list

  /**
   * Test whether a test JSON string is a subtree of a reference JSON string
   *
   * A test object is a subtree if each leaf JSON primitive in the test object is identical to the one at the same position in the reference object.
   * @param Reference object
   * @param Test object
   * @returns Either a Left with a sequence of path -> msg tuples in case of difference, or a Right in case of no differences.
   */
  def caseInsensitiveSubTree(reference: String, test: String): Either[DL, Unit] = caseInsensitiveSubTree(Json.parse(reference), Json.parse(test))

  /**
   * Test whether a test JsValue is a subtree of a reference JsValue
   *
   * A test object is a subtree if each leaf JSON primitive in the test object is identical to the one at the same position in the reference object.
   * @param Reference object
   * @param Test object
   * @returns Either a Left with a sequence of path -> msg tuples in case of difference, or a Right in case of no differences.
   */
  def caseInsensitiveSubTree(reference: JsValue, test: JsValue): Either[DL, Unit] = checkSubTree(reference, test, "")

  private def checkSubTree(reference: JsValue, test: JsValue, path: String): Either[DL, Unit] = {
    // JsUndefined is a subtree of everything by definition
    if (test.isInstanceOf[JsUndefined]) {
      Right()
    } else if (test.getClass != reference.getClass) {
      Left(List((path, "type %s instead of %s" format (typeName(test.getClass), typeName(reference.getClass)))))
    } else {
      test match {
        case JsNull => if (reference == JsNull) Right() else difference(path, "is null")
        case b: JsBoolean => if (b.value == reference.asInstanceOf[JsBoolean].value) Right() else difference(path, "boolean value differs")
        case n: JsNumber => {
          // Java BigDecimal says 1.0 and 1 are not equal, even though their compareTo is zero.
          // Scala BigDecimals says 1.0 and 1 are equal.
          // We don't want 1.0 and 1 in JSON to be considered equal, because the spec doesn't say we should
          // and Java's distinction is reasonable.
          if (n.value == reference.asInstanceOf[JsNumber].value && n.value.scale == reference.asInstanceOf[JsNumber].value.scale) Right() else difference(path, "number value differs")
        }
        case s: JsString => if (s.value == reference.asInstanceOf[JsString].value) Right() else difference(path, "string content differs")
        case o: JsObject => flattenLeft(o.fields.map(pair => checkSubTree(reference \ pair._1, pair._2, addSegment(path, pair._1))))
        case a: JsArray => {
          if (a.value.size != reference.asInstanceOf[JsArray].value.size) difference(path, "array size differs") else
            flattenLeft(a.value.zipWithIndex.map(pair => checkSubTree(reference(pair._2), pair._1, addIndex(path, pair._2))))
        }
        case u: JsUndefined => Right() // 
      }
    }
  }

  private def difference(path: String, msg: String) = Left(List((path, msg)))

  private def flattenLeft[A](eithers: Seq[Either[Seq[A], Unit]]): Either[Seq[A], Unit] = eithers.filter(_.isLeft).flatMap(_.left.get) match {
    case Nil => Right()
    case values => Left(values)
  }

  private def typeName(clazz: Class[_ <: JsValue]) = clazz.getSimpleName.substring(2).toLowerCase.reverse.dropWhile(_ == '$').reverse

  private def addSegment(path: String, segment: String) = if (path == "") segment else path + "." + segment
  private def addIndex(path: String, index: Int) = path + "[" + index + "]"
}