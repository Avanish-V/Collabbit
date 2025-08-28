package com.iota.campusX.Screens.Profile

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.iota.campusX.ui.UIComponents.AutoCompleteFieldOfStudyDropdown
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.CircularLoading
import com.iota.campusX.ui.UIComponents.CourseDuration
import com.iota.campusX.ui.UIComponents.CustomDatePicker
import com.iota.campusX.ui.UIComponents.EditProfileIconButton
import com.iota.campusX.ui.UIComponents.SubmitButton
import com.iota.campusX.ui.theme.LightTheme_Blue
import com.mr0xf00.easycrop.CropError
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.CropperStyle
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.rememberImagePicker
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import kotlinx.coroutines.launch
import saveBitmapToCache

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel,
) {

    val editProfileViewModel: EditProfileViewModel = viewModel()
    val editType = editProfileViewModel.editType.collectAsState()
    val context = LocalContext.current
    val userProfile = userProfileViewModel.userBaseProfile.collectAsState().value

    val modifyState = userProfileViewModel.modifyState.collectAsState().value
    val profileEditValue = navController.currentBackStackEntry?.savedStateHandle?.get<ProfileEdit>("PROFILE_EDIT")


    val snackBarHostState = remember { SnackbarHostState() }
    val imageCropper = rememberImageCropper()

    val scope = rememberCoroutineScope()
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val focusManager = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val pickedImage = remember { mutableStateOf<Uri?>(null) }
    
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
                snackBarHostState.showSnackbar(
                    message = "Saved",
                    duration = SnackbarDuration.Indefinite
                )
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
            editProfileViewModel.editName(userProfile.userName)
            editProfileViewModel.editGender(userProfile.userGender)
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

                                if (editType.value == null) return@Row

                                IconButton(
                                    onClick = {

                                    scope.launch {

                                        keyboardController?.hide()

                                        when(val value = editType.value){

                                            is EditProfileType.UserName -> {

                                                userProfileViewModel.modifyName(value.name)

                                            }
                                            is EditProfileType.UserGender -> {

                                                userProfileViewModel.modifyGender(value.gender)
                                            }
                                            is EditProfileType.UserImage -> {

                                                userProfileViewModel.modifyProfileImage(value.imageUri)

                                            }

                                            null -> {}
                                        }
                                    }
                                },
                                ) {
                                    if (isLoading){
                                        CircularLoading(
                                            color = MaterialTheme.colorScheme.primary
                                        )
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

                        AsyncImage(
                            modifier = Modifier
                                .size(120.dp)
                                .border(
                                    width = 6.dp,
                                    color = Color.White,
                                    shape = MaterialTheme.shapes.small
                                ).shadow(
                                    elevation = 6.dp,
                                    shape = MaterialTheme.shapes.small
                                )
                                .clip(
                                    MaterialTheme.shapes.small
                                ),
                            model = if (pickedImage.value == null) userProfile?.userImage else pickedImage.value,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            fallback = painterResource(R.drawable.landscape_placeholder_svgrepo_com),

                            )

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
                            editProfileViewModel.editType(editType = EditProfileType.UserName(it.toString()))
                        },
                        label = "Name",
                        placeHolder = "Enter your name",
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
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            it.done(false)
                                        }

                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                                }
                                Text(text = "Crop")
                                IconButton(onClick = {
                                    scope.launch {
                                        it.done(true)
                                    }
                                }) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                                }
                            }


                        }
                    )
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
                                        text = it,
                                        modifier = Modifier.padding(10.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline
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
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    }
                }
            )
        }

        ProfileEdit.EDIT_CAMPUS -> {

            var isCalenderVisible by remember { mutableStateOf(false) }
            var calenderSwitch by rememberSaveable { mutableIntStateOf(0) }

            LaunchedEffect(Unit) {
                editProfileViewModel.editCampus(userProfile?.campus ?: Campus())
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
                    userProfileViewModel = userProfileViewModel,
                    selectedUniversity = editProfileViewModel.campus.value.university,
                    onUniversitySelected = {
                        editProfileViewModel.editUniversity(
                            University(
                                university = it.university,
                                logo = it.logo
                            )
                        )
                    },
                    onFieldChange = {
                        editProfileViewModel.editUniversity(University(university = it))
                        userProfileViewModel.onUniversityQueryChanged(it)
                    },
                    onClearClick = {
                        editProfileViewModel.editUniversity(null)
                    }
                )

                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editProfileViewModel.campus.value.collegeName?:"",
                    onValueChange = { editProfileViewModel.editCollege(it.toString()) },
                    label = "College",
                    placeHolder = "Enter your college",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                AutoCompleteFieldOfStudyDropdown(
                    fieldOptions = fieldsOfStudy,
                    selectedField = editProfileViewModel.campus.value.fieldOfStudy?:"",
                    onFieldChange = {
                        editProfileViewModel.editFieldOfStudy(it.toString())
                    },
                    label = "Field of study"
                )

                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editProfileViewModel.campus.value.campusCode?.uppercase() ?:"",
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

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(start = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (editProfileViewModel.campus.value.courseStart?.month.isNullOrEmpty() ){
                                Text(
                                    text = "Start",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }else{
                                Text(
                                    text = "${editProfileViewModel.campus.value.courseStart?.month} ${editProfileViewModel.campus.value.courseStart?.year}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            IconButton(onClick = {
                                calenderSwitch = 0
                                isCalenderVisible = !isCalenderVisible
                            }) {
                                Icon(
                                    modifier = Modifier.size(22.dp),
                                    painter = painterResource(R.drawable.calendar),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                        }

                        Text(
                            "-",
                            modifier = Modifier.padding(horizontal = 5.dp),
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(start = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (editProfileViewModel.campus.value.courseEnd?.month.isNullOrEmpty()){
                                Text(
                                    text="End",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }else{
                                Text(
                                    text = "${editProfileViewModel.campus.value.courseEnd?.month} ${editProfileViewModel.campus.value.courseEnd?.year}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            IconButton(onClick = {
                                calenderSwitch = 1
                                isCalenderVisible = !isCalenderVisible
                            }) {
                                Icon(
                                    modifier = Modifier.size(22.dp),
                                    painter = painterResource(R.drawable.calendar),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
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
    selectedUniversity : University?,
    userProfileViewModel: UserProfileViewModel,
    onFieldChange: (String) -> Unit,
    onUniversitySelected: (University) -> Unit,
    onClearClick:()-> Unit
) {


    val universityListState by userProfileViewModel.universityData.collectAsState()

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

        ExposedDropdownMenu(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            expanded = universityListState is UiState.Success,
            onDismissRequest = { expanded = false }
        ) {

            when(universityListState){

                is UiState.Success -> {

                    val universityList = (universityListState as UiState.Success<List<UniversityDTO>>).data

                    universityList.forEach { selectionOption ->
                        DropdownMenuItem(
                            onClick = {
                                onUniversitySelected(
                                    University(
                                        university = selectionOption.name,
                                        logo = selectionOption.logo
                                    )
                                )
                                userProfileViewModel.resetUniversityData()
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
                is UiState.Error -> {

                    Text(text = (universityListState as UiState.Error).toString())

                }
                is UiState.Loading -> {

                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = LightTheme_Blue,
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
        modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.background).imePadding().padding(WindowInsets.statusBars.asPaddingValues()),
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
                    CircularLoading(
                        MaterialTheme.colorScheme.primary
                    )
                } else {
                    SubmitButton {
                        onSubmitClick.invoke()
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

            if (!editIconVisible){
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
                        color =  MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
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
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                )
                Text(text = it.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }

}

