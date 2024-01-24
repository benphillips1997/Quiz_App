package com.example.quizapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun CalculatorPage(navController: NavController,
                   appViewModel: AppViewModel,
                   modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val calculatorState by appViewModel.uiCalculatorState.collectAsState()

        val calculatorButtonText = appViewModel.getCalculatorButtonText()

        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BackButton(
                navController = navController,
                navPage = "tool-selection-page")
        }

        Spacer(modifier = Modifier.height(20.dp))

        calculatorState.userInput.forEach { item ->
            println("State: $item")
        }
        println("Output text: ${calculatorState.outputText}")

        // remove decimal places is they are 0
        var inputText = ""
        inputText = if (calculatorState.outputText.toDoubleOrNull() == null) {
    //            println("not int")
            calculatorState.outputText
        } else {
            if (calculatorState.outputText.toDouble().rem(1) == 0.0) {
    //                println("is int")
                calculatorState.outputText.toDouble().toInt().toString()
            } else {
                calculatorState.outputText
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // input
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = inputText,
                    style = TextStyle(
                        fontSize = 50.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier
                        .align(alignment = Alignment.End)
                        .padding(4.dp)
                )
            }

            // create calculator buttons as grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .padding(6.dp)
            ) {
                items(calculatorButtonText,
                    span = { item ->
                        var span = 1
                        // set '0' button to span 2 columns
                        if (item == "0") {
                            span = 2
                        }
                        else if (item == "AC") {
                            span = 3
                        }
                        GridItemSpan(span)
                    }) { item ->
                    CalculatorButton(
                        appViewModel = appViewModel,
                        currentInput = calculatorState.userInput,
                        outputText = calculatorState.outputText,
                        buttonText = item
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(appViewModel: AppViewModel,
                     currentInput: MutableList<String>,
                     outputText: String,
                     buttonText: String) {
    // width of button variable
    val localDensity = LocalDensity.current
    var width by remember { mutableStateOf(0.dp) }

    var buttonColor = colorResource(R.color.calculator_button_color)
    var textColor = colorResource(R.color.calculator_button_text)

    var span = 1

    // set span of AC and 0 buttons to correctly apply height
    if (buttonText == "AC") {
        span = 3
    }
    else if (buttonText == "0") {
        span = 2
    }

    Button(
        onClick = {
            val output = calculate(
                appViewModel,
                currentInput,
                buttonText,
                outputText)
            println(output[0])

            appViewModel.updateCalculatorInput(output)
        },
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = textColor
        ),
        modifier = Modifier
            .padding(2.dp)
            // set height to same as width
            .onGloballyPositioned { coordinates ->
                width = with(localDensity) { coordinates.size.width.toDp() }
            }
            .height(width / span)
    ) {
        Text(
            text = buttonText,
            style = TextStyle(
                fontSize = 22.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal
            ),
            overflow = TextOverflow.Visible
        )
    }
}

fun calculate(appViewModel: AppViewModel,
              currentInput: MutableList<String>,
              newInput: String,
              outputText: String): MutableList<String> {
    var output = currentInput

    // reset
    if (newInput == "AC") {
        appViewModel.updateCalculatorText("")
        return mutableListOf<String>("")
    }

    // don't allow multiple dots in a number
    if (newInput == ".") {
        return if (output[output.size-1].contains(".") || output[output.size-1] == "") {
            output
        } else {
            appViewModel.updateCalculatorText("$outputText.")
            output[output.size-1] += "."
            output
        }
    }

    // if current output is empty and input is a number or a minus then add it
    if (output[0] == "") {
        if (newInput.toDoubleOrNull() != null || newInput == "-") {
            appViewModel.updateCalculatorText("$outputText$newInput")
            output[output.size-1] += newInput
            return output
        }
    }

    // if previous input and this input was operator then replace it
    if (isOperator(newInput) && isOperator(output[output.size-1])) {
        appViewModel.updateCalculatorText(outputText.slice(IntRange(0, (outputText.length-2))) + newInput)
        output[output.size-1] = newInput
    }
    // if previous input was operator and this one isn't then push
    else if (isOperator(output[output.size-1])) {
        appViewModel.updateCalculatorText("$outputText$newInput")
        output.add(newInput)
    }
    // if last input and this input is a number then extend
    else if (output[output.size-1].toDoubleOrNull() != null && newInput.toDoubleOrNull() != null) {
        appViewModel.updateCalculatorText("$outputText$newInput")
        output[output.size-1] += newInput
    }
    // if last input was number and this is operator then push
    else if (output[output.size-1].toDoubleOrNull() != null && isOperator(newInput)) {
        appViewModel.updateCalculatorText("$outputText$newInput")
        output.add(newInput)
    }

    if (newInput == "=") {
        // remove leading operator unless it's a minus
        if (isOperator(output[0]) && output[0] != "-") {
            output.removeFirst()
        }

        // remove trailing operator
        if (isOperator(output[output.size-1])) {
            output.removeLast()
        }

        var sumTotal = 0.0

        var indexes = mutableListOf<Int>()

        // do multiplications and divisions
        output.forEachIndexed{ index, item ->
            when (item) {
                "x" -> {
                    val sum = output[index-1].toDouble() * output[index+1].toDouble()
                    output[index-1] = sum.toString()
                    output[index+1] = sum.toString()
                    output[index] = "!"
                    indexes.add(index-1)
                }
                "÷" -> {
                    val sum = output[index-1].toDouble() / output[index+1].toDouble()
                    output[index-1] = sum.toString()
                    output[index+1] = sum.toString()
                    output[index] = "!"
                    indexes.add(index-1)
                }
                else -> {
                    // is a number so do nothing
                }
            }
        }

        // check the minuses
        output.forEachIndexed { index, item ->
            if (item == "-") {
                output[index+1] = "-${output[index+1]}"
            }
        }

        // add total
        output.forEachIndexed { index, item ->
            if (item.toDoubleOrNull() != null) {
                if (!indexes.contains(index)) {
                    sumTotal += item.toDouble()
                }
            }
        }

        output = mutableListOf<String>(sumTotal.toString())
        appViewModel.updateCalculatorText("${sumTotal.toString()}")
    }

    return output
}

fun isOperator(input: String): Boolean {
    if (input == "÷" || input == "+" || input == "-" || input == "x") {
        return true
    }
    return false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesPage(navController: NavController,
              appViewModel: AppViewModel,
              modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row (
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ElevatedButton(
                onClick = {
                    navController.navigate("tool-selection-page")
                    appViewModel.writeNotesDataToInternalStorage(context)
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

        val notesState by appViewModel.uiNotesState.collectAsState()

        // notes
        TextField(
            value = notesState.notes,
            onValueChange = {
                appViewModel.updateNotesState(it)
            },
            textStyle = TextStyle(
                fontSize = 24.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp, bottom = 10.dp))
    }
}

@Composable
fun TimerPage(navController: NavController,
              appViewModel: AppViewModel,
              modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row (
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BackButton(
                navController = navController,
                navPage = "tool-selection-page")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            Spacer(modifier = Modifier.height(38.dp))

            // time management
            var seconds by remember { mutableIntStateOf(0) }
            var minutes by remember { mutableIntStateOf(0) }
            var hours by remember { mutableIntStateOf(0) }
            var run by remember { mutableStateOf(false) }

            // coroutine to manage time
            LaunchedEffect(run) {
                while (run) {
                    delay(1.seconds)
                    seconds++
                    if (seconds >= 60) {
                        seconds = 0
                        minutes += 1
                    }
                    if (minutes >= 60) {
                        minutes = 0
                        hours += 1
                    }
                }
            }

            // parse to string and add 0 if below 10 to keep minimum 2 0's
            val textSeconds = if (seconds < 10) {
                "0$seconds"
            }
            else {
                seconds.toString()
            }

            val textMinutes = if (minutes < 10) {
                "0$minutes"
            }
            else {
                minutes.toString()
            }

            val textHours = if (hours < 10) {
                "0$hours"
            }
            else {
                hours.toString()
            }

            val textTime = "$textHours:$textMinutes:$textSeconds"


            // time text
            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = textTime,
                    style = TextStyle(
                        fontSize = 44.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // start button
            ElevatedButton(
                onClick = {
                    run = true
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
                    text = stringResource(R.string.timer_page_start),
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // stop button
            ElevatedButton(
                onClick = {
                    run = false
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
                    text = stringResource(R.string.timer_page_stop),
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // reset button
            ElevatedButton(
                onClick = {
                    seconds = 0
                    minutes = 0
                    hours = 0
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
                    text = stringResource(R.string.timer_page_reset),
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
    }
}