package com.example.quizapp.model

data class CalculatorState(
    val userInput: MutableList<String> = mutableListOf<String>(""),
    val outputText: String = ""
)
