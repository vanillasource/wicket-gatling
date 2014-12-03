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
import org.scalatest.WordSpec
import org.scalatest.BeforeAndAfterAll
import io.gatling.http.Predef._
import com.vanillasource.wicket.gatling.HttpConfig._

class HttpConfigTests extends WordSpec with BeforeAndAfterAll {
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

   override def beforeAll() {
      GatlingConfiguration.setUp()
   }
}

