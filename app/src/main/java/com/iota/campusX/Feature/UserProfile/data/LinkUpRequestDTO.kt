import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class LinkUpRequestDTO(
    var senderId: String = "",
    var status: Boolean = false,

    @Contextual
    @ServerTimestamp
    val createdAt: Timestamp? = null
)
