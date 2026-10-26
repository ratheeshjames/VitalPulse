package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "profiles")
data class ProfileEntity(
  @PrimaryKey val name: String,
  val unitSystemCode: String = "m",
  val heightCm: Double = 170.0,
  val heightFt: Int = 5,
  val heightIn: Double = 7.0,
  val weightDisplay: Double = 70.0,
  val weightUnitCode: String = "kg",
  val age: Int = 30,
  val sexCode: String = "m",
  val activityLevelIndex: Int = 1,
  val waistInput: String = "",
  val hipInput: String = "",
  val goalWeightDisplay: Double = 65.0,
  val weeklyRateKg: Double = 0.5,
  val startWeightKg: Double? = null
)

@Entity(tableName = "weight_entries")
data class WeightEntryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val profileName: String,
  val dateIso: String,
  val weightKg: Double,
  val bmi: Double,
  val note: String
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
  @PrimaryKey val id: Int = 1,
  val activeProfileName: String = "Me",
  val themeModeName: String = "SYSTEM",
  val accentPresetName: String = "KINETIC_INDIGO",
  val useDynamicColor: Boolean = false,
  val useAsianCutoffs: Boolean = false
)

@Dao
interface VitalPulseDao {
  @Query("SELECT * FROM profiles ORDER BY name ASC")
  fun observeProfiles(): Flow<List<ProfileEntity>>

  @Query("SELECT * FROM profiles ORDER BY name ASC")
  suspend fun getProfilesSnapshot(): List<ProfileEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertProfile(profile: ProfileEntity)

  @Query("DELETE FROM profiles WHERE name = :name")
  suspend fun deleteProfile(name: String)

  @Query("DELETE FROM profiles")
  suspend fun deleteAllProfiles()

  @Query("SELECT * FROM weight_entries ORDER BY dateIso ASC, id ASC")
  fun observeAllEntries(): Flow<List<WeightEntryEntity>>

  @Query("SELECT * FROM weight_entries ORDER BY dateIso ASC, id ASC")
  suspend fun getAllEntriesSnapshot(): List<WeightEntryEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertEntry(entry: WeightEntryEntity)

  @Query("DELETE FROM weight_entries WHERE id = :id")
  suspend fun deleteEntryById(id: Long)

  @Query("DELETE FROM weight_entries WHERE profileName = :profileName")
  suspend fun deleteEntriesForProfile(profileName: String)

  @Query("DELETE FROM weight_entries")
  suspend fun deleteAllEntries()

  @Query("SELECT * FROM app_settings WHERE id = 1")
  fun observeSettings(): Flow<AppSettingsEntity?>

  @Query("SELECT * FROM app_settings WHERE id = 1")
  suspend fun getSettingsSnapshot(): AppSettingsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertSettings(settings: AppSettingsEntity)
}

@Database(
  entities = [ProfileEntity::class, WeightEntryEntity::class, AppSettingsEntity::class],
  version = 1,
  exportSchema = false
)
abstract class VitalPulseDatabase : RoomDatabase() {
  abstract fun dao(): VitalPulseDao

  companion object {
    @Volatile private var INSTANCE: VitalPulseDatabase? = null

    fun getInstance(context: Context): VitalPulseDatabase {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
          context.applicationContext,
          VitalPulseDatabase::class.java,
          "vitalpulse_biometrics.db"
        ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
      }
    }
  }
}
