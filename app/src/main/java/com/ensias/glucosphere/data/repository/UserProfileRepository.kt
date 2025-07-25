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
    fun getUserProfile(): Flow<UserProfile?> = userProfileDao.getActiveUserProfile()

    suspend fun insertUserProfile(userProfile: UserProfile) {
        // Deactivate all users first, then insert new active user
        userProfileDao.deactivateAllUsers()
        userProfileDao.insertUserProfile(userProfile.copy(isActive = true))
    }

    suspend fun updateUserProfile(userProfile: UserProfile) {
        userProfileDao.updateUserProfile(userProfile)
    }

    suspend fun userProfileExists(): Boolean = userProfileDao.hasActiveUser()

    suspend fun checkUserExists(username: String): Boolean = userProfileDao.checkUserExists(username)

    suspend fun setActiveUser(username: String) {
        userProfileDao.deactivateAllUsers()
        userProfileDao.setActiveUser(username)
    }

    suspend fun logout() {
        userProfileDao.deactivateAllUsers()
    }

    fun getAllUsers(): Flow<List<UserProfile>> = userProfileDao.getAllUsers()
}
