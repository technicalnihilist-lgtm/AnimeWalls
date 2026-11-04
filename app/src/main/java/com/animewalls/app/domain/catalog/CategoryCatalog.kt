package com.animewalls.app.domain.catalog

import com.animewalls.app.domain.model.CategoryGroup
import com.animewalls.app.domain.model.WallpaperCategory

/**
 * The browse tree. Each entry maps to one or more real Wikimedia Commons search
 * queries -- there is no bundled or hand-picked image list anywhere in the app.
 * Queries were tuned against the live API so that they return genuine results;
 * franchise and character queries are kept exactly as specified even though
 * Commons has no freely licensed artwork for them, so the app can explain that
 * honestly instead of showing something it has no right to show.
 */
object CategoryCatalog {

    val popularAnime = listOf(
        WallpaperCategory("naruto", "Naruto", CategoryGroup.POPULAR_ANIME, listOf("Naruto Uzumaki", "Naruto anime series"), "Masashi Kishimoto's ninja saga."),
        WallpaperCategory("one_piece", "One Piece", CategoryGroup.POPULAR_ANIME, listOf("One Piece manga", "Monkey D. Luffy"), "Pirates, oceans and the Grand Line."),
        WallpaperCategory("dragon_ball", "Dragon Ball", CategoryGroup.POPULAR_ANIME, listOf("Dragon Ball manga", "Son Goku anime"), "Saiyans, ki blasts and tournaments."),
        WallpaperCategory("bleach", "Bleach", CategoryGroup.POPULAR_ANIME, listOf("Bleach manga", "Ichigo Kurosaki"), "Soul Reapers and the world of spirits."),
        WallpaperCategory("demon_slayer", "Demon Slayer", CategoryGroup.POPULAR_ANIME, listOf("Demon Slayer Kimetsu no Yaiba", "Tanjiro Kamado"), "Taisho-era demon hunting."),
        WallpaperCategory("jujutsu_kaisen", "Jujutsu Kaisen", CategoryGroup.POPULAR_ANIME, listOf("Jujutsu Kaisen", "Gojo Satoru"), "Cursed energy and Tokyo sorcery."),
        WallpaperCategory("attack_on_titan", "Attack on Titan", CategoryGroup.POPULAR_ANIME, listOf("Attack on Titan", "Shingeki no Kyojin"), "Walls, titans and freedom."),
        WallpaperCategory("death_note", "Death Note", CategoryGroup.POPULAR_ANIME, listOf("Death Note manga", "Light Yagami"), "A notebook and a moral abyss."),
        WallpaperCategory("my_hero_academia", "My Hero Academia", CategoryGroup.POPULAR_ANIME, listOf("My Hero Academia", "Boku no Hero Academia"), "Quirks, heroes and U.A. High."),
        WallpaperCategory("chainsaw_man", "Chainsaw Man", CategoryGroup.POPULAR_ANIME, listOf("Chainsaw Man manga", "Denji Chainsaw Man"), "Devils, contracts and chaos."),
        WallpaperCategory("one_punch_man", "One Punch Man", CategoryGroup.POPULAR_ANIME, listOf("One Punch Man", "Saitama One Punch Man"), "One hit, endless boredom."),
        WallpaperCategory("tokyo_ghoul", "Tokyo Ghoul", CategoryGroup.POPULAR_ANIME, listOf("Tokyo Ghoul", "Ken Kaneki"), "Half-human, half-ghoul, all night."),
        WallpaperCategory("hunter_x_hunter", "Hunter x Hunter", CategoryGroup.POPULAR_ANIME, listOf("Hunter x Hunter", "Gon Freecss"), "Hunters, Nen and adventure."),
        WallpaperCategory("fullmetal_alchemist", "Fullmetal Alchemist", CategoryGroup.POPULAR_ANIME, listOf("Fullmetal Alchemist", "Edward Elric"), "Equivalent exchange, two brothers."),
        WallpaperCategory("spy_x_family", "Spy x Family", CategoryGroup.POPULAR_ANIME, listOf("Spy x Family", "Anya Forger"), "A fake family, a real mission."),
    )

    val characters = listOf(
        WallpaperCategory("char_naruto", "Naruto Uzumaki", CategoryGroup.CHARACTERS, listOf("Naruto Uzumaki"), "The knucklehead ninja of Konoha."),
        WallpaperCategory("char_sasuke", "Sasuke Uchiha", CategoryGroup.CHARACTERS, listOf("Sasuke Uchiha"), "Last of the Uchiha."),
        WallpaperCategory("char_luffy", "Monkey D. Luffy", CategoryGroup.CHARACTERS, listOf("Monkey D. Luffy"), "The man who will be Pirate King."),
        WallpaperCategory("char_zoro", "Roronoa Zoro", CategoryGroup.CHARACTERS, listOf("Roronoa Zoro"), "Three swords, one promise."),
        WallpaperCategory("char_goku", "Goku", CategoryGroup.CHARACTERS, listOf("Son Goku Dragon Ball"), "Earth's strongest Saiyan."),
        WallpaperCategory("char_vegeta", "Vegeta", CategoryGroup.CHARACTERS, listOf("Vegeta Dragon Ball"), "Prince of all Saiyans."),
        WallpaperCategory("char_ichigo", "Ichigo Kurosaki", CategoryGroup.CHARACTERS, listOf("Ichigo Kurosaki"), "Substitute Soul Reaper."),
        WallpaperCategory("char_tanjiro", "Tanjiro Kamado", CategoryGroup.CHARACTERS, listOf("Tanjiro Kamado"), "Water-breathing demon slayer."),
        WallpaperCategory("char_gojo", "Gojo Satoru", CategoryGroup.CHARACTERS, listOf("Gojo Satoru"), "The strongest sorcerer."),
        WallpaperCategory("char_levi", "Levi Ackerman", CategoryGroup.CHARACTERS, listOf("Levi Ackerman"), "Humanity's strongest soldier."),
        WallpaperCategory("char_eren", "Eren Yeager", CategoryGroup.CHARACTERS, listOf("Eren Yeager"), "Attack Titan inheritor."),
        WallpaperCategory("char_saitama", "Saitama", CategoryGroup.CHARACTERS, listOf("Saitama One Punch Man"), "The hero for fun."),
    )

