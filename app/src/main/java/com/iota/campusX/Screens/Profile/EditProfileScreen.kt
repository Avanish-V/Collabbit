package com.iota.campusX.Screens.Profile

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.Gender
import com.iota.campusX.Feature.UserProfile.data.University
import com.iota.campusX.Feature.UserProfile.data.UniversityDTO
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.R
import com.iota.campusX.Utils.CustomTextField
import com.iota.campusX.Utils.CustomTextFieldWithLeadingIcon
import com.iota.campusX.Utils.ProfileEdit
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.CourseDuration
import com.iota.campusX.ui.UIComponents.CustomDatePicker
import com.iota.campusX.ui.theme.Black400
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.Black900
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.background
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.secondary
import com.iota.campusX.ui.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel,
) {

    val editProfileViewModel: EditProfileViewModel = viewModel()
    val context = LocalContext.current
    val userProfileState = userProfileViewModel.userBaseProfile.collectAsState().value
    val universityListState = userProfileViewModel.universityData.collectAsState().value
    val modifyState = userProfileViewModel.modifyState.collectAsState().value
    val profileEditValue = navController.currentBackStackEntry?.savedStateHandle?.get<ProfileEdit>("PROFILE_EDIT")

    val userProfile = (userProfileState as? UiState.Success)?.data
    val universityList = (universityListState as? UiState.Success)?.data


    val snackBarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()
    var isLoading by rememberSaveable { mutableStateOf(false) }
    val focusManager = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var editComponent by remember { mutableStateOf("") }

    val pickedImage = remember { mutableStateOf<Uri?>(null) }
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                pickedImage.value = uri
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
    }

    LaunchedEffect(pickedImage.value) {
        if (pickedImage.value != null) {
            editComponent = "UPDATE_IMAGE"
        }
    }

    LaunchedEffect(modifyState) {
        when (modifyState) {
            is UiState.Loading -> {
                isLoading = true
            }
            is UiState.Success -> {
                navController.popBackStack()
            }
            is UiState.Error -> {
                scope.launch {
                    snackBarHostState.showSnackbar(
                        message = modifyState.message,
                        withDismissAction = true
                    )
                }
                isLoading = false
            }
            else -> {
                isLoading = false
            }
        }
    }


    when (profileEditValue) {

        ProfileEdit.PROFILE_SCREEN -> {

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Edit Profile") },
                        navigationIcon = {
                            IconButton(onClick = {
                                keyboardController?.hide()
                                navController.popBackStack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = secondary
                        ),
                        actions = {
                            Row(modifier = Modifier.padding(end = 12.dp)) {

                                if (editComponent.isNotEmpty()) {

                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = primary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        IconButton(onClick = {
                                            scope.launch {

                                                when (editComponent) {

                                                    "EDIT_NAME" -> {

                                                        if (userProfile?.userName == editProfileViewModel.name.value) return@launch

                                                        userProfileViewModel.modifyName(
                                                            editProfileViewModel.name.value
                                                        )

                                                    }

                                                    "EDIT_GENDER" -> {

                                                        userProfileViewModel.modifyGender(
                                                            editProfileViewModel.gender.value
                                                        )
                                                    }

                                                    "UPDATE_IMAGE" -> {

                                                        if (pickedImage.value == null) return@launch

                                                        userProfileViewModel.modifyProfileImage(
                                                            pickedImage.value!!
                                                        )
                                                    }

                                                }


                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null
                                            )
                                        }
                                    }

                                }

                            }
                        }
                    )
                },
                snackbarHost = {
                    SnackbarHost(hostState = snackBarHostState)
                },
                containerColor = secondary
            ) { innerPadding ->

                LaunchedEffect(Unit) {
                    editProfileViewModel.editName(userProfile?.userName ?: "")
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Card(
                            Modifier
                                .size(100.dp)
                                .border(
                                    width = 2.dp,
                                    color = secondary,
                                    shape = CircleShape
                                ),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            )

                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    modifier = Modifier.fillMaxSize(),
                                    model = if (pickedImage.value != null) pickedImage.value else userProfile?.userImage,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )

                                IconButton(onClick = {
                                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

                                }) {
                                    Icon(
                                        painter = painterResource(R.drawable.camera),
                                        contentDescription = null,
                                        tint = White900
                                    )
                                }

                            }
                        }

