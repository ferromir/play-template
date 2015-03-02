// Copyright (C) 2015 Fernando Romero.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package controllers

import play.api.mvc._
import play.api.libs.json._
import com.wordnik.swagger.annotations._

@Api(value = "/hc", description = "Health check")
trait HealthCheckController { this: Controller =>

  case class HealthCheckStatus(status: String)
  implicit val statusFmt = Json.format[HealthCheckStatus]

  @ApiOperation(
    httpMethod = "GET",
    value = "get status",
    nickname = "getStatus",
    response = classOf[HealthCheckStatus]
  )
  def getStatus = Action { Ok(Json.toJson(HealthCheckStatus("ok"))) }
}

object HealthCheckController extends Controller with HealthCheckController
