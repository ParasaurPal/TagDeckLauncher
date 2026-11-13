package com.example.ui.screens

import android.content.Context
import android.view.KeyEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.db.HomeElementEntity
import com.example.data.db.ImageResourceEntity
import com.example.data.system.BatteryTelemetry
import com.example.data.system.DeviceTelemetryProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

fun resolveDynamicTokens(template: String, context: Context): String {
    if (!template.contains("{")) return template
    val now = System.currentTimeMillis()
    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(now))
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(now))
    val dayStr = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(now))
    val stor = DeviceTelemetryProvider.readStorageAndRam(context)
    return template
        .replace("{TIME}", timeStr)
        .replace("{DATE}", dateStr)
        .replace("{DAY}", dayStr)
        .replace("{RAM}", "${String.format("%.1f", stor.availableRamGb)}/${String.format("%.1f", stor.totalRamGb)}GB")
        .replace("{STORAGE}", "${String.format("%.0f", stor.usedStorageGb)}/${String.format("%.0f", stor.totalStorageGb)}GB")
}

@Composable
fun LiveClockWidgetContent(element: HomeElementEntity) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(15_000L)
        }
    }
    val timeStr = remember(nowMillis) { SimpleDateFormat("h:mm", Locale.getDefault()).format(Date(nowMillis)) }
    val amPmStr = remember(nowMillis) { SimpleDateFormat("a", Locale.getDefault()).format(Date(nowMillis)) }
    val dateStr = remember(nowMillis) { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date(nowMillis)) }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = timeStr,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color(element.textColorHex),
                maxLines = 1,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = amPmStr,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Text(
            text = dateStr,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(element.textColorHex).copy(alpha = 0.78f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun AnalogClockWidgetContent(element: HomeElementEntity) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }
    val cal = remember(nowMillis) { Calendar.getInstance().apply { timeInMillis = nowMillis } }
    val hour = cal.get(Calendar.HOUR)
    val minute = cal.get(Calendar.MINUTE)
    val second = cal.get(Calendar.SECOND)
    val accent = MaterialTheme.colorScheme.primary
    val textColor = Color(element.textColorHex)

    Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) / 2f
        drawCircle(
            color = textColor.copy(alpha = 0.25f),
            radius = radius,
            center = center,
            style = Stroke(width = 3f),
        )
        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30.0) - 90.0)
            val start = Offset(
                center.x + (radius * 0.82f) * cos(angle).toFloat(),
                center.y + (radius * 0.82f) * sin(angle).toFloat(),
            )
            val end = Offset(
                center.x + (radius * 0.94f) * cos(angle).toFloat(),
                center.y + (radius * 0.94f) * sin(angle).toFloat(),
            )
            drawLine(color = textColor.copy(alpha = 0.7f), start = start, end = end, strokeWidth = if (i % 3 == 0) 4f else 2f)
        }

        val hourAngle = Math.toRadians(((hour + minute / 60f) * 30.0) - 90.0)
        drawLine(
            color = textColor,
            start = center,
            end = Offset(center.x + radius * 0.50f * cos(hourAngle).toFloat(), center.y + radius * 0.50f * sin(hourAngle).toFloat()),
            strokeWidth = 6f,
            cap = StrokeCap.Round,
        )

        val minAngle = Math.toRadians((minute * 6.0) - 90.0)
        drawLine(
            color = textColor,
            start = center,
            end = Offset(center.x + radius * 0.72f * cos(minAngle).toFloat(), center.y + radius * 0.72f * sin(minAngle).toFloat()),
            strokeWidth = 4f,
            cap = StrokeCap.Round,
        )

        val secAngle = Math.toRadians((second * 6.0) - 90.0)
        drawLine(
            color = accent,
            start = center,
            end = Offset(center.x + radius * 0.80f * cos(secAngle).toFloat(), center.y + radius * 0.80f * sin(secAngle).toFloat()),
            strokeWidth = 2f,
            cap = StrokeCap.Round,
        )
        drawCircle(color = accent, radius = 5f, center = center)
    }
}