//                        IconButton(
//                            onClick = { /*TODO*/ },
//                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
//                        ) {
//                            Icon(
//                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
//                                contentDescription = null,
//                                tint = primary
//                            )
//                        }
//
//                        Card(
//                            Modifier
//                                .size(100.dp)
//                                .border(
//                                    width = 2.dp,
//                                    color = secondary,
//                                    shape = CircleShape
//                                ),
//                            shape = CircleShape,
//                            colors = CardDefaults.cardColors(
//                                containerColor = Color.Transparent
//                            )
//
//                        ) {
//
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxSize()
//                                    .padding(6.dp)
//                                    .clip(CircleShape),
//                                contentAlignment = Alignment.BottomEnd
//                            ) {
//                                Image(
//                                    modifier = Modifier.fillMaxSize(),
//                                    painter = painterResource(R.drawable.man),
//                                    contentDescription = null,
//                                    contentScale = ContentScale.Crop
//                                )
//
//                            }
//                        }


                    }

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(text = "Name", fontWeight = FontWeight.Bold)

                            Image(
                                modifier = Modifier.clickable(
                                    onClick = {
                                        editComponent = "EDIT_NAME"
                                        scope.launch {
                                            delay(1000)
                                        }
                                        focusManager.requestFocus()
                                        keyboardController?.show()
                                    },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ),
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Back",
                                colorFilter = ColorFilter.tint(primary)
                            )

                        }

                        CustomTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusManager),
                            value = editProfileViewModel.name.value,
                            onValueChange = { editProfileViewModel.editName(it.toString()) },
                            label = "",
                            placeHolder = "Enter your name",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            enabled = editComponent == "EDIT_NAME"
                        )


                    }

                    LaunchedEffect(Unit) {
                        editProfileViewModel.editGender(userProfile?.userGender ?: Gender.UNSPECIFIED )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(text = "Gender", fontWeight = FontWeight.Bold)

                            Image(
                                modifier = Modifier.clickable(
                                    onClick = {
                                        editComponent = "EDIT_GENDER"
                                    },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ),
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Back",
                                colorFilter = ColorFilter.tint(primary)
                            )

                        }

                        GenderSelector(
                            selectedGender = editProfileViewModel.gender.value,
                            onSelect = {
                                editProfileViewModel.editGender(it)
                            },
                            enabled = editComponent == "EDIT_GENDER"
                        )

                    }

                }


            }

        }

        ProfileEdit.EDIT_ABOUT_SCREEN -> {

            LaunchedEffect(Unit) {
                editProfileViewModel.editAbout(userProfile?.userBio.toString())
            }

            EditPage(
                onCancelClick = { navController.popBackStack() },
                onSubmitClick = {

                    if (editProfileViewModel.about.value.isEmpty()) return@EditPage Toast.makeText(
                        context,
                        "Bio cannot be empty",
                        Toast.LENGTH_SHORT
                    ).show()

                    scope.launch {
                        userProfileViewModel.modifyAbout(about = editProfileViewModel.about.value)
                    }
                },
                isLoading = isLoading
            ) {
                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editProfileViewModel.about.value,
                    onValueChange = { editProfileViewModel.editAbout(it.toString()) },
                    label = "About",
                    placeHolder = "What about you?",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }

        }

        ProfileEdit.EDIT_INTERESTS -> {

            val interestList by editProfileViewModel.items.collectAsState()
            val interestText = rememberSaveable { mutableStateOf("") }
            LaunchedEffect(Unit) {
                editProfileViewModel.setAllItems(userProfile?.interests ?: emptyList())
            }

            EditPage(
                onCancelClick = {navController.popBackStack() },
                onSubmitClick = {

                    if (interestList.isEmpty()) return@EditPage

                    scope.launch {
                        userProfileViewModel.updateInterests(interestList)
                    }
                },
                isLoading = isLoading,
                content = {

                    CustomTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = interestText.value,
                        onValueChange = { interestText.value = it.toString() },
                        label = "Interest",
                        placeHolder = "What about your interest?",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {

                                if (interestText.value.trim().isEmpty()) return@KeyboardActions
                                editProfileViewModel.addItem(interestText.value)
                                interestText.value = ""
                            }
                        )
                    )



                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        interestList.forEach {
                            AssistChip(
                                onClick = {

                                },
                                label = {
                                    Text(
                                        it,
                                        modifier = Modifier.padding(10.dp),
                                        color = Color.Black
                                    )
                                },
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = Color.LightGray
                                ),
                                trailingIcon = {
                                    Icon(
                                        modifier = Modifier.clickable(
                                            onClick = {
                                                editProfileViewModel.removeItem(it)
                                            },
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ),
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color.Black
                                    )
                                }
                            )
                        }

                    }


                }
            )
        }

        ProfileEdit.EDIT_CAMPUS -> {

            var selectedUni by remember { mutableStateOf<Pair<String, String>?>(null) }
            var uniSearchText by rememberSaveable { mutableStateOf("") }
            var isCalenderVisible by remember { mutableStateOf(false) }
            var calenderSwitch by rememberSaveable { mutableIntStateOf(0) }

            LaunchedEffect(Unit) {
                editProfileViewModel.editCampus(userProfile?.campus ?: Campus())
            }

            LaunchedEffect(Unit) {
                editProfileViewModel.editUniversity(
                    University(
                        university = selectedUni?.first ?: "",
                        logo = selectedUni?.second ?: ""
                    )
                )
            }

            EditPage(
                onCancelClick = { navController.popBackStack() },
                onSubmitClick = {
                    scope.launch {
                        userProfileViewModel.modifyCampus(editProfileViewModel.campus.value)
                    }
                },
                isLoading = isLoading
            ) {

                UniversityDropdown(
                    universityList = universityList,
                    userProfileViewModel = userProfileViewModel,
                    modifier = Modifier,
                    onUniversitySelected = {
                        editProfileViewModel.editUniversity(
                            University(
                                university = it?.first ?: "",
                                logo = it?.second ?: ""
                            )
                        )
                    },
                    campus = editProfileViewModel.campus.value,
                )

                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editProfileViewModel.campus.value.collegeName,
                    onValueChange = { editProfileViewModel.editCollege(it.toString()) },
                    label = "College",
                    placeHolder = "Enter your college",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                var selectedStudy by remember { mutableStateOf("") }

                AutoCompleteFieldOfStudyDropdown(
                    fieldOptions = fieldsOfStudy,
                    selectedField = selectedStudy,
                    onFieldChange = {
                        selectedStudy = it
                    },
                    label = "Field of study"
                )

                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editProfileViewModel.campus.value.campusCode?:"",
                    onValueChange = {
                        editProfileViewModel.editCampusCode(it.toString())
                    },
                    label = "College/University Code",
                    placeHolder = "Code",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editProfileViewModel.campus.value.degree?:"",
                    onValueChange = {
                        editProfileViewModel.editDegree(it.toString())
                    },
                    label = "Degree",
                    placeHolder = "Ex-Bachelor",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editProfileViewModel.campus.value.fieldOfStudy,
                    onValueChange = {
                        editProfileViewModel.editFieldOfStudy(it.toString())
                    },
                    label = "Field of study",
                    placeHolder = "Ex-Computer Science",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )



                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.titleMedium,
                        color = Black800,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(start = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${editProfileViewModel.campus.value.courseStart?.month?:"End"} ${editProfileViewModel.campus.value.courseStart?.year?:""}"
                            )
                            IconButton(onClick = {
                                calenderSwitch = 0
                                isCalenderVisible = !isCalenderVisible
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.calendar_1),
                                    contentDescription = null,
                                    tint = Black400
                                )
                            }
                        }

                        Text(
                            "-",
                            modifier = Modifier.padding(horizontal = 5.dp),
                            style = typography.headingMedium
                        )

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(start = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${editProfileViewModel.campus.value.courseEnd?.month?:"Start"} ${editProfileViewModel.campus.value.courseEnd?.year?:""}"
                            )
                            IconButton(onClick = {
                                calenderSwitch = 1
                                isCalenderVisible = !isCalenderVisible
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.calendar_1),
                                    contentDescription = null,
                                    tint = Black400
                                )
                            }
                        }

                    }
                }


                CustomDatePicker(
                    isVisible = isCalenderVisible,
                    onDismiss = {
                        isCalenderVisible = false
                    },
                    onDateSelected = {
                        if (calenderSwitch == 0) {
                            editProfileViewModel.editCourseStart(CourseDuration(month = it.first, year = it.second))
                        }
                        if (calenderSwitch == 1) {
                            editProfileViewModel.editCourseEnd(CourseDuration(month = it.first, year = it.second))
                        }
                        isCalenderVisible = false
                    }

                )



            }


        }

        null -> {

        }
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







