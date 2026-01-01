import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.BaseProfileDTO

fun UserProfileEntity.toDomain(): BaseProfileDTO = BaseProfileDTO(
    uid = uid,
    name = name,
    image = image,
    email = email,
    about = about?:"",
    tagline = tagline?:"",
    campus = campus
)

fun BaseProfileDTO.toEntity(): UserProfileEntity = UserProfileEntity(
    uid = uid,
    name = name,
    image = image,
    email = email,
    about = about,
    tagline = tagline,
    campus = campus
)
