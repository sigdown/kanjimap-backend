package com.example.users

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val name: String,
)

@Serializable
data class UserCreateRequest(
    val email: String,
    val name: String,
) {
    fun validate() {
        require(email.isNotBlank()) { "email must not be blank" }
        require("@" in email) { "email must be valid" }
        require(name.isNotBlank()) { "name must not be blank" }
    }
}

@Serializable
data class UserUpdateRequest(
    val email: String? = null,
    val name: String? = null,
) {
    fun validate() {
        require(email == null || email.isNotBlank()) { "email must not be blank" }
        require(email == null || "@" in email) { "email must be valid" }
        require(name == null || name.isNotBlank()) { "name must not be blank" }
    }
}

@Serializable
data class ErrorResponse(
    val error: String,
)
