package com.example.data.system

import com.example.data.db.AppEntity
import com.example.data.db.ElementStyleExtras
import com.example.data.db.HomeElementEntity
import com.example.data.db.HomePageEntity
import com.example.data.db.TagEntity
import com.example.data.prefs.LauncherSettings

data class StarterTemplateDescriptor(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val accentHex: Long,
    val previewColors: List<Long>,
)

data class GeneratedTemplateLayout(
    val pages: List<HomePageEntity>,
    val elements: List<HomeElementEntity>,
    val settingsTransform: (LauncherSettings) -> LauncherSettings,
)

object StarterTemplates {
    val catalog: List<StarterTemplateDescriptor> = listOf(
        StarterTemplateDescriptor(
            id = "CYBER_HUD",
            title = "Cyber HUD",
            subtitle = "Pinned status ribbon, glowing gauges, 3D Cube transitions & widget stack",
            badge = "HUD + STACKS",
            accentHex = 0xFF38BDF8L,
            previewColors = listOf(0xFF070B14L, 0xFF38BDF8L, 0xFFA855F7L),
        ),
        StarterTemplateDescriptor(
            id = "EDITORIAL_MINIMAL",
            title = "Editorial Minimal",
            subtitle = "Bold readable typography, clean frames, quick notes & distraction-free app grid",
            badge = "READABLE",
            accentHex = 0xFF0F172AL,
            previewColors = listOf(0xFFF4F7FBL, 0xFF0F172AL, 0xFF6366F1L),
        ),
        StarterTemplateDescriptor(
            id = "GLASS_BENTO",
            title = "Glass Bento Dashboard",
            subtitle = "Layered bento tiles, Focus Timer, Search Bar, Calculator & Scroll Container",
            badge = "BENTO PRO",
            accentHex = 0xFF34D399L,
            previewColors = listOf(0xFF0B1524L, 0xFF34D399L, 0xFFFBBF24L),
        ),
    )

    fun generate(
        templateId: String,
        apps: List<AppEntity>,
        tags: List<TagEntity>,
    ): GeneratedTemplateLayout = when (templateId) {
        "EDITORIAL_MINIMAL" -> buildEditorialMinimal(apps, tags)
        "GLASS_BENTO" -> buildGlassBento(apps, tags)
        else -> buildCyberHud(apps, tags)
    }

