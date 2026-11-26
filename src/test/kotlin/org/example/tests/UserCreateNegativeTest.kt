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

class UserCreateNegativeTest {

    private val client = UserApiClient()

    @Test
    @DisplayName("Чек-лист №3: Отсутствует поле username → 400, «A username is required»")
    fun missingUsername() {
        val response = client.createUser(
            mapOf(
                "email" to uniqueEmail(uniqueUsername()),
                "password" to validPassword()
            )
        )

        assertBadRequest(response, "A username is required")
    }

    @Test
    @DisplayName("Чек-лист №4: Отсутствует поле email → 400, «An Email is required»")
    fun missingEmail() {
        val response = client.createUser(
            mapOf(
                "username" to uniqueUsername(),
                "password" to validPassword()
            )
        )

        assertBadRequest(response, "An Email is required")
    }

    @Test
    @DisplayName("Чек-лист №5: Отсутствует поле password → 400, «A password is required»")
    fun missingPassword() {
        val response = client.createUser(
            mapOf(
                "username" to uniqueUsername(),
                "email" to uniqueEmail(uniqueUsername())
            )
        )

        assertBadRequest(response, "A password is required")
    }

    @Test
    @DisplayName("Чек-лист №6: Пустые значения полей (username=\"\", email=\"\", password=\"\") → 400, «A username is required»")
    fun emptyValues() {
        val response = client.createUser(
            mapOf(
                "username" to "",
                "email" to "",
                "password" to ""
            )
        )

        assertBadRequest(response, "A username is required")
    }

    @Test
    @DisplayName("Чек-лист №7: Регистрация с уже занятым username → 400, «This username is taken. Try another.»")
    fun duplicateUsername() {
        val username = uniqueUsername()
        val first = client.createUser(username, uniqueEmail(username), validPassword())
        assertThat(first.statusCode).describedAs("Первичная регистрация должна пройти успешно").isEqualTo(200)

        val second = client.createUser(username, uniqueEmail(uniqueUsername()), validPassword())

        assertBadRequest(second, "This username is taken. Try another.")
    }

    @Test
    @DisplayName("Чек-лист №8: Регистрация с уже занятым email → 400, «Email already exists»")
    fun duplicateEmail() {
        val username = uniqueUsername()
        val email = uniqueEmail(username)
        val first = client.createUser(username, email, validPassword())
        assertThat(first.statusCode).describedAs("Первичная регистрация должна пройти успешно").isEqualTo(200)

        val second = client.createUser(uniqueUsername(), email, validPassword())

        assertBadRequest(second, "Email already exists")
    }

    private fun assertBadRequest(response: Response, expectedMessage: String) {
        assertThat(response.statusCode)
            .describedAs("Ожидался статус 400, получен %d. Тело ответа: %s", response.statusCode, response.asString())
            .isEqualTo(400)

        val body = response.asCreateUserResponse()
        assertThat(body.success).describedAs("success должен быть false").isFalse()

        val message = checkNotNull(body.message) {
            "message должен присутствовать в ответе об ошибке. Тело: ${response.asString()}"
        }
        assertThat(message)
            .describedAs("Сообщение об ошибке должно содержать «%s»", expectedMessage)
            .contains(expectedMessage)
    }
}
