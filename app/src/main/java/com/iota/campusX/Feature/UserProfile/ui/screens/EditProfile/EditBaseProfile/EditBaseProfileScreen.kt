package com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditBaseProfile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.iota.campusX.Feature.UserProfile.ui.Components.EditPage
import com.iota.campusX.Feature.UserProfile.ui.Components.GenderSelector
import com.iota.campusX.Feature.UserProfile.ui.screens.EditEvents.EditProfileActions
import com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditEducation.EducationSectionCard
import com.iota.campusX.R
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.UserAvatar
import com.mr0xf00.easycrop.CropError
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.CropperStyle
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.rememberImagePicker
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import saveBitmapToCache

@Composable
fun EditBaseProfileScreen(
    navController: NavController,
    editAction: EditProfileActions.EditBasicDetails,
    snackBarHostState: SnackbarHostState,
    viewModel: EditBaseProfileViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = remember { FocusRequester() }

    val name by viewModel.name.collectAsState()
    val tagline by viewModel.tagline.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val pickedImage by viewModel.pickedImage.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setInitialData(editAction.basicDetails)
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            navController.popBackStack()
        } else if (uiState is UiState.Error) {
            snackBarHostState.showSnackbar((uiState as UiState.Error).message)
        }
    }

    val imageCropper = rememberImageCropper()
    val imagePicker = rememberImagePicker(onImage = { uri ->
        scope.launch {
            val result = imageCropper.crop(uri, context)
            when (result) {
                CropError.LoadingError -> snackBarHostState.showSnackbar("Error loading image")
                CropError.SavingError -> snackBarHostState.showSnackbar("Error saving image")
                CropResult.Cancelled -> {}
                is CropResult.Success -> {
                    val cachedUri = saveBitmapToCache(context, result.bitmap.asAndroidBitmap())
                    viewModel.onImagePicked(cachedUri)
                }
            }
        }
    })

    EditPage(
        onCancelClick = { navController.popBackStack() },
        onSubmitClick = {
            context.vibrate()
            viewModel.updateProfile()
        },
        isLoading = uiState is UiState.Loading,
        topBarTitle = "Basic Info",
        snackBarHostState = snackBarHostState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with Illustration
            BaseProfileHeader()

            // Avatar Selection Card
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier
                            .size(140.dp)
                            .padding(4.dp),
                        shape = CircleShape,
                        border = BorderStroke(4.dp, MaterialTheme.colorScheme.primaryContainer),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        UserAvatar(
                            imageUrl = pickedImage?.toString() ?: editAction.basicDetails.image,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    FilledIconButton(
                        onClick = { imagePicker.pick(mimetype = "image/*") },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_camera_alt_24),
                            contentDescription = "Pick Photo",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Identity Details Card
            EducationSectionCard(title = "Identity") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Display Name") },
                        placeholder = { Text("How should we call you?") },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusManager),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.user_normal),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    OutlinedTextField(
                        value = tagline,
                        onValueChange = { viewModel.onTaglineChange(it) },
                        label = { Text("Professional Tagline") },
                        placeholder = { Text("e.g. Aspiring AI Researcher") },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusManager),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.pencil),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Gender",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        GenderSelector(
                            selectedGender = gender,
                            onSelect = { viewModel.onGenderChange(it) },
                            enabled = true
                        )
                    }
                }
            }

            // Identity Tip Section
            IdentityTipSection()

            Spacer(modifier = Modifier.height(24.dp))
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
                        modifier = Modifier.fillMaxWidth().background(color = MaterialTheme.colorScheme.background),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scope.launch { it.done(false) } }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Cancel")
                        }
                        Text(text = "Crop")
                        IconButton(onClick = { scope.launch { it.done(true) } }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun BaseProfileHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Your Digital Presence",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "Set up your basic identity to build your network on campus.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp).padding(top = 8.dp)
        )
    }
}

@Composable
fun IdentityTipSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.info),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "A clear profile picture and descriptive tagline significantly increase your chances of finding great collaboration partners.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
