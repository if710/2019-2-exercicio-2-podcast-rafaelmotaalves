package br.ufpe.cin.android.podcast

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import java.io.FileReader
import java.lang.Exception
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val FEED_URL: String = "https://hipsters.tech/feed/podcast/"
    private var feedItems: List<ItemFeed> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        feed_items_view.layoutManager = LinearLayoutManager(this)

        feed_items_view.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        LoadFeedTask().execute()
    }

    fun renderFeedItems () {
        feed_items_view.adapter = ItemFeedAdapter(feedItems, this)
    }


    internal inner class LoadFeedTask : AsyncTask<Void, Void, List<ItemFeed>>() {

        override fun doInBackground(vararg p0: Void?): List<ItemFeed> {
            var result: List<ItemFeed>

            var db = ItemFeedDB.getDatabase(applicationContext)

            try {
                var rssFeedText: String = URL(FEED_URL).readText()

                result = Parser.parse(rssFeedText)

                var db = ItemFeedDB.getDatabase(applicationContext)

                db.itemFeedDAO().addItemsFeed(*result.toTypedArray())
            } catch (err : Exception) {
                Log.d ("FetchFeedError", err.message)
                result = db.itemFeedDAO().allFeedItems().toList()
            }

            return result
        }

        override fun onPostExecute(result: List<ItemFeed>?) {
            super.onPostExecute(result)

            if (result != null) {
                feedItems = result
                renderFeedItems()
            }

        }

    }

}
