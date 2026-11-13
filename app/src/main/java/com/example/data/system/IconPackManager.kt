package com.example.data.system

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.util.concurrent.ConcurrentHashMap

data class InstalledIconPack(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap?,
    val mappedEntryCount: Int = 0,
)

private data class ParsedIconPack(
    val componentToDrawable: Map<String, String>,
    val packageToDrawable: Map<String, String>,
)

class IconPackManager(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val parsedPacks = ConcurrentHashMap<String, ParsedIconPack>()
    private val resolvedBitmapCache = ConcurrentHashMap<String, ImageBitmap>()
    private val negativeLookupCache = ConcurrentHashMap<String, Boolean>()

    companion object {
        private val ICON_PACK_INTENT_ACTIONS = listOf(
            "org.adw.launcher.THEMES",
            "com.novalauncher.THEME",
            "com.gau.go.launcherex.theme",
            "com.anddoes.launcher.THEME",
            "ginlemon.smartlauncher.THEMES",
            "com.teslacoilsw.launcher.THEME",
        )
    }

    suspend fun discoverInstalledIconPacks(): List<InstalledIconPack> = withContext(Dispatchers.IO) {
        val ownPkg = context.packageName
        val discovered = LinkedHashMap<String, InstalledIconPack>()

        for (action in ICON_PACK_INTENT_ACTIONS) {
            val intent = Intent(action)
            val resolved = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.queryIntentActivities(
                        intent,
                        PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                }
            } catch (_: Exception) {
                emptyList()
            }

            for (ri in resolved) {
                val pkg = ri.activityInfo?.packageName ?: continue
                if (pkg == ownPkg || pkg in discovered) continue
                val label = try {
                    ri.loadLabel(packageManager)?.toString()?.takeIf { it.isNotBlank() } ?: pkg
                } catch (_: Exception) {
                    pkg
                }
                val packIcon = try {
                    drawableToBitmap(ri.loadIcon(packageManager))?.asImageBitmap()
                } catch (_: Exception) {
                    null
                }
                val parsed = getOrParsePack(pkg)
                discovered[pkg] = InstalledIconPack(
                    packageName = pkg,
                    label = label,
                    icon = packIcon,
                    mappedEntryCount = parsed.componentToDrawable.size,
                )
            }
        }

        discovered.values.sortedBy { it.label.lowercase() }
    }

    suspend fun resolveIconFromHierarchy(
        packageName: String,
        componentName: String,
        packOrder: List<String>,
    ): ImageBitmap? = withContext(Dispatchers.IO) {
        if (packOrder.isEmpty()) return@withContext null

        val normalizedComponent = normalizeComponentKey(packageName, componentName)
        for (packPkg in packOrder) {
            val cacheKey = "$packPkg|$packageName|$normalizedComponent"
            resolvedBitmapCache[cacheKey]?.let { return@withContext it }
            if (negativeLookupCache.containsKey(cacheKey)) continue

            val loaded = loadIconFromSinglePack(packPkg, packageName, normalizedComponent)
            if (loaded != null) {
                resolvedBitmapCache[cacheKey] = loaded
                return@withContext loaded
            } else {
                negativeLookupCache[cacheKey] = true
            }
        }
        null
    }

    fun getCachedIconFromHierarchy(
        packageName: String,
        componentName: String,
        packOrder: List<String>,
    ): ImageBitmap? {
        if (packOrder.isEmpty()) return null
        val normalizedComponent = normalizeComponentKey(packageName, componentName)
        for (packPkg in packOrder) {
            val cacheKey = "$packPkg|$packageName|$normalizedComponent"
            resolvedBitmapCache[cacheKey]?.let { return it }
            if (!negativeLookupCache.containsKey(cacheKey)) {
                return null
            }
        }
        return null
    }

    fun clearAllCaches() {
        parsedPacks.clear()
        resolvedBitmapCache.clear()
        negativeLookupCache.clear()
    }

    fun invalidatePackage(packageName: String) {
        parsedPacks.remove(packageName)
        val keysToRemove = resolvedBitmapCache.keys.filter {
            it.startsWith("$packageName|") || it.contains("|$packageName|")
        }
        keysToRemove.forEach { resolvedBitmapCache.remove(it) }
        val negToRemove = negativeLookupCache.keys.filter {
            it.startsWith("$packageName|") || it.contains("|$packageName|")
        }
        negToRemove.forEach { negativeLookupCache.remove(it) }
    }

    private fun loadIconFromSinglePack(
        packPkg: String,
        appPackageName: String,
        normalizedComponent: String?,
    ): ImageBitmap? {
        return try {
            val packRes = packageManager.getResourcesForApplication(packPkg)
            val parsed = getOrParsePack(packPkg, packRes)
            val drawableName = (if (normalizedComponent != null) parsed.componentToDrawable[normalizedComponent] else null)
                ?: parsed.packageToDrawable[appPackageName]
                ?: return null

            val cleanName = drawableName.substringBeforeLast('.')
            val resId = packRes.getIdentifier(cleanName, "drawable", packPkg)
                .takeIf { it != 0 }
                ?: packRes.getIdentifier(cleanName, "mipmap", packPkg)
            if (resId == 0) return null

            val drawable = ResourcesCompat.getDrawable(packRes, resId, null) ?: return null
            drawableToBitmap(drawable)?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    private fun getOrParsePack(packPkg: String, providedRes: Resources? = null): ParsedIconPack {
        parsedPacks[packPkg]?.let { return it }

        val componentMap = HashMap<String, String>()
        val packageMap = HashMap<String, String>()

        try {
            val res = providedRes ?: packageManager.getResourcesForApplication(packPkg)
            val xmlResId = res.getIdentifier("appfilter", "xml", packPkg)
            if (xmlResId != 0) {
                res.getXml(xmlResId).use { parser ->
                    readAppFilterXml(parser, componentMap, packageMap)
                }
            } else {
                try {
                    res.assets.open("appfilter.xml").use { stream ->
                        val factory = XmlPullParserFactory.newInstance()
                        val parser = factory.newPullParser()
                        parser.setInput(stream, "UTF-8")
                        readAppFilterXml(parser, componentMap, packageMap)
                    }
                } catch (_: Exception) {
                }
            }
        } catch (_: Exception) {
        }

        val result = ParsedIconPack(componentMap, packageMap)
        parsedPacks[packPkg] = result
        return result
    }

    private fun readAppFilterXml(
        parser: XmlPullParser,
        componentMap: MutableMap<String, String>,
        packageMap: MutableMap<String, String>,
    ) {
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                val componentAttr = parser.getAttributeValue(null, "component")
                val drawableAttr = parser.getAttributeValue(null, "drawable")
                if (!componentAttr.isNullOrBlank() && !drawableAttr.isNullOrBlank()) {
                    val inner = extractComponentInner(componentAttr)
                    if (inner != null) {
                        componentMap[inner] = drawableAttr.trim()
                        val pkg = inner.substringBefore('/')
                        if (pkg.isNotBlank() && pkg !in packageMap) {
                            packageMap[pkg] = drawableAttr.trim()
                        }
                    }
                }
            }
            eventType = parser.next()
        }
    }

    private fun normalizeComponentKey(packageName: String, componentName: String): String? {
        if (componentName.isNotBlank()) {
            val unflattened = ComponentName.unflattenFromString(componentName)
            if (unflattened != null) {
                return "${unflattened.packageName}/${unflattened.className}"
            }
            return extractComponentInner(componentName)
        }
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val resolvedComp = launchIntent?.component
        return resolvedComp?.let { "${it.packageName}/${it.className}" }
    }

    private fun extractComponentInner(raw: String): String? {
        val trimmed = raw.trim()
        val body = if (trimmed.startsWith("ComponentInfo{") && trimmed.endsWith("}")) {
            trimmed.substring("ComponentInfo{".length, trimmed.length - 1)
        } else {
            trimmed
        }
        val slash = body.indexOf('/')
        if (slash <= 0 || slash >= body.lastIndex) return null
        val pkg = body.substring(0, slash).trim()
        val clsRaw = body.substring(slash + 1).trim()
        val cls = if (clsRaw.startsWith(".")) "$pkg$clsRaw" else clsRaw
        return "$pkg/$cls"
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 144
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 144
        val targetW = width.coerceAtMost(192)
        val targetH = height.coerceAtMost(192)
        return try {
            val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (_: Exception) {
            null
        }
    }
}
