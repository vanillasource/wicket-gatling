/**
  * Copyright (C) 2014 VanillaSource
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */

package com.vanillasource.wicket.gatling

import io.gatling.core.config.GatlingConfiguration
import scala.collection.mutable
import org.scalatest.WordSpec
import io.gatling.http.Predef._
import io.gatling.core.validation._
import io.gatling.core.session.Session
import io.gatling.http.response.Response
import io.gatling.http.response.HttpResponse
import io.gatling.http.response.StringResponseBody
import java.nio.charset.Charset
import com.vanillasource.wicket.gatling.HttpConfig._

class HttpConfigTests extends WordSpec {
   implicit val httpCache: mutable.Map[Any, Any] = mutable.Map() // You need this for calling check() simpler
   GatlingConfiguration.setUp()
   val config = http.baseURL("http://localhost").enableWicketTargets()
   val check = config.protocol.responsePart.checks(0)

   "Building the http configuration" when {
      "base url was not yet called" should {
         "throw exception" in {
            intercept[IllegalArgumentException] {
               http.enableWicketTargets()
            }
         }
      }
      "base url already set" should {
         "register new extractor check" in {
            val config = http.baseURL("http://localhost").enableWicketTargets()

            assert(config.protocol.responsePart.checks.size === 1)
         }
      }
   }
   "The extractor check" when {
      "there is no response body" should {
         "have no update" in {
            val checkResult = check.check(emptyResponse(), Session("scenarioName", "userId"))

            val result = checkResult.map(_.hasUpdate)

            assert(result === Success(false))
         }
         "not update session" in {
            val checkResult = check.check(emptyResponse(), Session("scenarioName", "userId"))

            val result = checkResult.map(_.update.map(_.apply(Session("currentScenario", "userId"))))

            assert(result === Success(None))
         }
      }
      "there is a response body" should {
         "have update" in {
            val checkResult = check.check(emptyResponse().copy(bodyLength = 1), Session("scenarioName", "userId"))

            val result = checkResult.map(_.hasUpdate)

            assert(result === Success(true))
         }
         "update session with targets" in {
            val checkResult = check.check(emptyResponse().copy(bodyLength = 1), Session("scenarioName", "userId"))

            val result = checkResult.map(_.update.map(_.apply(Session("currentScenario", "userId"))))

            info("Result: "+result)

            assert(result.get.get.attributes.contains("wicketTargets"))
         }
      }
   }

   private def emptyResponse() = HttpResponse(
      request = null,
      nettyRequest = None,
      status = None,
      headers = null,
      body = StringResponseBody("body", Charset.defaultCharset()),
      checksums = Map(),
      bodyLength = 0,
      charset = null,
      firstByteSent = 0,
      lastByteSent = 0,
      firstByteReceived = 0,
      lastByteReceived = 0
   )
}

