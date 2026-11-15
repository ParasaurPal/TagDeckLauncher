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
        CustomFontEntity::class,
        ThemeProfileEntity::class,
    ],
    version = 8,
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

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN iconOpenStateEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE categories ADD COLUMN openIconKey TEXT")
                db.execSQL("ALTER TABLE categories ADD COLUMN openCustomIconPath TEXT")
                db.execSQL("ALTER TABLE categories ADD COLUMN openCustomEmoji TEXT")
                db.execSQL("ALTER TABLE categories ADD COLUMN openColorHex INTEGER")
                db.execSQL("ALTER TABLE home_pages ADD COLUMN pageGesturesJson TEXT")
                db.execSQL("ALTER TABLE home_pages ADD COLUMN transitionOverride TEXT")
                db.execSQL("ALTER TABLE home_pages ADD COLUMN transitionDurationMs INTEGER NOT NULL DEFAULT 320")
                db.execSQL("ALTER TABLE home_pages ADD COLUMN loopNavigation INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE home_pages ADD COLUMN parallaxEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE home_elements ADD COLUMN parentContainerId INTEGER")
                db.execSQL("ALTER TABLE home_elements ADD COLUMN styleExtrasJson TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE home_elements ADD COLUMN rotationXDeg REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE home_elements ADD COLUMN rotationYDeg REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE home_elements ADD COLUMN perspective REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE home_elements ADD COLUMN widgetStackItemsJson TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE home_elements ADD COLUMN widgetStackIndex INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE home_elements ADD COLUMN widgetStackSwipeEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE home_elements ADD COLUMN pinnedToViewport INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE home_elements ADD COLUMN pinEdge TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE home_elements ADD COLUMN pinOffsetDp REAL NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_home_elements_parentContainerId ON home_elements(parentContainerId)")
                db.execSQL("CREATE TABLE IF NOT EXISTS custom_fonts (fontId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, displayName TEXT NOT NULL, localPath TEXT NOT NULL, familyName TEXT NOT NULL, weight INTEGER NOT NULL, isItalic INTEGER NOT NULL, isFavorite INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS theme_profiles (profileId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL, settingsJson TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, isBuiltIn INTEGER NOT NULL)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE home_pages ADD COLUMN showDockOverride INTEGER")
                db.execSQL("ALTER TABLE home_elements ADD COLUMN pinnedToAllPages INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE home_elements ADD COLUMN visibilityGroupId TEXT")
                db.execSQL("ALTER TABLE home_elements ADD COLUMN landscapeWidthDp REAL")
                db.execSQL("ALTER TABLE home_elements ADD COLUMN landscapeHeightDp REAL")
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
                ).addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    // Preserve launcher layouts, tags, images, and settings if a migration is missing.
                    // Add an explicit Room Migration before increasing the schema version.
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
