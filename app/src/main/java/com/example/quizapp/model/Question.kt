package com.example.quizapp.model

data class QuestionsState(
    val questions: List<Question> = listOf(),
    val questionNumber: Int = 1,
    val answeredQuestion: Boolean = false,
    val answers: MutableList<String> = mutableListOf(),
    val correctAnswers: Int = 0,
    val shuffled: Boolean = false,
    val quitQuiz: Boolean = false,
    val chosenSubject: String = ""
)

data class Question(
    val subject: String = "",
    val question: String = "",
    val correctAnswer: String = "",
    val wrongAnswers: List<String> = listOf(),
    val information: String = ""
)

data class Answer(
    val subject: String = "",
    val dateTime: String = "",
    val score: String = ""
)