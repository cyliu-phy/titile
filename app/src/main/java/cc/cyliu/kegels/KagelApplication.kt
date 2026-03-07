package cc.cyliu.kegels

import android.app.Application
import android.app.LocaleManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.LocaleList
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import cc.cyliu.kegels.data.datastore.AppPreferences
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class KagelApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        applyLanguagePreference()
        createNotificationChannel()
    }

    /**
     * Reads the saved language tag from DataStore on startup and applies it via the
     * system LocaleManager (API 33+). runBlocking is acceptable here since DataStore
     * reads from disk are fast and this is in Application.onCreate().
     */
    private fun applyLanguagePreference() {
        val tag = runBlocking {
            dataStore.data.first()[AppPreferences.LANGUAGE_TAG]
        } ?: return
        if (tag == "system") return
        getSystemService(LocaleManager::class.java)
            .applicationLocales = LocaleList.forLanguageTags(tag)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.notification_channel_desc)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "kegel_reminder"
    }
}
