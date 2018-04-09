package edu.csulb.phylo.Astral

class User(email: String, username: String, password: String) {
    private val email: String
    private val username:String
    private val password:String

    init{
        this.email = email
        this.username = username
        this.password = password
    }
}