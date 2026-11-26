package com.example.ui.screens

import android.util.Xml
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.net.HttpURLConnection
import java.net.URL

private data class RssStory(val title: String, val description: String, val link: String)

@Composable
fun RssBriefingWidget() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tagdeck_rss_briefing", android.content.Context.MODE_PRIVATE) }
    var feedUrl by remember { mutableStateOf(prefs.getString("feed_url", "").orEmpty()) }
    var stories by remember { mutableStateOf<List<RssStory>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        val target = feedUrl.trim()
        if (!target.startsWith("https://") && !target.startsWith("http://")) {
            error = "Enter a valid RSS feed URL."
            return
        }
        loading = true
        error = null
        prefs.edit().putString("feed_url", target).apply()
        scope.launch {
            try {
                stories = withContext(Dispatchers.IO) { readRssStories(target) }
                if (stories.isEmpty()) error = "The feed loaded but contained no readable stories."
            } catch (t: Throwable) {
                error = t.message ?: "Unable to load this feed."
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (feedUrl.isNotBlank()) refresh()
    }

    Column(
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(Color(0xE51C2635), Color(0xC9141C29), Color(0xB52D3544))))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Article, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("News Briefing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            IconButton(onClick = { refresh() }, enabled = !loading && feedUrl.isNotBlank()) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Refresh feed")
            }
        }
        OutlinedTextField(
            value = feedUrl,
            onValueChange = { feedUrl = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("RSS feed URL") },
            placeholder = { Text("https://example.com/feed.xml") },
        )
        Button(onClick = { refresh() }, enabled = !loading && feedUrl.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text(if (loading) "Loading…" else "Load / Refresh feed")
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        if (stories.isNotEmpty()) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(stories, key = { it.link.ifBlank { it.title } }) { story ->
                    Surface(
                        color = Color(0x663B465A),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (story.link.isNotBlank()) runCatching {
                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(story.link)))
                            }
                        },
                    ) {
                        Column(Modifier.padding(11.dp)) {
                            Text(story.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            if (story.description.isNotBlank()) {
                                Spacer(Modifier.height(3.dp))
                                Text(story.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        } else if (!loading && error == null) {
            Text("Add an RSS feed URL to see headlines here.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun readRssStories(feedUrl: String): List<RssStory> {
    val connection = (URL(feedUrl).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 10_000
        readTimeout = 12_000
        setRequestProperty("User-Agent", "TagDeckLauncher/1.0")
        setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml")
    }
    try {
        connection.connect()
        if (connection.responseCode !in 200..299) error("Feed server returned HTTP ${connection.responseCode}.")
        val parser = Xml.newPullParser()
        parser.setInput(connection.inputStream, null)
        val result = mutableListOf<RssStory>()
        var inItem = false
        var title = ""
        var description = ""
        var link = ""
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT && result.size < 40) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name.lowercase()) {
                    "item", "entry" -> { inItem = true; title = ""; description = ""; link = "" }
                    "title" -> if (inItem) title = parser.nextText().trim()
                    "description", "summary", "content" -> if (inItem) description = parser.nextText().trim()
                    "link" -> if (inItem) {
                        link = parser.getAttributeValue(null, "href")?.trim().orEmpty()
                        if (link.isBlank()) link = parser.nextText().trim()
                    }
                }
                XmlPullParser.END_TAG -> if (parser.name.equals("item", true) || parser.name.equals("entry", true)) {
                    if (title.isNotBlank()) result += RssStory(title, description, link)
                    inItem = false
                }
            }
            event = parser.next()
        }
        return result
    } finally {
        connection.disconnect()
    }
}