@Composable
fun CalendarWidgetContent(element: HomeElementEntity) {
    val now = remember { Date() }
    val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(now).uppercase()
    val dayNum = SimpleDateFormat("d", Locale.getDefault()).format(now)
    val weekday = SimpleDateFormat("EEEE", Locale.getDefault()).format(now)
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(month, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(dayNum, style = MaterialTheme.typography.displayMedium, color = Color(element.textColorHex), fontWeight = FontWeight.ExtraBold)
        Text(weekday, style = MaterialTheme.typography.bodySmall, color = Color(element.textColorHex).copy(alpha = 0.75f))
    }
}

@Composable
fun BatteryWidgetContent(element: HomeElementEntity) {
    val context = LocalContext.current
    val battery by remember(context) { DeviceTelemetryProvider.observeBattery(context) }
        .collectAsState(initial = BatteryTelemetry(100, false, 28f))
    val accent = if (battery.levelPercent > 20) Color(0xFF34D399L) else Color(0xFFEF4444L)
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (battery.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
            contentDescription = "Battery",
            tint = accent,
            modifier = Modifier.size(28.dp),
        )
        Text(
            text = "${battery.levelPercent}%",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(element.textColorHex),
        )
        Text(
            text = if (battery.isCharging) "Charging" else "${battery.temperatureC.roundToInt()}°C",
            style = MaterialTheme.typography.labelSmall,
            color = Color(element.textColorHex).copy(alpha = 0.75f),
        )
    }
}

@Composable
fun StorageRamWidgetContent(element: HomeElementEntity) {
    val context = LocalContext.current
    var stats by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            stats++
        }
    }
    val data = remember(stats) { DeviceTelemetryProvider.readStorageAndRam(context) }
    val storFrac = (data.usedStorageGb / data.totalStorageGb.coerceAtLeast(1f)).coerceIn(0f, 1f)
    val usedRam = (data.totalRamGb - data.availableRamGb).coerceAtLeast(0f)
    val ramFrac = (usedRam / data.totalRamGb.coerceAtLeast(1f)).coerceIn(0f, 1f)

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Text("System Memory", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color(element.textColorHex))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "Storage ${data.usedStorageGb.roundToInt()} / ${data.totalStorageGb.roundToInt()} GB",
                style = MaterialTheme.typography.labelSmall,
                color = Color(element.textColorHex).copy(alpha = 0.8f),
            )
            LinearProgressIndicator(progress = { storFrac }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape))
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "RAM ${String.format("%.1f", usedRam)} / ${String.format("%.1f", data.totalRamGb)} GB",
                style = MaterialTheme.typography.labelSmall,
                color = Color(element.textColorHex).copy(alpha = 0.8f),
            )
            LinearProgressIndicator(progress = { ramFrac }, color = Color(0xFFA78BFAL), modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape))
        }
    }
}

@Composable
fun CompassWidgetContent(element: HomeElementEntity) {
    val context = LocalContext.current
    val azimuth by remember(context) { DeviceTelemetryProvider.observeCompassAzimuth(context) }
        .collectAsState(initial = 0f)
    val cardinal = remember(azimuth) {
        val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        dirs[((azimuth + 22.5f) / 45f).toInt() % 8]
    }
    val accent = MaterialTheme.colorScheme.primary
    val textColor = Color(element.textColorHex)

    Box(modifier = Modifier.fillMaxSize().padding(10.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val r = min(size.width, size.height) / 2f * 0.88f
            drawCircle(color = textColor.copy(alpha = 0.2f), radius = r, center = center, style = Stroke(3f))
            val rad = Math.toRadians((-azimuth - 90.0))
            drawLine(
                color = accent,
                start = center,
                end = Offset(center.x + r * 0.78f * cos(rad).toFloat(), center.y + r * 0.78f * sin(rad).toFloat()),
                strokeWidth = 6f,
                cap = StrokeCap.Round,
            )
        }
        Surface(color = Color.Black.copy(alpha = 0.45f), shape = RoundedCornerShape(10.dp)) {
            Text(
                text = "${azimuth.roundToInt()}° $cardinal",
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}

@Composable
fun MediaWidgetContent(element: HomeElementEntity) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(
                text = element.label.ifBlank { "Media Control" },
                color = Color(element.textColorHex),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { DeviceTelemetryProvider.sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS) }) {
                Icon(Icons.Rounded.SkipPrevious, "Previous", tint = Color(element.textColorHex))
            }
            IconButton(onClick = { DeviceTelemetryProvider.sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) }) {
                Icon(Icons.Rounded.PlayArrow, "Play/Pause", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { DeviceTelemetryProvider.sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_NEXT) }) {
                Icon(Icons.Rounded.SkipNext, "Next", tint = Color(element.textColorHex))
            }
        }
    }
}

