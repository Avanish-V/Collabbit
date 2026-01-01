package com.iota.campusX.Feature.UserProfile.ui.screens

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Campus
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Duration
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Gender
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.University
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.UniversityDTO
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.UpdateProfileDTO
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.EditProfileType
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.EditProfileViewModel
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.UpdateProfileViewModel
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.UserProfileViewModel
import com.iota.campusX.R
import com.iota.campusX.Utils.CircularLoading
import com.iota.campusX.Utils.CustomTextField
import com.iota.campusX.Utils.CustomTextFieldWithLeadingIcon
import com.iota.campusX.Utils.ProfileEdit
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.AutoCompleteFieldOfStudyDropdown
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.CourseDurationPicker
import com.iota.campusX.ui.UIComponents.EditProfileIconButton
import com.iota.campusX.ui.UIComponents.SubmitButton
import com.iota.campusX.ui.UIComponents.UserAvatar
import com.mr0xf00.easycrop.CropError
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.CropperStyle
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.rememberImagePicker
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import saveBitmapToCache

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel = koinInject(),
    updateProfileViewModel: UpdateProfileViewModel = koinInject()
) {

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val editProfileViewModel: EditProfileViewModel = viewModel()

    val editType = editProfileViewModel.editType.collectAsState()
    val updateProfile = editProfileViewModel.updateProfile.collectAsState()

    val universityListState by updateProfileViewModel.universityData.collectAsState()

    val profileState = userProfileViewModel.userBaseProfile.collectAsState().value
    val modifyState = updateProfileViewModel.state.collectAsState().value
    val profileEditValue = navController.currentBackStackEntry?.savedStateHandle?.get<ProfileEdit>("PROFILE_EDIT")

    val userProfile = when(profileState){
        is UiState.Success<*> -> {
            (profileState as UiState.Success<BaseProfileDTO>).data
        }
        else -> {
            null
        }
    }

    var isLoading by rememberSaveable { mutableStateOf(false) }
    val pickedImage = remember { mutableStateOf<Uri?>(null) }


    val imageCropper = rememberImageCropper()

    val imagePicker = rememberImagePicker(onImage = { uri ->
        scope.launch {
            val result = imageCropper.crop(uri, context)
            when (result) {
                CropError.LoadingError -> {
                    snackBarHostState.showSnackbar("Error")
                }
                CropError.SavingError -> {
                    snackBarHostState.showSnackbar("Error")
                }
                CropResult.Cancelled -> {
                }
                is CropResult.Success -> pickedImage.value = saveBitmapToCache(context,result.bitmap.asAndroidBitmap())
            }
        }
    })

    LaunchedEffect(modifyState) {
        when (modifyState) {
            is UiState.Loading -> {
                isLoading = true
            }
            is UiState.Success -> {
                editProfileViewModel.editTypeSetNull()
                navController.popBackStack()
            }
            is UiState.Error -> {
                snackBarHostState.showSnackbar(
                    message = modifyState.message,
                )
                isLoading = false
            }
            else -> {
                isLoading = false
            }
        }
    }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            editProfileViewModel.editName(userProfile.name)
            editProfileViewModel.editGender(userProfile.gender)
            editProfileViewModel.editTagline(userProfile.tagline)
        }
    }

    LaunchedEffect(pickedImage.value) {
        pickedImage.value?.let {
            editProfileViewModel.editType(EditProfileType.UserImage(it))
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
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        actions = {
                            Row(modifier = Modifier.padding(end = 12.dp)) {

                                Log.d("EditProfileScreen", "EditProfileScreen: ${updateProfile.value}")

                                if (updateProfile.value == null) return@TopAppBar

                                IconButton(
                                    onClick = {
                                    scope.launch {

                                        keyboardController?.hide()

                                        updateProfileViewModel.modifyProfile(
                                            mutation = UpdateProfileViewModel.ProfileMutation.UpdateProfile(updateProfile.value)
                                        )

                                    }
                                },
                                ) {
                                    if (isLoading){
                                        CircularLoading(MaterialTheme.colorScheme.primary)
                                    }else{
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    )
                },
                snackbarHost = {
                    SnackbarHost(hostState = snackBarHostState)
                },
            ) { innerPadding ->

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(contentAlignment = Alignment.BottomEnd){

                        Box(modifier = Modifier
                            .size(80.dp)

                            .border(
                                width = 6.dp,
                                color = Color.White,
                                shape = MaterialTheme.shapes.small
                            )
                            .shadow(
                                elevation = 6.dp,
                                shape = MaterialTheme.shapes.small
                            )
                            .clip(
                                MaterialTheme.shapes.large
                            )
                        ){

                            UserAvatar(
                                imageUrl = (if (pickedImage.value == null) userProfile?.image else pickedImage.value) as String?,
                                bgColor = userProfile?.bgColor ?: "",
                                visibilityMode = VisibilityMode.USER,
                                modifier = Modifier.fillMaxSize()
                            )

                        }

                        IconButton(
                            onClick = {
                                imagePicker.pick(
                                    mimetype =  "image/*"
                                )
                            },
                            modifier = Modifier.offset(x = 8.dp,y = 8.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ){
                            Icon(
                                painter = painterResource(R.drawable.outline_camera_alt_24),
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }

                    CustomTextField(
                        modifier = Modifier.fillMaxWidth().focusRequester(focusManager),
                        value = editProfileViewModel.name.value,
                        onValueChange = {
                            editProfileViewModel.editName(it.toString())
                            editProfileViewModel.updateProfileFields(UpdateProfileDTO(updateUserName = it.toString()))
                        },
                        label = "Name",
                        placeHolder = "Enter your name",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    )

                    CustomTextField(
                        modifier = Modifier.fillMaxWidth().focusRequester(focusManager),
                        value = editProfileViewModel.tagline.value,
                        onValueChange = {
                            editProfileViewModel.editTagline(it.toString())
                            editProfileViewModel.updateProfileFields(UpdateProfileDTO(updateTagline = it.toString()))
                        },
                        label = "Tagline",
                        placeHolder = "Add a tagline",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    )


                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        Text(text = "Gender", fontWeight = FontWeight.Bold)

                        GenderSelector(
                            selectedGender = editProfileViewModel.gender.value,
                            onSelect = {
                                editProfileViewModel.editGender(it)
                                editProfileViewModel.editType(editType = EditProfileType.UserGender(it))
                            },
                            enabled = true
                        )

                    }

                }

                val cropState = imageCropper.cropState

                cropState?.let {
                    ImageCropperDialog(
                        state = it,
                        style = CropperStyle(
                            overlay = MaterialTheme.colorScheme.background,
                            rectColor = MaterialTheme.colorScheme.onBackground,
                            autoZoom = false,
                            guidelines = null,
                        ),
                        topBar = {

                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.background
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            it.done(false)
                                        }
                                    },

                                ) {
                                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                                }
                                Text(text = "Crop")
                                IconButton(
                                    onClick = {
                                    scope.launch {
                                        it.done(true)
                                    }
                                },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }

        ProfileEdit.EDIT_ABOUT_SCREEN -> {

            LaunchedEffect(Unit) {
                editProfileViewModel.editAbout(userProfile?.about.toString())
            }

            EditPage(
                onCancelClick = { navController.popBackStack() },
                onSubmitClick = {
                    updateProfileViewModel.modifyProfile(
                        mutation = UpdateProfileViewModel.ProfileMutation.UpdateProfile(
                            UpdateProfileDTO(updateAbout = editProfileViewModel.about.value))
                    )
                },
                isLoading = isLoading,
                topBarTitle = "About",
                snackBarHostState = snackBarHostState
            ) {
                CustomTextField(
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 180.dp),
                    value = editProfileViewModel.about.value,
                    onValueChange = { editProfileViewModel.editAbout(it.toString()) },
                    label = "About",
                    placeHolder = "What about you?",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    maxLines = 6
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "${editProfileViewModel.about.value.count()}/500",
                        color = if (editProfileViewModel.about.value.count() > 500) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
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
                    updateProfileViewModel.modifyProfile(
                        mutation = UpdateProfileViewModel.ProfileMutation.Interests(value = interestList)
                    )
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
                        trailingIcon = {
                            Button(
                                modifier = Modifier.padding(end = 6.dp),
                                onClick = {
                                    if (interestText.value.trim().isEmpty()) return@Button
                                    editProfileViewModel.addItem(interestText.value)
                                    interestText.value = ""
                                },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text("Add")
                            }
                        },
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
                                        text = it,
                                        modifier = Modifier.padding(10.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
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
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }
                            )
                        }
                    }
                },
                topBarTitle = "Interests",
                snackBarHostState = snackBarHostState
            )
        }

        ProfileEdit.EDIT_CAMPUS -> {


            LaunchedEffect(Unit) {
                editProfileViewModel.editCampus(userProfile?.campus ?: Campus())
            }

            EditPage(
                onCancelClick = { navController.popBackStack() },
                onSubmitClick = {
                    updateProfileViewModel.modifyProfile(
                        mutation = UpdateProfileViewModel.ProfileMutation.Campus(
                            value = editProfileViewModel.campus.value,
                            oldCampusId = userProfile?.campus?.code
                        )
                    )
                },
                isLoading = isLoading,
                topBarTitle = "Edit Campus",
                snackBarHostState = snackBarHostState
            ) {


                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editProfileViewModel.campus.value.collegeName?:"",
                    onValueChange = { editProfileViewModel.editCollege(it.toString()) },
                    label = "College",
                    placeHolder = "Enter Your College",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )


                UniversityDropdown(
                    selectedUniversity = University(
                        university = editProfileViewModel.campus.value.university ?: "",
                        logo = editProfileViewModel.campus.value.logo ?: ""
                    ),
                    universityListState = universityListState,
                    onUniversitySelected = {
                        editProfileViewModel.editUniversity(
                            University(
                                university = it.university,
                                logo = it.logo
                            )
                        )
                        updateProfileViewModel.resetUniversityData()
                    },
                    onFieldChange = {
                        editProfileViewModel.editUniversity(University(university = it))
                        updateProfileViewModel.onUniversityQueryChanged(it)
                    },
                    onClearClick = {
                        editProfileViewModel.editUniversity(null)
                        updateProfileViewModel.resetUniversityData()
                    }
                )


                AutoCompleteFieldOfStudyDropdown(
                    fieldOptions = fieldsOfStudy,
                    selectedField = editProfileViewModel.campus.value.fieldOfStudy?:"",
                    onFieldChange = {
                        editProfileViewModel.editFieldOfStudy(it.toString())
                    },
                    label = "Field Of Study"
                )

                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editProfileViewModel.campus.value.code?.uppercase() ?:"",
                    onValueChange = {
                        editProfileViewModel.editCampusCode(it.toString())
                    },
                    label = "College/Institute/University Code",
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

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    CourseDurationPicker(
                        initialDuration = Duration(
                            courseStart = editProfileViewModel.campus.value.courseStart,
                            startTimestamp = editProfileViewModel.campus.value.startTimestamp,
                            courseEnd = editProfileViewModel.campus.value.courseEnd,
                            endTimestamp = editProfileViewModel.campus.value.endTimestamp
                        ),
                        onDurationSelected = {
                            editProfileViewModel.duration(it.courseStart, it.courseEnd, it.startTimestamp, it.endTimestamp, it.isCurrent)
                        }
                    )
                }
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
    selectedUniversity : University?,
    universityListState:  UiState<List<UniversityDTO>>,
    onFieldChange: (String) -> Unit,
    onUniversitySelected: (University) -> Unit,
    onClearClick:()-> Unit
) {


    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {

        CustomTextFieldWithLeadingIcon(
            modifier = Modifier.fillMaxWidth(),
            value = selectedUniversity?.university ?: "",
            onValueChange = {
                onFieldChange(it.toString())
            },
            label = "University",
            enabled = true,
            placeHolder = "University",
            leadingIcon = {
                CircleImage(
                    image = selectedUniversity?.logo ?: "",
                    modifier = Modifier.size(34.dp),
                    onClick = {},
                    visibility = VisibilityMode.USER
                )
            },
            trailingIcon = {
                if (selectedUniversity != null) {
                    IconButton(onClick = {
                        onClearClick.invoke()
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

        when(universityListState){

            is UiState.Success -> {

                val universityList = universityListState.data

                ExposedDropdownMenu(
                    containerColor = MaterialTheme.colorScheme.surface,
                    expanded = universityList.isNotEmpty(),
                    onDismissRequest = {
                        onClearClick.invoke()
                    }
                ) {
                    universityList.forEach { selectionOption ->
                        DropdownMenuItem(
                            onClick = {
                                onUniversitySelected(
                                    University(
                                        university = selectionOption.name,
                                        logo = selectionOption.logo
                                    )
                                )

                            },
                            text = {
                                Text(text = selectionOption.name, style = MaterialTheme.typography.bodyMedium)
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

            }
            is UiState.Error -> {

                Log.d("UNIVERSITY_ERROR_LIST", "UniversityDropdown: ${universityListState.message}")

            }
            is UiState.Loading -> {


            }
            else -> {}
        }


    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPage(
    onCancelClick: (ProfileEdit) -> Unit,
    onSubmitClick: () -> Unit,
    isLoading: Boolean,
    topBarTitle: String,
    snackBarHostState: SnackbarHostState,
    content: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = topBarTitle, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = { onCancelClick(ProfileEdit.PROFILE_SCREEN) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier.padding(end = 12.dp).height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularLoading(MaterialTheme.colorScheme.primary)
                        } else {
                            SubmitButton { onSubmitClick.invoke() }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
        // 👇 important to handle keyboard properly
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // ✅ instead of scrollable()
                .imePadding() // ✅ pushes content above keyboard
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}


@Composable
fun ProfileComponent(
    title: String,
    onEditClick: () -> Unit = {},
    body: @Composable () -> Unit,
    contentDescription: String,
    editIconVisible: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header row (title + edit button)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            if (editIconVisible){
                EditProfileIconButton {
                    onEditClick.invoke()
                }
            }

        }

        Spacer(modifier = Modifier.height(8.dp))

        body()

    }
}

@Composable
fun GenderSelector(
    enabled: Boolean,
    selectedGender: Gender,
    onSelect: (Gender) -> Unit
) {

    val genders = listOf(Gender.MALE, Gender.FEMALE)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        genders.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(
                        color =  MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(8.dp)
                    )

                ,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                RadioButton(
                    enabled = enabled,
                    selected = selectedGender == it,
                    onClick = { onSelect(it) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                Text(text = it.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }

}

