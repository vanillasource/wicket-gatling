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
import com.ning.http.client.uri.Uri
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
  *    .baseUri(baseUri)
  *    .enableWicketTargets()
  *    ...
  * }}}
  */
object HttpConfig {
   /**
     * An implicit conversion which defines the enableWicketTargets() method seemingly on the
     * original protocol builder. Note: when wicket targets are enabled the base URIs are saved,
     * so do not modify the base URIs after method is applied.
     */
   implicit class HttpConfigBuilderExtension(builder: HttpProtocolBuilder) {
      def enableWicketTargets() = {
         if (builder.protocol.baseURLs.isEmpty) {
            throw new IllegalArgumentException("can not enable wicket targets because base URIs are not yet set, please set base URIs on http config with baseURL() or baseURLs()")
         }
         builder.check(wicketTargetsExtractor(builder.protocol.baseURLs.map(Uri.create(_))))
      }
   }

   /**
     * A Gatling check implementation which extracts all the wicket targets from the response body.
     */
   private def wicketTargetsExtractor(baseUris: List[Uri]) = new HttpCheck(new Check[Response] {
      def check(response: Response, session: Session)(implicit cache: mutable.Map[Any, Any]): Validation[CheckResult] = new CheckResult(None, None) {
         override def hasUpdate = response.hasResponseBody

         override def update = {
            if (!hasUpdate || response.uri == None) {
               None
            } else {
               Some(_.set("wicketTargets", WicketTargets(createRequestUri(baseUris, response.uri.get), response.body.string)))
            }
         }
      }
   }, HttpCheckScope.Body, Some(StringResponseBodyUsageStrategy))

   /**
     * Create a relative URI from the context-root relative request URI and the
     * given base URIs. Gatling takes uris relative to the given base uris, which
     * might already contain context-roots. The requests however contain the full uri.
     *
     * Base URI might be: `http://localhost:8080/myapp`
     *
     * Request uri might be: `http://localhost:8080/myapp/wicket/bookmarkable/...
     *
     * This method will generate: `/wicket/bookmarkable/...`
     *
     * @param baseUris The base urls defined by the http configuration. This
     *    might be multiple URIs in in which case gatling will choose one at
     *    random for each request.
     * @param requestUri The request Uri of the current request.
     */
   private def createRequestUri(baseUris: List[Uri], requestUri: Uri) = {
      val acutalBaseUris = baseUris.filter(candidateBaseUri => requestUri.toUrl().startsWith(candidateBaseUri.toUrl()))
      if (acutalBaseUris.isEmpty) {
         throw new IllegalArgumentException("request uri '"+requestUri.toUrl()+"' was not part of any of the given base uris: "+baseUris)
      }
      requestUri.toUrl().substring(acutalBaseUris(0).toUrl().length())
   }
}

