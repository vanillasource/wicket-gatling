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

/**
  * Contains all the Wicket targets from a response body. An object of this type will be always present
  * in the session object.
  */
case class WicketTargets(targets: List[String])(val requestUri: String, val responseBody: String)

object WicketTargets {
   /**
     * Build the targets object from an URI and the corresponding body. All URIs
     * extracted from the body will be applied as relative URIs.
     */
   def apply(requestUri: String, responseBody: String) = 
      new WicketTargets(List())(requestUri, responseBody)
}

