package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ChallengeEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.DevBackground
import com.example.ui.theme.DevCardBorder
import com.example.ui.theme.DevPrimary
import com.example.ui.theme.DevPrimaryDark
import com.example.ui.theme.DevSecondary
import com.example.ui.theme.DevSurface
import com.example.ui.theme.DevSurfaceVariant
import com.example.ui.theme.DevTextMuted
import com.example.ui.theme.DevTextPrimary
import com.example.ui.theme.DevTextSecondary
import com.example.ui.theme.TermError
import com.example.ui.theme.TermSuccess

@Composable
fun ChallengesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val challenges by viewModel.allChallenges.collectAsState()
    val activeChallenge by viewModel.activeChallenge.collectAsState()
    val submissionResult by viewModel.challengeSubmission.collectAsState()
    val isCompiling by viewModel.isCompiling.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf(
        "All",
        "Ch 1: Basics",
        "Ch 2: Conditions",
        "Ch 3: Loops",
        "Ch 4: Functions",
        "Ch 5: Arrays",
        "Ch 6: Strings",
        "Ch 7: Pointers",
        "Ch 8: Structures"
    )

    val filteredChallenges = remember(challenges, selectedCategory) {
        if (selectedCategory == "All") challenges
        else challenges.filter { it.category == selectedCategory }
    }

    val solvedCount = challenges.count { it.isCompleted }
    val totalXp = challenges.filter { it.isCompleted }.sumOf { it.xpReward }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DevBackground)
    ) {
        // Curriculum Banner
        Surface(
            color = DevSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DevCardBorder)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "BCA C Practice Lab",
                        color = DevTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Chapter-wise challenges • Learn by doing it yourself!",
                        color = DevTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // XP pill
                    Box(
                        modifier = Modifier
                            .background(DevPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, DevPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⚡ $totalXp XP",
                            color = DevPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Solved pill
                    Box(
                        modifier = Modifier
                            .background(DevSecondary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, DevSecondary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = DevSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$solvedCount/${challenges.size} Solved",
                                color = DevSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (cat in categories) {
                val isSelected = selectedCategory == cat
                Surface(
                    onClick = { selectedCategory = cat },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) DevPrimary else DevSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) DevPrimary else DevCardBorder
                    ),
                    modifier = Modifier.testTag("filter_chip_$cat")
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color(0xFF0F172A) else DevTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Challenge List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredChallenges, key = { it.id }) { ch ->
                ChallengeCard(
                    challenge = ch,
                    onClick = { viewModel.openChallenge(ch) }
                )
            }
        }
    }

    // Practice Detail & Hint Modal
    if (activeChallenge != null) {
        ChallengeDetailDialog(
            challenge = activeChallenge!!,
            submissionResult = submissionResult,
            isCompiling = isCompiling,
            onClose = { viewModel.closeChallengeDetail() },
            onLoadInEditor = { viewModel.loadChallengeIntoEditor(activeChallenge!!) },
            onLoadSolutionInEditor = { viewModel.loadSolutionIntoEditor(activeChallenge!!) },
            onSubmit = { code -> viewModel.submitChallengeSolution(activeChallenge!!.id, code) }
        )
    }
}

