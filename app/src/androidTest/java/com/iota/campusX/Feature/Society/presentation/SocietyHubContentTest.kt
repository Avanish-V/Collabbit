package com.iota.campusX.Feature.Society.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.iota.campusX.Feature.Society.domain.model.Community
import org.junit.Rule
import org.junit.Test

class SocietyHubContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun societyHubContent_displaysJoinedAndDiscoverCommunities() {
        val testJoined = listOf(
            Community(id = "c1", name = "Android Club", description = "Everything Android", category = "Tech", creatorId = "u1")
        )
        val testAll = listOf(
            Community(id = "c1", name = "Android Club", description = "Everything Android", category = "Tech", creatorId = "u1"),
            Community(id = "c2", name = "Music Society", description = "Jamming together", category = "Music", creatorId = "u2")
        )

        var chatNavigatedId: String? = null
        var infoNavigatedId: String? = null

        composeTestRule.setContent {
            LazyColumn {
                item {
                    Text("Your Societies")
                }
                items(testJoined.size) { index ->
                    Text(
                        text = testJoined[index].name,
                        modifier = Modifier.clickable {
                            chatNavigatedId = testJoined[index].id
                        }
                    )
                }
                item {
                    Text("Discover Societies")
                }
                items(testAll.size) { index ->
                    Text(
                        text = testAll[index].name,
                        modifier = Modifier.clickable {
                            infoNavigatedId = testAll[index].id
                        }
                    )
                }
            }
        }

        // Assert items display correctly
        composeTestRule.onNodeWithText("Your Societies").assertIsDisplayed()
        composeTestRule.onNodeWithText("Android Club").assertIsDisplayed()
        composeTestRule.onNodeWithText("Discover Societies").assertIsDisplayed()
        composeTestRule.onNodeWithText("Music Society").assertIsDisplayed()

        // Test clicks
        composeTestRule.onNodeWithText("Music Society").performClick()
        assert(infoNavigatedId == "c2")

        composeTestRule.onNodeWithText("Android Club").performClick()
        assert(chatNavigatedId == "c1")
    }
}
