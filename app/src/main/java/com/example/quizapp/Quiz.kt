package com.example.quizapp

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun QuizPage(navController: NavController,
             appViewModel: AppViewModel,
             modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext
    println("Quiz page opened")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val questionsState by appViewModel.uiQuestionsState.collectAsState()
        val question = questionsState.questions[questionsState.questionNumber-1]


        // combine correct and incorrect answers and shuffle
        val answers = questionsState.answers
        if (!questionsState.shuffled) {
            answers.add(question.correctAnswer)
            question.wrongAnswers.forEach { answer ->
                answers.add(answer)
            }
            answers.shuffle()
            appViewModel.updateShuffledState(true)
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // quit button
            ElevatedButton(
                onClick = { appViewModel.updateQuitQuizState(true) },
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.dp, Color.Black),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 10.dp,
                    pressedElevation = 4.dp,
                    disabledElevation = 0.dp,
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.white_button_background),
                    contentColor = colorResource(R.color.blue_button_text))
            ) {
                Text(
                    text = stringResource(R.string.quiz_page_quit_button),
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal
                    ),
                )
            }

            // question number
            Card(
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color.Black),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.white_button_background),
                    contentColor = colorResource(R.color.blue_button_text)
                )
            ) {
                Text(
                    text = stringResource(R.string.quiz_page_question_number, questionsState.questionNumber),
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.CenterHorizontally))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // question
        Text(
            text = question.question,
            style = TextStyle(
                fontSize = 34.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal
            ),
            textAlign = TextAlign.Center
            )

        Spacer(modifier = Modifier.height(20.dp))

        val correctAnswerIndex = answers.indexOf(question.correctAnswer)
        // answer buttons
        for (i in 0..3) {
            var correct = false
            if (i == correctAnswerIndex) {
                correct = true
            }
            AnswerButton(appViewModel = appViewModel,
                answerText = answers[i],
                correct = correct,
                questionsState.correctAnswers,
                questionsState.answeredQuestion)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (questionsState.answeredQuestion) {
            // learning information about question
            Text(
                text = question.information,
                style = TextStyle(
                    fontSize = 28.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (questionsState.questionNumber < 10) {
                // next button
                ElevatedButton(
                    onClick = {
                        appViewModel.updateQuestionNumberState(questionsState.questionNumber + 1)
                        appViewModel.updateQuestionAnsweredState(false)
                        appViewModel.resetAnswers()
                        appViewModel.updateShuffledState(false)
                    },
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.dp, Color.Black),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 10.dp,
                        pressedElevation = 4.dp,
                        disabledElevation = 0.dp,
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.white_button_background),
                        contentColor = colorResource(R.color.blue_button_text))) {
                    Text(
                        text = stringResource(R.string.quiz_page_next_button),
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier
                            .padding(horizontal = 14.dp))
                }
            }
            else {
                // finish button
                ElevatedButton(
                    onClick = {
                        appViewModel.addUserAnswers(
                            questionsState.chosenSubject,
                            questionsState.correctAnswers,
                            context)
                        navController.navigate("stats-page")
                    },
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.dp, Color.Black),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 10.dp,
                        pressedElevation = 4.dp,
                        disabledElevation = 0.dp,
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.white_button_background),
                        contentColor = colorResource(R.color.blue_button_text))) {
                    Text(
                        text = stringResource(R.string.quiz_page_finish_button),
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier
                            .padding(horizontal = 14.dp))
                }
            }
        }

        // check whether to load popup to quit
        if (questionsState.quitQuiz) {
            QuitQuizPopup(navController = navController,
                appViewModel = appViewModel)
        }
    }
}