@Composable
fun PhotoSlideshowWidgetContent(
    element: HomeElementEntity,
    imageResources: List<ImageResourceEntity>,
) {
    val paths = remember(element.imagePath, imageResources) {
        (listOfNotNull(element.imagePath) + imageResources.map { it.localPath })
            .distinct()
            .filter { File(it).exists() }
    }
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(paths.size) {
        if (paths.size > 1) {
            while (true) {
                delay(5000L)
                index = (index + 1) % paths.size
            }
        }
    }

    if (paths.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Image, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                Text("Add images to library", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            }
        }
    } else {
        AsyncImage(
            model = File(paths[index % paths.size]),
            contentDescription = "Slideshow photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}


private data class WeatherSnapshot(
    val city: String,
    val temperature: String,
    val feelsLike: String,
    val condition: String,
    val humidity: String,
    val wind: String,
)

private fun loadCityWeather(city: String): WeatherSnapshot {
    fun getJson(url: String): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8000
            readTimeout = 8000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "TagDeckLauncher/1.0")
        }
        return try {
            if (connection.responseCode !in 200..299) error("Weather service returned ${connection.responseCode}")
            JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }

    val encoded = URLEncoder.encode(city.trim(), "UTF-8")
    val geo = getJson("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1&language=en&format=json")
    val place = geo.optJSONArray("results")?.optJSONObject(0) ?: error("City not found")
    val lat = place.getDouble("latitude")
    val lon = place.getDouble("longitude")
    val displayCity = listOfNotNull(place.optString("name").takeIf { it.isNotBlank() }, place.optString("admin1").takeIf { it.isNotBlank() }).distinct().joinToString(", ")
    val forecast = getJson(
        "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m&timezone=auto"
    ).getJSONObject("current")
    val code = forecast.optInt("weather_code", -1)
    val condition = when (code) {
        0 -> "Clear sky"
        1 -> "Mainly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        56, 57 -> "Freezing drizzle"
        61, 63, 65 -> "Rain"
        66, 67 -> "Freezing rain"
        71, 73, 75, 77 -> "Snow"
        80, 81, 82 -> "Rain showers"
        85, 86 -> "Snow showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with hail"
        else -> "Current conditions"
    }
    return WeatherSnapshot(
        city = displayCity.ifBlank { city },
        temperature = "${forecast.optDouble("temperature_2m").roundToInt()}°",
        feelsLike = "${forecast.optDouble("apparent_temperature").roundToInt()}°",
        condition = condition,
        humidity = "${forecast.optInt("relative_humidity_2m")}%",
        wind = "${forecast.optDouble("wind_speed_10m").roundToInt()} km/h",
    )
}

@Composable
fun WeatherWidgetContent(element: HomeElementEntity) {
    val city = element.label.trim()
    var snapshot by remember(element.elementId, city) { mutableStateOf<WeatherSnapshot?>(null) }
    var error by remember(element.elementId, city) { mutableStateOf<String?>(null) }
    LaunchedEffect(city) {
        snapshot = null
        error = null
        if (city.isBlank()) {
            error = "Edit widget label to set a city"
        } else {
            runCatching { withContext(Dispatchers.IO) { loadCityWeather(city) } }
                .onSuccess { snapshot = it }
                .onFailure { error = it.message ?: "Weather unavailable" }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val data = snapshot
        if (data != null) {
            Text(data.city, color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(data.temperature, color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(data.condition, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(5.dp))
            Text("Feels ${data.feelsLike}  •  Humidity ${data.humidity}  •  Wind ${data.wind}", color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.labelSmall, maxLines = 2)
        } else {
            Text(error ?: "Loading weather…", color = Color.White, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text("Weather by Open-Meteo", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
        }
    }
}
