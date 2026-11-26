package org.example.tests

import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.example.client.UserApiClient
import org.example.utils.TestHelper.createRandomUser
import org.example.utils.TestHelper.getUsers
import org.example.utils.TestHelper.uniqueEmail
import org.example.utils.TestHelper.uniqueUsername
import org.example.utils.TestHelper.validPassword
import org.example.utils.asUserList
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class UserGetTest {

    private val client = UserApiClient()

    @Test
    @DisplayName("Чек-лист №9: GET /user/get → 200, возвращается массив пользователей")
    fun getUsersReturnsArray() {
        val response: Response = client.getUsers()

        assertThat(response.statusCode)
            .describedAs("Ожидался статус 200, получен %d. Тело ответа: %s", response.statusCode, response.asString())
            .isEqualTo(200)

        val users = response.asUserList()
        assertThat(users).describedAs("Ответ должен быть JSON-массивом").isNotNull()
    }

    @Test
    @DisplayName("Чек-лист №10: Структура объекта пользователя — все поля присутствуют")
    fun userObjectHasAllFields() {
        createRandomUser(client)

        val users = getUsers(client)
        assertThat(users).describedAs("Список пользователей не должен быть пустым").isNotEmpty()

        assertThat(users).allSatisfy { user ->
            assertThat(user.id).describedAs("id пользователя %s", user.username).isNotNull()
            assertThat(user.username).describedAs("username пользователя id=%s", user.id).isNotNull()
            assertThat(user.email).describedAs("email пользователя id=%s", user.id).isNotNull()
            assertThat(user.password).describedAs("password пользователя id=%s", user.id).isNotNull()
            assertThat(user.createdAt).describedAs("created_at пользователя id=%s", user.id).isNotNull()
            assertThat(user.updatedAt).describedAs("updated_at пользователя id=%s", user.id).isNotNull()
        }
    }

    @Test
    @DisplayName("Чек-лист №11: Пароль в списке хеширован, не возвращается в открытом виде")
    fun passwordsInListAreHashed() {
        val username = uniqueUsername()
        val password = validPassword()
        val createResponse = client.createUser(username, uniqueEmail(username), password)
        assertThat(createResponse.statusCode).describedAs("Регистрация должна пройти успешно").isEqualTo(200)

        val users = getUsers(client)
        assertThat(users).describedAs("Список пользователей не должен быть пустым").isNotEmpty()

        assertThat(users)
            .describedAs("Ни один пароль в списке не должен совпадать с отправленным «%s» и должен быть bcrypt-хешем", password)
            .allSatisfy { user ->
                assertThat(user.password)
                    .isNotEqualTo(password)
                    .startsWith("\$2a\$")
            }
    }
}
