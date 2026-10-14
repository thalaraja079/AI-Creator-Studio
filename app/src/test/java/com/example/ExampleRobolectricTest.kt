package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AiTool
import com.example.data.AiToolId
import com.example.data.CreatorPreferences
import com.example.data.GenerationHistoryItem
import com.example.ui.CreatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AI Creator Studio", appName)
  }

  @Test
  fun `all eight tools are configured with titles and presets`() {
    assertEquals(8, AiTool.ALL.size)
    val toolIds = AiTool.ALL.map { it.id }.toSet()
    assertTrue(toolIds.contains(AiToolId.YOUTUBE_SEO_TAGS))
    assertTrue(toolIds.contains(AiToolId.YOUTUBE_SCRIPT))
    assertTrue(toolIds.contains(AiToolId.IMAGE_PROMPT_CRAFTER))
    assertTrue(toolIds.contains(AiToolId.REELS_SHORTS_HOOKS))
    assertTrue(toolIds.contains(AiToolId.PRODUCT_AD_COPY))
    assertTrue(toolIds.contains(AiToolId.AI_TRANSLATOR_POLISHER))
    assertTrue(toolIds.contains(AiToolId.EMAIL_SPONSORSHIP_PITCH))
    assertTrue(toolIds.contains(AiToolId.CREATIVE_STORY_WRITER))

    AiTool.ALL.forEach { tool ->
      assertTrue(tool.title.isNotBlank())
      assertTrue(tool.placeholder.isNotBlank())
      assertTrue(tool.samplePrompts.isNotEmpty())
    }
  }

  @Test
  fun `creator preferences quota deduction and replenishment`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = CreatorPreferences(context)
    prefs.resetQuotaToFull()

    assertEquals(5, prefs.getDailyFreeQuota())
    assertEquals(5, prefs.getRemainingQuota())

    // Deduct 1 credit
    val deducted = prefs.deductCredit()
    assertTrue(deducted)
    assertEquals(4, prefs.getRemainingQuota())

    // Add 1 credit (simulating rewarded ad)
    val added = prefs.addCredit()
    assertTrue(added)
    assertEquals(5, prefs.getRemainingQuota())

    // Cannot add beyond max
    val addedBeyondMax = prefs.addCredit()
    assertFalse(addedBeyondMax)
    assertEquals(5, prefs.getRemainingQuota())
  }

  @Test
  fun `generation history serialization and clearing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = CreatorPreferences(context)
    prefs.clearHistory()
    assertEquals(0, prefs.getHistory().size)

    val item = GenerationHistoryItem(
      toolId = AiToolId.YOUTUBE_SEO_TAGS.name,
      toolTitle = "YouTube SEO & Viral Tags",
      emojiIcon = "📈",
      prompt = "AI video ideas",
      output = "### [TITLES]\n1. Best AI Tools"
    )

    prefs.saveHistoryItem(item)
    val history = prefs.getHistory()
    assertEquals(1, history.size)
    assertEquals("YouTube SEO & Viral Tags", history[0].toolTitle)
    assertEquals("AI video ideas", history[0].prompt)

    prefs.deleteHistoryItem(item.id)
    assertEquals(0, prefs.getHistory().size)
  }

  @Test
  fun `tool navigation state toggles detail screen`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = CreatorViewModel(context as Application)
    assertFalse(viewModel.uiState.value.isToolDetailOpen)

    viewModel.selectTool(AiToolId.IMAGE_PROMPT_CRAFTER, openDetail = true)
    assertTrue(viewModel.uiState.value.isToolDetailOpen)
    assertEquals(AiToolId.IMAGE_PROMPT_CRAFTER, viewModel.uiState.value.selectedToolId)

    viewModel.closeToolDetail()
    assertFalse(viewModel.uiState.value.isToolDetailOpen)
  }

  @Test
  fun `auth flow signup login and name update`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = CreatorViewModel(context as Application)

    // Sign up new user
    val signedUp = viewModel.signUp("Sarah Connor", "sarah@creator.ai", "secretPass123")
    assertTrue(signedUp)
    assertTrue(viewModel.uiState.value.isLoggedIn)
    assertEquals("Sarah Connor", viewModel.uiState.value.userName)
    assertEquals("sarah@creator.ai", viewModel.uiState.value.userEmail)

    // Update user profile name
    viewModel.updateUserName("Sarah C. Producer")
    assertEquals("Sarah C. Producer", viewModel.uiState.value.userName)

    // Log out
    viewModel.logout()
    assertFalse(viewModel.uiState.value.isLoggedIn)

    // Log back in
    val loggedIn = viewModel.login("sarah@creator.ai", "secretPass123")
    assertTrue(loggedIn)
    assertTrue(viewModel.uiState.value.isLoggedIn)
  }

  @Test
  fun `google play compliant delete account wipes all data`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = CreatorViewModel(context as Application)

    viewModel.signUp("John Doe", "john@doe.ai", "pass123")
    assertTrue(viewModel.uiState.value.isLoggedIn)

    // Add dummy history item
    val item = GenerationHistoryItem(
      toolId = "TEST",
      toolTitle = "Test Tool",
      emojiIcon = "🧪",
      prompt = "Hello",
      output = "World"
    )
    val prefs = CreatorPreferences(context)
    prefs.saveHistoryItem(item)
    assertTrue(prefs.getHistory().isNotEmpty())

    // Trigger delete account
    viewModel.deleteAccount()
    assertFalse(viewModel.uiState.value.isLoggedIn)
    assertEquals(0, prefs.getHistory().size)
    assertFalse(prefs.isLoggedIn())
  }
}
