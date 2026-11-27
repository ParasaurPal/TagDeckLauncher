package com.example.ui.components

import android.graphics.Path
import android.graphics.RectF

/**
 * Minimalist geometric vector glyph definition on a normalized 0..100 coordinate canvas
 * (safe drawing area ~18..82 so outlines, sticker borders, and keycap bezels have breathing room).
 */
data class AnyconsGlyph(
    val key: String,
    val primaryStrokes: List<Path>,
    val detailStrokes: List<Path> = emptyList(),
    val fillPaths: List<Path> = primaryStrokes,
    val accentFillPaths: List<Path> = detailStrokes,
    val paletteIndex: Int = 0,
)

object AnyconsGlyphCatalog {

    fun resolveGlyphKey(packageName: String, appName: String, categoryId: Long): String {
        resolveExactAppGlyphKey(packageName, appName)?.let { return it }
        return when (categoryId) {
            1L -> "palette"      // Art & Design
            2L -> "car"          // Auto & Vehicles
            3L -> "sparkle"      // Beauty
            4L -> "books"        // Books & Reference
            5L -> "work"         // Business
            6L -> "books"        // Comics
            7L -> "messages"     // Communication
            8L -> "heart"        // Dating
            9L -> "education"    // Education
            10L -> "video"       // Entertainment
            11L -> "calendar"    // Events
            12L -> "finance"     // Finance
            13L -> "food"        // Food & Drink
            14L -> "fitness"     // Health & Fitness
            15L -> "home"        // House & Home
            16L -> "code"        // Libraries & Demo
            17L -> "sparkle"     // Lifestyle
            18L -> "maps"        // Maps & Navigation
            19L -> "heart"       // Medical
            20L -> "music"       // Music & Audio
            21L -> "news"        // News & Magazines
            22L -> "heart"       // Parenting
            23L -> "palette"     // Personalization
            24L -> "camera"      // Photography
            25L -> "notes"       // Productivity
            26L -> "shopping"    // Shopping
            27L -> "social"      // Social
            28L -> "fitness"     // Sports
            29L -> "settings"    // Tools
            30L -> "travel"      // Travel & Local
            31L -> "video"       // Video Players & Editors
            32L -> "weather"     // Weather
            100L, in 101L..117L -> "games"
            else -> "apps"
        }
    }

