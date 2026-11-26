package org.example.client

import io.restassured.RestAssured.given
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification

class UserApiClient {

    private val baseSpec: RequestSpecification

    init {
        val baseUri = System.getProperty("baseUri", "http://18.194.45.232")
        val port = System.getProperty("port", "3333").toInt()
        baseSpec = given()
            .baseUri(baseUri)
            .port(port)
    }

    fun createUser(username: String, email: String, password: String): Response =
        createUser(
            mapOf(
                "username" to username,
                "email" to email,
                "password" to password
            )
        )

    fun createUser(formData: Map<String, String>): Response {
        val request = given()
            .spec(baseSpec)
            .contentType("multipart/form-data")
        formData.forEach { (key, value) -> request.multiPart(key, value) }
        return request.post("/user/create")
    }

    fun getUsers(): Response =
        given()
            .spec(baseSpec)
            .get("/user/get")
}