    val scenery = listOf(
        WallpaperCategory("sc_streets", "Japanese Streets", CategoryGroup.SCENERY, listOf("Japanese street", "Japan street neon"), "Neon alleys, vending machines, rain-slick asphalt."),
        WallpaperCategory("sc_sakura", "Cherry Blossom Trees", CategoryGroup.SCENERY, listOf("cherry blossom Japan", "sakura trees Japan"), "Sakura season in full bloom."),
        WallpaperCategory("sc_night", "Anime Night Sky", CategoryGroup.SCENERY, listOf("Japan night sky", "star trail Japan"), "Skylines and stars over Japan."),
        WallpaperCategory("sc_forests", "Anime Forests", CategoryGroup.SCENERY, listOf("Japanese forest", "bamboo forest Japan"), "Cedar, bamboo and shrine paths."),
        WallpaperCategory("sc_mountains", "Mountain Landscapes", CategoryGroup.SCENERY, listOf("Mount Fuji", "Japanese Alps mountains"), "Fuji-san and the alpine ranges."),
        WallpaperCategory("sc_ocean", "Ocean Views", CategoryGroup.SCENERY, listOf("Japan sea coast", "Okinawa beach"), "Coastlines from Hokkaido to Okinawa."),
        WallpaperCategory("sc_rain", "Rainy Anime Streets", CategoryGroup.SCENERY, listOf("Japan rain street", "Kyoto rain"), "Wet pavements and reflections."),
        WallpaperCategory("sc_sunset", "Sunset Landscapes", CategoryGroup.SCENERY, listOf("Japan sunset", "Mount Fuji sunset"), "Golden hour across the islands."),
        WallpaperCategory("sc_school", "School Life Scenery", CategoryGroup.SCENERY, listOf("school building Japan", "Japanese classroom"), "Classrooms, corridors and schoolyards."),
        WallpaperCategory("sc_houses", "Traditional Japanese Houses", CategoryGroup.SCENERY, listOf("traditional Japanese house", "machiya Kyoto"), "Machiya, tatami and tiled roofs."),
    )

    val collections = listOf(
        WallpaperCategory("col_dark", "Dark Anime", CategoryGroup.COLLECTIONS, listOf("Japan night alley", "Japanese temple night"), "Low light, high contrast, AMOLED-friendly."),
        WallpaperCategory("col_aesthetic", "Aesthetic Anime", CategoryGroup.COLLECTIONS, listOf("Kyoto street", "Japan aesthetic"), "Soft tones and quiet compositions."),
        WallpaperCategory("col_romantic", "Romantic Anime", CategoryGroup.COLLECTIONS, listOf("sakura lantern festival", "Japanese garden evening"), "Lantern light and warm evenings."),
        WallpaperCategory("col_sad", "Sad Anime", CategoryGroup.COLLECTIONS, listOf("Japan rainy night", "Kyoto misty morning"), "Melancholy weather and empty platforms."),
        WallpaperCategory("col_action", "Action Anime", CategoryGroup.COLLECTIONS, listOf("Japanese festival fireworks", "matsuri festival"), "Matsuri energy and fireworks."),
        WallpaperCategory("col_fantasy", "Fantasy Anime", CategoryGroup.COLLECTIONS, listOf("ukiyo-e", "Japanese mythology"), "Ukiyo-e, dragons and folklore."),
        WallpaperCategory("col_minimal", "Minimal Anime", CategoryGroup.COLLECTIONS, listOf("Japan minimal architecture", "Japanese zen garden"), "Zen restraint, negative space."),
        WallpaperCategory("col_girls", "Anime Girls", CategoryGroup.COLLECTIONS, listOf("anime girl illustration", "moe anime girl"), "Freely licensed anime-style illustration."),
        WallpaperCategory("col_boys", "Anime Boys", CategoryGroup.COLLECTIONS, listOf("anime boy", "bishounen"), "Freely licensed anime-style illustration."),
        WallpaperCategory("col_couples", "Anime Couples", CategoryGroup.COLLECTIONS, listOf("anime couple"), "Two characters, one frame."),
    )

    val all: List<WallpaperCategory> = popularAnime + characters + scenery + collections

    fun byId(id: String): WallpaperCategory? = all.firstOrNull { it.id == id }

    fun byGroup(group: CategoryGroup): List<WallpaperCategory> = all.filter { it.group == group }

    /** Rotating queries behind the "Featured" and "Trending" rails on Home. */
    val featuredQueries = listOf(
        "cherry blossom Japan", "Mount Fuji", "Kyoto street", "Japan night sky",
        "Japanese temple", "Okinawa beach", "Japanese garden", "Tokyo skyline night",
    )

    val trendingQueries = listOf(
        "ukiyo-e", "Japanese festival fireworks", "bamboo forest Japan",
        "Japan rain street", "snow Japan", "Japanese zen garden",
        "anime girl illustration", "Japan autumn leaves",
    )

    val recentQueries = listOf(
        "Japan landscape", "Japanese shrine", "Osaka night", "Hokkaido winter",
        "Japanese classroom", "Shibuya crossing", "torii gate Japan", "Japan coast ocean",
    )
}
