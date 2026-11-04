package com.animewalls.app.domain.license

/**
 * The single gate every image must pass before it may be shown as a downloadable
 * wallpaper.
 *
 * Wikimedia Commons hosts a lot of material that is NOT safe to redistribute:
 * photographs of copyrighted characters (cosplay, murals, merchandise), magazine
 * and DVD scans, franchise logos, and adult material. A free licence on the
 * *photograph* does not grant rights in the *character* it depicts, so those are
 * rejected outright. When a whole category is rejected this way the UI shows an
 * explicit empty state instead of inventing content.
 */
object LicensePolicy {

    /** Only these may be redistributed (with attribution / ShareAlike respected). */
    private val CC_BY_FAMILY = Regex("""^cc by( sa)? \d\.\d.*$""")

    private val NON_COMMERCIAL_OR_ND = Regex("""\bnc\b|non ?commercial|\bnd\b|no ?deriv""")

    private val ADULT = Regex(
        """hentai|ecchi|\bnude\b|nudity|\bnaked\b|nsfw|erotic|\bporn|topless|lingerie|bikini|""" +
        """underwear|\bbreasts?\b|fetish|bdsm|lolicon|shotacon|\bgore\b|\bguro\b|penis|vagina|nipple"""
    )

    /** Unusable for a clean wallpaper feed: derivative works, scans, merch, UI chrome. */
    private val DERIVATIVE = Regex(
        """cosplay|costume|cosplayer|mural|graffiti|logo|wordmark|screenshot|screen shot|poster|""" +
        """flyer|dvd|blu.?ray|\bvhs\b|cover art|magazine|\bscan\b|figurine|action figure|nendoroid|""" +
        """funko|\bplush\b|\btoy\b|merchandise|keychain|sticker|badge|trading card|convention|""" +
        """\bexpo\b|comiket|booth|stall|fan ?art|doujin|parody|watermark|\bmap\b|diagram|\bchart\b|""" +
        """graph\b|\bflag\b|coat of arms|\bseal\b|signature|document|slide|title card|anime expo|""" +
        """wikipedia|\bwig\b|manga museum|bookshop|book store|comic market"""
    )

    /**
     * Franchises whose characters are copyrighted. Any file mentioning one of
     * these is treated as an unverified derivative work and withheld, because a
     * Commons licence on the file cannot cover the underlying character.
     */
    private val FRANCHISE = Regex(
        """naruto|sasuke|sakura haruno|kakashi|one piece|luffy|\bzoro\b|\bnami\b|dragon ?ball|""" +
        """goku|vegeta|bleach|ichigo|kurosaki|demon slayer|kimetsu|tanjiro|nezuko|jujutsu|gojo|""" +
        """itadori|attack on titan|shingeki|levi ackerman|eren yeager|mikasa|death note|yagami|""" +
        """\bryuk\b|my hero academia|boku no hero|\bdeku\b|izuku|bakugo|chainsaw man|denji|makima|""" +
        """one punch man|saitama|\bgenos\b|tokyo ghoul|kaneki|hunter x hunter|gon freecss|killua|""" +
        """fullmetal alchemist|edward elric|spy x family|anya forger|loid forger|yor forger"""
    )

    /** Words that describe *how* an image was found rather than what it shows. */
    private val SOFT_TOKENS = setOf(
        "landscape", "view", "illustration", "art", "photo", "scenery", "scene",
        "style", "drawing", "image", "night", "day", "sunset", "sky",
    )

    enum class Rejection { ADULT, FRANCHISE, DERIVATIVE, LICENSE, QUALITY, IRRELEVANT }

    fun normalize(value: String): String =
        value.lowercase().replace(Regex("[\\s_\\-]+"), " ").trim()

    /** Strips HTML that Commons embeds in Artist / Credit / ImageDescription. */
    fun stripHtml(value: String?): String =
        (value ?: "").replace(Regex("<[^>]+>"), " ")
            .replace(Regex("&amp;"), "&").replace(Regex("&quot;"), "\"")
            .replace(Regex("&lt;"), "<").replace(Regex("&gt;"), ">")
            .replace(Regex("&nbsp;"), " ")
            .replace(Regex("\\s+"), " ").trim()

    fun isAllowedLicense(shortName: String, licenseCode: String, usageTerms: String): Boolean {
        val short = normalize(shortName)
        if (short.isEmpty()) return false
        val blob = "$short ${normalize(licenseCode)} ${normalize(usageTerms)}"
        if (NON_COMMERCIAL_OR_ND.containsMatchIn(blob)) return false
        if (short.startsWith("cc0")) return true
        if (short.contains("public domain") || short == "pd" || short.startsWith("pd ")) return true
        if (short.contains("no restrictions")) return true
        return CC_BY_FAMILY.matches(short)
    }

    fun rejectionReason(title: String, description: String): Rejection? {
        val blob = normalize("$title ${description.take(400)}")
        if (ADULT.containsMatchIn(blob)) return Rejection.ADULT
        if (FRANCHISE.containsMatchIn(blob)) return Rejection.FRANCHISE
        if (DERIVATIVE.containsMatchIn(blob)) return Rejection.DERIVATIVE
        return null
    }

    fun tokens(query: String): List<String> {
        val out = ArrayList<String>()
        for (raw in query.lowercase().split(Regex("[^a-z0-9]+"))) {
            if (raw.length < 3) continue
            val word = if (raw == "japanese") "japan" else raw
            if (!out.contains(word)) out.add(word)
        }
        return out
    }

    /** Every distinctive token of the query must actually describe the image. */
    fun isRelevant(query: String, haystack: String): Boolean {
        val blob = normalize(haystack)
        val required = tokens(query).filter { it !in SOFT_TOKENS }.ifEmpty { tokens(query) }
        if (required.isEmpty()) return true
        return required.all { blob.contains(it) }
    }

    fun isWallpaperQuality(width: Int, height: Int): Boolean {
        if (width <= 0 || height <= 0) return false
        val bigEnough = (width >= 1200 && height >= 900) || (height >= 1600 && width >= 900)
        if (!bigEnough) return false
        val ratio = width.toFloat() / height.toFloat()
        return ratio in 0.4f..2.6f
    }
}
