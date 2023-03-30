package com.example.ggestagram.navigation.model

import android.net.Uri

data class ContentDTO(var explain : String?=null,
                      var imageUrl: String?=null,
                      var uid : String? = null ,
                      var userId : String? = null,
                      var timeStamp : Long? =null,
                      var favoriteCount : Int=0,
                      var favorites : MutableMap<String,Boolean> = HashMap())
{
    data class Comment(var uid:String? = null ,
                       var userId: String? = null ,
                       var comment : String? = null ,
                       var timeStamp: Long? = null)
}