    fun resolveExactAppGlyphKey(packageName: String, appName: String): String? {
        val combined = "${packageName.lowercase()} ${appName.lowercase()}"
        return when {
            combined.hasAny("dialer", "phone", "telecom", "call") && !combined.hasAny("recorder") -> "phone"
            combined.hasAny("whatsapp", "telegram", "signal", "messaging", "messages", "sms", "mms", "viber", "wechat", "line") -> "messages"
            combined.hasAny("discord", "slack", "teams", "zoom", "meet", "skype", "chat") -> "chat_alt"
            combined.hasAny("gmail", "email", "mail", "outlook", "protonmail", "spark", "thunderbird", "k9") -> "mail"
            combined.hasAny("chrome", "firefox", "brave", "edge", "opera", "vivaldi", "duckduckgo", "browser", "kiwi", "tor", "webview") -> "browser"
            combined.hasAny("camera", "gcam", "opencamera") -> "camera"
            combined.hasAny("photos", "gallery", "lightroom", "snapseed", "vsco", "piktures", "aves") -> "photos"
            combined.hasAny("spotify", "music", "tidal", "pandora", "deezer", "soundcloud", "apple.android.music", "poweramp", "symfonium", "retro") -> "music"
            combined.hasAny("podcast", "pocketcasts", "antennapod", "audible") -> "podcast"
            combined.hasAny("youtube", "netflix", "hulu", "disney", "plex", "vlc", "mxplayer", "twitch", "primevideo", "jellyfin", "kodi", "mpv", "video") -> "video"
            combined.hasAny("maps", "waze", "osmand", "transit", "citymapper", "uber", "lyft", "navigation") -> "maps"
            combined.hasAny("compass") -> "compass"
            combined.hasAny("calendar", "etar", "proton.android.calendar", "acalendar") -> "calendar"
            combined.hasAny("clock", "deskclock", "alarm", "timer", "stopwatch") -> "clock"
            combined.hasAny("calculator", "calc") -> "calculator"
            combined.hasAny("settings", "preferences") -> "settings"
            combined.hasAny("keep", "notes", "notion", "obsidian", "joplin", "evernote", "todo", "tasks", "ticktick", "any.do") -> "notes"
            combined.hasAny("files", "documentsui", "drive", "dropbox", "onedrive", "nextcloud", "solidexplorer", "mixplorer", "cxfile") -> "files"
            combined.hasAny("vending", "play store", "fdroid", "f-droid", "aurora", "galaxystore") -> "store"
            combined.hasAny("contacts", "people") -> "contacts"
            combined.hasAny("weather", "breezy", "overdrop", "accuweather", "forecast") -> "weather"
            combined.hasAny("wallet", "bank", "chase", "paypal", "venmo", "cashapp", "revolut", "wise", "monzo", "mint", "ynab", "finance") -> "finance"
            combined.hasAny("strava", "fitbit", "garmin", "health", "fitness", "workout", "peloton", "nike", "calm", "headspace") -> "fitness"
            combined.hasAny("news", "nytimes", "bbc", "cnn", "reddit", "feedly", "inoreader", "rss") -> "news"
            combined.hasAny("kindle", "goodreads", "books", "wikipedia", "bible", "moonreader", "librera", "mihon", "tachiyomi") -> "books"
            combined.hasAny("instagram", "facebook", "twitter", "x.com", "threads", "tiktok", "snapchat", "mastodon", "bluesky", "pinterest", "linkedin") -> "social"
            combined.hasAny("chatgpt", "openai", "gemini", "claude", "perplexity", "copilot", "assistant") -> "ai"
            combined.hasAny("github", "termux", "terminal", "code", "vscode", "acode", "python", "git") -> "code"
            combined.hasAny("bitwarden", "1password", "keepass", "authenticator", "authy", "protonvpn", "mullvad", "wireguard", "vpn", "shield") -> "shield"
            combined.hasAny("amazon", "ebay", "etsy", "target", "walmart", "shopify", "shop") -> "shopping"
            combined.hasAny("doordash", "ubereats", "grubhub", "starbucks", "mcdonald", "food", "recipe", "coffee") -> "food"
            combined.hasAny("airbnb", "booking", "expedia", "flight", "airline", "trip", "hotel") -> "travel"
            combined.hasAny("translate", "deepl", "duolingo") -> "translate"
            combined.hasAny("recorder", "voicerecorder", "microphone") -> "recorder"
            combined.hasAny("flashlight", "torch") -> "flashlight"
            combined.hasAny("search", "googlequicksearchbox", "duckduckgo") -> "search"
            combined.hasAny("steam", "minecraft", "roblox", "chess", "lichess", "play.games", "game") -> "games"
            combined.hasAny("home", "smartthings", "homeassistant", "hue", "nest") -> "home"
            else -> null
        }
    }

    fun getGlyph(key: String): AnyconsGlyph = glyphs[key] ?: glyphs["apps"]!!

    private fun String.hasAny(vararg tokens: String): Boolean = tokens.any { this.contains(it) }

    // --- Path builders on 0..100 canvas (centered around 50,50; active bounds ~18..82) ---

    private fun circle(cx: Float, cy: Float, r: Float): Path = Path().apply {
        addCircle(cx, cy, r, Path.Direction.CW)
    }

    private fun roundRect(l: Float, t: Float, r: Float, b: Float, rx: Float, ry: Float = rx): Path = Path().apply {
        addRoundRect(RectF(l, t, r, b), rx, ry, Path.Direction.CW)
    }

    private fun line(x1: Float, y1: Float, x2: Float, y2: Float): Path = Path().apply {
        moveTo(x1, y1)
        lineTo(x2, y2)
    }

    private fun poly(closed: Boolean = false, vararg pts: Pair<Float, Float>): Path = Path().apply {
        if (pts.isEmpty()) return@apply
        moveTo(pts[0].first, pts[0].second)
        for (i in 1 until pts.size) lineTo(pts[i].first, pts[i].second)
        if (closed) close()
    }

