import com.iota.campusX.Feature.UserProfile.OfflineSupport.UserProfileEntity
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO

fun UserProfileEntity.toDomain(): BaseProfileDTO = BaseProfileDTO(
    id = id,
    token = token,
    userName = userName,
    userImage = userImage,
    userEmail = userEmail,
    userBio = userBio,
    userGender = userGender,
    interests = interests,
    metaData = metaData,
    campus = campus,
    count = count
)

fun BaseProfileDTO.toEntity(): UserProfileEntity = UserProfileEntity(
    id = id,
    token = token,
    userName = userName,
    userImage = userImage,
    userEmail = userEmail,
    userBio = userBio,
    userGender = userGender,
    interests = interests,
    metaData = metaData,
    campus = campus,
    count = count,
    isRequestSent = false
)
