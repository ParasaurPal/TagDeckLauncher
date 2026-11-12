package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.db.CategoryEntity
import java.io.File

data class TagIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector,
)

val TagIconCatalog: List<TagIconOption> = listOf(
    TagIconOption("tag", "Tag", Icons.Rounded.LocalOffer),
    TagIconOption("work", "Work", Icons.Rounded.Work),
    TagIconOption("palette", "Creative", Icons.Rounded.Palette),
    TagIconOption("play", "Media", Icons.Rounded.PlayArrow),
    TagIconOption("gamepad", "Gaming", Icons.Rounded.SportsEsports),
    TagIconOption("build", "Tools", Icons.Rounded.Build),
    TagIconOption("bolt", "Focus", Icons.Rounded.Bolt),
    TagIconOption("forum", "Social", Icons.Rounded.Forum),
    TagIconOption("music", "Music", Icons.Rounded.MusicNote),
    TagIconOption("camera", "Camera", Icons.Rounded.CameraAlt),
    TagIconOption("code", "Dev", Icons.Rounded.Code),
    TagIconOption("school", "Learn", Icons.Rounded.School),
    TagIconOption("shop", "Shop", Icons.Rounded.ShoppingBag),
    TagIconOption("web", "Web", Icons.Rounded.Public),
    TagIconOption("star", "Star", Icons.Rounded.Star),
    TagIconOption("heart", "Love", Icons.Rounded.Favorite),
    TagIconOption("bookmark", "Saved", Icons.Rounded.Bookmarks),
    TagIconOption("folder", "Folder", Icons.Rounded.Folder),
    TagIconOption("home", "Home", Icons.Rounded.Home),
    TagIconOption("sparkle", "Magic", Icons.Rounded.AutoAwesome),
    TagIconOption("filter", "Rule", Icons.Rounded.FilterList),
    TagIconOption("car", "Auto", Icons.Rounded.DirectionsCar),
    TagIconOption("book", "Books", Icons.Rounded.Book),
    TagIconOption("comic", "Comics", Icons.AutoMirrored.Rounded.MenuBook),
    TagIconOption("event", "Events", Icons.Rounded.Event),
    TagIconOption("finance", "Finance", Icons.Rounded.AccountBalance),
    TagIconOption("food", "Food", Icons.Rounded.Restaurant),
    TagIconOption("fitness", "Fitness", Icons.Rounded.FitnessCenter),
    TagIconOption("lifestyle", "Life", Icons.Rounded.Spa),
    TagIconOption("map", "Maps", Icons.Rounded.Map),
    TagIconOption("medical", "Medical", Icons.Rounded.MedicalServices),
    TagIconOption("news", "News", Icons.Rounded.Newspaper),
    TagIconOption("parenting", "Family", Icons.Rounded.ChildCare),
    TagIconOption("brush", "Theme", Icons.Rounded.Brush),
    TagIconOption("social", "Social+", Icons.Rounded.Public),
    TagIconOption("sports", "Sports", Icons.Rounded.SportsSoccer),
    TagIconOption("travel", "Travel", Icons.Rounded.Flight),
    TagIconOption("video", "Video", Icons.Rounded.Movie),
    TagIconOption("weather", "Weather", Icons.Rounded.WbSunny),
    TagIconOption("board", "Board", Icons.Rounded.GridView),
    TagIconOption("card", "Card", Icons.Rounded.Style),
    TagIconOption("casino", "Casino", Icons.Rounded.Casino),
    TagIconOption("puzzle", "Puzzle", Icons.Rounded.Extension),
    TagIconOption("rpg", "RPG", Icons.Rounded.Shield),
    TagIconOption("sim", "Sim", Icons.Rounded.Build),
    TagIconOption("strategy", "Strategy", Icons.Rounded.Psychology),
    TagIconOption("trivia", "Trivia", Icons.Rounded.Quiz),
    TagIconOption("word", "Word", Icons.Rounded.TextFields),
)

fun tagIconVector(key: String): ImageVector =
    TagIconCatalog.find { it.key == key }?.icon ?: Icons.Rounded.LocalOffer

@Composable
fun TagBadgeIcon(
    iconKey: String,
    customImagePath: String?,
    colorHex: Long,
    size: Dp = 36.dp,
    contentDescription: String? = null,
    circular: Boolean = false,
) {
    val accent = Color(colorHex)
    val shape = if (circular) CircleShape else RoundedCornerShape(size * 0.3f)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(accent.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center,
    ) {
        if (!customImagePath.isNullOrBlank() && File(customImagePath).exists()) {
            AsyncImage(
                model = File(customImagePath),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = tagIconVector(iconKey),
                contentDescription = contentDescription,
                tint = accent,
                modifier = Modifier.size(size * 0.56f),
            )
        }
    }
}

@Composable
fun CategoryBadgeIcon(
    category: CategoryEntity,
    size: Dp = category.iconSizeDp.dp,
    contentDescription: String? = category.name,
) {
    val accent = Color(category.colorHex)
    val storedBg = Color(category.backgroundColorHex)
    val bg = if (category.backgroundType != "INHERIT" || storedBg.alpha > 0.45f) {
        accent.copy(alpha = 0.22f)
    } else {
        storedBg
    }
    val shape = RoundedCornerShape(size * 0.30f)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        when {
            !category.customIconPath.isNullOrBlank() && File(category.customIconPath).exists() -> {
                AsyncImage(
                    model = File(category.customIconPath),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            !category.customEmoji.isNullOrBlank() -> {
                Text(
                    text = category.customEmoji,
                    fontSize = (size.value * 0.54f).sp,
                )
            }
            else -> {
                Icon(
                    imageVector = tagIconVector(category.iconKey),
                    contentDescription = contentDescription,
                    tint = accent,
                    modifier = Modifier.size(size * 0.58f),
                )
            }
        }
    }
}
