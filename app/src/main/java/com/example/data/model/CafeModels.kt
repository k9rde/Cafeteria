package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

enum class TableStatus(val labelAr: String) {
    AVAILABLE("فارغ"),
    OCCUPIED("مشغول"),
    BILL_REQUESTED("بانتظار الحساب")
}

enum class TableFilter(val labelAr: String) {
    ALL("الكل"),
    AVAILABLE("فارغ"),
    OCCUPIED("مشغول"),
    BILL_REQUESTED("بانتظار الحساب")
}

enum class CurrencyOption(
    val code: String,
    val symbol: String,
    val labelAr: String,
    val usesDecimals: Boolean
) {
    IQD("IQD", "د.ع", "دينار عراقي", false),
    USD("USD", "$", "دولار أمريكي", true),
    SAR("SAR", "ر.س", "ريال سعودي", true),
    AED("AED", "د.إ", "درهم إماراتي", true),
    JOD("JOD", "د.أ", "دينار أردني", true),
    EGP("EGP", "ج.م", "جنيه مصري", true);

    companion object {
        fun fromCode(code: String): CurrencyOption =
            entries.firstOrNull { it.code == code } ?: IQD
    }
}

enum class WallpaperOption(
    val key: String,
    val labelAr: String
) {
    ESPRESSO("espresso", "صالة الإسبريسو"),
    OCEAN("ocean", "أمواج المحيط"),
    EMERALD("emerald", "ضباب الزمرد"),
    CLASSIC("classic", "تدرج كلاسيكي");

    companion object {
        fun fromKey(key: String): WallpaperOption =
            entries.firstOrNull { it.key == key } ?: ESPRESSO
    }
}

data class AppSettingsState(
    val glassOpacity: Float = 0.40f,
    val blurIntensity: Float = 16f,
    val isDarkMode: Boolean = true,
    val wallpaper: WallpaperOption = WallpaperOption.ESPRESSO,
    val currency: CurrencyOption = CurrencyOption.IQD,
    val ownerPin: String = ""
)

data class TopSoldItem(
    val itemName: String,
    val categoryName: String,
    val totalQuantity: Int,
    val totalRevenue: Double
)

data class DailySummaryState(
    val totalRevenue: Double = 0.0,
    val closedTablesCount: Int = 0,
    val topItems: List<TopSoldItem> = emptyList(),
    val topHookah: TopSoldItem? = null,
    val topDrink: TopSoldItem? = null
)

fun formatPrice(amount: Double, currency: CurrencyOption): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        if (currency.usesDecimals) {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        } else {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
    }
    val formatted = formatter.format(if (currency.usesDecimals) amount else kotlin.math.round(amount))
    return if (currency == CurrencyOption.USD) {
        "${currency.symbol}$formatted"
    } else {
        "$formatted ${currency.symbol}"
    }
}
