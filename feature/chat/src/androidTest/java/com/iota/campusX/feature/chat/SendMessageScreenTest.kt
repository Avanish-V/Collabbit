package com.iota.campusX.feature.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.iota.campusX.Feature.Chats.presentation.ui.MessageInputBar
import org.junit.Rule
import org.junit.Test

class SendMessageScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun messageInputBar_showsPlaceholderWhenEnabled() {
        composeTestRule.setContent {
            MessageInputBar(
                messageText = "",
                enabled = true,
                onMessageChange = {}
            )
        }
        composeTestRule.onNodeWithText("Type a message..").assertIsDisplayed()
    }

    @Test
    fun messageInputBar_showsWaitingPlaceholderWhenDisabled() {
        composeTestRule.setContent {
            MessageInputBar(
                messageText = "",
                enabled = false,
                onMessageChange = {}
            )
        }
        composeTestRule.onNodeWithText("Waiting for acceptance..").assertIsDisplayed()
    }
}
