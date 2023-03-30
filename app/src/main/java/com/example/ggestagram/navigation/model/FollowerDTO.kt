package com.example.ggestagram.navigation.model

data class FollowerDTO(
    var followerCount : Int  = 0,
    var followers : MutableMap<String,Boolean> = HashMap(),
    var followingCount : Int = 0,
    var following : MutableMap<String,Boolean> = HashMap()




)
