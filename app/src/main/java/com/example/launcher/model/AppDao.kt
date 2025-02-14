package com.example.launcher.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppEntity)

    @Query("SELECT * FROM apps ORDER BY label ASC")
    fun getAllApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppByPackage(packageName: String): AppEntity?

    @Query("DELETE FROM apps WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Query("SELECT * FROM folders")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE name = :name LIMIT 1")
    suspend fun getFolderByName(name: String): FolderEntity?

    @Query("""
    SELECT apps.* FROM apps
    INNER JOIN app_folders ON apps.packageName = app_folders.packageName
    WHERE app_folders.folderId = :folderId
""")
    fun getAppsInFolder(folderId: Int): Flow<List<AppEntity>>

    @Query("DELETE FROM app_folders WHERE packageName = :packageName AND folderId = :folderId")
    suspend fun removeAppFromFolder(packageName: String, folderId: Int)

    @Query("DELETE FROM folders WHERE id = :folderId") // Call delete folder and delete folder apps together
    suspend fun deleteFolder(folderId: Int)

    @Query("DELETE FROM app_folders WHERE folderId = :folderId")
    suspend fun deleteFolderApps(folderId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Change to ignore to prevent duplicate apps?
    suspend fun insertAppIntoFolder(appFolder: AppFolderEntity)
}
