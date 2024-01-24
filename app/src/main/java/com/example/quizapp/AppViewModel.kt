package com.example.quizapp

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.quizapp.model.Answer
import com.example.quizapp.model.CalculatorState
import com.example.quizapp.model.Notes
import com.example.quizapp.model.Question
import com.example.quizapp.model.QuestionsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AppViewModel: ViewModel() {
    // ------------------------- Question management ----------------------
    private val _uiQuestionsState = MutableStateFlow(QuestionsState())
    val uiQuestionsState: StateFlow<QuestionsState> = _uiQuestionsState.asStateFlow()

    private var allQuestions = mutableListOf<Question>()

    fun loadQuestions(subject: String) {
//        println(subject)
        _uiQuestionsState.update{ currentState ->
            currentState.copy(
                questions = getRandomQuestions(subject),
                questionNumber = 1,
                answeredQuestion = false,
                answers = mutableListOf(),
                correctAnswers = 0,
                shuffled = false,
                quitQuiz = false,
                chosenSubject = subject
            )
        }
    }

    // returns 10 questions of a subject
    private fun getRandomQuestions(subject: String): MutableList<Question> {
//        println("Getting questions")
//        println("Subject: $subject")
        // filter questions of a certain subject
        val subjectQuestions: MutableList<Question>
        if (subject != "General") {
            val questions = allQuestions
            subjectQuestions = mutableListOf<Question>()
            questions.forEach { question ->
                if (question.subject == subject) {
                    subjectQuestions.add(question)
                }
            }
        }
        else {
            subjectQuestions = allQuestions
        }

//        println("Subject questions size: " + subjectQuestions.size)

        // randomly select 10 questions
        val usingQuestions = mutableListOf<Question>()
        for (i in 1..10) {
            val end = subjectQuestions.size - 1
            val randomIndex = (0..end).random()
            usingQuestions.add(subjectQuestions[randomIndex])
            subjectQuestions.removeAt(randomIndex)
        }
//        println("Questions returned: " + usingQuestions.size)
        return usingQuestions
    }

    fun updateQuestionNumberState(questionNumber: Int) {
        _uiQuestionsState.update{ currentState ->
            currentState.copy(questionNumber = questionNumber)
        }
    }

    fun updateQuestionAnsweredState(answered: Boolean) {
        _uiQuestionsState.update{ currentState ->
            currentState.copy(answeredQuestion = answered)
        }
    }

    fun resetAnswers() {
        _uiQuestionsState.update{ currentState ->
            currentState.copy(answers = mutableListOf())
        }
    }

    fun updateCorrectAnswers(correctAnswers: Int) {
        _uiQuestionsState.update{ currentState ->
            currentState.copy(correctAnswers = correctAnswers)
        }
    }

    fun updateShuffledState(shuffled: Boolean) {
        _uiQuestionsState.update{ currentState ->
            currentState.copy(shuffled = shuffled)
        }
    }

    fun updateQuitQuizState(quitQuiz: Boolean) {
        _uiQuestionsState.update{ currentState ->
            currentState.copy(quitQuiz = quitQuiz)
        }
    }


    fun readInitialQuestionData(context: Context) {
        // open initial data
        val inputStream = context.resources.openRawResource(R.raw.initial_question_data)
        val reader = BufferedReader(InputStreamReader(inputStream))

        val questionList = mutableListOf<Question>()

        reader.forEachLine { line ->
            val items = line.split(",")
            val wrongAnswerList = (items[3].split(";"))
            questionList.add(Question(items[0], items[1], items[2], wrongAnswerList, items[4]))
        }

        allQuestions = questionList
    }








    // --------------------------- User question answer management ------------------------


    private var userAnswers = mutableListOf<Answer>()

    fun getUserAnswers(): MutableList<Answer> {
        return userAnswers
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addUserAnswers(subject: String, score: Int, context: Context) {
        // get current date and time
        val format = DateTimeFormatter.ofPattern("dd-MM-yyyy  HH:mm")
        val dateTime = LocalDateTime.now().format(format)

        userAnswers.add(Answer(subject, dateTime.toString(), score.toString()))
        writeQuizAnswerDataToInternalStorage(context)
    }


    fun writeQuizAnswerDataToInternalStorage(context: Context) {
        val fileName = "quiz_answer_data.csv"
        val outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)

        userAnswers.forEach{ answer ->
            val data = "${answer.subject},${answer.dateTime},${answer.score}\n"
            outputStream.write(data.toByteArray())
        }
        outputStream.close()
//        println("Wrote data to storage")
    }

    // updates data in model with internal storage data
    fun readQuizAnswerDataFromInternalStorage(context: Context) {
        val fileName = "quiz_answer_data.csv"

        val answerList = mutableListOf<Answer>()

        try {
            val inputStream = context.openFileInput(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.forEachLine { line ->
                val items = line.split(",")
                answerList.add(Answer(items[0], items[1], items[2]))
            }
        }
        catch(e: FileNotFoundException) {
            val outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            outputStream.close()
        }

        userAnswers = answerList
    }



// --------------------------- User notes data management ------------------------

    private val _uiNotesState = MutableStateFlow(Notes())
    val uiNotesState: StateFlow<Notes> = _uiNotesState.asStateFlow()

    fun updateNotesState(newNotes: String) {
        _uiNotesState.update{ currentState ->
            currentState.copy(notes = newNotes)
        }
    }

    fun writeNotesDataToInternalStorage(context: Context) {
        val fileName = "notes_data.txt"
        val outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)

        val data = _uiNotesState.value.notes
//        println("Notes state: ${_uiNotesState.value.notes}\nData: $data")
        outputStream.write(data.toByteArray())
        outputStream.close()
    }

    // updates data in model with internal storage data
    fun readNotesDataFromInternalStorage(context: Context) {
        val fileName = "notes_data.txt"

        var notesData = ""

        try {
            val inputStream = context.openFileInput(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.forEachLine { line ->
                notesData += line
            }
        }
        catch(e: FileNotFoundException) {
            val outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            outputStream.close()
        }
        updateNotesState(notesData)
    }






    // ----------------------- Calculator management -------------------------
    private val _uiCalculatorState = MutableStateFlow(CalculatorState())
    val uiCalculatorState: StateFlow<CalculatorState> = _uiCalculatorState.asStateFlow()

    private var calculatorButtonText: MutableList<String> = mutableListOf()

    fun getCalculatorButtonText(): MutableList<String> {
//        println("Calc text size: " + calculatorButtonText.size)
        return calculatorButtonText
    }

    fun updateCalculatorInput(input: MutableList<String>) {
        _uiCalculatorState.update{ currentState ->
            currentState.copy(userInput = input)
        }
    }

    fun updateCalculatorText(text: String) {
        _uiCalculatorState.update{ currentState ->
            currentState.copy(outputText = text)
        }
    }

    fun readInitialCalculatorData(context: Context) {
        // open initial data
        val inputStream = context.resources.openRawResource(R.raw.initial_calculator_data)
        val reader = BufferedReader(InputStreamReader(inputStream))

        val calculatorText = mutableListOf<String>()

        reader.forEachLine{ line ->
            println("Line: $line")
            val split = line.split(",")
            split.forEach{ symbol ->
                calculatorText.add(symbol)
            }
        }

        calculatorButtonText = calculatorText
    }
}