    private val glyphs: Map<String, AnyconsGlyph> by lazy {
        listOf(
            // 1. Phone handset
            AnyconsGlyph(
                key = "phone",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(27f, 22f)
                        lineTo(39f, 22f)
                        quadTo(43f, 22f, 44f, 26f)
                        lineTo(46f, 36f)
                        quadTo(47f, 40f, 43f, 43f)
                        lineTo(38f, 47f)
                        quadTo(43f, 57f, 53f, 62f)
                        lineTo(57f, 57f)
                        quadTo(60f, 53f, 64f, 54f)
                        lineTo(74f, 56f)
                        quadTo(78f, 57f, 78f, 61f)
                        lineTo(78f, 73f)
                        quadTo(78f, 78f, 72f, 78f)
                        cubicTo(42f, 77f, 23f, 58f, 22f, 28f)
                        quadTo(22f, 22f, 27f, 22f)
                        close()
                    }
                ),
                detailStrokes = listOf(
                    Path().apply {
                        addArc(RectF(52f, 22f, 78f, 48f), -90f, 90f)
                    },
                    Path().apply {
                        addArc(RectF(58f, 28f, 72f, 42f), -90f, 90f)
                    },
                ),
                paletteIndex = 2, // Emerald
            ),
            // 2. Messages speech bubble
            AnyconsGlyph(
                key = "messages",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(22f, 26f)
                        quadTo(22f, 20f, 28f, 20f)
                        lineTo(72f, 20f)
                        quadTo(78f, 20f, 78f, 26f)
                        lineTo(78f, 60f)
                        quadTo(78f, 66f, 72f, 66f)
                        lineTo(42f, 66f)
                        lineTo(26f, 79f)
                        lineTo(28f, 66f)
                        quadTo(22f, 66f, 22f, 60f)
                        close()
                    }
                ),
                detailStrokes = listOf(
                    circle(37f, 43f, 3.2f),
                    circle(50f, 43f, 3.2f),
                    circle(63f, 43f, 3.2f),
                ),
                paletteIndex = 4, // Sky blue
            ),
            // 3. Alt Chat / Discord / Slack (dual overlapping bubbles)
            AnyconsGlyph(
                key = "chat_alt",
                primaryStrokes = listOf(
                    roundRect(20f, 22f, 66f, 56f, 12f),
                    Path().apply {
                        moveTo(28f, 56f)
                        lineTo(24f, 70f)
                        lineTo(39f, 56f)
                    }
                ),
                detailStrokes = listOf(
                    roundRect(42f, 42f, 80f, 74f, 10f),
                ),
                paletteIndex = 5, // Indigo
            ),
            // 4. Mail envelope
            AnyconsGlyph(
                key = "mail",
                primaryStrokes = listOf(roundRect(18f, 26f, 82f, 74f, 9f)),
                detailStrokes = listOf(
                    poly(false, 21f to 30f, 50f to 54f, 79f to 30f),
                    line(21f, 70f, 39f, 52f),
                    line(79f, 70f, 61f, 52f),
                ),
                paletteIndex = 0, // Coral
            ),
            // 5. Browser globe
            AnyconsGlyph(
                key = "browser",
                primaryStrokes = listOf(circle(50f, 50f, 30f)),
                detailStrokes = listOf(
                    Path().apply { addOval(RectF(36f, 20f, 64f, 80f), Path.Direction.CW) },
                    line(21f, 42f, 79f, 42f),
                    line(21f, 58f, 79f, 58f),
                ),
                paletteIndex = 4, // Azure
            ),
            // 6. Camera
            AnyconsGlyph(
                key = "camera",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(24f, 32f)
                        lineTo(35f, 32f)
                        lineTo(41f, 23f)
                        lineTo(59f, 23f)
                        lineTo(65f, 32f)
                        lineTo(76f, 32f)
                        quadTo(82f, 32f, 82f, 38f)
                        lineTo(82f, 71f)
                        quadTo(82f, 77f, 76f, 77f)
                        lineTo(24f, 77f)
                        quadTo(18f, 77f, 18f, 71f)
                        lineTo(18f, 38f)
                        quadTo(18f, 32f, 24f, 32f)
                        close()
                    }
                ),
                detailStrokes = listOf(
                    circle(50f, 54f, 13f),
                    circle(71f, 41f, 2.5f),
                ),
                paletteIndex = 6, // Violet/Slate
            ),
            // 7. Photos / Gallery (mountain + sun frame)
            AnyconsGlyph(
                key = "photos",
                primaryStrokes = listOf(roundRect(20f, 22f, 80f, 78f, 11f)),
                detailStrokes = listOf(
                    poly(false, 22f to 68f, 40f to 48f, 54f to 62f, 64f to 51f, 78f to 68f),
                    circle(36f, 36f, 5f),
                ),
                paletteIndex = 1, // Amber/Sunset
            ),
            // 8. Music note
            AnyconsGlyph(
                key = "music",
                primaryStrokes = listOf(
                    circle(34f, 68f, 10f),
                    circle(68f, 62f, 10f),
                    poly(false, 44f to 68f, 44f to 26f, 78f to 21f, 78f to 62f),
                ),
                detailStrokes = listOf(
                    line(44f, 38f, 78f, 33f),
                ),
                paletteIndex = 7, // Orchid/Magenta
            ),
            // 9. Podcast / Radio waves
            AnyconsGlyph(
                key = "podcast",
                primaryStrokes = listOf(
                    circle(50f, 44f, 8f),
                    Path().apply { moveTo(44f, 78f); lineTo(50f, 56f); lineTo(56f, 78f); close() },
                ),
                detailStrokes = listOf(
                    Path().apply { addArc(RectF(30f, 24f, 70f, 64f), 140f, 260f) },
                    Path().apply { addArc(RectF(20f, 14f, 80f, 74f), 145f, 250f) },
                ),
                paletteIndex = 6,
            ),
            // 10. Video / Play
            AnyconsGlyph(
                key = "video",
                primaryStrokes = listOf(roundRect(18f, 25f, 82f, 75f, 12f)),
                detailStrokes = listOf(
                    poly(true, 43f to 39f, 65f to 50f, 43f to 61f),
                ),
                paletteIndex = 0, // Red/Coral
            ),
            // 11. Maps Pin
            AnyconsGlyph(
                key = "maps",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(50f, 81f)
                        cubicTo(28f, 60f, 25f, 49f, 25f, 40f)
                        arcTo(RectF(25f, 18f, 75f, 68f), 180f, 180f, false)
                        cubicTo(75f, 49f, 72f, 60f, 50f, 81f)
                        close()
                    }
                ),
                detailStrokes = listOf(circle(50f, 42f, 9f)),
                paletteIndex = 3, // Teal
            ),
            // 12. Compass
            AnyconsGlyph(
                key = "compass",
                primaryStrokes = listOf(circle(50f, 50f, 30f)),
                detailStrokes = listOf(
                    poly(true, 58f to 42f, 65f to 35f, 56f to 56f, 35f to 65f, 44f to 44f),
                    circle(50f, 50f, 3f),
                ),
                paletteIndex = 3,
            ),
            // 13. Calendar
            AnyconsGlyph(
                key = "calendar",
                primaryStrokes = listOf(roundRect(20f, 25f, 80f, 78f, 10f)),
                detailStrokes = listOf(
                    line(36f, 18f, 36f, 31f),
                    line(64f, 18f, 64f, 31f),
                    line(20f, 42f, 80f, 42f),
                    circle(37f, 56f, 3.2f),
                    circle(50f, 56f, 3.2f),
                    circle(63f, 56f, 3.2f),
                    circle(37f, 68f, 3.2f),
                    circle(50f, 68f, 3.2f),
                ),
                paletteIndex = 0,
            ),
            // 14. Clock
            AnyconsGlyph(
                key = "clock",
                primaryStrokes = listOf(circle(50f, 50f, 30f)),
                detailStrokes = listOf(
                    poly(false, 50f to 32f, 50f to 51f, 63f to 59f),
                    circle(50f, 50f, 2.5f),
                ),
                paletteIndex = 4,
            ),
            // 15. Calculator
            AnyconsGlyph(
                key = "calculator",
                primaryStrokes = listOf(roundRect(24f, 18f, 76f, 82f, 10f)),
                detailStrokes = listOf(
                    roundRect(32f, 26f, 68f, 40f, 4f),
                    circle(36f, 51f, 3f), circle(50f, 51f, 3f), circle(64f, 51f, 3f),
                    circle(36f, 63f, 3f), circle(50f, 63f, 3f), circle(64f, 63f, 3f),
                    circle(36f, 74f, 3f), line(48f, 74f, 66f, 74f),
                ),
                paletteIndex = 1,
            ),
            // 16. Settings Gear
            AnyconsGlyph(
                key = "settings",
                primaryStrokes = listOf(
                    Path().apply {
                        val cx = 50f; val cy = 50f
                        val outer = 30f; val inner = 22f
                        val teeth = 8
                        for (i in 0 until teeth) {
                            val a0 = Math.toRadians((i * 360.0 / teeth) - 10.0)
                            val a1 = Math.toRadians((i * 360.0 / teeth) + 10.0)
                            val a2 = Math.toRadians((i * 360.0 / teeth) + 20.0)
                            val a3 = Math.toRadians(((i + 1) * 360.0 / teeth) - 20.0)
                            val x0 = cx + (outer * kotlin.math.cos(a0)).toFloat()
                            val y0 = cy + (outer * kotlin.math.sin(a0)).toFloat()
                            val x1 = cx + (outer * kotlin.math.cos(a1)).toFloat()
                            val y1 = cy + (outer * kotlin.math.sin(a1)).toFloat()
                            val x2 = cx + (inner * kotlin.math.cos(a2)).toFloat()
                            val y2 = cy + (inner * kotlin.math.sin(a2)).toFloat()
                            val x3 = cx + (inner * kotlin.math.cos(a3)).toFloat()
                            val y3 = cy + (inner * kotlin.math.sin(a3)).toFloat()
                            if (i == 0) moveTo(x0, y0) else lineTo(x0, y0)
                            lineTo(x1, y1); lineTo(x2, y2); lineTo(x3, y3)
                        }
                        close()
                    }
                ),
                detailStrokes = listOf(circle(50f, 50f, 10f)),
                paletteIndex = 5,
            ),
            // 17. Notes / Tasks
            AnyconsGlyph(
                key = "notes",
                primaryStrokes = listOf(roundRect(24f, 19f, 76f, 81f, 9f)),
                detailStrokes = listOf(
                    line(34f, 36f, 66f, 36f),
                    line(34f, 50f, 66f, 50f),
                    line(34f, 64f, 55f, 64f),
                ),
                paletteIndex = 1,
            ),
            // 18. Files / Folder
            AnyconsGlyph(
                key = "files",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(20f, 28f)
                        quadTo(20f, 22f, 26f, 22f)
                        lineTo(42f, 22f)
                        lineTo(49f, 30f)
                        lineTo(74f, 30f)
                        quadTo(80f, 30f, 80f, 36f)
                        lineTo(80f, 71f)
                        quadTo(80f, 77f, 74f, 77f)
                        lineTo(26f, 77f)
                        quadTo(20f, 77f, 20f, 71f)
                        close()
                    }
                ),
                detailStrokes = listOf(line(20f, 43f, 80f, 43f)),
                paletteIndex = 1,
            ),
            // 19. Store (Play Triangle inside Bag / Prism)
            AnyconsGlyph(
                key = "store",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(26f, 24f)
                        quadTo(26f, 20f, 30f, 21f)
                        lineTo(77f, 46f)
                        quadTo(82f, 50f, 77f, 54f)
                        lineTo(30f, 79f)
                        quadTo(26f, 80f, 26f, 76f)
                        close()
                    }
                ),
                detailStrokes = listOf(
                    line(28f, 23f, 59f, 62f),
                    line(28f, 77f, 59f, 38f),
                ),
                paletteIndex = 2,
            ),
            // 20. Contacts (Person)
            AnyconsGlyph(
                key = "contacts",
                primaryStrokes = listOf(
                    circle(50f, 34f, 12f),
                    Path().apply {
                        moveTo(24f, 77f)
                        cubicTo(24f, 59f, 36f, 53f, 50f, 53f)
                        cubicTo(64f, 53f, 76f, 59f, 76f, 77f)
                        close()
                    }
                ),
                detailStrokes = emptyList(),
                paletteIndex = 4,
            ),
            // 21. Weather (Sun + Cloud)
            AnyconsGlyph(
                key = "weather",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(31f, 72f)
                        lineTo(68f, 72f)
                        arcTo(RectF(56f, 48f, 80f, 72f), 90f, -180f, false)
                        arcTo(RectF(36f, 34f, 68f, 66f), 0f, -155f, false)
                        arcTo(RectF(20f, 48f, 44f, 72f), -125f, -145f, false)
                        close()
                    }
                ),
                detailStrokes = listOf(
                    circle(36f, 34f, 9f),
                    line(36f, 18f, 36f, 21f),
                    line(20f, 34f, 23f, 34f),
                    line(24f, 23f, 27f, 26f),
                ),
                paletteIndex = 4,
            ),
            // 22. Finance / Wallet
            AnyconsGlyph(
                key = "finance",
                primaryStrokes = listOf(roundRect(19f, 28f, 79f, 74f, 10f)),
                detailStrokes = listOf(
                    roundRect(56f, 44f, 82f, 58f, 5f),
                    circle(64f, 51f, 2.5f),
                    line(26f, 28f, 65f, 21f),
                ),
                paletteIndex = 2,
            ),
            // 23. Fitness / Heart pulse
            AnyconsGlyph(
                key = "fitness",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(50f, 78f)
                        cubicTo(22f, 60f, 18f, 44f, 21f, 32f)
                        cubicTo(24f, 21f, 39f, 20f, 50f, 33f)
                        cubicTo(61f, 20f, 76f, 21f, 79f, 32f)
                        cubicTo(82f, 44f, 78f, 60f, 50f, 78f)
                        close()
                    }
                ),
                detailStrokes = listOf(
                    poly(false, 26f to 50f, 39f to 50f, 45f to 39f, 54f to 60f, 60f to 48f, 74f to 48f),
                ),
                paletteIndex = 0,
            ),
            // 24. News
            AnyconsGlyph(
                key = "news",
                primaryStrokes = listOf(roundRect(20f, 23f, 80f, 77f, 9f)),
                detailStrokes = listOf(
                    roundRect(29f, 32f, 48f, 48f, 3f),
                    line(56f, 35f, 71f, 35f),
                    line(56f, 45f, 71f, 45f),
                    line(29f, 58f, 71f, 58f),
                    line(29f, 67f, 62f, 67f),
                ),
                paletteIndex = 5,
            ),
            // 25. Books
            AnyconsGlyph(
                key = "books",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(50f, 30f)
                        quadTo(35f, 22f, 20f, 27f)
                        lineTo(20f, 73f)
                        quadTo(35f, 68f, 50f, 76f)
                        quadTo(65f, 68f, 80f, 73f)
                        lineTo(80f, 27f)
                        quadTo(65f, 22f, 50f, 30f)
                        close()
                    }
                ),
                detailStrokes = listOf(
                    line(50f, 30f, 50f, 76f),
                    line(29f, 40f, 42f, 42f),
                    line(58f, 42f, 71f, 40f),
                ),
                paletteIndex = 1,
            ),
            // 26. Social (Share / Network / Spark)
            AnyconsGlyph(
                key = "social",
                primaryStrokes = listOf(
                    circle(30f, 50f, 9f),
                    circle(67f, 29f, 9f),
                    circle(67f, 71f, 9f),
                ),
                detailStrokes = listOf(
                    line(38f, 45f, 59f, 33f),
                    line(38f, 55f, 59f, 67f),
                ),
                paletteIndex = 7,
            ),
            // 27. Games Controller
            AnyconsGlyph(
                key = "games",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(31f, 33f)
                        lineTo(69f, 33f)
                        cubicTo(81f, 33f, 84f, 56f, 79f, 69f)
                        quadTo(75f, 75f, 67f, 68f)
                        lineTo(59f, 58f)
                        lineTo(41f, 58f)
                        lineTo(33f, 68f)
                        quadTo(25f, 75f, 21f, 69f)
                        cubicTo(16f, 56f, 19f, 33f, 31f, 33f)
                        close()
                    }
                ),
                detailStrokes = listOf(
                    line(32f, 42f, 32f, 52f),
                    line(27f, 47f, 37f, 47f),
                    circle(63f, 44f, 2.8f),
                    circle(70f, 49f, 2.8f),
                ),
                paletteIndex = 5,
            ),
            // 28. AI / Sparkle
            AnyconsGlyph(
                key = "ai",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(50f, 18f)
                        quadTo(53f, 47f, 82f, 50f)
                        quadTo(53f, 53f, 50f, 82f)
                        quadTo(47f, 53f, 18f, 50f)
                        quadTo(47f, 47f, 50f, 18f)
                        close()
                    }
                ),
                detailStrokes = listOf(
                    Path().apply {
                        moveTo(73f, 21f); lineTo(75f, 27f); lineTo(81f, 29f); lineTo(75f, 31f); lineTo(73f, 37f); lineTo(71f, 31f); lineTo(65f, 29f); lineTo(71f, 27f); close()
                    }
                ),
                paletteIndex = 6,
            ),
            // 29. Code / Terminal
            AnyconsGlyph(
                key = "code",
                primaryStrokes = listOf(roundRect(19f, 23f, 81f, 77f, 10f)),
                detailStrokes = listOf(
                    poly(false, 32f to 41f, 44f to 50f, 32f to 59f),
                    line(50f, 60f, 68f, 60f),
                ),
                paletteIndex = 3,
            ),
            // 30. Security Shield
            AnyconsGlyph(
                key = "shield",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(50f, 19f)
                        lineTo(76f, 29f)
                        lineTo(76f, 49f)
                        cubicTo(76f, 65f, 64f, 76f, 50f, 82f)
                        cubicTo(36f, 76f, 24f, 65f, 24f, 49f)
                        lineTo(24f, 29f)
                        close()
                    }
                ),
                detailStrokes = listOf(
                    poly(false, 38f to 51f, 46f to 59f, 63f to 41f),
                ),
                paletteIndex = 2,
            ),
            // 31. Shopping Bag
            AnyconsGlyph(
                key = "shopping",
                primaryStrokes = listOf(roundRect(23f, 33f, 77f, 80f, 8f)),
                detailStrokes = listOf(
                    Path().apply { addArc(RectF(36f, 18f, 64f, 46f), 180f, 180f) },
                    line(23f, 46f, 77f, 46f),
                ),
                paletteIndex = 1,
            ),
            // 32. Food / Fork & Knife
            AnyconsGlyph(
                key = "food",
                primaryStrokes = listOf(
                    line(35f, 22f, 35f, 78f),
                    Path().apply { moveTo(27f, 22f); lineTo(27f, 39f); quadTo(27f, 47f, 35f, 47f); quadTo(43f, 47f, 43f, 39f); lineTo(43f, 22f) },
                    Path().apply { moveTo(63f, 78f); lineTo(63f, 22f); quadTo(74f, 27f, 74f, 47f); lineTo(63f, 47f) },
                ),
                detailStrokes = emptyList(),
                paletteIndex = 0,
            ),
            // 33. Travel / Plane
            AnyconsGlyph(
                key = "travel",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(50f, 19f)
                        lineTo(56f, 39f)
                        lineTo(80f, 54f)
                        lineTo(80f, 62f)
                        lineTo(56f, 54f)
                        lineTo(56f, 71f)
                        lineTo(66f, 78f)
                        lineTo(66f, 82f)
                        lineTo(50f, 77f)
                        lineTo(34f, 82f)
                        lineTo(34f, 78f)
                        lineTo(44f, 71f)
                        lineTo(44f, 54f)
                        lineTo(20f, 62f)
                        lineTo(20f, 54f)
                        lineTo(44f, 39f)
                        close()
                    }
                ),
                detailStrokes = emptyList(),
                paletteIndex = 4,
            ),
            // 34. Translate / Language
            AnyconsGlyph(
                key = "translate",
                primaryStrokes = listOf(
                    roundRect(19f, 23f, 56f, 56f, 7f),
                    roundRect(44f, 44f, 81f, 77f, 7f),
                ),
                detailStrokes = listOf(
                    line(29f, 35f, 46f, 35f),
                    line(37f, 30f, 37f, 48f),
                    poly(false, 54f to 68f, 62f to 52f, 70f to 68f),
                    line(57f, 63f, 67f, 63f),
                ),
                paletteIndex = 4,
            ),
            // 35. Voice Recorder / Mic
            AnyconsGlyph(
                key = "recorder",
                primaryStrokes = listOf(roundRect(39f, 20f, 61f, 54f, 11f)),
                detailStrokes = listOf(
                    Path().apply { addArc(RectF(29f, 34f, 71f, 68f), 0f, 180f) },
                    line(50f, 68f, 50f, 80f),
                    line(38f, 80f, 62f, 80f),
                ),
                paletteIndex = 0,
            ),
            // 36. Flashlight / Bolt
            AnyconsGlyph(
                key = "flashlight",
                primaryStrokes = listOf(
                    poly(true, 55f to 18f, 28f to 54f, 48f to 54f, 44f to 82f, 72f to 46f, 52f to 46f),
                ),
                detailStrokes = emptyList(),
                paletteIndex = 1,
            ),
            // 37. Search Magnifier
            AnyconsGlyph(
                key = "search",
                primaryStrokes = listOf(circle(44f, 44f, 20f)),
                detailStrokes = listOf(line(59f, 59f, 78f, 78f)),
                paletteIndex = 4,
            ),
            // 38. Home
            AnyconsGlyph(
                key = "home",
                primaryStrokes = listOf(
                    poly(true, 50f to 21f, 80f to 46f, 73f to 46f, 73f to 77f, 27f to 77f, 27f to 46f, 20f to 46f),
                ),
                detailStrokes = listOf(roundRect(43f, 55f, 57f, 77f, 3f)),
                paletteIndex = 3,
            ),
            // 39. Palette
            AnyconsGlyph(
                key = "palette",
                primaryStrokes = listOf(circle(50f, 50f, 29f)),
                detailStrokes = listOf(
                    circle(38f, 41f, 4f),
                    circle(53f, 35f, 4f),
                    circle(64f, 46f, 4f),
                    circle(44f, 61f, 5f),
                ),
                paletteIndex = 7,
            ),
            // 40. Work / Briefcase
            AnyconsGlyph(
                key = "work",
                primaryStrokes = listOf(roundRect(20f, 32f, 80f, 76f, 9f)),
                detailStrokes = listOf(
                    roundRect(38f, 22f, 62f, 32f, 4f),
                    line(20f, 51f, 80f, 51f),
                ),
                paletteIndex = 5,
            ),
            // 41. Car
            AnyconsGlyph(
                key = "car",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(24f, 48f)
                        lineTo(31f, 30f)
                        quadTo(33f, 26f, 38f, 26f)
                        lineTo(62f, 26f)
                        quadTo(67f, 26f, 69f, 30f)
                        lineTo(76f, 48f)
                        lineTo(80f, 52f)
                        lineTo(80f, 71f)
                        lineTo(20f, 71f)
                        lineTo(20f, 52f)
                        close()
                    }
                ),
                detailStrokes = listOf(
                    circle(32f, 59f, 4f),
                    circle(68f, 59f, 4f),
                    line(26f, 46f, 74f, 46f),
                ),
                paletteIndex = 4,
            ),
            // 42. Education / Cap
            AnyconsGlyph(
                key = "education",
                primaryStrokes = listOf(poly(true, 50f to 24f, 83f to 41f, 50f to 58f, 17f to 41f)),
                detailStrokes = listOf(
                    Path().apply { moveTo(31f, 49f); lineTo(31f, 67f); quadTo(50f, 78f, 69f, 67f); lineTo(69f, 49f) },
                    line(79f, 43f, 79f, 65f),
                ),
                paletteIndex = 5,
            ),
            // 43. Heart
            AnyconsGlyph(
                key = "heart",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(50f, 78f)
                        cubicTo(22f, 60f, 18f, 44f, 21f, 32f)
                        cubicTo(24f, 21f, 39f, 20f, 50f, 33f)
                        cubicTo(61f, 20f, 76f, 21f, 79f, 32f)
                        cubicTo(82f, 44f, 78f, 60f, 50f, 78f)
                        close()
                    }
                ),
                detailStrokes = emptyList(),
                paletteIndex = 0,
            ),
            // 44. Sparkle
            AnyconsGlyph(
                key = "sparkle",
                primaryStrokes = listOf(
                    Path().apply {
                        moveTo(50f, 19f)
                        quadTo(53f, 47f, 81f, 50f)
                        quadTo(53f, 53f, 50f, 81f)
                        quadTo(47f, 53f, 19f, 50f)
                        quadTo(47f, 47f, 50f, 19f)
                        close()
                    }
                ),
                detailStrokes = emptyList(),
                paletteIndex = 7,
            ),
            // 45. Default Apps (2x2 minimalist rounded grid)
            AnyconsGlyph(
                key = "apps",
                primaryStrokes = listOf(
                    roundRect(22f, 22f, 45f, 45f, 7f),
                    roundRect(55f, 22f, 78f, 45f, 7f),
                    roundRect(22f, 55f, 45f, 78f, 7f),
                ),
                detailStrokes = listOf(
                    circle(66.5f, 66.5f, 11.5f),
                ),
                paletteIndex = 4,
            ),
        ).associateBy { it.key }
    }
}
