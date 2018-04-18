package edu.csulb.phylo.Astral

import java.io.Serializable

data class AstralUser(val username: String, val email: String,
                      val password: String?) : Serializable {
    val token: String?

    init{
        token = null
    }
}