
package com.iota.campusX.Screens.Profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.University
import com.iota.campusX.Feature.UserProfile.data.UniversityDTO
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.R
import com.iota.campusX.Utils.CalendarSelector
import com.iota.campusX.Utils.CustomTextField
import com.iota.campusX.Utils.ProfileEdit
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.timeMillsToString
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.Black400
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.Black900
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.secondary
import com.iota.campusX.ui.theme.background
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.file.WatchEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel,
) {

    val editProfileViewModel: EditProfileViewModel = viewModel()

    val userProfile = userProfileViewModel.userBaseProfile.collectAsState().value.baseProfileData
    var screenValue by rememberSaveable { mutableStateOf(ProfileEdit.PROFILE_SCREEN) }
    val scope = rememberCoroutineScope()
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val pickedImage = remember { mutableStateOf<Uri?>(null) }
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                pickedImage.value = uri
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    when (screenValue) {

        ProfileEdit.PROFILE_SCREEN -> {

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Edit Profile") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
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
                            Row (modifier = Modifier.padding(end = 12.dp)){
                                if (pickedImage.value != null){
                                    if (isLoading){
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = primary,
                                            strokeWidth = 2.dp
                                        )
                                    }else{
                                        IconButton(onClick = {
                                            scope.launch {
                                                userProfileViewModel.modifyProfileImage(pickedImage.value!!).collect{
                                                    when(it){
                                                        is ResultState.Loading->{
                                                            isLoading = true
                                                        }
                                                        is ResultState.Success-> {
                                                            isLoading = false
                                                            userProfileViewModel.updateProfileImage(Uri.parse(pickedImage.value.toString()).toString())
                                                            navController.popBackStack()
                                                        }
                                                        is ResultState.Error->{
                                                            isLoading = false
                                                        }
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
                containerColor = secondary
            ) { innerPadding ->

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
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

                                IconButton(onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }){
                                    Icon(
                                        painter = painterResource(R.drawable.camera),
                                        contentDescription = null,
                                        tint = White900
                                    )
                                }

                            }
                        }

                        IconButton(
                            onClick = { /*TODO*/ },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                tint = primary
                            )
                        }

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
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Image(
                                    modifier = Modifier.fillMaxSize(),
                                    painter = painterResource(R.drawable.man),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )

                            }
                        }


                    }

                    ProfileComponent(
                        title = "Name",
                        onEditClick = { screenValue = ProfileEdit.EDIT_NAME_SCREEN },
                        body = {
                            Text(text = "${userProfile?.userName}")
                        },
                        contentDescription = "NAME"

                    )

                    ProfileComponent(
                        title = "Bio",
                        onEditClick = { screenValue = ProfileEdit.EDIT_ABOUT_SCREEN },
                        body = { Text("${userProfile?.userBio}") },
                        contentDescription = "BIO"

                    )

                    ProfileComponent(
                        title = "Gender",
                        onEditClick = {
                            screenValue = ProfileEdit.EDIT_GENDER
                        },
                        body = { Text(userProfile?.userGender?: "Select Gender") },
                        contentDescription = "GENDER"

                    )

                    ProfileComponent(
                        title = "Intrests",
                        onEditClick = { /*TODO*/ },
                        body = {
                            Text("Work on progress")
                        },
                        contentDescription = "INTERESTS"
                    )

                    ProfileComponent(
                        title = "Campus Detail",
                        onEditClick = {
                            screenValue = ProfileEdit.EDIT_CAMPUS
                        },
                        body = {

                           Campus(
                               userProfile?.campus
                           )


                        },
                        contentDescription = "CAMPUS"
                    )


                }


            }

        }

        ProfileEdit.EDIT_NAME_SCREEN -> {

            LaunchedEffect(Unit) {
                editProfileViewModel.editName(userProfile?.userName ?: "")
            }

            EditPage(
                onCancelClick = { screenValue = ProfileEdit.PROFILE_SCREEN },
                onSubmitClick = {
                    scope.launch {
                        userProfileViewModel.modifyName(editProfileViewModel.name.value).collect {
                            when (it) {
                                is ResultState.Loading -> {
                                    isLoading = true
                                }
                                is ResultState.Success -> {
                                    userProfileViewModel.updateName(
                                        name = editProfileViewModel.name.value
                                    )
                                    isLoading = false
                                    screenValue = ProfileEdit.PROFILE_SCREEN
                                }
                                is ResultState.Error -> {
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                isLoading = isLoading
            ) {
                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editProfileViewModel.name.value,
                    onValueChange = {editProfileViewModel.editName(it.toString()) },
                    label = "Name",
                    placeHolder = "Enter your name",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }

        }

        ProfileEdit.EDIT_ABOUT_SCREEN -> {

            LaunchedEffect(Unit) {
                editProfileViewModel.editAbout(userProfile?.userBio.toString())
            }

            EditPage(
                onCancelClick = { screenValue = ProfileEdit.PROFILE_SCREEN },
                onSubmitClick = {
                    scope.launch {
                        userProfileViewModel.modifyAbout(about = editProfileViewModel.about.value).collect {
                            when (it) {

                                is ResultState.Loading -> {
                                    isLoading = true
                                }

                                is ResultState.Success -> {
                                    isLoading = false
                                    userProfileViewModel.updateAbout(editProfileViewModel.about.value)
                                    screenValue = ProfileEdit.PROFILE_SCREEN

                                }

                                is ResultState.Error -> {
                                    isLoading = false
                                }
                            }
                        }
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


        }

        ProfileEdit.EDIT_GENDER -> {

            var selectedGender by remember { mutableStateOf("") }

            EditPage(
                onCancelClick = { screenValue = ProfileEdit.PROFILE_SCREEN },
                onSubmitClick = {

                    scope.launch {
                        userProfileViewModel.modifyGender(gender = selectedGender).collect{
                            when(it){
                                is ResultState.Loading->{
                                    isLoading = true
                                }
                                is ResultState.Success-> {
                                    isLoading = false
                                    userProfileViewModel.updateGender(selectedGender)
                                    screenValue = ProfileEdit.PROFILE_SCREEN

                                }
                                is ResultState.Error->{
                                    isLoading = false
                                }
                            }
                        }
                    }

                },
                isLoading = isLoading
            ) {
                GenderSelector(
                    selectedGender = selectedGender,
                    onSelect = { selectedGender = it }
                )

            }

        }

        ProfileEdit.EDIT_CAMPUS -> {

            var selectedUni by remember { mutableStateOf<Pair<String, String>?>(null) }
            var uniSearchText by rememberSaveable { mutableStateOf("") }
            var isCalenderVisible by remember { mutableStateOf(false) }
            var calenderSwitch by rememberSaveable { mutableIntStateOf(0) }

            LaunchedEffect(Unit) {
                editProfileViewModel.editCampus(userProfile?.campus ?: Campus())
            }

            EditPage(
                onCancelClick = { screenValue = ProfileEdit.PROFILE_SCREEN },
                onSubmitClick = {
                    scope.launch {
                        userProfileViewModel.modifyCampus(editProfileViewModel.campus.value)
                            .collect {
                                when(it){
                                    is ResultState.Loading->{
                                        isLoading = true
                                    }
                                    is ResultState.Success->{
                                        userProfileViewModel.updateCampus(
                                            editProfileViewModel.campus.value
                                        )
                                        isLoading = false
                                        screenValue = ProfileEdit.PROFILE_SCREEN
                                    }
                                    is ResultState.Error->{
                                        isLoading = false
                                    }
                                }
                            }
                    }
                },
                isLoading = isLoading
            ) {

                UniversityDropdown(
                    universityList = userProfileViewModel.universityData.collectAsState().value.universityList,
                    userProfileViewModel = userProfileViewModel,
                    modifier = Modifier,
                    selectedUni = {
                        editProfileViewModel.editUniversity(
                            University(
                                university = it?.first ?: "",
                                logo = it?.second ?: ""
                            )
                        )
                    }
                )

                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editProfileViewModel.campus.value.collegeName,
                    onValueChange = {editProfileViewModel.editCollege(it.toString())},
                    label = "College",
                    placeHolder = "Enter your college",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = editProfileViewModel.campus.value.campusCode,
                    onValueChange = {
                        editProfileViewModel.editCampusCode(it.toString())
                    },
                    label = "College/University Code",
                    placeHolder = "Code",
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


                Column (verticalArrangement = Arrangement.spacedBy(12.dp)){

                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.titleMedium,
                        color = Black800,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Row (
                            modifier = Modifier.weight(1f).height(48.dp)
                                .border(
                                    width = 1.dp,
                                    color = Black300,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(start = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Text(text = timeMillsToString(editProfileViewModel.campus.value.courseStart))
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
                            "To",
                            modifier = Modifier.padding(horizontal = 5.dp),
                            style = typography.labelRegular
                        )

                        Row (
                            modifier = Modifier.weight(1f).height(48.dp)
                                .border(
                                    width = 1.dp,
                                    color = Black300,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(start = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Text(text = timeMillsToString(editProfileViewModel.campus.value.courseEnd))
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

                CalendarSelector(
                    isVisible = isCalenderVisible,
                    onConfirm = {
                        if (calenderSwitch == 0){
                            editProfileViewModel.editCourseStart(it)
                        }
                        if (calenderSwitch == 1){
                            editProfileViewModel.editCourseEnd(it)
                        }
                    },
                    onDismiss = {
                        isCalenderVisible = false
                    }
                )


            }


        }
    }


}

@Composable
fun UniversityDropdown(
    universityList: List<UniversityDTO>, // Replace with your actual University model
    userProfileViewModel: UserProfileViewModel,
    modifier: Modifier = Modifier,
    selectedUni:(Pair<String, String>?) -> Unit
) {
    var selectedUni by remember { mutableStateOf<Pair<String, String>?>(null) }
    var uniSearchText by rememberSaveable { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    selectedUni(selectedUni)

    LaunchedEffect(uniSearchText) {
        delay(3000)
        userProfileViewModel.fetchUniversityData(uniSearchText)
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isDropdownExpanded = universityList.isNotEmpty() },
            value = uniSearchText,
            onValueChange = {
                uniSearchText = it
                isDropdownExpanded = true
            },
            placeholder = { Text("University") },
            trailingIcon = {
                if (selectedUni != null) {
                    IconButton(
                        onClick = {
                            selectedUni = null
                            uniSearchText = ""
                            userProfileViewModel.clearUniversityData()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            leadingIcon = {
                AsyncImage(
                    model = selectedUni?.second ?: "",
                    contentDescription = null,
                    placeholder = painterResource(R.drawable.image),
                    modifier = Modifier.size(24.dp)
                )
            },
            singleLine = true
        )

        DropdownMenu(
            expanded = isDropdownExpanded && universityList.isNotEmpty(),
            onDismissRequest = {
                isDropdownExpanded = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            universityList.forEach { university ->
                DropdownMenuItem(
                    text = { Text(university.name) },
                    onClick = {
                        selectedUni = university.name to university.logo
                        uniSearchText = university.name
                        isDropdownExpanded = false
                        userProfileViewModel.clearUniversityData()
                    },
                    leadingIcon = {
                        AsyncImage(
                            model = university.logo,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}



@Composable
fun ProfileComponent(
    title: String,
    onEditClick: () -> Unit,
    body: @Composable () -> Unit,
    contentDescription: String, )
{

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(width = 2.dp, color = background, shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

            Text(text = title, fontWeight = FontWeight.Bold)

            IconButton(onClick = { onEditClick.invoke() }) {
                Icon(
                    painter = painterResource(R.drawable.edit),
                    contentDescription = "Back",
                    tint = primary
                )
            }

        }

        body()

    }


}

@Composable
fun GenderSelector(
    selectedGender: String,
    onSelect: (String) -> Unit
) {

    val genders = listOf("Male", "Female")

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
                    selected = selectedGender == it,
                    onClick = { onSelect(it) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = primary,
                        unselectedColor = Black900
                    )
                )
                Text(text = it)
            }
        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestComponent(
    interestList: List<String>
)
{

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
fun EditPage(
    onCancelClick: (ProfileEdit) -> Unit,
    onSubmitClick: () -> Unit,
    isLoading: Boolean ,
    content: @Composable () -> Unit,

) {

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .background(color = White900)) {

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
                if (isLoading){
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = primary,
                        strokeWidth = 2.dp
                    )
                }else{
                    IconButton(onClick = { onSubmitClick.invoke() }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                }


        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }


    }

}

