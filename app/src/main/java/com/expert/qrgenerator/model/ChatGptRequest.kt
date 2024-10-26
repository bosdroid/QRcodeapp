package com.expert.qrgenerator.model

data class ChatGptRequest(
    val model: String,
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

data class ChatGptResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
