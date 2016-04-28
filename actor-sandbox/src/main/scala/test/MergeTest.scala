package test

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{SerializationFeature, DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper


object MergeTest {

  type MapType = Map[String, Any]

  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
  mapper.setSerializationInclusion(Include.NON_NULL);
  mapper.setSerializationInclusion(Include.NON_EMPTY);
  mapper.registerModule(DefaultScalaModule)


  val pay1 = "{\"id\":4104389,\"action\":\"CREATE\",\"offeredInRunning\":false,\"eventDescriptor\":{\"typeId\":32343,\"subclassId\":3},\"markets\":[{\"id\":58431534,\"action\":\"CREATE\",\"offeredInRunning\":false,\"typeId\":19500,\"resulted\":true,\"selections\":[{\"id\":394894556,\"action\":\"CREATE\",\"resultType\":\"LOSE\",\"startingPriceNumerator\":6,\"startingPriceDenominator\":1,\"participantKey\":\"Church Street\",\"resultStatus\":\"CONFIRMED\"},{\"id\":394894557,\"action\":\"CREATE\",\"resultType\":\"WIN\",\"place\":1,\"startingPriceNumerator\":3,\"startingPriceDenominator\":1,\"participantKey\":\"Markland Flight\",\"resultStatus\":\"CONFIRMED\"},{\"id\":394894558,\"action\":\"CREATE\",\"resultType\":\"LOSE\",\"startingPriceNumerator\":8,\"startingPriceDenominator\":1,\"participantKey\":\"Salacres Adele\",\"resultStatus\":\"CONFIRMED\"},{\"id\":394894559,\"action\":\"CREATE\",\"resultType\":\"PLACE\",\"place\":2,\"startingPriceNumerator\":5,\"startingPriceDenominator\":2,\"participantKey\":\"Slaneyside Fran\",\"resultStatus\":\"CONFIRMED\"},{\"id\":394894560,\"action\":\"CREATE\",\"resultType\":\"PLACE\",\"place\":3,\"startingPriceNumerator\":9,\"startingPriceDenominator\":4,\"participantKey\":\"Miniskirt\",\"resultStatus\":\"CONFIRMED\"},{\"id\":394894561,\"action\":\"CREATE\",\"resultType\":\"LOSE\",\"startingPriceNumerator\":6,\"startingPriceDenominator\":1,\"participantKey\":\"Twilight Bella\",\"resultStatus\":\"CONFIRMED\"},{\"id\":394894562,\"action\":\"CREATE\",\"resultType\":\"PLACE\",\"place\":3,\"characteristic\":\"UNNAMED_FAVOURITE\",\"startingPriceNumerator\":9,\"startingPriceDenominator\":4,\"resultStatus\":\"CONFIRMED\"},{\"id\":394894563,\"action\":\"CREATE\",\"characteristic\":\"UNNAMED_SECOND_FAVOURITE\",\"startingPriceNumerator\":5,\"startingPriceDenominator\":2,\"resultStatus\":\"CONFIRMED\"}]}]}"
  val pay2 = "{\"id\":4104389,\"action\":\"UPDATE\",\"offeredInRunning\":false,\"eventDescriptor\":{\"typeId\":32343,\"subclassId\":4},\"resulted\":true,\"markets\":[{\"id\":58431534,\"action\":\"UPDATE\",\"offeredInRunning\":false,\"typeId\":19500,\"resulted\":true,\"selections\":[{\"id\":394894556,\"action\":\"UPDATE\",\"resultType\":\"LOSE\",\"startingPriceNumerator\":6,\"startingPriceDenominator\":1,\"participantKey\":\"Church Street\",\"resultStatus\":\"CONFIRMED\"},{\"id\":394894557,\"action\":\"UPDATE\",\"resultType\":\"WIN\",\"place\":1,\"startingPriceNumerator\":3,\"startingPriceDenominator\":1,\"participantKey\":\"Markland Flight\",\"resultStatus\":\"CONFIRMED\"},{\"id\":394894558,\"action\":\"UPDATE\",\"resultType\":\"LOSE\",\"startingPriceNumerator\":8,\"startingPriceDenominator\":1,\"participantKey\":\"Salacres Adele\",\"resultStatus\":\"CONFIRMED\"},{\"id\":394894559,\"action\":\"UPDATE\",\"resultType\":\"PLACE\",\"place\":2,\"startingPriceNumerator\":5,\"startingPriceDenominator\":2,\"participantKey\":\"Slaneyside Fran\",\"resultStatus\":\"CONFIRMED\"},{\"id\":394894560,\"action\":\"UPDATE\",\"resultType\":\"PLACE\",\"place\":3,\"startingPriceNumerator\":9,\"startingPriceDenominator\":4,\"participantKey\":\"Miniskirt\",\"resultStatus\":\"CONFIRMED\"},{\"id\":394894561,\"action\":\"UPDATE\",\"resultType\":\"LOSE\",\"startingPriceNumerator\":6,\"startingPriceDenominator\":1,\"participantKey\":\"Twilight Bella\",\"resultStatus\":\"CONFIRMED\"},{\"id\":394894562,\"action\":\"UPDATE\",\"resultType\":\"PLACE\",\"place\":3,\"characteristic\":\"UNNAMED_FAVOURITE\",\"startingPriceNumerator\":9,\"startingPriceDenominator\":4,\"resultStatus\":\"CONFIRMED\"},{\"id\":394894563,\"action\":\"UPDATE\",\"resultType\":\"PLACE\",\"place\":2,\"characteristic\":\"UNNAMED_SECOND_FAVOURITE\",\"startingPriceNumerator\":5,\"startingPriceDenominator\":2,\"resultStatus\":\"CONFIRMED\"}]}]}"

  val json1 = "{\"id\":1234, \"action\": \"CREATE\", \"markets\":[{\"id\":1234, \"action\":\"CREATE\", \"status\":\"INIT\", \"display\":false}]}"
  val json2 = "{\"id\":1234, \"action\": \"UPDATE\", \"order\": 1, \"markets\":[{\"id\":1234, \"action\":\"UPDATE\", \"status\":\"COMPLETE\"}]}"

  def main(args: Array[String]) {
    val m1 = toMap(pay1)
    val m2 = toMap(pay2)

    println(anyToJsonString(merge(m1, m2)))
  }

  def merge[K, V](m1:Map[K, V], m2:Map[K, V]):Map[K, Any] =
    (m1.keySet ++ m2.keySet) map {
      i => i -> {
        (m1.get(i), m2.get(i)) match {
          case (Some(v1:List[Map[K,Any]]), Some(v2:List[Map[K,Any]])) => v1.zip(v2) map { e => merge(e._1, e._2) }
          case (Some(v1:Map[K,Any]), Some(v2:Map[K,Any])) => merge(v1, v2)
          case (Some(v1:Any), Some(v2:Any)) => v2
          case (None, Some(v2:Any)) => v2
          case (Some(v1:Any), None) => v1
        }
      }
    } toMap

  private def toMap(data:String):Map[String,Any] = {
    mapper.readValue[Map[String,Any]](data)
  }

  private def anyToJsonString[A](any:A): String  = {
    mapper.writeValueAsString(any)
  }

}
