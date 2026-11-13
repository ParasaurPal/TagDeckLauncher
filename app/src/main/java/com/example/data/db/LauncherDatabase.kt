package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AppEntity::class,
        CategoryEntity::class,
        AppCategoryCrossRef::class,
        TagEntity::class,
        AppTagCrossRef::class,
        SavedTagRuleEntity::class,
        HomePageEntity::class,
        LauncherWindowEntity::class,
        HomeElementEntity::class,
        LayoutSnapshotEntity::class,
        SidebarConfigEntity::class,
        GestureBindingEntity::class,
        ImageResourceEntity::class,
        BackgroundPresetEntity::class,
    ],
    version = 6,
    exportSchema = false,
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun launcherDao(): LauncherDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN backgroundType TEXT NOT NULL DEFAULT 'INHERIT'")
                db.execSQL("ALTER TABLE categories ADD COLUMN backgroundGradientEndHex INTEGER")
                db.execSQL("ALTER TABLE categories ADD COLUMN backgroundImagePath TEXT")
                db.execSQL("ALTER TABLE categories ADD COLUMN backgroundBlurDp REAL NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN backgroundAlpha REAL NOT NULL DEFAULT 1.0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN showInMainBar INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tags ADD COLUMN showOnHome INTEGER NOT NULL DEFAULT 1")
            }
        }

        @Volatile
        private var INSTANCE: LauncherDatabase? = null

        fun getInstance(context: Context): LauncherDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LauncherDatabase::class.java,
                    "tagdeck_launcher.db"
                ).addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    // Preserve launcher layouts, tags, images, and settings if a migration is missing.
                    // Add an explicit Room Migration before increasing the schema version.
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
