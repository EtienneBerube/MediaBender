package com.example.mediabender

import android.content.SharedPreferences
import com.example.mediabender.helpers.PlayerAccountSharedPreferenceHelper
import com.example.mediabender.models.MediaPlayer
import com.example.mediabender.models.PlayerAccount
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyString
import org.mockito.Matchers.eq
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner

/**
 * Class testing the shared preference helper for the player Account
 */
@RunWith(MockitoJUnitRunner::class)
class SharedPreferencesHelperTest {
    private val TEST_PASSWORD = "ABC123"
    private val TEST_EMAIL = "test@email.com"
    private val PLAYER = MediaPlayer.SPOTIFY

    private var account: PlayerAccount? = null
    private var mMockSharedPreferencesHelper: PlayerAccountSharedPreferenceHelper? = null
    private var mMockBrokenSharedPreferencesHelper: PlayerAccountSharedPreferenceHelper? = null

    @Mock
    var mMockSharedPreferences: SharedPreferences? = null
    @Mock
    var mMockBrokenSharedPreferences: SharedPreferences? = null
    @Mock
    var mMockEditor: SharedPreferences.Editor? = null
    @Mock
    var mMockBrokenEditor: SharedPreferences.Editor? = null

    @Before
    fun initMocks() {
        // Create SharedPreferenceEntry to persist.
        account = PlayerAccount(TEST_EMAIL, TEST_PASSWORD)
        // Create a mocked SharedPreferences.
        mMockSharedPreferencesHelper = createMockSharedPreference()
        // Create a mocked SharedPreferences that fails at saving data.
        mMockBrokenSharedPreferencesHelper = createBrokenMockSharedPreference()
    }

    @Test
    fun sharedPreferencesHelper_SaveAndReadPersonalInformation() {

        mMockSharedPreferencesHelper!!.savePlayerAccount(account, PLAYER)

        val savedSharedPreferenceEntry = mMockSharedPreferencesHelper!!.getPlayerAccount(PLAYER)

        Assert.assertEquals(account!!.email, savedSharedPreferenceEntry!!.email)
        Assert.assertEquals(account!!.password, savedSharedPreferenceEntry!!.password)
    }

    @Test
    fun sharedPreferencesHelper_SavePersonalInformationFailed_ReturnsFalse() {
        // Read personal information from a broken SharedPreferencesHelper
        mMockBrokenSharedPreferencesHelper!!.savePlayerAccount(account, PLAYER)
        val savedSharedPreferenceEntry = mMockBrokenSharedPreferencesHelper!!.getPlayerAccount(PLAYER)
        Assert.assertEquals(null, savedSharedPreferenceEntry)
    }

    /**
     * Creates a mocked SharedPreferences.
     */
    private fun createMockSharedPreference(): PlayerAccountSharedPreferenceHelper {

        `when`<String>(
            mMockSharedPreferences!!.getString(
                eq("${PLAYER.packageName}_email"),
                anyString()
            )
        ).thenReturn(account!!.email)

        `when`<String>(
            mMockSharedPreferences!!.getString(
                eq("${PLAYER.packageName}_password"),
                anyString()
            )
        ).thenReturn(account!!.password)

        // Mocking a successful commit.
        `when`(mMockEditor!!.commit()).thenReturn(true)
        // Return the MockEditor when requesting it.
        `when`<SharedPreferences.Editor>(mMockSharedPreferences!!.edit()).thenReturn(mMockEditor)
        return PlayerAccountSharedPreferenceHelper(mMockSharedPreferences as SharedPreferences)
    }

    /**
     * Creates a mocked SharedPreferences that fails when writing.
     */
    private fun createBrokenMockSharedPreference(): PlayerAccountSharedPreferenceHelper {

        `when`<String>(
            mMockBrokenSharedPreferences!!.getString(
                eq("${PLAYER.packageName}_email"),
                anyString()
            )
        ).thenReturn(null)

        `when`<String>(
            mMockBrokenSharedPreferences!!.getString(
                eq("${PLAYER.packageName}_password"),
                anyString()
            )
        ).thenReturn(account!!.password)

        // Mocking a successful commit.
        `when`(mMockEditor!!.commit()).thenReturn(true)
        // Return the MockEditor when requesting it.
        `when`<SharedPreferences.Editor>(mMockBrokenSharedPreferences!!.edit()).thenReturn(mMockEditor)
        return PlayerAccountSharedPreferenceHelper(mMockBrokenSharedPreferences as SharedPreferences)
    }
}