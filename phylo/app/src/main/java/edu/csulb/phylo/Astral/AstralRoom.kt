package edu.csulb.phylo.Astral

import java.util.*

class AstralRoom(val roomName: String, val longitude: Float, val latitude: Float,
                val expiration: Long) {
    var roomKey : String?

    init{
        roomKey = null
    }

    //Create and return a key to lock a room or folder inside of a room
    fun lockRoom() : String? {
        roomKey = UUID.randomUUID().toString()
        //Generate a UUID object which is Cryptographically Secure
        return roomKey
    }
}