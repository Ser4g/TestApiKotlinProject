package org.example.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateUserResponse(
    val success: Boolean,
    val details: Details?,
    @JsonDeserialize(using = StringOrListDeserializer::class)
    val message: List<String>?
) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Details(
        val username: String?,
        val email: String?,
        val password: String?,
        val createdAt: String?,
        val updatedAt: String?,
        val id: Int?
    )
}
