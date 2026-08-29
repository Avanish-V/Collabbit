package com.iota.campusX.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.iota.campusX.ui.UIComponents.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowcaseScreen(
    title: String,
    navHostController: NavHostController,
    type: ShowcaseType
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            when (type) {
                ShowcaseType.OPPORTUNITIES -> {
                    items(opportunityList) { opp ->
                        OpportunityCard(
                            title = opp.title,
                            organization = opp.org,
                            deadline = opp.deadline,
                            color = opp.color
                        )
                    }
                }
                ShowcaseType.COURSES -> {
                    items(courseList) { course ->
                        CourseCard(
                            courseName = course.name,
                            instructor = course.instructor,
                            rating = course.rating,
                            students = course.students,
                            gradient = course.gradient
                        )
                    }
                }
                ShowcaseType.COLLABORATIONS -> {
                    items(collabList) { collab ->
                        CollaborationCard(
                            projectName = collab.name,
                            description = collab.desc,
                            tags = collab.tags,
                            membersCount = collab.members
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

enum class ShowcaseType {
    OPPORTUNITIES, COURSES, COLLABORATIONS
}

// Mock Data
data class OppData(val title: String, val org: String, val deadline: String, val color: Color)
val opportunityList = listOf(
    OppData("Google Summer of Code", "Google Open Source", "Mar 24, 2026", Color(0xFF4285F4)),
    OppData("UX Design Internship", "Airbnb Design", "Apr 10, 2026", Color(0xFFFF5A5F)),
    OppData("Research Assistant", "Stanford AI Lab", "May 01, 2026", Color(0xFF8C1515))
)

data class CourseData(val name: String, val instructor: String, val rating: Double, val students: String, val gradient: Brush)
val courseList = listOf(
    CourseData("Advanced Kotlin Flow", "Dr. Alexander Root", 4.9, "12k", Brush.horizontalGradient(listOf(Color(0xFF6200EE), Color(0xFFBB86FC)))),
    CourseData("UI UX Design Masterclass", "Sarah Jenkins", 4.8, "45k", Brush.horizontalGradient(listOf(Color(0xFF00C853), Color(0xFFB9F6CA)))),
    CourseData("Machine Learning Basics", "Andrew Ng", 5.0, "1M", Brush.horizontalGradient(listOf(Color(0xFF03A9F4), Color(0xFF81D4FA))))
)

data class CollabData(val name: String, val desc: String, val tags: List<String>, val members: Int)
val collabList = listOf(
    CollabData("EcoTracker App", "Building an app to track carbon footprint using real-time data.", listOf("Mobile", "Environment"), 12),
    CollabData("Campus Marketplace", "A peer-to-peer marketplace for university books and gear.", listOf("React", "NodeJS"), 8),
    CollabData("AI Study Buddy", "Developing a GPT-powered study assistant for students.", listOf("Python", "AI"), 15)
)
