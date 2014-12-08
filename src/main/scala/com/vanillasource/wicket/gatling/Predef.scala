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
     * Get all the wicket URIs that match the given parameters. See [[WicketTargets.getUris]].
     */
   def wicketUris(targetType: TargetType, pathSpec: Expression[String]*): Expression[List[String]] = session => {
      if (!session.contains("wicketTargets")) {
         "no wicket targets available in session, probably not enabled through enableWicketTargets(), or page was not loaded properly".failure
      } else {
         switchSeqAndExpression(pathSpec.toList).apply(session).map(pathSpecStrings => {
            session("wicketTargets").as[WicketTargets].getUris(targetType, pathSpecStrings:_*)
         })
      }
   }

   /**
     * Turn the expression and sequence inside out. This is done so it can be
     * easily mapped once, and any potential errors in any of the expressions
     * immediately lead to failure.
     */
   private def switchSeqAndExpression[T](expressions: List[Expression[T]]): Expression[List[T]] = session => {
      if (expressions.isEmpty) {
         Nil.success
      } else {
         for (
            current <- expressions.head.apply(session);
            rest <- switchSeqAndExpression(expressions.tail).apply(session)
         ) yield (current :: rest)
      }
   }

   /**
     * Makes it possible to select a single URI from possible multiple ones using
     * a supplied logic.
     * 
     * Intended usage is on any method here that returns multiple URIs, for
     * example selecting a link randomly:
     * {{{
     *    wicketLinks("link-id").selectUri(links -> links(rnd.nextInt(links.size)))
     * }}}
     */
   implicit class SelectableUriList(uris: Expression[List[String]]) {
      def selectUri(mapper: List[String] => String) = uris.andThen(_.map(mapper))
   }

   /**
     * Can be used in conditional executions like doIf() calls, the following way:
     * {{{
     *    .doIf(wicketUriExists("next-page")) {
     *       exec(http("...") ...)
     *    }
     * }}}
     */
   def wicketUriExists(pathSpec: Expression[String]*): Expression[Boolean] = 
      wicketUris(TargetType.Any, pathSpec:_*).map(!_.isEmpty)

   /**
     * Expects and returns exactly one URI. If either no URI can be found
     * for the given specification, or more than one URI is found, then a failure
     * is returned.
     */
   def wicketUri(targetType: TargetType, pathSpec: Expression[String]*) = wicketUris(targetType, pathSpec:_*).andThen(_.flatMap(uriList =>
      if (uriList.isEmpty) {
         s"on the given path '$pathSpec' is no URI to be found".failure
      } else if (uriList.size > 1) {
         s"on thet given path '$pathSpec' there are multiple URIs to be found".failure
      } else {
         uriList.head.success
      }
   ))

   /**
     * Returns all links found under the given path.
     */
   def wicketLinksUnder(pathSpec: Expression[String]*) = wicketUris(TargetType.Link, pathSpec:_*)

   /**
     * Returns all forms found under the given path.
     */
   def wicketFormsUnder(pathSpec: Expression[String]*) = wicketUris(TargetType.Form, pathSpec:_*)

   /**
     * Returns exactly one link found under the given path. Expected
     * to be used with the get() method:
     * {{{
     * http("Go to next page")
     *    .get(wicketLinkUnder("next-page-link"))
     * }}}
     */
   def wicketLinkUnder(pathSpec: Expression[String]*) = wicketUri(TargetType.Link, pathSpec:_*)

   /**
     * Returns exactly one form found under the given path. Expected
     * to be used with the post() method:
     * {{{
     * http("Submit User Form")
     *    .post(wicketFormUnder("user-form"))
     * }}}
     */
   def wicketFormUnder(pathSpec: Expression[String]*) = wicketUri(TargetType.Form, pathSpec:_*)
}

