package org.example.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException

class StringOrListDeserializer : JsonDeserializer<List<String>>() {

    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext): List<String> {
        if (parser.currentToken() == JsonToken.VALUE_STRING) {
            return listOf(parser.getText())
        }
        @Suppress("UNCHECKED_CAST")
        return parser.readValueAs(List::class.java) as List<String>
    }
}
