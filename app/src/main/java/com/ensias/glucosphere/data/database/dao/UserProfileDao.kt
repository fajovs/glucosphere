package com.ensias.glucosphere.data.database.dao

import androidx.room.*
import com.ensias.glucosphere.data.database.entity.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE isActive = 1 LIMIT 1")
    fun getActiveUserProfile(): Flow<UserProfile?>

    @Query("SELECT EXISTS(SELECT 1 FROM user_profile WHERE username = :username)")
    suspend fun checkUserExists(username: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM user_profile WHERE isActive = 1)")
    suspend fun hasActiveUser(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)

    @Update
    suspend fun updateUserProfile(userProfile: UserProfile)

    @Query("UPDATE user_profile SET isActive = 0")
    suspend fun deactivateAllUsers()

    @Query("UPDATE user_profile SET isActive = 1 WHERE username = :username")
    suspend fun setActiveUser(username: String)

    @Query("SELECT * FROM user_profile WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserProfile?

    @Query("SELECT * FROM user_profile")
    fun getAllUsers(): Flow<List<UserProfile>>
}