@Composable
fun ChallengeCard(
    challenge: ChallengeEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (challenge.isCompleted) DevSurface.copy(alpha = 0.85f) else DevSurface
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (challenge.isCompleted) DevSecondary.copy(alpha = 0.6f) else DevCardBorder,
                RoundedCornerShape(10.dp)
            )
            .testTag("challenge_card_${challenge.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (challenge.isCompleted) DevSecondary.copy(alpha = 0.2f)
                        else DevSurfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (challenge.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Solved",
                        tint = DevSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "#${challenge.id}",
                        color = DevTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = challenge.title,
                    color = DevTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = challenge.category,
                        color = DevTextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "•",
                        color = DevTextMuted,
                        fontSize = 11.sp
                    )
                    val diffColor = when (challenge.difficulty) {
                        "Easy" -> Color(0xFF4ADE80)
                        "Medium" -> Color(0xFFFBBF24)
                        else -> Color(0xFFF87171)
                    }
                    Text(
                        text = challenge.difficulty,
                        color = diffColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Hint Pill & XP Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFBBF24).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "💡 Hint",
                        color = Color(0xFFFBBF24),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(DevPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+${challenge.xpReward} XP",
                        color = DevPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeDetailDialog(
    challenge: ChallengeEntity,
    submissionResult: com.example.ui.ChallengeSubmissionResult?,
    isCompiling: Boolean,
    onClose: () -> Unit,
    onLoadInEditor: () -> Unit,
    onLoadSolutionInEditor: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var showHint by remember { mutableStateOf(true) } // Hint is shown by default so students learn!
    var isSolutionRevealed by remember { mutableStateOf(false) } // Solution is hidden until requested!

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DevSurface,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxSize(0.92f)
                .border(1.dp, DevCardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = challenge.title,
                            color = DevTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${challenge.category} • ${challenge.difficulty} • +${challenge.xpReward} XP",
                            color = DevPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (challenge.isCompleted) {
                        Box(
                            modifier = Modifier
                                .background(DevSecondary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "SOLVED ✓",
                                color = DevSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(color = DevCardBorder, modifier = Modifier.padding(vertical = 10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Description
                    Text(
                        text = "Problem Statement",
                        color = DevTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = challenge.description,
                        color = DevTextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Test Cases Card
                    Text(
                        text = "Expected Input & Output",
                        color = DevTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DevSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, DevCardBorder, RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (challenge.testInputs.isNotEmpty()) {
                                Text(
                                    text = "Input:",
                                    color = DevTextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = challenge.testInputs,
                                    color = DevTextPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            Text(
                                text = "Expected Output:",
                                color = DevTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = challenge.expectedOutputs,
                                color = TermSuccess,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // DO IT YOURSELF - HINT CARD (Prominently displayed so they do it themselves to learn!)
                    Surface(
                        color = Color(0xFF262013),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showHint = !showHint },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "💡 Hint & Step-by-Step Logic",
                                        color = Color(0xFFFBBF24),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = if (showHint) "Hide" else "Show",
                                    color = Color(0xFFFBBF24),
                                    fontSize = 12.sp
                                )
                            }

                            if (showHint) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = challenge.hints,
                                    color = Color(0xFFFEF3C7),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "✍️ Try writing this logic yourself in the editor first to learn!",
                                    color = Color(0xFFFCD34D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // REVEAL SOLUTION SECTION (Hidden by default; option to show if they can't do it themselves)
                    Surface(
                        color = DevSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DevCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isSolutionRevealed = !isSolutionRevealed },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isSolutionRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = if (isSolutionRevealed) DevPrimary else DevTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isSolutionRevealed) "Hide Reference Solution" else "Stuck? Show Solution Code",
                                        color = if (isSolutionRevealed) DevPrimary else DevTextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = if (isSolutionRevealed) "▲" else "▼",
                                    color = DevTextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            if (!isSolutionRevealed) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try solving it yourself first! If you get stuck, you can reveal the working solution code here.",
                                    color = DevTextMuted,
                                    fontSize = 11.sp
                                )
                            } else {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Reference C Code:",
                                    color = DevPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = Color(0xFF0B0F17),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DevCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = challenge.solutionCode.ifEmpty { challenge.starterCode },
                                        color = DevTextPrimary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onLoadSolutionInEditor,
                                    colors = ButtonDefaults.buttonColors(containerColor = DevPrimaryDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Load Solution into Editor", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Submission Result Banner
                    if (submissionResult != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = if (submissionResult.isPassed) Color(0xFF0F291E) else Color(0xFF2E1515),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (submissionResult.isPassed) Color(0xFF10B981) else Color(0xFFEF4444)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (submissionResult.isPassed) "SUCCESS!" else "TEST FAILED",
                                    color = if (submissionResult.isPassed) TermSuccess else TermError,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = submissionResult.message,
                                    color = DevTextPrimary,
                                    fontSize = 12.sp
                                )
                                if (submissionResult.userOutput.isNotEmpty() && !submissionResult.isPassed) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Your Output:\n${submissionResult.userOutput}",
                                        color = DevTextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = DevCardBorder, modifier = Modifier.padding(vertical = 10.dp))

                // Bottom Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.9f).height(44.dp)
                    ) {
                        Text(text = "Close", color = DevTextSecondary, fontSize = 12.sp)
                    }

                    // Primary Action: Open Editor with starter skeleton to do it yourself!
                    Button(
                        onClick = onLoadInEditor,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DevPrimary),
                        modifier = Modifier.weight(1.6f).height(44.dp).testTag("open_in_editor_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Code Yourself ✍️",
                            color = Color(0xFF0F172A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Test Solution
                    Button(
                        onClick = { onSubmit(challenge.userCode ?: challenge.starterCode) },
                        enabled = !isCompiling,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DevSecondary),
                        modifier = Modifier.weight(1.2f).height(44.dp).testTag("submit_solution_button")
                    ) {
                        if (isCompiling) {
                            CircularProgressIndicator(
                                color = Color(0xFF0F172A),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Test",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
