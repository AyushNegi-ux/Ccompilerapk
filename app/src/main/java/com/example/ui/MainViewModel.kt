package com.example.ui

import android.app.Application
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.compiler.CCompiler
import com.example.compiler.CompilerResult
import com.example.data.AppDatabase
import com.example.data.model.ChallengeEntity
import com.example.data.model.DailyActivityEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.UserStatsEntity
import com.example.data.repository.CCompilerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
    EDITOR("Editor"),
    TERMINAL("Terminal"),
    CHALLENGES("Practice"),
    PROGRESS("Streaks"),
    FILES("Projects")
}

data class ChallengeSubmissionResult(
    val isPassed: Boolean,
    val userOutput: String,
    val expectedOutput: String,
    val xpEarned: Int = 0,
    val message: String = ""
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = CCompilerRepository(
        database.projectDao(),
        database.challengeDao(),
        database.activityDao(),
        database.userStatsDao()
    )

    // Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.EDITOR)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // App Initialization State (Loading Screen)
    private val _isAppReady = MutableStateFlow(false)
    val isAppReady: StateFlow<Boolean> = _isAppReady.asStateFlow()

    // Current Project / Code
    private val _currentProjectId = MutableStateFlow<Int?>(1)
    val currentProjectId: StateFlow<Int?> = _currentProjectId.asStateFlow()

    private val _currentProjectTitle = MutableStateFlow("main.c")
    val currentProjectTitle: StateFlow<String> = _currentProjectTitle.asStateFlow()

    private val _editorCode = MutableStateFlow(TextFieldValue())
    val editorCode: StateFlow<TextFieldValue> = _editorCode.asStateFlow()

    // Challenge currently being practiced in editor
    private val _currentChallengeWorkingOn = MutableStateFlow<ChallengeEntity?>(null)
    val currentChallengeWorkingOn: StateFlow<ChallengeEntity?> = _currentChallengeWorkingOn.asStateFlow()

    // Stdin buffer for terminal
    private val _stdinInput = MutableStateFlow("")
    val stdinInput: StateFlow<String> = _stdinInput.asStateFlow()

    // Compiler Execution Result
    private val _compilerResult = MutableStateFlow<CompilerResult?>(null)
    val compilerResult: StateFlow<CompilerResult?> = _compilerResult.asStateFlow()

    private val _isCompiling = MutableStateFlow(false)
    val isCompiling: StateFlow<Boolean> = _isCompiling.asStateFlow()

    // Active Challenge Detail
    private val _activeChallenge = MutableStateFlow<ChallengeEntity?>(null)
    val activeChallenge: StateFlow<ChallengeEntity?> = _activeChallenge.asStateFlow()

    private val _challengeSubmission = MutableStateFlow<ChallengeSubmissionResult?>(null)
    val challengeSubmission: StateFlow<ChallengeSubmissionResult?> = _challengeSubmission.asStateFlow()

    // Reactive DB Data
    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChallenges: StateFlow<List<ChallengeEntity>> = repository.allChallenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStatsEntity?> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentActivities: StateFlow<List<DailyActivityEntity>> = repository.recentActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()

            // Load initial file
            val initialProject = repository.getProjectById(1)
            if (initialProject != null) {
                _currentProjectId.value = initialProject.id
                _currentProjectTitle.value = initialProject.title
                _editorCode.value = TextFieldValue(initialProject.code)
            }

            // Brief delay to showcase the loading screen with developer credits
            kotlinx.coroutines.delay(1800)
            _isAppReady.value = true
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun updateEditorCode(newVal: TextFieldValue) {
        _editorCode.value = newVal
    }

    fun updateStdinInput(newVal: String) {
        _stdinInput.value = newVal
    }

    fun runCode(isDebugMode: Boolean = false) {
        viewModelScope.launch {
            _isCompiling.value = true
            val code = _editorCode.value.text
            val stdin = _stdinInput.value

            val result = CCompiler.compileAndRun(
                sourceCode = code,
                stdinInput = stdin,
                isDebugMode = isDebugMode
            )
            _compilerResult.value = result
            _isCompiling.value = false

            // Record run for daily streak and stats
            repository.recordCodeRun()

            // Automatically switch to terminal tab to view results
            _currentTab.value = AppTab.TERMINAL
        }
    }

    fun clearTerminal() {
        _compilerResult.value = null
    }

    fun selectProject(project: ProjectEntity) {
        _currentChallengeWorkingOn.value = null
        _currentProjectId.value = project.id
        _currentProjectTitle.value = project.title
        _editorCode.value = TextFieldValue(project.code)
        _currentTab.value = AppTab.EDITOR
    }

    fun saveCurrentProject() {
        viewModelScope.launch {
            val id = _currentProjectId.value ?: 0
            val newId = repository.saveProject(
                title = _currentProjectTitle.value,
                code = _editorCode.value.text,
                id = id
            )
            _currentProjectId.value = newId.toInt()
        }
    }

    fun createNewProject(title: String = "untitled.c") {
        viewModelScope.launch {
            val starterTemplate = """#include <stdio.h>

int main() {
    printf("Hello from ${title}!\n");
    return 0;
}
"""
            val newId = repository.saveProject(title, starterTemplate)
            _currentProjectId.value = newId.toInt()
            _currentProjectTitle.value = title
            _editorCode.value = TextFieldValue(starterTemplate)
            _currentTab.value = AppTab.EDITOR
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
            if (_currentProjectId.value == project.id) {
                // Switch to first available project
                val remaining = allProjects.value.filter { it.id != project.id }
                if (remaining.isNotEmpty()) {
                    selectProject(remaining.first())
                } else {
                    createNewProject("main.c")
                }
            }
        }
    }

    fun openChallenge(challenge: ChallengeEntity) {
        _activeChallenge.value = challenge
        _challengeSubmission.value = null
    }

    fun closeChallengeDetail() {
        _activeChallenge.value = null
        _challengeSubmission.value = null
    }

    fun loadChallengeIntoEditor(challenge: ChallengeEntity) {
        _currentProjectTitle.value = "practice_ch_${challenge.id}.c"
        _currentProjectId.value = null // Not a saved custom project
        _currentChallengeWorkingOn.value = challenge
        _stdinInput.value = challenge.testInputs
        // Loads user's draft if existing, otherwise the clean starter template (NOT the solution!)
        _editorCode.value = TextFieldValue(challenge.userCode ?: challenge.starterCode)
        _activeChallenge.value = null
        _currentTab.value = AppTab.EDITOR
    }

    fun loadSolutionIntoEditor(challenge: ChallengeEntity) {
        _currentProjectTitle.value = "practice_ch_${challenge.id}.c"
        _currentProjectId.value = null
        _currentChallengeWorkingOn.value = challenge
        _stdinInput.value = challenge.testInputs
        _editorCode.value = TextFieldValue(
            if (challenge.solutionCode.isNotBlank()) challenge.solutionCode else challenge.starterCode
        )
        _activeChallenge.value = null
        _currentTab.value = AppTab.EDITOR
    }

    fun dismissCurrentChallengeBanner() {
        _currentChallengeWorkingOn.value = null
    }

    private fun isOutputMatching(userOutput: String, expectedOutput: String): Boolean {
        val uTrim = userOutput.trim()
        val eTrim = expectedOutput.trim()
        if (uTrim == eTrim) return true

        // Filter out prompts like "Enter...: " from lines
        val extractAnswers = { text: String ->
            text.lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map { line ->
                    if (line.contains(":") && (line.startsWith("Enter", ignoreCase = true) || line.startsWith("Input", ignoreCase = true))) {
                        line.substringAfter(":").trim()
                    } else {
                        line
                    }
                }
                .filter { it.isNotBlank() }
        }

        val uAns = extractAnswers(userOutput)
        val eAns = extractAnswers(expectedOutput)

        if (uAns.isNotEmpty() && uAns == eAns) return true

        // If all expected answer tokens/lines are found in user output
        if (eAns.isNotEmpty() && eAns.all { exp -> userOutput.contains(exp) }) {
            return true
        }

        return false
    }

    fun submitChallengeSolution(challengeId: Int, codeToTest: String) {
        viewModelScope.launch {
            _isCompiling.value = true
            val challenge = repository.getChallengeById(challengeId) ?: return@launch
            val testInput = challenge.testInputs
            val expectedOutput = challenge.expectedOutputs

            val result = CCompiler.compileAndRun(codeToTest, testInput)
            val userOutput = result.output

            val isPassed = result.isSuccess && isOutputMatching(userOutput, expectedOutput)
            var xpEarned = 0
            val message: String

            if (isPassed) {
                xpEarned = repository.completeChallenge(challengeId, codeToTest)
                message = "All test cases passed! You earned +$xpEarned XP 🔥"
            } else if (!result.isSuccess) {
                message = "Compilation or Runtime error occurred. Check syntax and logic."
            } else {
                message = "Output mismatch. Compare your program output with the expected output."
            }

            _challengeSubmission.value = ChallengeSubmissionResult(
                isPassed = isPassed,
                userOutput = userOutput,
                expectedOutput = expectedOutput,
                xpEarned = xpEarned,
                message = message
            )
            _isCompiling.value = false
        }
    }
}
