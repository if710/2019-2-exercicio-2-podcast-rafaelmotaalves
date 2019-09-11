package br.ufpe.cin.android.podcast

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import java.io.FileReader

private const val RSS_MIME_TYPE = "application/rss+xml"
private const val GET_RSS_FILE_REQUEST_CODE = 1

class MainActivity : AppCompatActivity() {

    private var feedItems: List<ItemFeed> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btn_sign.setOnClickListener {
            // Create a intent that looks for the RSS file type
            var getFileIntent  = Intent(Intent.ACTION_GET_CONTENT)
            getFileIntent.type = RSS_MIME_TYPE

            // Executing the intent passing an id that will be used get the type of the intent
            startActivityForResult(getFileIntent, GET_RSS_FILE_REQUEST_CODE)
        }


        feed_items_view.layoutManager = LinearLayoutManager(this)

        feed_items_view.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, returnIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, returnIntent)

        // If the resultCode is different than success do nothing
        if (resultCode !== Activity.RESULT_OK) {
            return
        }

        // If the requestCode is equal to the GET_RSS_FILE_REQUEST_CODE execute the file logic
        if (requestCode == GET_RSS_FILE_REQUEST_CODE && returnIntent?.data != null) {
            var fileURI: Uri = returnIntent?.data!!

            LoadPodcastsTask().execute(fileURI)
        }
    }

    fun renderFeedItems () {
        feed_items_view.adapter = ItemFeedAdapter(feedItems, this)
    }

    fun addFeedItems (newFeedItems: List<ItemFeed>) {
        feedItems = feedItems.union(newFeedItems).toList()
    }


    internal inner class LoadPodcastsTask : AsyncTask<Uri, Void, List<ItemFeed>>() {


        override fun doInBackground(vararg fileUris: Uri): List<ItemFeed> {
            var result: List<ItemFeed> = emptyList()

            for (uri in fileUris) {
                var rssText = getRssFileText(uri)

                var parsedFeedItems = Parser.parse(rssText)

                result = result.union(parsedFeedItems).toList()
            }

            return result
        }

        override fun onPostExecute(resultFeedItems: List<ItemFeed>?) {
            super.onPostExecute(feedItems)

            if (resultFeedItems != null) {
                addFeedItems(resultFeedItems)
                renderFeedItems()
            }

        }

        private fun getRssFileText (fileUri: Uri) : String {

            var inputPFD = contentResolver.openFileDescriptor(fileUri, "r")

            var fileDescriptor = inputPFD?.fileDescriptor

            var fileReader = FileReader(fileDescriptor)

            return fileReader.readText()
        }


    }

}
