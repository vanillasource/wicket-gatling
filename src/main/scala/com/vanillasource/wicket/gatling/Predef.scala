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
import io.gatling.http.Predef._
import io.gatling.core.check.Check
import io.gatling.core.check.CheckResult
import scala.collection.mutable
import io.gatling.core.validation.Validation
import io.gatling.http.check.HttpCheck
import io.gatling.http.check.HttpCheckScope
import io.gatling.http.response.Response
import io.gatling.http.response.StringResponseBodyUsageStrategy
import io.gatling.core.session.Expression
import io.gatling.core.validation._

/**
  * Import all methods from this object to use all wicket-gatling functions.
  *
  * Usage: 
  * {{{
  * import com.vanillasource.wicket.gatling.Predef._
  * }}}
  */
object Predef {
   /**
     * A Gatling check implementation which extracts all the wicket targets from the response body. To use this
     * implicitly for each response, bind to the http configuration you are using with the base url.
     * {{{
     * val httpConfig = http.
     *    baseUrl(baseUrl).
     *    check(wicketTargetsExtractor(baseUrl)).
     *    ...
     * }}}
     */
   def wicketTargetsExtractor(baseUrl: String) = new HttpCheck(new Check[Response] {
      def check(response: Response, session: Session)(implicit cache: mutable.Map[Any, Any]): Validation[CheckResult] = new CheckResult(None, None) {
         override def hasUpdate = response.hasResponseBody

         override def update = if (!hasUpdate) None else
            Some(session => {
               session.set("wicketTargets", WicketTargets(baseUrl, response.uri.get.getPath(), response.body.string))
            })
      }
   }, HttpCheckScope.Body, Some(StringResponseBodyUsageStrategy))

}