    private fun buildCyberHud(
        apps: List<AppEntity>,
        tags: List<TagEntity>,
    ): GeneratedTemplateLayout {
        val page1 = HomePageEntity(pageId = 1001L, title = "Command Deck", pageOrder = 0, isDefaultHome = true)
        val page2 = HomePageEntity(pageId = 1002L, title = "System & Apps", pageOrder = 1)
        val stackId = 2001L
        val elements = mutableListOf<HomeElementEntity>()

        // Sticky Pinned HUD Header across all pages
        elements += HomeElementEntity(
            elementId = 2000L,
            pageId = page1.pageId,
            type = "TEXT_LABEL",
            xFraction = 0.04f,
            yFraction = 0.02f,
            widthDp = 340f,
            heightDp = 42f,
            zIndex = 20,
            pinnedToAllPages = true,
            label = "{GREETING} · BAT {BATTERY} · {NETWORK}",
            fontSizeSp = 12f,
            backgroundColorHex = 0xAA0B1324L,
            textColorHex = 0xFF38BDF8L,
            borderColorHex = 0x8838BDF8L,
            borderWidthDp = 1.2f,
            cornerRadiusDp = 14f,
            styleExtrasJson = ElementStyleExtras(
                borderStyle = "GRADIENT",
                borderGradientEndHex = 0xFFA855F7L,
                shadowColorHex = 0x6638BDF8L,
                shadowBlurDp = 10f,
                fontWeight = "BOLD",
                textTransform = "UPPERCASE",
                letterSpacingEm = 0.06f,
            ).toJson(),
        )

        // Hero Widget Stack on Page 1
        elements += HomeElementEntity(
            elementId = stackId,
            pageId = page1.pageId,
            type = "WIDGET_STACK",
            xFraction = 0.04f,
            yFraction = 0.11f,
            widthDp = 340f,
            heightDp = 148f,
            zIndex = 2,
            label = "Telemetry Stack",
            backgroundColorHex = 0xCC0D1628L,
            borderColorHex = 0x8838BDF8L,
            borderWidthDp = 1.4f,
            cornerRadiusDp = 24f,
            styleExtrasJson = ElementStyleExtras(
                stackSwipeAxis = "VERTICAL",
                stackIndicatorStyle = "PILL",
                shadowColorHex = 0x5538BDF8L,
                shadowBlurDp = 14f,
            ).toJson(),
        )
        elements += HomeElementEntity(
            elementId = 2002L,
            pageId = page1.pageId,
            parentContainerId = stackId,
            type = "DIGITAL_CLOCK",
            widthDp = 320f,
            heightDp = 132f,
            zIndex = 0,
            label = "Live Clock",
            backgroundColorHex = 0x00000000L,
            borderWidthDp = 0f,
            textColorHex = 0xFFF8FAFC,
        )
        elements += HomeElementEntity(
            elementId = 2003L,
            pageId = page1.pageId,
            parentContainerId = stackId,
            type = "GAUGE_WIDGET",
            widthDp = 320f,
            heightDp = 132f,
            zIndex = 1,
            label = "Battery Ring",
            backgroundColorHex = 0x00000000L,
            borderWidthDp = 0f,
            textColorHex = 0xFF34D399L,
            styleExtrasJson = ElementStyleExtras(gaugeStyle = "RING", gaugeMetric = "BATTERY").toJson(),
        )
        elements += HomeElementEntity(
            elementId = 2004L,
            pageId = page1.pageId,
            parentContainerId = stackId,
            type = "GAUGE_WIDGET",
            widthDp = 320f,
            heightDp = 132f,
            zIndex = 2,
            label = "Day Progress",
            backgroundColorHex = 0x00000000L,
            borderWidthDp = 0f,
            textColorHex = 0xFF38BDF8L,
            styleExtrasJson = ElementStyleExtras(gaugeStyle = "ARC", gaugeMetric = "DAY_PROGRESS").toJson(),
        )

        // Device Toggles Row
        listOf(
            Triple("TORCH", "Torch", 0.04f),
            Triple("RINGER", "Ringer", 0.36f),
            Triple("WIFI_PANEL", "Wi-Fi", 0.68f),
        ).forEachIndexed { idx, (action, title, xf) ->
            elements += HomeElementEntity(
                elementId = 2010L + idx,
                pageId = page1.pageId,
                type = "DEVICE_TOGGLE_WIDGET",
                xFraction = xf,
                yFraction = 0.34f,
                widthDp = 104f,
                heightDp = 76f,
                zIndex = 3 + idx,
                label = title,
                backgroundColorHex = 0xAA0E182DL,
                borderColorHex = 0x6638BDF8L,
                borderWidthDp = 1.2f,
                cornerRadiusDp = 18f,
                styleExtrasJson = ElementStyleExtras(
                    deviceToggleAction = action,
                    useCustomCorners = true,
                    cornerTopStartDp = 22f,
                    cornerTopEndDp = 8f,
                    cornerBottomEndDp = 22f,
                    cornerBottomStartDp = 8f,
                ).toJson(),
            )
        }

        // App Group Grid
        elements += HomeElementEntity(
            elementId = 2020L,
            pageId = page1.pageId,
            type = "APP_GROUP_WIDGET",
            xFraction = 0.04f,
            yFraction = 0.48f,
            widthDp = 340f,
            heightDp = 180f,
            zIndex = 6,
            label = "Priority Apps",
            backgroundColorHex = 0x990C1322L,
            borderColorHex = 0x44FFFFFF,
            borderWidthDp = 1f,
            cornerRadiusDp = 22f,
            styleExtrasJson = ElementStyleExtras(
                appGroupSource = if (apps.any { it.favorite }) "FAVORITES" else "RECENT",
                appGroupColumns = 4,
                appGroupIconSizeDp = 44,
            ).toJson(),
        )

        // Page 2: Tag Shortcuts + Mini Calculator + Page Switcher
        elements += HomeElementEntity(
            elementId = 2030L,
            pageId = page2.pageId,
            type = "PAGE_SWITCHER_WIDGET",
            xFraction = 0.04f,
            yFraction = 0.11f,
            widthDp = 340f,
            heightDp = 54f,
            zIndex = 1,
            label = "Pages",
            backgroundColorHex = 0xBB0F172AL,
            borderColorHex = 0x5538BDF8L,
            cornerRadiusDp = 18f,
        )
        elements += HomeElementEntity(
            elementId = 2031L,
            pageId = page2.pageId,
            type = "MINI_CALCULATOR_WIDGET",
            xFraction = 0.04f,
            yFraction = 0.21f,
            widthDp = 340f,
            heightDp = 240f,
            zIndex = 2,
            label = "Quick Calc",
            backgroundColorHex = 0xCC0D1628L,
            borderColorHex = 0x55A855F7L,
            cornerRadiusDp = 24f,
        )
        tags.take(3).forEachIndexed { idx, tag ->
            elements += HomeElementEntity(
                elementId = 2040L + idx,
                pageId = page2.pageId,
                type = "TAG_SHORTCUT",
                xFraction = 0.04f + idx * 0.32f,
                yFraction = 0.58f,
                widthDp = 104f,
                heightDp = 90f,
                zIndex = 3 + idx,
                label = tag.name,
                targetTagId = tag.tagId,
                backgroundColorHex = 0x88111B33L,
                borderColorHex = tag.colorHex,
                borderWidthDp = 1.3f,
                cornerRadiusDp = 20f,
            )
        }

        return GeneratedTemplateLayout(
            pages = listOf(page1, page2),
            elements = elements,
            settingsTransform = { s ->
                s.copy(
                    themeMode = "DARK",
                    accentColorHex = 0xFF38BDF8L,
                    secondaryColorHex = 0xFFA855F7L,
                    pageTransitionStyle = "CUBE",
                    ambientEffectType = "AURORA",
                    showCustomStatusStrip = false,
                    pageIndicatorStyle = "PILL",
                    pageIndicatorPlacement = "BOTTOM",
                )
            },
        )
    }

