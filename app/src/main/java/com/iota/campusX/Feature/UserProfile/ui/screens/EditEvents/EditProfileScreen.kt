package com.iota.campusX.Feature.UserProfile.ui.screens.EditEvents

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditBaseProfile.EditBaseProfileScreen
import com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditEducation.EditEducationScreen
import com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditEducation.EditEducationViewModel
import com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditSkills.EditSkillsScreen
import com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditSummary.EditSummaryScreen
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    editProfileViewModel: EditProfileViewModel
) {

    val snackBarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    val editAction by editProfileViewModel.editAction.collectAsState()


    when (editAction) {

        is EditProfileActions.EditBasicDetails->{

            EditBaseProfileScreen(
                navController = navController,
                editAction = editAction as EditProfileActions.EditBasicDetails,
                snackBarHostState = snackBarHostState
            )

        }
        is EditProfileActions.EditEducation->{
            val editEducationViewModel: EditEducationViewModel = koinViewModel()
            EditEducationScreen(
                initialEducation = (editAction as EditProfileActions.EditEducation).education,
                navController = navController,
                editEducationViewModel = editEducationViewModel,
                snackBarHostState = snackBarHostState
            )

        }
        is EditProfileActions.EditSkills->{
            EditSkillsScreen(
                editAction = editAction as EditProfileActions.EditSkills,
                navController = navController,
                editSkillsViewModel = koinViewModel(),
                snackBarHostState = snackBarHostState
            )
        }
        is EditProfileActions.EditSummary->{

            EditSummaryScreen(
                snackBarHostState = snackBarHostState,
                editAction = editAction as EditProfileActions.EditSummary,
                navController = navController
            )

        }

        else -> {}
    }
}

val fieldsOfStudy = listOf(
    "Computer Science & Engineering",
    "Mechanical Engineering",
    "Civil Engineering",
    "Electrical Engineering",
    "Electronics & Communication",
    "Information Technology",
    "Business Administration",
    "Law",
    "Medicine",
    "Pharmacy",
    "Data Science",
    "Cybersecurity"
)