@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversityDropdown(
    universityList: List<UniversityDTO>?,
    userProfileViewModel: UserProfileViewModel,
    modifier: Modifier = Modifier,
    onUniversitySelected: (Pair<String, String>?) -> Unit,
    campus: Campus
) {
    var selectedUniversity by rememberSaveable { mutableStateOf<Pair<String, String>?>(null) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    val universityListState by userProfileViewModel.universityData.collectAsState()

    val coroutineScope = rememberCoroutineScope()


    // Notify parent
    LaunchedEffect(selectedUniversity) {
        onUniversitySelected(selectedUniversity)
    }

    LaunchedEffect(Unit) {
        searchText = campus.university?.university ?: ""

    }
    // Re-request focus when dropdown expands and was previously focused
    LaunchedEffect(universityList) {
        if (isFocused) {
            coroutineScope.launch {
                delay(50) // Let composition settle
            }
        }
    }

    var expanded by remember { mutableStateOf(false) }


    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {

        CustomTextFieldWithLeadingIcon(
            modifier = Modifier.fillMaxWidth(),
            value = searchText,
            onValueChange = {
                searchText = it.toString()
                userProfileViewModel.onUniversityQueryChanged(it.toString())
            },
            label = "Search",
            enabled = true,
            placeHolder = "Search",
            leadingIcon = {
                CircleImage(
                    image = selectedUniversity?.second ?: (campus.university?.logo ?: ""),
                    modifier = Modifier.size(34.dp)
                ) { }
            },
            trailingIcon = {
                if (selectedUniversity != null) {
                    IconButton(onClick = {
                        selectedUniversity = null
                        searchText = ""
                        userProfileViewModel.resetUniversityData()
                    }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            keyboardActions = KeyboardActions { }
        )

        ExposedDropdownMenu(
            containerColor = Color.White,
            expanded = universityListState is UiState.Success,
            onDismissRequest = { expanded = false }
        ) {

            when(universityListState){

                is UiState.Success -> {

                    val universityList = (universityListState as UiState.Success<List<UniversityDTO>>).data

                    universityList.forEach { selectionOption ->
                        DropdownMenuItem(
                            onClick = {
                                selectedUniversity = Pair(selectionOption.name, selectionOption.logo)
                                searchText = selectedUniversity?.first ?: ""
                                onUniversitySelected(selectedUniversity)
                                userProfileViewModel.resetUniversityData()

                            },
                            text = {
                                Text(text = selectionOption.name)
                            },
                            leadingIcon = {
                                AsyncImage(
                                    model = selectionOption.logo,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                )
                            }
                        )
                    }
                }
                is UiState.Error -> {

                    Text(text = (universityListState as UiState.Error).toString())

                }
                is UiState.Loading -> {

                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = primary,
                        strokeWidth = 2.dp
                    )

                }
                else -> {}
            }

        }
    }

}


@Composable
fun EditPage(
    onCancelClick: (ProfileEdit) -> Unit,
    onSubmitClick: () -> Unit,
    isLoading: Boolean,
    content: @Composable () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(WindowInsets.statusBars.asPaddingValues())
            .background(color = White900),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(60.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onCancelClick(ProfileEdit.PROFILE_SCREEN) }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = onSubmitClick) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}


@Composable
fun ProfileComponent(
    title: String,
    onEditClick: () -> Unit,
    body: @Composable () -> Unit,
    contentDescription: String,
    isCurrentUser: Boolean,
    isContentExist: Boolean

) {

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(text = title, fontWeight = FontWeight.Bold)

            if (isCurrentUser) {
                Image(
                    modifier = Modifier.clickable(
                        onClick = { onEditClick.invoke() },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                    imageVector = if (isContentExist) Icons.Default.Add else Icons.Default.Edit,
                    contentDescription = "Back",
                    colorFilter = ColorFilter.tint(primary)
                )
            }

        }
        body.invoke()
    }


}

@Composable
fun GenderSelector(
    enabled: Boolean,
    selectedGender: Gender,
    onSelect: (Gender) -> Unit
) {

    val genders = listOf(Gender.MALE, Gender.FEMALE)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        genders.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(color = background, shape = RoundedCornerShape(5.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                RadioButton(
                    enabled = enabled,
                    selected = selectedGender == it,
                    onClick = { onSelect(it) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = primary,
                        unselectedColor = Black900
                    )
                )
                Text(text = it.name)
            }
        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestComponent(
    interestList: List<String>
) {

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (interestList.isNotEmpty()) {
            interestList.forEach {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.background(
                        color = background,
                        shape = RoundedCornerShape(10.dp)
                    )
                ) {
                    Text(text = it, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }

    }


}

val interestList = listOf("Coding", "Gaming", "Entrepreneur")


@Composable
fun AutoCompleteFieldOfStudyDropdown(
    modifier: Modifier = Modifier,
    fieldOptions: List<String>,
    selectedField: String,
    onFieldChange: (String) -> Unit,
    label: String = "Field of Study"
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var query by remember { mutableStateOf(selectedField) }
    var expanded by remember { mutableStateOf(false) }
    var filteredSuggestions by remember { mutableStateOf(emptyList<String>()) }

    // Debounce logic with coroutine
    LaunchedEffect(query) {
        snapshotFlow { query }
            .debounce(300) // 300ms debounce
            .collectLatest { typedText ->
                filteredSuggestions = fieldOptions.filter {
                    it.contains(typedText, ignoreCase = true)
                }.take(5)
                expanded = filteredSuggestions.isNotEmpty()
            }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                onFieldChange(it)
            },
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Toggle dropdown"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            filteredSuggestions.forEach { suggestion ->
                DropdownMenuItem(
                    onClick = {
                        query = suggestion
                        onFieldChange(suggestion)
                        expanded = false
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    text = {
                        Text(suggestion)
                    }
                )

            }
        }
    }
}
