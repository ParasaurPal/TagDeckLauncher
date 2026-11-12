package com.example.data.db

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build

data class CategorySeed(
    val id: Long,
    val name: String,
    val iconKey: String,
    val colorHex: Long,
    val categoryType: String, // "APP", "GAMES_PARENT", "GAME_SUB"
    val parentCategoryId: Long? = null,
)

object PlayCategoryCatalog {
    const val GAMES_PARENT_ID = 100L

    val appCategories: List<CategorySeed> = listOf(
        CategorySeed(1L, "Art & Design", "palette", 0xFFF472B6L, "APP"),
        CategorySeed(2L, "Auto & Vehicles", "car", 0xFF60A5FAL, "APP"),
        CategorySeed(3L, "Beauty", "sparkle", 0xFFFB7185L, "APP"),
        CategorySeed(4L, "Books & Reference", "book", 0xFFFBBF24L, "APP"),
        CategorySeed(5L, "Business", "work", 0xFF38BDF8L, "APP"),
        CategorySeed(6L, "Comics", "comic", 0xFFF97316L, "APP"),
        CategorySeed(7L, "Communication", "forum", 0xFF34D399L, "APP"),
        CategorySeed(8L, "Dating", "heart", 0xFFEC4899L, "APP"),
        CategorySeed(9L, "Education", "school", 0xFFA78BFAL, "APP"),
        CategorySeed(10L, "Entertainment", "play", 0xFFFB7185L, "APP"),
        CategorySeed(11L, "Events", "event", 0xFFF59E0BL, "APP"),
        CategorySeed(12L, "Finance", "finance", 0xFF10B981L, "APP"),
        CategorySeed(13L, "Food & Drink", "food", 0xFFFB923CL, "APP"),
        CategorySeed(14L, "Health & Fitness", "fitness", 0xFF22C55EL, "APP"),
        CategorySeed(15L, "House & Home", "home", 0xFF14B8A6L, "APP"),
        CategorySeed(16L, "Libraries & Demo", "code", 0xFF94A3B8L, "APP"),
        CategorySeed(17L, "Lifestyle", "lifestyle", 0xFFC084FCL, "APP"),
        CategorySeed(18L, "Maps & Navigation", "map", 0xFF38BDF8L, "APP"),
        CategorySeed(19L, "Medical", "medical", 0xFFEF4444L, "APP"),
        CategorySeed(20L, "Music & Audio", "music", 0xFFA855F7L, "APP"),
        CategorySeed(21L, "News & Magazines", "news", 0xFF60A5FAL, "APP"),
        CategorySeed(22L, "Parenting", "parenting", 0xFFF472B6L, "APP"),
        CategorySeed(23L, "Personalization", "brush", 0xFF818CF8L, "APP"),
        CategorySeed(24L, "Photography", "camera", 0xFF2DD4BFL, "APP"),
        CategorySeed(25L, "Productivity", "bolt", 0xFF38BDF8L, "APP"),
        CategorySeed(26L, "Shopping", "shop", 0xFFF97316L, "APP"),
        CategorySeed(27L, "Social", "social", 0xFF6366F1L, "APP"),
        CategorySeed(28L, "Sports", "sports", 0xFF34D399L, "APP"),
        CategorySeed(29L, "Tools", "build", 0xFFFBBF24L, "APP"),
        CategorySeed(30L, "Travel & Local", "travel", 0xFF06B6D4L, "APP"),
        CategorySeed(31L, "Video Players & Editors", "video", 0xFFEF4444L, "APP"),
        CategorySeed(32L, "Weather", "weather", 0xFF38BDF8L, "APP"),
    )

    val gamesParent = CategorySeed(GAMES_PARENT_ID, "Games", "gamepad", 0xFF34D399L, "GAMES_PARENT")

