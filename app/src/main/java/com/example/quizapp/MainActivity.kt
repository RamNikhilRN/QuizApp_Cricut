package com.example.quizapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// ----- Model Definitions -----

/**
 * Defining the four types of quiz questions.
 */
sealed class Question(val text: String) {
    class TrueFalse(text: String) : Question(text)
    class SingleChoice(text: String, val options: List<String>) : Question(text)
    class MultiChoice(text: String, val options: List<String>) : Question(text)
    class TextEntry(text: String) : Question(text)
}

/**
 * Holding the current question index and user answers.
 * logic-->When currentIndex equals questions.size, the quiz is complete.
 */
data class QuizState(
    val currentIndex: Int = 0,
    val answers: Map<Int, Any> = emptyMap()
)

// ----- ViewModel -----

/**
 * QuizViewModel persists the list of questions and the current quiz state,
 * ensuring state survives configuration changes (rotation, backgrounding).
 */
class QuizViewModel : ViewModel() {
    // List of quiz questions about Android.
    val questions = listOf(
        Question.TrueFalse("True or False: An Activity is destroyed during recomposition?"),
        Question.SingleChoice(
            "Which of the following is used to build UI in modern Android development?",
            listOf("XML", "Jetpack Compose", "Android Views", "Flutter")
        ),
        Question.MultiChoice(
            "Which of the following are Android Architecture Components?",
            listOf("LiveData", "ViewModel", "Room", "Fragment")
        ),
        Question.TextEntry("Describe what a ContentProvider is used for in Android.")
    )

    // Backing state for quiz progress. We update the whole map to trigger recomposition.
    var state by mutableStateOf(QuizState())
        private set

    /**
     * Updates the answer for the current question.
     */
    fun updateAnswer(answer: Any) {
        state = state.copy(answers = state.answers.toMutableMap().apply {
            put(state.currentIndex, answer)
        })
    }

    /**
     * Proceeds to the next question. On the last question, marks the quiz as complete.
     */
    fun nextQuestion() {
        if (state.currentIndex < questions.lastIndex) {
            state = state.copy(currentIndex = state.currentIndex + 1)
        } else if (state.currentIndex == questions.lastIndex) {
            state = state.copy(currentIndex = questions.size)
        }
    }

    /**
     * Resets the quiz to its initial state.
     */
    fun resetQuiz() {
        state = QuizState()
    }
}

// ----- Main Activity -----

/**
 * MainActivity sets the content view using Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                QuizApp()
            }
        }
    }
}

// ----- Composable Functions -----

/**
 * The main app composable.
 * It shows either the current quiz question or, if complete, the final screen.
 */
@Composable
fun QuizApp(viewModel: QuizViewModel = viewModel()) {
    val quizState = viewModel.state

    if (quizState.currentIndex < viewModel.questions.size) {
        QuizQuestionScreen(
            question = viewModel.questions[quizState.currentIndex],
            currentAnswer = quizState.answers[quizState.currentIndex],
            onAnswerSelected = { answer -> viewModel.updateAnswer(answer) },
            onNext = { viewModel.nextQuestion() },
            isLast = quizState.currentIndex == viewModel.questions.lastIndex
        )
    } else {
        FinalScreen(onRetry = { viewModel.resetQuiz() })
    }
}

/**
 * Displays a quiz question along with its answer options.
 * The "Next" button is enabled only when an answer is selected.
 */
@Composable
fun QuizQuestionScreen(
    question: Question,
    currentAnswer: Any?,
    onAnswerSelected: (Any) -> Unit,
    onNext: () -> Unit,
    isLast: Boolean
) {
    // Enable "Next" only if an answer is selected.
    val canProceed = when (question) {
        is Question.TrueFalse -> currentAnswer != null
        is Question.SingleChoice -> currentAnswer != null
        is Question.MultiChoice -> (currentAnswer as? Set<Int>)?.isNotEmpty() ?: false
        is Question.TextEntry -> (currentAnswer as? String)?.trim()?.isNotEmpty() ?: false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Apply padding so that the content does not cover the camera cutout/status bar.
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(16.dp)
    ) {
        Text(
            text = question.text,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Display UI for the given question type.
        when (question) {
            is Question.TrueFalse -> {
                TrueFalseQuestion(
                    selectedAnswer = currentAnswer as? Boolean,
                    onAnswerSelected = onAnswerSelected
                )
            }
            is Question.SingleChoice -> {
                SingleChoiceQuestion(
                    options = question.options,
                    selectedOption = currentAnswer as? Int,
                    onOptionSelected = onAnswerSelected
                )
            }
            is Question.MultiChoice -> {
                MultiChoiceQuestion(
                    options = question.options,
                    selectedOptions = currentAnswer as? Set<Int> ?: emptySet(),
                    onOptionsChanged = onAnswerSelected
                )
            }
            is Question.TextEntry -> {
                TextEntryQuestion(
                    text = currentAnswer as? String ?: "",
                    onTextChanged = onAnswerSelected
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // "Next" (or "Submit" on the last question) button.
        Button(
            onClick = onNext,
            enabled = canProceed,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = if (isLast) "Submit" else "Next")
        }
    }
}

/**
 * A True/False question UI.
 * By default, buttons use a light color (from the theme's surfaceVariant).
 * When selected, the button converts to a dark color (using the primary color).
 */
@Composable
fun TrueFalseQuestion(
    selectedAnswer: Boolean?,
    onAnswerSelected: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { onAnswerSelected(true) },
            colors = if (selectedAnswer == true)
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            else
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text("True")
        }
        Button(
            onClick = { onAnswerSelected(false) },
            colors = if (selectedAnswer == false)
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            else
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text("False")
        }
    }
}

/**
 * A single-choice question UI using radio buttons.
 */
@Composable
fun SingleChoiceQuestion(
    options: List<String>,
    selectedOption: Int?,
    onOptionSelected: (Int) -> Unit
) {
    Column {
        options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = (selectedOption == index),
                        onValueChange = { onOptionSelected(index) }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedOption == index),
                    onClick = { onOptionSelected(index) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(option)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * A multi-choice question UI using checkboxes.
 */
@Composable
fun MultiChoiceQuestion(
    options: List<String>,
    selectedOptions: Set<Int>,
    onOptionsChanged: (Set<Int>) -> Unit
) {
    Column {
        options.forEachIndexed { index, option ->
            val isChecked = selectedOptions.contains(index)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = isChecked,
                        onValueChange = { checked ->
                            val newSet = if (checked) selectedOptions + index else selectedOptions - index
                            onOptionsChanged(newSet)
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = null // Handled by toggleable.
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(option)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * A text-entry question UI with an outlined text field.
 */
@Composable
fun TextEntryQuestion(
    text: String,
    onTextChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChanged,
        label = { Text("Your Answer") },
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Add-on screen when the quiz is complete.
 * The final screen shown after quiz submission, displaying a thank you message
 * and a retry button.
 */
@Composable
fun FinalScreen(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Thank you for completing the quiz!",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
