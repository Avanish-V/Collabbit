import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.R
import com.iota.campusX.Utils.UiState
//import com.iota.campusX.ui.theme.typography
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModel
import com.iota.campusX.ui.UIComponents.PrimaryButton


class ConsentAgreeViewModel(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _isAgree = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val isAgree: StateFlow<UiState<Boolean>> = _isAgree.asStateFlow()

    companion object {
        private val SWITCH_PREF_KEY = booleanPreferencesKey("consent_agree")
    }

    init {
        loadSwitchState()
    }

    private fun loadSwitchState() {
        viewModelScope.launch {
            _isAgree.value = UiState.Loading
            val result = runCatching {
                dataStore.data
                    .map { preferences ->
                        preferences[SWITCH_PREF_KEY] ?: false
                    }
                    .first()
            }

            _isAgree.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load consent") }
            )
        }
    }

    fun saveSwitchState(isAgree: Boolean) {
        viewModelScope.launch {
            _isAgree.value = UiState.Loading
            dataStore.edit { settings ->
                settings[SWITCH_PREF_KEY] = isAgree
            }
            _isAgree.value = UiState.Success(isAgree)
        }
    }

    fun toggleConsent() {
        val current = (_isAgree.value as? UiState.Success)?.data ?: false
        saveSwitchState(!current)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsentBottomSheet(isVisible:Boolean,onDismiss: () -> Unit,onAgree:()-> Unit) {

    if (isVisible){
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
        ){

            LazyColumn (
                contentPadding = PaddingValues(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ){
                item {
                    Text("Community guidelines", style = MaterialTheme.typography.titleLarge)
                }

                item {

                    Image(
                        modifier = Modifier.size(250.dp),
                        painter = painterResource(R.drawable.law_outline__1_),
                        contentDescription = null
                    )
                }

                items(consentList) {
                    ConsentSingleMessage(
                        title = it.title,
                        description = it.description
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    PrimaryButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        buttonText = "I agree and understand",
                        onClick = {
                            onAgree.invoke()
                        }

                    )
                }
            }
        }
    }


}

@Composable
fun ConsentSingleMessage(title: String,description: String) {

    Column (verticalArrangement = Arrangement.spacedBy(6.dp)){
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(description, style = MaterialTheme.typography.bodyMedium)
    }

}

data class ConsentMessage(
    val title: String,
    val description: String,
)

val consentList = listOf<ConsentMessage>(
    ConsentMessage(
        title = "Respectful Communication",
        description = "Treat others with kindness and consideration. Avoid hate speech or harassment."
    ),
    ConsentMessage(
        title = "Content Standards",
        description = "Share content that is appropriate for all ages and adheres to legal and ethical standards"
    ),
    ConsentMessage(
        title = "Privacy Protection",
        description = "Respect the privacy of others. Do not share personal information without consent"
    ),
    ConsentMessage(
        title = "Report Mechanism",
        description = "Use the reporting feature to flag inappropriate content or behaviour for swift action"
    ),
    ConsentMessage(
    title = "Safety Measures",
    description = "Take precautions to ensure your own safety and the safety of others. Avoid sharing sensitive information"
)

)
