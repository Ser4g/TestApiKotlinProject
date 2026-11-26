package org.example.tests

import org.assertj.core.api.Assertions.assertThat
import org.example.client.UserApiClient
import org.example.model.CreateUserResponse
import org.example.model.UserDto
import org.example.utils.TestHelper.createRandomUser
import org.example.utils.TestHelper.findUserByUsername
import org.example.utils.TestHelper.getUsers
import org.example.utils.TestHelper.uniqueEmail
import org.example.utils.TestHelper.uniqueUsername
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class UserFlowTest {

    private val client = UserApiClient()

    private val USER_COUNT = 3

    @Test
    @DisplayName("Чек-лист №12: create → get → пользователь присутствует с корректными данными")
    fun createdUserIsPresentInList() {
        val details = createRandomUser(client)
        val username = checkNotNull(details.username) { "username должен присутствовать в details" }

        val created = checkNotNull(findUserByUsername(client, username)) {
            "Пользователь $username должен присутствовать в /user/get"
        }
        assertThat(created.username).isEqualTo(username)
        assertThat(created.email).describedAs("email в /user/get").isEqualTo(details.email)
        assertThat(created.id).describedAs("id в /user/get должен совпадать с id из ответа create").isEqualTo(details.id)
        assertThat(created.password).describedAs("Пароль должен быть хеширован").startsWith("\$2a\$")
        assertThat(created.createdAt).describedAs("created_at").isNotBlank()
        assertThat(created.updatedAt).describedAs("updated_at").isNotBlank()
    }

    @Test
    @DisplayName("Чек-лист №13: Несколько регистраций — все пользователи в /user/get, id уникальны и инкрементируются")
    fun multipleRegistrations() {
        val usersBefore = getUsers(client)
        val maxIdBefore = usersBefore.mapNotNull(UserDto::id).maxOrNull() ?: 0

        val created = ArrayList<CreateUserResponse.Details>()
        repeat(USER_COUNT) {
            created.add(createRandomUser(client))
        }

        val ids = created.map(CreateUserResponse.Details::id)
        assertThat(ids).describedAs("id должны быть уникальны").doesNotHaveDuplicates()
        assertThat(ids)
            .describedAs("id должны инкрементироваться: каждый новый id больше максимального существовавшего (%d)", maxIdBefore)
            .allSatisfy { id -> assertThat(id).isGreaterThan(maxIdBefore) }

        for (details in created) {
            val username = checkNotNull(details.username) { "username должен присутствовать в details" }
            val user = checkNotNull(findUserByUsername(client, username)) {
                "Пользователь $username должен присутствовать в /user/get"
            }
            assertThat(user.id).describedAs("id в /user/get для %s", username).isEqualTo(details.id)
            assertThat(user.email).describedAs("email в /user/get для %s", username).isEqualTo(details.email)
        }
    }

    @Test
    @DisplayName("Чек-лист №14: create (ошибка) → get: пользователь НЕ добавлен в список")
    fun failedCreateDoesNotAddUser() {
        val username = uniqueUsername()

        val failedResponse = client.createUser(
            mapOf(
                "username" to username,
                "email" to uniqueEmail(username)
            )
        )
        assertThat(failedResponse.statusCode)
            .describedAs("Регистрация без password должна завершиться ошибкой")
            .isEqualTo(400)

        assertThat(findUserByUsername(client, username))
            .describedAs("Пользователь %s не должен быть добавлен в /user/get после неуспешной регистрации", username)
            .isNull()
    }
}
