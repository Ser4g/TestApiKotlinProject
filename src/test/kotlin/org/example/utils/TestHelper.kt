package org.example.utils

import io.restassured.common.mapper.TypeRef
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.example.client.UserApiClient
import org.example.model.CreateUserResponse
import org.example.model.UserDto
import java.util.UUID

fun Response.asCreateUserResponse(): CreateUserResponse = `as`(CreateUserResponse::class.java)

fun Response.asUserList(): List<UserDto> = `as`(object : TypeRef<List<UserDto>>() {})

object TestHelper {

    fun uniqueUsername(): String = "test_" + UUID.randomUUID().toString().substring(0, 8)

    fun uniqueEmail(username: String): String = "$username@test.ru"

    fun validPassword(): String = "P@ssw0rd123"

    fun createRandomUser(client: UserApiClient): CreateUserResponse.Details {
        val username = uniqueUsername()
        val response = client.createUser(username, uniqueEmail(username), validPassword())
        assertThat(response.statusCode)
            .describedAs("Предусловие: создание пользователя должно пройти успешно. Тело: %s", response.asString())
            .isEqualTo(200)
        return checkNotNull(response.asCreateUserResponse().details) {
            "Предусловие: в ответе create должен присутствовать details. Тело: ${response.asString()}"
        }
    }

    fun getUsers(client: UserApiClient): List<UserDto> {
        val response = client.getUsers()
        assertThat(response.statusCode)
            .describedAs("Предусловие: получение списка пользователей должно пройти успешно. Тело: %s", response.asString())
            .isEqualTo(200)
        return response.asUserList()
    }

    fun findUserByUsername(client: UserApiClient, username: String): UserDto? =
        getUsers(client).firstOrNull { it.username == username }
}