@Composable
fun AnswerButton(appViewModel: AppViewModel,
                 answerText: String,
                 correct: Boolean,
                 correctAnswers: Int,
                 answered: Boolean) {
    var backColor = colorResource(R.color.white_button_background)
    var textColor = colorResource(R.color.blue_button_text)

    // control the button color change on selection
    val selected = remember { mutableStateOf(false) }
    var enabled = remember { mutableStateOf(true) }

    // change green for correct answer
    if (selected.value && correct) {
        backColor = colorResource(R.color.right_answer_selected_container)
        textColor = colorResource(R.color.right_answer_selected_text)
    }
    // change red for wrong answer
    else if(selected.value) {
        backColor = colorResource(R.color.wrong_answer_selected_container)
        textColor = colorResource(R.color.wrong_answer_selected_text)
    }

    // disable other buttons on selection
    if (answered && !selected.value) {
        enabled.value = false
    }

    // reset colours and enable button on next question
    if (!answered) {
        backColor = colorResource(R.color.white_button_background)
        enabled.value = true
        selected.value = false
    }

    ElevatedButton(
        onClick = {
            appViewModel.updateQuestionAnsweredState(true)
            if (correct) {
                appViewModel.updateCorrectAnswers(correctAnswers+1)
            }
            selected.value = true
        },
        enabled = enabled.value,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.Black),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 10.dp,
            pressedElevation = 4.dp,
            disabledElevation = 0.dp,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = backColor,
            contentColor = textColor,
            disabledContainerColor = colorResource(R.color.white_button_background),
            disabledContentColor = colorResource(R.color.blue_button_text)),
        modifier = Modifier
            .fillMaxWidth()) {
        Text(
            text = answerText,
            style = TextStyle(
                fontSize = 28.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium
            ))
    }
}

@Composable
fun QuitQuizPopup(navController: NavController,
                  appViewModel: AppViewModel) {
    var buttonWidth by remember { mutableStateOf(0.dp) }
    val localDensity = LocalDensity.current
    // create popup for quit confirmation
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = stringResource(R.string.quiz_page_quit_text),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 30.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier
                    .padding(horizontal = 2.dp, vertical = 4.dp)
            )
        },
        confirmButton = {
            ElevatedButton(
                onClick = { navController.navigate("quiz-selection-page") },
                border = BorderStroke(1.dp, Color.Black),
            ) {
                Text(
                    text = stringResource(R.string.quiz_page_quit_confirm_button),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            buttonWidth = with(localDensity) { coordinates.size.width.toDp() }
                        }
                )
            }
        },
        dismissButton = {
            ElevatedButton(
                onClick = { appViewModel.updateQuitQuizState(false) },
                border = BorderStroke(1.dp, Color.Black),
            ) {
                Text(
                    text = stringResource(R.string.quiz_page_quit_cancel_button),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(buttonWidth)
                )
            }
        }
    )
}

@Composable
fun StatsPage(navController: NavController,
              appViewModel: AppViewModel,
              modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BackButton(
                navController = navController,
                navPage = "quiz-selection-page")
        }

        Spacer(modifier = Modifier.height(16.dp))

        val userAnswers = appViewModel.getUserAnswers()

        // display title text
        if (userAnswers.size == 0) {
            Text(
                text = stringResource(R.string.stats_page_no_stats_text),
                style = TextStyle(
                    fontSize = 34.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium
                ),
            )
        }
        else {
            Text(
                text = stringResource(R.string.stats_page_title),
                style = TextStyle(
                    fontSize = 34.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // display list of games
            LazyColumn(userScrollEnabled = true) {
                items(userAnswers.size) {i ->
                    val index = userAnswers.size - i - 1
                    GameHistoryBlock(
                        subject = userAnswers[index].subject,
                        dateTime = userAnswers[index].dateTime,
                        score = userAnswers[index].score
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun GameHistoryBlock(subject: String,
                     dateTime: String,
                     score: String) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val textStyle = TextStyle(
                fontSize = 22.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = subject,
                style = textStyle
            )
            Text(
                text = dateTime,
                style = textStyle,
                modifier = Modifier
                    .padding(4.dp)
            )
            Text(
                text = "$score/10",
                style = textStyle
            )
        }
    }
}