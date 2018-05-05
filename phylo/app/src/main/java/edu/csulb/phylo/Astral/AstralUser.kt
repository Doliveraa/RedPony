package edu.csulb.phylo.Astral

import java.io.Serializable

data class AstralUser(val username: String, val email: String,
                      val password: String?) : Serializable {
    val token: String?
    var name: String?

    init{
        token = null
        name = null
    }

    /**
     * Turns the user's information into a map
     */
    fun toMap() : Map<String,String?> {
        //Initialize the HashMap
        val userInfoMap : HashMap<String, String?> = HashMap<String,String?>()

        userInfoMap.put("username", username)
        userInfoMap.put("email", email)
        userInfoMap.put("password", password)

        return userInfoMap.toMap()
    }
}