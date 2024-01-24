package com.example.quizapp

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quizapp.ui.theme.QuizAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var appViewModel: AppViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val context = LocalContext.current.applicationContext
                    appViewModel = viewModel()

                    // load question data
                    appViewModel.readInitialQuestionData(context)

                    // load calculator data
                    appViewModel.readInitialCalculatorData(context)

                    // load answer data
                    appViewModel.readQuizAnswerDataFromInternalStorage(context)

                    // load notes data
                    appViewModel.readNotesDataFromInternalStorage(context)

                    MainApp(appViewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        val context: Context = this
        // save data on destroy
        appViewModel.writeQuizAnswerDataToInternalStorage(this)
        appViewModel.writeNotesDataToInternalStorage(this)

        super.onDestroy()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainApp(appViewModel: AppViewModel, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    // modifier for every page
    val globalModifier = Modifier
        .paint(
            painter = painterResource(R.drawable.background),
            alignment = Alignment.Center,
            contentScale = ContentScale.FillBounds
        )
        .padding(16.dp)
    NavHost(
        navController = navController,
        startDestination = "home-page"
    ) {
        composable("home-page") { HomePage(navController, appViewModel, globalModifier) }
        composable("quiz-selection-page") { QuizSelectionPage(navController, appViewModel, globalModifier) }
        composable("stats-page") { StatsPage(navController, appViewModel, globalModifier) }
        composable("quiz-page") { QuizPage(navController, appViewModel, globalModifier) }
        composable("tool-selection-page") { ToolSelectionPage(navController, appViewModel, globalModifier) }
        composable("calculator-page") { CalculatorPage(navController, appViewModel, globalModifier) }
        composable("notes-page") { NotesPage(navController, appViewModel, globalModifier) }
        composable("timer-page") { TimerPage(navController, appViewModel, globalModifier) }
    }
}

@Composable
fun HomePage(navController: NavController,
             appViewModel: AppViewModel,
             modifier: Modifier = Modifier) {
    println("Home page opened")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        // title text
        Text(
            text = stringResource(R.string.home_page_welcome),
            style = TextStyle(
                fontSize = 38.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 2nd title text
        Text(
            text = stringResource(R.string.home_page_message),
            style = TextStyle(
                fontSize = 24.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(50.dp))

        // height and width variables of top button so bottom button can be the same
        val localDensity = LocalDensity.current
        var height by remember { mutableStateOf(0.dp) }
        var width by remember { mutableStateOf(0.dp) }


        val buttonTextMod = Modifier.padding(8.dp)

        // quiz selection button
        ElevatedButton(
            onClick = { navController.navigate("quiz-selection-page") },
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(1.dp, Color.Black),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp,
                pressedElevation = 4.dp,
                disabledElevation = 0.dp,
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.white_button_background),
                contentColor = colorResource(R.color.blue_button_text)),
            modifier = Modifier
                .fillMaxWidth()
                // sets height and width variables
                .onGloballyPositioned { coordinates ->
                    height = with(localDensity) { coordinates.size.height.toDp() }
                    width = with(localDensity) { coordinates.size.width.toDp() }
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.home_page_quiz_button_title),
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    modifier = buttonTextMod
                )
                Text(
                    text = stringResource(R.string.home_page_quiz_button_text),
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal),
                    textAlign = TextAlign.Center,
                    modifier = buttonTextMod
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // tool selection button
        ElevatedButton(
            onClick = { navController.navigate("tool-selection-page") },
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(1.dp, Color.Black),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp,
                pressedElevation = 4.dp,
                disabledElevation = 0.dp,
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.white_button_background),
                contentColor = colorResource(R.color.blue_button_text)),
            modifier = Modifier
                .fillMaxWidth()
                // set size to same as above button
                .size(height = height, width = width)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.home_page_tools_button_title),
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    modifier = buttonTextMod
                )

                Text(
                    text = stringResource(R.string.home_page_tools_button_text),
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal),
                    textAlign = TextAlign.Center,
                    modifier = buttonTextMod
                )
            }
        }
    }
}

