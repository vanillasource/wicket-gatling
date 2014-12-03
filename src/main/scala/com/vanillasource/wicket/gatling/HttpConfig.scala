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

import io.gatling.core.Predef._
import io.gatling.core.check.Check
import io.gatling.core.check.CheckResult
import scala.collection.mutable
import io.gatling.core.validation.Validation
import io.gatling.http.check.HttpCheck
import io.gatling.http.check.HttpCheckScope
import io.gatling.http.response.Response
import io.gatling.http.response.StringResponseBodyUsageStrategy
import io.gatling.core.validation._
import io.gatling.http.config.HttpProtocolBuilder

/**
  * Object to attach wicket target extraction to any Gatling Http Configuration.
  *
  * Usage: 
  * {{{
  * import com.vanillasource.wicket.gatling.HttpConfig._
  *
  * val httpConfig = http
  *    .baseUrl(baseUrl)
  *    .enableWicketTargets()
  *    ...
  * }}}
  */
object HttpConfig {
   /**
     * An implicit conversion which defines the enableWicketTargets() method seemingly on the
     * original protocol builder. Note: when wicket targets are enabled the base URLs are saved,
     * so do not modify the base URLs after method is applied.
     */
   implicit def httpConfigBuilderExtension(builder: HttpProtocolBuilder) = new {
      def enableWicketTargets() = {
         if (builder.protocol.baseURLs.isEmpty) {
            throw new IllegalArgumentException("can not enable wicket targets because base URLs are not yet set, please set base URLs on http config with baseURL() or baseURLs()")
         }
         builder.check(wicketTargetsExtractor(builder.protocol.baseURLs))
      }
   }

   /**
     * A Gatling check implementation which extracts all the wicket targets from the response body.
     */
   private def wicketTargetsExtractor(baseUrls: List[String]) = new HttpCheck(new Check[Response] {
      def check(response: Response, session: Session)(implicit cache: mutable.Map[Any, Any]): Validation[CheckResult] = new CheckResult(None, None) {
         override def hasUpdate = response.hasResponseBody

         override def update = {
            if (!hasUpdate) {
               None
            } else {
               Some(_.set("wicketTargets", WicketTargets(baseUrls, response.uri.get.getPath(), response.body.string)))
            }
         }
      }
   }, HttpCheckScope.Body, Some(StringResponseBodyUsageStrategy))

}