    val gameSubcategories: List<CategorySeed> = listOf(
        CategorySeed(101L, "Action", "gamepad", 0xFFEF4444L, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(102L, "Adventure", "travel", 0xFF22C55EL, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(103L, "Arcade", "bolt", 0xFFF59E0BL, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(104L, "Board", "board", 0xFF818CF8L, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(105L, "Card", "card", 0xFFEC4899L, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(106L, "Casino", "casino", 0xFFFBBF24L, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(107L, "Casual", "sparkle", 0xFF38BDF8L, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(108L, "Educational", "school", 0xFFA78BFAL, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(109L, "Music", "music", 0xFFA855F7L, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(110L, "Puzzle", "puzzle", 0xFF14B8A6L, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(111L, "Racing", "car", 0xFFF97316L, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(112L, "Role Playing", "rpg", 0xFF6366F1L, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(113L, "Simulation", "sim", 0xFF60A5FAL, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(114L, "Sports", "sports", 0xFF34D399L, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(115L, "Strategy", "strategy", 0xFF06B6D4L, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(116L, "Trivia", "trivia", 0xFFF472B6L, "GAME_SUB", GAMES_PARENT_ID),
        CategorySeed(117L, "Word", "word", 0xFF94A3B8L, "GAME_SUB", GAMES_PARENT_ID),
    )

    val allSeedCategories: List<CategorySeed> = appCategories + gamesParent + gameSubcategories
}

data class ClassificationResult(
    val primaryCategoryId: Long,
    val additionalCategoryIds: List<Long>,
    val isGame: Boolean,
    val confidence: String, // "AUTHORITATIVE", "HEURISTIC", "UNCERTAIN"
)

object AppCategorizer {

    /**
     * Classifies an installed Android application into Google Play-style categories.
     * Uses ApplicationInfo.category (API 26+) where exposed by the OS, combined with
     * a transparent package/name heuristic, clearly marking uncertain classifications.
     */
    fun classify(
        context: Context,
        packageName: String,
        appName: String,
    ): ClassificationResult {
        val pkg = packageName.lowercase()
        val name = appName.lowercase()
        val combined = "$pkg $name"

        val osCategory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                context.packageManager.getApplicationInfo(packageName, 0).category
            } catch (_: Exception) {
                ApplicationInfo.CATEGORY_UNDEFINED
            }
        } else {
            ApplicationInfo.CATEGORY_UNDEFINED
        }

        // 1. Authoritative OS Game flag or strong game package hints
        val gameHints = listOf(
            "minecraft", "mojang", "pokemon", "niantic", "roblox", "supercell",
            "clash", "brawlstars", "fortnite", "epicgames", "genshin", "hoyoverse",
            "unity3d", "gameloft", "rovio", "angrybirds", "candycrush", "king.",
            "play.games", "steam", "chess", "sudoku", "solitaire", "wordle", "crossword",
            "puzzle", "racing", "asphalt", "needforspeed", "rpg", "dungeon", "arcade",
            "trivia", "casino", "poker", "blackjack", "simcity", "sims", "tycoon"
        )
        val isOsGame = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            osCategory == ApplicationInfo.CATEGORY_GAME

        if (isOsGame || gameHints.any { combined.contains(it) }) {
            val subId = detectGameSubcategory(combined)
            return ClassificationResult(
                primaryCategoryId = PlayCategoryCatalog.GAMES_PARENT_ID,
                additionalCategoryIds = listOfNotNull(subId),
                isGame = true,
                confidence = if (isOsGame) "AUTHORITATIVE" else "HEURISTIC",
            )
        }

        // 2. OS ApplicationInfo.category mapping (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && osCategory != ApplicationInfo.CATEGORY_UNDEFINED) {
            val mappedId = when (osCategory) {
                ApplicationInfo.CATEGORY_AUDIO -> 20L // Music & Audio
                ApplicationInfo.CATEGORY_VIDEO -> 31L // Video Players & Editors
                ApplicationInfo.CATEGORY_IMAGE -> 24L // Photography
                ApplicationInfo.CATEGORY_SOCIAL -> 27L // Social
                ApplicationInfo.CATEGORY_NEWS -> 21L // News & Magazines
                ApplicationInfo.CATEGORY_MAPS -> 18L // Maps & Navigation
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> 25L // Productivity
                else -> null
            }
            if (mappedId != null) {
                return ClassificationResult(
                    primaryCategoryId = mappedId,
                    additionalCategoryIds = emptyList(),
                    isGame = false,
                    confidence = "AUTHORITATIVE",
                )
            }
        }

        // 3. Maintainable package & name classification across Play Store taxonomy
        val ruleMatch: Long? = when {
            combined.containsAny("whatsapp", "telegram", "signal", "messenger", "messages", "mms", "dialer", "phone", "contacts", "slack", "teams", "zoom", "meet", "discord", "skype", "gmail", "email", "mail", "outlook") -> 7L // Communication
            combined.containsAny("instagram", "facebook", "twitter", "x.com", "threads", "tiktok", "snapchat", "reddit", "mastodon", "bluesky", "pinterest", "linkedin") -> 27L // Social
            combined.containsAny("camera", "photo", "gallery", "lightroom", "snapseed", "vsco", "gcam") -> 24L // Photography
            combined.containsAny("figma", "canva", "sketch", "procreate", "illustrator", "art", "draw", "paint") -> 1L // Art & Design
            combined.containsAny("spotify", "music", "soundcloud", "tidal", "pandora", "podcast", "audible", "shazam", "audio", "recorder") -> 20L // Music & Audio
            combined.containsAny("youtube", "netflix", "hulu", "disney", "plex", "vlc", "mxplayer", "video", "twitch", "primevideo", "max") -> 31L // Video Players & Editors
            combined.containsAny("maps", "waze", "uber", "lyft", "transit", "gps", "navigation", "compass") -> 18L // Maps & Navigation
            combined.containsAny("weather", "forecast", "accuweather", "radar") -> 32L // Weather
            combined.containsAny("bank", "chase", "paypal", "venmo", "cashapp", "robinhood", "coinbase", "mint", "wallet", "finance", "budget", "tax") -> 12L // Finance
            combined.containsAny("amazon", "ebay", "etsy", "target", "walmart", "shop", "store", "cart", "bestbuy") -> 26L // Shopping
            combined.containsAny("uber eats", "doordash", "grubhub", "starbucks", "mcdonald", "food", "recipe", "drink", "coffee", "yelp") -> 13L // Food & Drink
            combined.containsAny("strava", "fitbit", "garmin", "peloton", "nike", "fitness", "health", "workout", "run", "sleep", "meditation", "calm", "headspace") -> 14L // Health & Fitness
            combined.containsAny("airbnb", "booking", "expedia", "airline", "flight", "hotel", "trip", "travel") -> 30L // Travel & Local
            combined.containsAny("kindle", "goodreads", "audible", "books", "dictionary", "wikipedia", "bible", "reader", "epub") -> 4L // Books & Reference
            combined.containsAny("duolingo", "khan", "coursera", "udemy", "anki", "quizlet", "classroom", "school", "study") -> 9L // Education
            combined.containsAny("news", "nytimes", "bbc", "cnn", "wsj", "guardian", "flipboard", "feedly", "rss") -> 21L // News & Magazines
            combined.containsAny("notes", "notion", "obsidian", "todo", "tasks", "calendar", "docs", "sheets", "slides", "drive", "dropbox", "onedrive", "office", "keep", "trello", "asana") -> 25L // Productivity
            combined.containsAny("espn", "nba", "nfl", "mlb", "fifa", "sports", "scores") -> 28L // Sports
            combined.containsAny("launcher", "wallpaper", "icon", "theme", "widget", "kustom", "klwp") -> 23L // Personalization
            combined.containsAny("settings", "calculator", "clock", "alarm", "files", "browser", "chrome", "firefox", "edge", "brave", "vpn", "terminal", "authenticator", "1password", "bitwarden", "qr", "scanner") -> 29L // Tools
            else -> null
        }

        return if (ruleMatch != null) {
            ClassificationResult(
                primaryCategoryId = ruleMatch,
                additionalCategoryIds = emptyList(),
                isGame = false,
                confidence = "HEURISTIC",
            )
        } else {
            ClassificationResult(
                primaryCategoryId = 29L, // Tools fallback
                additionalCategoryIds = emptyList(),
                isGame = false,
                confidence = "UNCERTAIN",
            )
        }
    }

    private fun detectGameSubcategory(combined: String): Long? = when {
        combined.containsAny("minecraft", "roblox", "sims", "simcity", "tycoon", "sandbox", "craft") -> 113L // Simulation
        combined.containsAny("puzzle", "sudoku", "tetris", "match", "candy", "2048") -> 110L // Puzzle
        combined.containsAny("chess", "checkers", "monopoly", "board") -> 104L // Board
        combined.containsAny("solitaire", "hearthstone", "uno", "card", "magic") -> 105L // Card
        combined.containsAny("poker", "casino", "slots", "blackjack") -> 106L // Casino
        combined.containsAny("race", "racing", "asphalt", "kart", "drift", "forza") -> 111L // Racing
        combined.containsAny("rpg", "genshin", "pokemon", "fantasy", "dungeon", "dragon", "quest") -> 112L // Role Playing
        combined.containsAny("clash", "strategy", "civilization", "tactics", "tower") -> 115L // Strategy
        combined.containsAny("word", "crossword", "scrabble") -> 117L // Word
        combined.containsAny("trivia", "quiz") -> 116L // Trivia
        combined.containsAny("fifa", "nba", "golf", "tennis", "soccer", "football") -> 114L // Sports
        combined.containsAny("rhythm", "beat", "piano", "guitar") -> 109L // Music
        combined.containsAny("adventure", "zelda", "story", "telltale") -> 102L // Adventure
        combined.containsAny("arcade", "pacman", "pinball", "geometry") -> 103L // Arcade
        combined.containsAny("action", "shoot", "combat", "brawl", "fortnite", "pubg", "cod") -> 101L // Action
        else -> 107L // Casual
    }

    private fun String.containsAny(vararg tokens: String): Boolean = tokens.any { this.contains(it) }
}
