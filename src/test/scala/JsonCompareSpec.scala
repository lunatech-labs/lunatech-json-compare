import org.specs2.mutable._
import com.lunatech.jsoncompare.JsonCompare.{ caseInsensitiveSubTree => c }
import play.api.libs.json._
import play.api.libs.json.Json.toJson
import play.api.libs.json.Writes._
import org.specs2.matcher.DataTables

class JsonCompareSpec extends Specification with DataTables {
  "The 'checkSubTree' method" >> {
    "should accept identical values" >> {

      val values: Seq[JsValue] = Seq(
        toJson("a"),
        toJson(1),
        toJson(3.3),
        JsNull,
        JsArray(List(JsString("a"), JsNumber(5))),
        JsObject(List("a" -> JsString("b"))))

      values.foreach { value =>
        ("value %s" format value) >> {
          c(value, value) must beRight
        }
      }
    }

    "should not accept different values" >> {
      val values: Seq[(JsValue, JsValue)] = Seq(
        toJson("a") -> toJson("b"),
        toJson("a") -> toJson(1),
        toJson("a") -> JsNull,
        toJson(1) -> toJson(1.0),
        toJson(List("a")) -> toJson(Map("a" -> "b")))

      values.foreach {
        case (value1, value2) =>
          ("value %s != %s" format (value1, value2)) >> {
            c(value1, value2) must beLeft
          }

      }
    }

    val baseList = List(
      "a" -> toJson("bar"),
      "b" -> toJson("foo"))
    val extendedList = baseList ++ List(
      "c" -> toJson("zuk"))

    "should accept shuffled objects" >> {
      val ref = JsObject(baseList)
      val test = JsObject(baseList.reverse)

      c(ref, test) must beRight
    }

    "should accept objects with fewer values" >> {
      val ref = JsObject(extendedList)
      val test = JsObject(baseList)

      c(ref, test) must beRight
    }

    "should not accept objects with more values" >> {
      val ref = JsObject(baseList)
      val test = JsObject(extendedList)

      c(ref, test) must beLeft // TODO: Check error
    }

    "should not accept shuffled arrays" >> {
      val ref = JsArray(baseList.map(_._2))
      val test = JsArray(baseList.reverse.map(_._2))

      c(ref, test) must beLeft // TODO: Check error
    }

    "should not accept arrays with fewer values" >> {
      val ref = JsArray(extendedList.map(_._2))
      val test = JsArray(baseList.map(_._2))

      c(ref, test) must beLeft // TODO: Check error
    }

    "should not accept arrays with more values" >> {
      val ref = JsArray(baseList.map(_._2))
      val test = JsArray(extendedList.map(_._2))

      c(ref, test) must beLeft // TODO: Check error
    }
  }

}