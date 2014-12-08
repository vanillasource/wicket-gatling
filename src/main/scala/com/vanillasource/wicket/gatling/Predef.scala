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
      
}

