package org.example.tests

import org.assertj.core.api.Assertions.assertThat
import org.example.client.UserApiClient
import org.example.utils.TestHelper.createRandomUser
import org.example.utils.TestHelper.findUserByUsername
import org.example.utils.TestHelper.getUsers
import org.example.utils.TestHelper.uniqueEmail
import org.example.utils.TestHelper.uniqueUsername
import org.example.utils.TestHelper.validPassword
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class UserCreateSecurityTest {

    private val client = UserApiClient()

    @Test
    @DisplayName("Безопасность 2.1.1: SQL-инъекция в username/email — без 500 и без влияния на БД")
    fun sqlInjectionIsHandledSafely() {
        val markerUsername = checkNotNull(createRandomUser(client).username) {
            "username маркерного пользователя должен присутствовать в details"
        }

        val injectionInUsername = client.createUser(
            mapOf(
                "username" to "admin'--",
                "email" to uniqueEmail(uniqueUsername()),
                "password" to validPassword()
            )
        )
        assertThat(injectionInUsername.statusCode)
            .describedAs("SQL-инъекция в username не должна приводить к 500. Тело: %s", injectionInUsername.asString())
            .isNotEqualTo(500)

        val injectionInEmail = client.createUser(
            mapOf(
                "username" to uniqueUsername(),
                "email" to "'; TRUNCATE TABLE users;--",
                "password" to validPassword()
            )
        )
        assertThat(injectionInEmail.statusCode)
            .describedAs("SQL-инъекция в email не должна приводить к 500. Тело: %s", injectionInEmail.asString())
            .isNotEqualTo(500)

        val list = getUsers(client)
        assertThat(list).describedAs("Ответ /user/get должен оставаться корректным JSON-массивом").isNotNull()

        assertThat(findUserByUsername(client, markerUsername))
            .describedAs("Маркерный пользователь %s должен остаться в БД — данные не должны быть удалены инъекцией", markerUsername)
            .isNotNull()
    }
}
