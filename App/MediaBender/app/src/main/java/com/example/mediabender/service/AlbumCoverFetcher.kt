package com.example.mediabender.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.util.Log
import com.example.mediabender.DebounceException
import com.example.mediabender.MainActivity
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class AlbumCoverFetcher(private val context: Context) : AsyncTask<String, Void, Bitmap?>() {

    private val MUSIC_BRAINZ_API_URL = "https://musicbrainz.org/ws/2/release-group/"
    private val COVER_ART_ORG_API_URL = "https://coverartarchive.org/release-group/"

    override fun doInBackground(vararg params: String?): Bitmap? {
        var toReturn: Bitmap? = null

        val album = params[0]
        val artist = params[1]

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected ?: false

        if (isConnected) {

            Log.d("Cover Fetcher","Fetching cover for $album by $artist")

            val client = OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build()

            val completeMBurl = MUSIC_BRAINZ_API_URL + "?query=" + URLEncoder.encode(
                "release:\"${album}\" AND artist:\"${artist}\"",
                "UTF-8"
            ) + "&fmt=json"

            val request = Request.Builder()
                .url(completeMBurl)
                .build()

            Log.d("Cover Fetch", "Sending request to MB")

            val mBReponse = client.newCall(request)
                .execute()

            if(mBReponse.code !in 200..299){
                Log.d("Cover Fetcher","Album not in DB")
                return null
            }

            Log.d("Cover Fetch", "Got resposne from MB")

            val mBJsonResponse = ObjectMapper().readTree(mBReponse.body!!.string())
            val releases = mBJsonResponse.get("release-groups")

            if (releases != null && !releases.isEmpty) {
                //get first matching result
                val mbid = releases.elements().next()?.get("id")!!.textValue()
                val coverRequest = Request.Builder()
                    .url(COVER_ART_ORG_API_URL + "${mbid}/front")
                    .build()

                Log.d("Cover Fetch", "Sending request for cover with id: $mbid")
                val response = client.newCall(coverRequest)
                    .execute()

                Log.d("Cover Fetch", "Got response from cover")

                if(response.code in 200..299) {
                    val image = response.body!!.bytes()

                    Log.d("IMAGE", image.toString())

                    toReturn = BitmapFactory.decodeByteArray(image, 0, image.size)
                }else{
                    Log.d("Cover Fetcher","Not in cover db [${response.code}]")
                }
            }else{
                Log.d("Cover Fetch", "Empty response from MB (Not in DB)")
            }
        }

        return toReturn
    }

    //update album art
    override fun onPostExecute(result: Bitmap?) {
        (context as? MainActivity)?.let {
            it.changeCoverArt(result)
        }
    }
}