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

import java.util.regex.Pattern
import org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4
import scala.util.matching.Regex
import java.net.URI
import scala.annotation.tailrec

/**
  * Contains all the Wicket targets from a response body. An object of this type will be always present
  * in the session object.
  */
case class WicketTargets(targets: List[(TargetSpec, String)])(val requestUri: String, val responseBody: String) {
   /**
     * Get all the URIs from this targets object that match the
     * given type and path.
     * @param targetType The type of links to get
     * @param pathSpec The path components matcher specification ([[WicketTargets.matches]])
     * @return The matching URIs as strings in a list, that may be empty.
     */
   def getUris(targetType: TargetType, pathSpec: String*) = 
      targets.filter(_._1.matches(targetType, pathSpec:_*)).map(_._2).toList
}

object WicketTargets {
   private val linkOnclickPattern = 
      """<[^>]* wicketpath="([a-zA-Z0-9\-_]+)"[^>]*onclick="[^"]*window.location.href=&#039;(.+)&#039;[^"]*"[^>]*>""".r
   private val linkDirectPattern = 
      """<a[^>]* wicketpath="([a-zA-Z0-9\-_]+)"[^>]* href="([^"]+)"[^>]*>""".r
   private val linkDirectPatternReverse = 
      """<a[^>]* href="([^"]+)"[^>]* wicketpath="([a-zA-Z0-9\-_]+)"[^>]*>""".r
   private val formPattern = 
      """<form[^>]* wicketpath="([a-zA-Z0-9\-_]+)"[^>]* action="([^"]+)"[^>]*>""".r

   /**
     * Build the targets object from an URI and the corresponding body. All URIs
     * extracted from the body will be applied as relative URIs.
     */
   def apply(requestUri: String, responseBody: String) = 
      new WicketTargets(findAllUris(requestUri, responseBody))(requestUri, responseBody)

   private def findAllUris(requestUri: String, body: String) =
      findUris(linkOnclickPattern, TargetType.Link, requestUri, body) :::
      findUris(linkDirectPattern, TargetType.Link, requestUri, body) :::
      findUris(linkDirectPatternReverse, TargetType.Link, requestUri, body, 2, 1) :::
      findUris(formPattern, TargetType.Form, requestUri, body)

   private def findUris(pattern: Regex, targetType: TargetType, 
         requestUri: String, body: String, pathCaptureGroup: Int = 1, uriCaptureGroup: Int = 2) = {
      val matcher = pattern.findAllIn(body)
      matcher.map(_ => {
         (TargetSpec(targetType, matcher.group(pathCaptureGroup)), 
          new URI(requestUri).resolve(unescapeHtml4(matcher.group(uriCaptureGroup))).toString())
      }).toList
   }
}

/**
  * A target specification with the type, and the corresponding Wicket
  * path (already split).
  */
case class TargetSpec(val targetType: TargetType, val path: List[String]) {
   /**
     * Determines whether this target specification matches the
     * target type and path given.
     */
   def matches(targetType: TargetType, pathSpec: String*): Boolean = 
      targetType == this.targetType && matches(pathSpec:_*)

   /**
     * Determines whether this target specification matches the path
     * fragments given. The path fragments must represent a subset of
     * the full wicket path of this target. All fragments must be in
     * the same order as the actual path components, but do not need
     * to be complete.
     *
     * For example to matching a target on path `A - B - C - D - E` the
     * following are true:
     * - `matches("A") == true`
     * - `matches("C") == true`
     * - `matches("A", "C") == true`
     * - `matches("A", "B", "D") == true`
     * - `matches("B", "A") == false`
     * - `matches("A", "F") == false`
     */
   def matches(pathSpec: String*): Boolean = matches(pathSpec.toList, path)

   @tailrec
   private def matches(pathSpec: List[String], paths: List[String]): Boolean = 
      if (pathSpec.isEmpty) {
         true
      } else if (!path.contains(pathSpec.head)) {
         false
      } else {
         matches(pathSpec.tail, path.dropWhile(_ != pathSpec.head))
      }
}

object TargetSpec {
   def apply(targetType: TargetType, wicketPath: String) = new TargetSpec(targetType, wicketPath.split("_").toList)
}

sealed trait TargetType

object TargetType {
   case object Link extends TargetType
   case object Form extends TargetType
}


