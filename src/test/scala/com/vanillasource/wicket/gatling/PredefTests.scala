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
import org.scalatest.WordSpec
import io.gatling.core.validation._
import io.gatling.core.session.Session
import Predef._

class PredefTests extends WordSpec {
   "Getting URIs" when {
      "there is no targets object in session" should {
         "return failure" in {
            val uris = wicketUris(TargetType.Link).apply(noTargetsSession)

            assert(uris.isInstanceOf[Failure])
         }
      }
      "there are two links in the session and an empty query" should {
         "return both links" in {
            val uris = wicketUris(TargetType.Link).apply(targetsSession)

            assert(uris == Success(List("URI1", "URI2")))
         }
      }
      "selecting a link specifically" should {
         "return the one link" in {
            val uris = wicketUris(TargetType.Link, "A", "B").apply(targetsSession)

            assert(uris == Success(List("URI1")))
         }
      }
      "selecting a link with expression" should {
         "return the hit link" in {
            val uris = wicketUris(TargetType.Link, "A", "${var2}").apply(targetsSession)

            assert(uris == Success(List("URI1")))
         }
      }
      "selecting a link with a non-existing variable" should {
         "return failure" in {
            val uris = wicketUris(TargetType.Link, "A", "${varX}").apply(targetsSession)

            assert(uris.isInstanceOf[Failure])
         }
      }
   }
   "Selecting URIs from multiple ones" when {
      "selecting the first item" should {
         "return the first item" in {
            val uri = wicketUris(TargetType.Link).selectUri(_(0)).apply(targetsSession)

            assert(uri == Success("URI1"))
         }
      }
      "selecting a non-existent item" should {
         "throw exception" in {
            intercept[IndexOutOfBoundsException] {
               wicketUris(TargetType.Link).selectUri(_(3)).apply(targetsSession)
            }
         }
      }
   }
   "Determining whether a link exsists" should {
      "return true if link exists" in {
         val result = wicketUriExists("A").apply(targetsSession)

         assert(result == Success(true))
      }
      "return false if link does not exist" in {
         val result = wicketUriExists("D").apply(targetsSession)

         assert(result == Success(false))
      }
      "return failure if target is not in session" in {
         val result = wicketUriExists("A").apply(noTargetsSession)

         assert(result.isInstanceOf[Failure])
      }
   }
   "Getting exactly one uri" when {
      "there is only one uri" should {
         "return the uri" in {
            val result = wicketUri(TargetType.Any, "C").apply(targetsSession)

            assert(result == Success("URI2"))
         }
      }
      "there are multiple uris" should {
         "return failure" in {
            val result = wicketUri(TargetType.Any, "A").apply(targetsSession)

            assert(result.isInstanceOf[Failure])
         }
      }
      "there are no uris" should {
         "return failure" in {
            val result = wicketUri(TargetType.Any, "D").apply(targetsSession)

            assert(result.isInstanceOf[Failure])
         }
      }
   }

   def targetsSession = noTargetsSession
      .set("wicketTargets", WicketTargets(List(
            (TargetSpec(TargetType.Link, List("A", "B")), "URI1"),
            (TargetSpec(TargetType.Link, List("A", "C")), "URI2"))) ("", ""))

   def noTargetsSession = Session("scenarioName", "userId")
      .set("var1", "A")
      .set("var2", "B")
      .set("var3", "D")
}