    private fun buildEditorialMinimal(
        apps: List<AppEntity>,
        tags: List<TagEntity>,
    ): GeneratedTemplateLayout {
        val page1 = HomePageEntity(pageId = 1101L, title = "Editorial", pageOrder = 0, isDefaultHome = true)
        val elements = mutableListOf<HomeElementEntity>()

        elements += HomeElementEntity(
            elementId = 2101L,
            pageId = page1.pageId,
            type = "TEXT_LABEL",
            xFraction = 0.06f,
            yFraction = 0.04f,
            widthDp = 320f,
            heightDp = 64f,
            zIndex = 1,
            label = "{GREETING}\n{DATE}",
            fontSizeSp = 22f,
            backgroundColorHex = 0x00000000L,
            borderWidthDp = 0f,
            textColorHex = 0xFF0F172AL,
            styleExtrasJson = ElementStyleExtras(
                fontWeight = "BOLD",
                textAlign = "START",
                lineHeightMultiplier = 1.25f,
            ).toJson(),
        )
        elements += HomeElementEntity(
            elementId = 2102L,
            pageId = page1.pageId,
            type = "SEARCH_BAR_WIDGET",
            xFraction = 0.06f,
            yFraction = 0.16f,
            widthDp = 320f,
            heightDp = 52f,
            zIndex = 2,
            label = "Search apps, tags & settings",
            backgroundColorHex = 0xFFFFFFFFL,
            borderColorHex = 0xFFCBD5E1L,
            borderWidthDp = 1.2f,
            textColorHex = 0xFF0F172AL,
            cornerRadiusDp = 26f,
        )
        elements += HomeElementEntity(
            elementId = 2103L,
            pageId = page1.pageId,
            type = "QUICK_NOTES_WIDGET",
            xFraction = 0.06f,
            yFraction = 0.26f,
            widthDp = 320f,
            heightDp = 148f,
            zIndex = 3,
            label = "Daily Notes",
            backgroundColorHex = 0xFFFFFFFFL,
            borderColorHex = 0xFFE2E8F0L,
            borderWidthDp = 1.2f,
            textColorHex = 0xFF0F172AL,
            cornerRadiusDp = 22f,
        )
        elements += HomeElementEntity(
            elementId = 2104L,
            pageId = page1.pageId,
            type = "APP_GROUP_WIDGET",
            xFraction = 0.06f,
            yFraction = 0.50f,
            widthDp = 320f,
            heightDp = 190f,
            zIndex = 4,
            label = "Essentials",
            backgroundColorHex = 0xEEFFFFFFL,
            borderColorHex = 0xFFE2E8F0L,
            borderWidthDp = 1.2f,
            textColorHex = 0xFF0F172AL,
            cornerRadiusDp = 24f,
            styleExtrasJson = ElementStyleExtras(
                appGroupSource = if (apps.any { it.favorite }) "FAVORITES" else "RECENT",
                appGroupColumns = 4,
                appGroupIconSizeDp = 46,
            ).toJson(),
        )
        if (tags.isNotEmpty()) {
            elements += HomeElementEntity(
                elementId = 2105L,
                pageId = page1.pageId,
                type = "TAG_SHORTCUT",
                xFraction = 0.06f,
                yFraction = 0.78f,
                widthDp = 152f,
                heightDp = 64f,
                zIndex = 5,
                label = tags.first().name,
                targetTagId = tags.first().tagId,
                backgroundColorHex = 0xFF0F172AL,
                textColorHex = 0xFFFFFFFFL,
                borderWidthDp = 0f,
                cornerRadiusDp = 32f,
                styleExtrasJson = ElementStyleExtras(iconPlacement = "LEFT").toJson(),
            )
        }

        return GeneratedTemplateLayout(
            pages = listOf(page1),
            elements = elements,
            settingsTransform = { s ->
                s.copy(
                    themeMode = "LIGHT",
                    accentColorHex = 0xFF0F172AL,
                    secondaryColorHex = 0xFF6366F1L,
                    wallpaperType = "SOLID",
                    wallpaperSolidHex = 0xFFF4F7FBL,
                    wallpaperDimAlpha = 0f,
                    pageTransitionStyle = "DEPTH",
                    ambientEffectType = "NONE",
                    highReadabilityMode = true,
                    boldLabelsEnabled = true,
                    textOutlineEnabled = false,
                    labelBackdropEnabled = false,
                    autoContrastText = true,
                )
            },
        )
    }

