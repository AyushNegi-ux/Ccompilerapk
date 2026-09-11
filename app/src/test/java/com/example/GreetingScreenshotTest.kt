package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.ChallengeEntity
import com.example.ui.screens.ChallengeCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleChallenge = ChallengeEntity(
        id = 1,
        title = "Hello, C!",
        category = "Basics",
        difficulty = "Easy",
        description = "Print Hello World",
        starterCode = "",
        testInputs = "",
        expectedOutputs = "",
        hints = "",
        xpReward = 20,
        isCompleted = true
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        ChallengeCard(challenge = sampleChallenge, onClick = {})
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
