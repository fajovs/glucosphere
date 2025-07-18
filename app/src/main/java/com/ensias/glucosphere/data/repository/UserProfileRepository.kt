package com.ensias.glucosphere.data.repository

import com.ensias.glucosphere.data.database.dao.UserProfileDao
import com.ensias.glucosphere.data.database.entity.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val userProfileDao: UserProfileDao
) {
    fun getUserProfile(): Flow<UserProfile?> = userProfileDao.getUserProfile()

    suspend fun insertUserProfile(userProfile: UserProfile) {
        userProfileDao.insertUserProfile(userProfile)
    }

    suspend fun updateUserProfile(userProfile: UserProfile) {
        userProfileDao.updateUserProfile(userProfile)
    }

    suspend fun userProfileExists(): Boolean = userProfileDao.userProfileExists()
}