    private fun buildGlassBento(
        apps: List<AppEntity>,
        tags: List<TagEntity>,
    ): GeneratedTemplateLayout {
        val page1 = HomePageEntity(pageId = 1201L, title = "Bento Home", pageOrder = 0, isDefaultHome = true)
        val stackId = 2201L
        val containerId = 2210L
        val elements = mutableListOf<HomeElementEntity>()

        elements += HomeElementEntity(
            elementId = 2200L,
            pageId = page1.pageId,
            type = "SEARCH_BAR_WIDGET",
            xFraction = 0.05f,
            yFraction = 0.03f,
            widthDp = 330f,
            heightDp = 52f,
            zIndex = 1,
            label = "Spotlight Search",
            backgroundColorHex = 0x77122238L,
            borderColorHex = 0x5534D399L,
            borderWidthDp = 1f,
            cornerRadiusDp = 24f,
        )

        // Left Focus Timer Bento Card
        elements += HomeElementEntity(
            elementId = 2205L,
            pageId = page1.pageId,
            type = "FOCUS_TIMER_WIDGET",
            xFraction = 0.05f,
            yFraction = 0.13f,
            widthDp = 158f,
            heightDp = 158f,
            zIndex = 2,
            label = "Focus",
            backgroundColorHex = 0x88102033L,
            borderColorHex = 0x6634D399L,
            borderWidthDp = 1.2f,
            cornerRadiusDp = 26f,
            styleExtrasJson = ElementStyleExtras(focusMinutes = 25).toJson(),
        )

        // Right Gauge Widget Stack
        elements += HomeElementEntity(
            elementId = stackId,
            pageId = page1.pageId,
            type = "WIDGET_STACK",
            xFraction = 0.51f,
            yFraction = 0.13f,
            widthDp = 166f,
            heightDp = 158f,
            zIndex = 3,
            label = "Vitals Stack",
            backgroundColorHex = 0x88102033L,
            borderColorHex = 0x66FBBF24L,
            borderWidthDp = 1.2f,
            cornerRadiusDp = 26f,
            styleExtrasJson = ElementStyleExtras(
                stackSwipeAxis = "VERTICAL",
                stackAutoRotateSec = 8,
                stackIndicatorStyle = "DOTS",
            ).toJson(),
        )
        elements += HomeElementEntity(
            elementId = 2202L,
            pageId = page1.pageId,
            parentContainerId = stackId,
            type = "GAUGE_WIDGET",
            widthDp = 154f,
            heightDp = 142f,
            zIndex = 0,
            label = "Battery",
            backgroundColorHex = 0x00000000L,
            borderWidthDp = 0f,
            textColorHex = 0xFF34D399L,
            styleExtrasJson = ElementStyleExtras(gaugeStyle = "RING", gaugeMetric = "BATTERY").toJson(),
        )
        elements += HomeElementEntity(
            elementId = 2203L,
            pageId = page1.pageId,
            parentContainerId = stackId,
            type = "GAUGE_WIDGET",
            widthDp = 154f,
            heightDp = 142f,
            zIndex = 1,
            label = "RAM",
            backgroundColorHex = 0x00000000L,
            borderWidthDp = 0f,
            textColorHex = 0xFFFBBF24L,
            styleExtrasJson = ElementStyleExtras(gaugeStyle = "SEGMENTED", gaugeMetric = "RAM").toJson(),
        )

        // Scrollable Container Box of Tags / Shortcuts
        elements += HomeElementEntity(
            elementId = containerId,
            pageId = page1.pageId,
            type = "CONTAINER_BOX",
            xFraction = 0.05f,
            yFraction = 0.38f,
            widthDp = 330f,
            heightDp = 136f,
            zIndex = 4,
            label = "Tag Carousel",
            backgroundColorHex = 0x660D1A2BL,
            borderColorHex = 0x44FFFFFF,
            borderWidthDp = 1f,
            cornerRadiusDp = 24f,
            styleExtrasJson = ElementStyleExtras(containerScrollAxis = "HORIZONTAL").toJson(),
        )
        tags.take(5).forEachIndexed { idx, tag ->
            elements += HomeElementEntity(
                elementId = 2211L + idx,
                pageId = page1.pageId,
                parentContainerId = containerId,
                type = "TAG_SHORTCUT",
                widthDp = 96f,
                heightDp = 92f,
                zIndex = idx,
                label = tag.name,
                targetTagId = tag.tagId,
                backgroundColorHex = 0x8815253CL,
                borderColorHex = tag.colorHex,
                borderWidthDp = 1f,
                cornerRadiusDp = 20f,
            )
        }

        // Bottom App Group
        elements += HomeElementEntity(
            elementId = 2230L,
            pageId = page1.pageId,
            type = "APP_GROUP_WIDGET",
            xFraction = 0.05f,
            yFraction = 0.60f,
            widthDp = 330f,
            heightDp = 176f,
            zIndex = 5,
            label = "Favorite Apps",
            backgroundColorHex = 0x770E1D30L,
            borderColorHex = 0x44FFFFFF,
            borderWidthDp = 1f,
            cornerRadiusDp = 26f,
            styleExtrasJson = ElementStyleExtras(
                appGroupSource = if (apps.any { it.favorite }) "FAVORITES" else "MOST_USED",
                appGroupColumns = 4,
                appGroupIconSizeDp = 44,
            ).toJson(),
        )

        return GeneratedTemplateLayout(
            pages = listOf(page1),
            elements = elements,
            settingsTransform = { s ->
                s.copy(
                    themeMode = "DARK",
                    accentColorHex = 0xFF34D399L,
                    secondaryColorHex = 0xFFFBBF24L,
                    pageTransitionStyle = "PARALLAX",
                    ambientEffectType = "BOKEH",
                    showCustomStatusStrip = true,
                    wallpaperType = "GRADIENT",
                    wallpaperGradientStartHex = 0xFF071422L,
                    wallpaperGradientEndHex = 0xFF13293DL,
                    wallpaperGradientDirection = "DIAGONAL",
                )
            },
        )
    }
}
