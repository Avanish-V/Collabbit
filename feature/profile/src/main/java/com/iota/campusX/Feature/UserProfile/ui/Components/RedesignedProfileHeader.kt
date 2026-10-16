package com.iota.campusX.Feature.UserProfile.ui.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraInfoResponse
import com.iota.campusX.Feature.UserProfile.domain.Model.BaseProfile
import com.iota.campusX.core.designsystem.UserAvatar
import com.iota.campusX.feature.profile.R

@Composable
fun RedesignedProfileHeader(
    modifier: Modifier = Modifier,
    user: BaseProfile?,
    auraPoints: AuraInfoResponse?,
    onEditNameClick: (BaseProfile) -> Unit = {},
    onAddPhotoClick: () -> Unit = {},
    onAuraClick: () -> Unit = {},
    isCurrentUser: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(
                imageUrl = user?.image,
                modifier = Modifier.size(92.dp).clip(CircleShape)
            )

            AuraStreakCard(
                modifier = Modifier.weight(1f),
                aura = auraPoints ?: AuraInfoResponse(),
                onClick = onAuraClick
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            user?.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (isCurrentUser) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { user?.let { onEditNameClick(it) } },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.edit_2),
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        user?.tagline?.let {
            if (it.isNotEmpty()) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}