@Composable
fun QuizSelectionPage(navController: NavController,
                      appViewModel: AppViewModel,
                      modifier: Modifier = Modifier) {
    println("Quiz selection page opened")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BackButton(
                navController = navController,
                navPage = "home-page")

            // stats button
            ElevatedButton(
                onClick = { navController.navigate("stats-page") },
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.dp, Color.Black),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 10.dp,
                    pressedElevation = 4.dp,
                    disabledElevation = 0.dp,
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.white_button_background),
                    contentColor = colorResource(R.color.blue_button_text)),
            ) {
                Text(
                    text = stringResource(R.string.quiz_selection_page_stats_button),
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // title
        Text(
            text = stringResource(R.string.quiz_selection_page_title),
            style = TextStyle(
                fontSize = 34.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium
            ),
        )


        Spacer(modifier = Modifier.height(20.dp))

        val spacing = 14

        // subject buttons
        // maths
        SubjectButton(
            subjectName = stringResource(R.string.quiz_selection_page_maths_button),
            navController = navController,
            appViewModel = appViewModel
        )

        Spacer(modifier = Modifier.height(spacing.dp))

        // english
        SubjectButton(
            subjectName = stringResource(R.string.quiz_selection_page_english_button),
            navController = navController,
            appViewModel = appViewModel
        )

        Spacer(modifier = Modifier.height(spacing.dp))

        // science
        SubjectButton(
            subjectName = stringResource(R.string.quiz_selection_page_science_button),
            navController = navController,
            appViewModel = appViewModel
        )

        Spacer(modifier = Modifier.height(spacing.dp))

        // geography
        SubjectButton(
            subjectName = stringResource(R.string.quiz_selection_page_geography_button),
            navController = navController,
            appViewModel = appViewModel
        )

        Spacer(modifier = Modifier.height(spacing.dp))

        // history
        SubjectButton(
            subjectName = stringResource(R.string.quiz_selection_page_history_button),
            navController = navController,
            appViewModel = appViewModel
        )

        Spacer(modifier = Modifier.height(spacing.dp))

        // general
        SubjectButton(
            subjectName = stringResource(R.string.quiz_selection_page_general_button),
            navController = navController,
            appViewModel = appViewModel
        )

    }
}

@Composable
fun SubjectButton(subjectName: String,
                  navController: NavController,
                  appViewModel: AppViewModel, ) {
    ElevatedButton(
        onClick = {
            appViewModel.loadQuestions(subjectName)
            navController.navigate("quiz-page")
        },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.Black),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 10.dp,
            pressedElevation = 4.dp,
            disabledElevation = 0.dp,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.white_button_background),
            contentColor = colorResource(R.color.blue_button_text)),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = subjectName,
            style = TextStyle(
                fontSize = 28.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier
                .padding(6.dp)
        )
    }
}

@Composable
fun ToolSelectionPage(navController: NavController,
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
                navPage = "home-page")
        }

        Spacer(modifier = Modifier.height(18.dp))

        // heading text
        Text(
            text = stringResource(R.string.tool_selection_page_title),
            style = TextStyle(
                fontSize = 34.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium
            ),
            )

        Spacer(modifier = Modifier.height(20.dp))

        val spacing = 14

        // calculator button
        ToolSelectionButton(navController = navController,
            appViewModel = appViewModel,
            pageName = "calculator-page",
            buttonText = stringResource(R.string.tool_selection_page_calculator_button))

        Spacer(modifier = Modifier.height(spacing.dp))

        // notes button
        ToolSelectionButton(navController = navController,
            appViewModel = appViewModel,
            pageName = "notes-page",
            buttonText = stringResource(R.string.tool_selection_page_notes_button))

        Spacer(modifier = Modifier.height(spacing.dp))

        // timer button
        ToolSelectionButton(navController = navController,
            appViewModel = appViewModel,
            pageName = "timer-page",
            buttonText = stringResource(R.string.tool_selection_page_timer_button))
    }
}

@Composable
fun ToolSelectionButton(navController: NavController,
                        appViewModel: AppViewModel,
                        pageName: String,
                        buttonText: String) {
    ElevatedButton(
        onClick = { navController.navigate(pageName) },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.Black),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 10.dp,
            pressedElevation = 4.dp,
            disabledElevation = 0.dp,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.white_button_background),
            contentColor = colorResource(R.color.blue_button_text)),
        modifier = Modifier
            .fillMaxWidth()
        ) {
            Text(
                text = buttonText,
                style = TextStyle(
                    fontSize = 28.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .padding(6.dp)
            )
    }
}

@Composable
fun BackButton(navController: NavController,
               navPage: String) {
    ElevatedButton(
        onClick = { navController.navigate(navPage) },
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, Color.Black),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 10.dp,
            pressedElevation = 4.dp,
            disabledElevation = 0.dp,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.white_button_background),
            contentColor = colorResource(R.color.blue_button_text)),
    ) {
        Text(
            text = stringResource(R.string.back),
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal
            ),
        )
    }
}