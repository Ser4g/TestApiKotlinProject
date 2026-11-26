package org.example.tests

import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.example.client.UserApiClient
import org.example.utils.TestHelper.uniqueEmail
import org.example.utils.TestHelper.uniqueUsername
import org.example.utils.TestHelper.validPassword
import org.example.utils.asCreateUserResponse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class UserCreateTest {

    private val client = UserApiClient()

    @Test
    @DisplayName("Чек-лист №1: Успешная регистрация с валидными username, email, password")
    fun successfulRegistration() {
        val username = uniqueUsername()
        val email = uniqueEmail(username)
        val password = validPassword()

        val response: Response = client.createUser(username, email, password)

        assertThat(response.statusCode)
            .describedAs("Ожидался статус 200, получен %d. Тело ответа: %s", response.statusCode, response.asString())
            .isEqualTo(200)

        val body = response.asCreateUserResponse()

        assertThat(body.success).describedAs("success должен быть true").isTrue()

        val details = checkNotNull(body.details) {
            "details должен присутствовать в ответе. Тело: ${response.asString()}"
        }
        assertThat(details.username).describedAs("username в details").isEqualTo(username)
        assertThat(details.email).describedAs("email в details").isEqualTo(email)
        assertThat(details.password).describedAs("password в details").isNotBlank()
        assertThat(details.id).describedAs("id в details").isNotNull().isPositive()
        assertThat(details.createdAt).describedAs("created_at в details").isNotBlank()
        assertThat(details.updatedAt).describedAs("updated_at в details").isNotBlank()
    }

    @Test
    @DisplayName("Чек-лист №2: Пароль в ответе хеширован, не возвращается в открытом виде")
    fun passwordIsHashed() {
        val username = uniqueUsername()
        val password = validPassword()

        val response = client.createUser(username, uniqueEmail(username), password)

        assertThat(response.statusCode).isEqualTo(200)
        val details = checkNotNull(response.asCreateUserResponse().details) {
            "details должен присутствовать в ответе. Тело: ${response.asString()}"
        }

        assertThat(details.password)
            .describedAs("Пароль не должен совпадать с отправленным в открытом виде")
            .isNotEqualTo(password)
        assertThat(details.password)
            .describedAs("Пароль должен быть хеширован bcrypt (начинаться с \$2a\$)")
            .startsWith("\$2a\$")
    }
}
