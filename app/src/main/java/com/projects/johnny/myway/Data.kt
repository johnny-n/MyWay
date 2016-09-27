package com.projects.johnny.myway

import java.util.*

/*
    An object of this class represents a location that we
    may obtain a travel time from.
 */
data class MyLocation(var nameOfPlace: String,
                      var address: String = nameOfPlace) {
    var uuid = UUID.randomUUID()
}