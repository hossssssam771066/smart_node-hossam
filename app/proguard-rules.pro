# Project-specific ProGuard rules for Smart Node.

# Keep Room generated DAOs/databases
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

# Keep SQLCipher symbols (loaded via JNI)
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# Hilt
-dontwarn dagger.hilt.android.**
-keep class dagger.hilt.android.internal.** { *; }
