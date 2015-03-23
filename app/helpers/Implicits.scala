package helpers

import play.api.data.validation.ValidationError
import play.api.libs.json.{ JsValue, JsPath, JsError, Json }

object Helpers {

  def toJsonErrors(errors: Seq[(JsPath, Seq[ValidationError])]): JsValue = {
    Json.obj("errors" -> JsError.toFlatJson(errors))
  }
}

object Implicits {}

