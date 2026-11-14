package com.fable.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.fable.app.domain.model.Book

/**
 * Genre visual identities — Fable's signature design system.
 *
 * Every genre owns a small, carefully tuned seed palette. [GenreThemes] derives a full
 * [GenreColors] set from that seed for light and dark global themes, so genre colour always
 * harmonises with the surrounding app theme instead of fighting it. Screens never hardcode
 * genre colours; they resolve a [GenreMatch] for a book and ask for [genreColors].
 */
enum class CardTreatment {
    /** Antique gold hairline and a warm inner glow. */
    GILDED,
    /** Soft blush gradient and gentle diffuse shadow. */
    SOFT_GLOW,
    /** Deep, dramatic drop shadow. */
    SHADOWED,
    /** Luminous coloured halo. */
    LUMINOUS,
    /** Aged paper tone with ruled edge. */
    PARCHMENT,
    /** Thick ink outline with an offset block shadow. */
    BOLD_INK,
    /** Barely-there border and generous air. */
    AIRY,
}

enum class CoverMotif { STARS, PETALS, RAIN, MOON, GRID, LINES, FRAME, DOTS, WAVES, RAYS, HALFTONE, ORBIT, SLASH }

@Immutable
data class GenreSeed(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    /** The deepest tone of the genre — used for dark backgrounds and cover ink. */
    val ink: Color,
    /** The lightest tone of the genre — used for light backgrounds. */
    val paper: Color,
)

enum class Genre(
    val label: String,
    /** Open Library subject used when browsing this genre. */
    val subjectQuery: String,
    val tagline: String,
    val ornament: String,
    val treatment: CardTreatment,
    val motif: CoverMotif,
    val seed: GenreSeed,
    /** Lower-case fragments matched against book subjects. Most specific first. */
    val keywords: List<String>,
) {
    DARK_ROMANCE(
        "Dark Romance", "dark romance", "Dangerous desire, tender ruin", "✥",
        CardTreatment.SHADOWED, CoverMotif.SLASH,
        GenreSeed(Color(0xFF7A1F33), Color(0xFF3A0F1A), Color(0xFFC0485A), Color(0xFF140A0D), Color(0xFFF4E5E8)),
        listOf("dark romance", "mafia romance", "forbidden love"),
    ),
    HISTORICAL_ROMANCE(
        "Historical Romance", "historical romance", "Ballrooms, letters & longing", "✾",
        CardTreatment.GILDED, CoverMotif.PETALS,
        GenreSeed(Color(0xFF8E4A5A), Color(0xFFD49AA5), Color(0xFFC9A45C), Color(0xFF2E1419), Color(0xFFF8EEE6)),
        listOf("historical romance", "regency", "victorian romance"),
    ),
    GRAPHIC_NOVELS(
        "Graphic Novels", "graphic novels", "Panels, ink & bold heroes", "✸",
        CardTreatment.BOLD_INK, CoverMotif.HALFTONE,
        GenreSeed(Color(0xFF1F4FD8), Color(0xFFFF7A1A), Color(0xFFFFD23F), Color(0xFF0A0A0A), Color(0xFFFFF8E6)),
        listOf("graphic novel", "comic", "manga", "cartoons"),
    ),
    DYSTOPIAN(
        "Dystopian", "dystopias", "Broken futures, defiant hearts", "✕",
        CardTreatment.BOLD_INK, CoverMotif.SLASH,
        GenreSeed(Color(0xFFA0482A), Color(0xFF5A5E62), Color(0xFFB23A3A), Color(0xFF141516), Color(0xFFEDEBE8)),
        listOf("dystopia", "post-apocalyptic", "apocalyp", "totalitarian"),
    ),
    PARANORMAL(
        "Paranormal", "paranormal", "Things that whisper after dark", "☽",
        CardTreatment.LUMINOUS, CoverMotif.MOON,
        GenreSeed(Color(0xFF5B2A9E), Color(0xFF2B1A4F), Color(0xFF8FA3C7), Color(0xFF0F0A1F), Color(0xFFF0ECF7)),
        listOf("paranormal", "supernatural", "vampire", "werewol", "witches", "witchcraft", "occult"),
    ),
    YOUNG_ADULT(
        "Young Adult", "young adult fiction", "Firsts, friendships & fire", "✧",
        CardTreatment.LUMINOUS, CoverMotif.DOTS,
        GenreSeed(Color(0xFFE85D50), Color(0xFF9B7BE0), Color(0xFF4D9DE0), Color(0xFF2A1E3D), Color(0xFFFFF3EF)),
        listOf("young adult", "teen", "coming of age", "high school"),
    ),
    CHILDRENS(
        "Children's Fiction", "juvenile fiction", "Wonder at every page turn", "✿",
        CardTreatment.AIRY, CoverMotif.ORBIT,
        GenreSeed(Color(0xFF3F8FD8), Color(0xFFF5C84C), Color(0xFFF28C76), Color(0xFF1D3557), Color(0xFFF2F8FC)),
        listOf("children", "juvenile", "picture book", "nursery", "bedtime", "fairy tale"),
    ),
    SCIENCE_FICTION(
        "Science Fiction", "science fiction", "Stars, signals & what comes next", "◇",
        CardTreatment.LUMINOUS, CoverMotif.GRID,
        GenreSeed(Color(0xFF4B3BD6), Color(0xFF8A5CF6), Color(0xFF2FD3E0), Color(0xFF070B1E), Color(0xFFECEEFB)),
        listOf("science fiction", "sci-fi", "space", "robot", "time travel", "cyberpunk", "extraterrestrial", "aliens", "interplanetary"),
    ),
    FANTASY(
        "Fantasy", "fantasy", "Enchanted realms & ancient magic", "✦",
        CardTreatment.GILDED, CoverMotif.STARS,
        GenreSeed(Color(0xFF1F6B4F), Color(0xFF2F4F3A), Color(0xFFC9A24A), Color(0xFF0D2219), Color(0xFFEEF3E8)),
        listOf("fantasy", "magic", "wizard", "dragon", "elves", "sorcer", "mythical", "quests (expeditions)"),
    ),
    HORROR(
        "Horror", "horror", "Dread that lingers past midnight", "†",
        CardTreatment.SHADOWED, CoverMotif.RAIN,
        GenreSeed(Color(0xFF7A1622), Color(0xFFA3121F), Color(0xFF8E8A85), Color(0xFF0A0909), Color(0xFFEFEAE8)),
        listOf("horror", "ghost", "haunted", "zombie", "monster", "terror"),
    ),
    THRILLER(
        "Thriller", "thriller", "Pulse-racing, page-burning", "▲",
        CardTreatment.BOLD_INK, CoverMotif.SLASH,
        GenreSeed(Color(0xFFB3202A), Color(0xFF2E2E33), Color(0xFFF0B429), Color(0xFF0C0C0E), Color(0xFFF4EEEC)),
        listOf("thriller", "suspense", "espionage", "spy", "conspiracy", "psychological fiction"),
    ),
    CRIME(
        "Crime", "crime", "Cold cases & city shadows", "◆",
        CardTreatment.SHADOWED, CoverMotif.LINES,
        GenreSeed(Color(0xFF1B2A41), Color(0xFF5E6B7A), Color(0xFFA6202E), Color(0xFF0B111C), Color(0xFFEEF0F3)),
        listOf("crime", "murder", "police", "criminal", "noir", "gangster", "heist"),
    ),
    MYSTERY(
        "Mystery", "mystery", "Clues in the fog", "◈",
        CardTreatment.SHADOWED, CoverMotif.RAIN,
        GenreSeed(Color(0xFF2A3F66), Color(0xFF23595C), Color(0xFFAEB8C2), Color(0xFF0B1324), Color(0xFFEDF0F4)),
        listOf("mystery", "detective", "whodunit", "sleuth", "private investigators", "amateur sleuth"),
    ),
    ROMANCE(
        "Romance", "romance", "Love stories that linger", "❦",
        CardTreatment.SOFT_GLOW, CoverMotif.PETALS,
        GenreSeed(Color(0xFFB5677A), Color(0xFFE7B8B5), Color(0xFFD9C3A0), Color(0xFF3E1823), Color(0xFFFBF0EE)),
        listOf("romance", "love stor", "love-letters", "man-woman relationships", "courtship"),
    ),
    POETRY(
        "Poetry", "poetry", "Verse, rhythm & quiet wonder", "❀",
        CardTreatment.SOFT_GLOW, CoverMotif.WAVES,
        GenreSeed(Color(0xFF8C79B8), Color(0xFFA77A9A), Color(0xFFE8D9BC), Color(0xFF2E2140), Color(0xFFF5F0F8)),
        listOf("poetry", "poems", "verse", "sonnet", "haiku"),
    ),
    BIOGRAPHY(
        "Biography & Memoir", "biography", "Real lives, beautifully told", "✒",
        CardTreatment.PARCHMENT, CoverMotif.LINES,
        GenreSeed(Color(0xFF6F6A3A), Color(0xFF9A6B3C), Color(0xFFC9A879), Color(0xFF2B2219), Color(0xFFF4EBDB)),
        listOf("biography", "memoir", "autobiograph", "correspondence"),
    ),
    PHILOSOPHY(
        "Philosophy", "philosophy", "Big questions, patient answers", "∴",
        CardTreatment.GILDED, CoverMotif.ORBIT,
        GenreSeed(Color(0xFF1F2F52), Color(0xFF8A857B), Color(0xFFBF9B4C), Color(0xFF0E1528), Color(0xFFF4F1EA)),
        listOf("philosophy", "ethics", "metaphysics", "existential", "stoic", "logic"),
    ),
    SELF_HELP(
        "Self-Help", "self-help", "Small habits, quiet growth", "↗",
        CardTreatment.AIRY, CoverMotif.RAYS,
        GenreSeed(Color(0xFF4F8A55), Color(0xFFF2B48C), Color(0xFF3F7D4E), Color(0xFF1F3324), Color(0xFFFAF5EC)),
        listOf("self-help", "self help", "personal development", "self-actualization", "success", "motivation", "habits"),
    ),
    SPIRITUALITY(
        "Spirituality", "spirituality", "Stillness, light & meaning", "☉",
        CardTreatment.AIRY, CoverMotif.RAYS,
        GenreSeed(Color(0xFF6F8C6A), Color(0xFFA99BCB), Color(0xFFD4B567), Color(0xFF2A3328), Color(0xFFF7F5EE)),
        listOf("spiritual", "religio", "meditation", "mindfulness", "buddh", "christian", "prayer", "mysticism"),
    ),
    CLASSICS(
        "Classics", "classic literature", "Timeless voices, enduring pages", "❧",
        CardTreatment.GILDED, CoverMotif.FRAME,
        GenreSeed(Color(0xFF6E1F2B), Color(0xFF23432F), Color(0xFFC2A257), Color(0xFF1E1513), Color(0xFFF6EEDC)),
        listOf("classic", "english literature", "russian literature", "french literature", "epic"),
    ),
    HISTORICAL_FICTION(
        "Historical Fiction", "historical fiction", "History, lived from within", "⚜",
        CardTreatment.PARCHMENT, CoverMotif.FRAME,
        GenreSeed(Color(0xFF7A5230), Color(0xFF4A3220), Color(0xFFB8913F), Color(0xFF24180F), Color(0xFFF3EAD8)),
        listOf("historical fiction", "history", "world war", "medieval", "civil war", "ancient"),
    ),
    ADVENTURE(
        "Adventure", "adventure", "Maps, voyages & the unknown", "➤",
        CardTreatment.SHADOWED, CoverMotif.RAYS,
        GenreSeed(Color(0xFFC2601E), Color(0xFF2F5A3A), Color(0xFFD9C08F), Color(0xFF2A1C10), Color(0xFFF7EEDF)),
        listOf("adventure", "voyage", "sea stories", "pirates", "explor", "survival", "treasure"),
    ),
    LITERARY_FICTION(
        "Literary Fiction", "literary fiction", "Interior worlds, exquisite prose", "¶",
        CardTreatment.AIRY, CoverMotif.LINES,
        GenreSeed(Color(0xFF666B36), Color(0xFFB5603F), Color(0xFFD8C9A8), Color(0xFF2A2A22), Color(0xFFF7F2E7)),
        listOf("literary", "domestic fiction"),
    ),
    CONTEMPORARY(
        "Contemporary Fiction", "contemporary fiction", "Ordinary lives, extraordinary moments", "◦",
        CardTreatment.AIRY, CoverMotif.ORBIT,
        GenreSeed(Color(0xFF6F8468), Color(0xFFC0704F), Color(0xFFD9C7AE), Color(0xFF2B2D2B), Color(0xFFF4F0E8)),
        listOf("contemporary", "friendship", "family life", "humorous", "city and town life"),
    ),
    ;

    companion object {
        /** The genres offered in Discover, in a pleasing editorial order. */
        val showcase: List<Genre> = listOf(
            FANTASY, ROMANCE, MYSTERY, THRILLER, SCIENCE_FICTION, HORROR, DARK_ROMANCE, HISTORICAL_FICTION,
            LITERARY_FICTION, CLASSICS, POETRY, YOUNG_ADULT, CONTEMPORARY, BIOGRAPHY, PHILOSOPHY, ADVENTURE,
            HISTORICAL_ROMANCE, PARANORMAL, DYSTOPIAN, CHILDRENS, GRAPHIC_NOVELS, SPIRITUALITY, SELF_HELP, CRIME,
        )
    }
}

/** Result of resolving a book's (often messy) subject list to visual identities. */
@Immutable
data class GenreMatch(
    val primary: Genre?,
    val secondary: List<Genre>,
) {
    val label: String? get() = primary?.label

    companion object {
        val Unknown = GenreMatch(primary = null, secondary = emptyList())
    }
}

/** A fully derived, contrast-checked colour set for one genre in one global-theme mode. */
@Immutable
data class GenreColors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val accent: Color,
    val background: Color,
    val surface: Color,
    val text: Color,
    val mutedText: Color,
    val badgeContainer: Color,
    val badgeContent: Color,
    val border: Color,
    /** Atmospheric gradient used behind genre sections and the book-detail hero. */
    val gradient: List<Color>,
    /** Rich gradient used on typographic placeholder covers and genre tiles — always deep. */
    val coverGradient: List<Color>,
    /** Text colour that is legible on [coverGradient]. */
    val onCover: Color,
    val glow: Color,
    val treatment: CardTreatment,
    val motif: CoverMotif,
    val ornament: String,
) {
    val gradientBrush: Brush get() = Brush.verticalGradient(gradient)
    val coverBrush: Brush get() = Brush.linearGradient(coverGradient)
}

object GenreThemes {
    private const val MAX_SECONDARY = 2

    /** Resolve a list of free-form subjects to a primary genre and secondary indicators. */
    fun resolve(categories: List<String>): GenreMatch {
        if (categories.isEmpty()) return GenreMatch.Unknown
        val scores = IntArray(Genre.entries.size)
        val firstSeen = IntArray(Genre.entries.size) { Int.MAX_VALUE }
        categories.forEachIndexed { index, raw ->
            val subject = raw.lowercase()
            for (genre in Genre.entries) {
                if (genre.keywords.any { subject.contains(it) } || subject.contains(genre.label.lowercase())) {
                    // Exact label matches are strong evidence; keyword hits are weaker.
                    scores[genre.ordinal] += if (subject.trim() == genre.label.lowercase()) 3 else 2
                    if (index < firstSeen[genre.ordinal]) firstSeen[genre.ordinal] = index
                }
            }
        }
        val ranked = Genre.entries
            .filter { scores[it.ordinal] > 0 }
            // Higher score wins; ties go to the more specific genre (enum order), then to the
            // subject listed first.
            .sortedWith(
                compareByDescending<Genre> { scores[it.ordinal] }
                    .thenBy { it.ordinal }
                    .thenBy { firstSeen[it.ordinal] },
            )
        if (ranked.isEmpty()) return GenreMatch.Unknown
        return GenreMatch(primary = ranked.first(), secondary = ranked.drop(1).take(MAX_SECONDARY))
    }

    fun resolve(book: Book): GenreMatch = resolve(book.categories)

    /** Find the genre for a label or subject query (e.g. a Discover route argument). */
    fun fromName(name: String): Genre? {
        val normalized = name.trim().lowercase()
        if (normalized.isEmpty()) return null
        return Genre.entries.firstOrNull { it.label.lowercase() == normalized || it.subjectQuery == normalized }
            ?: resolve(listOf(name)).primary
    }

    private val lightCache: Map<Genre, GenreColors> = Genre.entries.associateWith { derive(it, dark = false) }
    private val darkCache: Map<Genre, GenreColors> = Genre.entries.associateWith { derive(it, dark = true) }

    fun colors(genre: Genre, dark: Boolean): GenreColors = (if (dark) darkCache else lightCache).getValue(genre)

    /**
     * Neutral identity for books whose subjects do not match a known genre.
     * It is built from the active global theme so it always belongs on screen.
     */
    fun fallback(
        primary: Color,
        secondary: Color,
        accent: Color,
        background: Color,
        surface: Color,
        text: Color,
        dark: Boolean,
        ornament: String,
    ): GenreColors {
        val ink = if (dark) lerp(background, Color.Black, 0.2f) else lerp(primary, Color.Black, 0.55f)
        val coverStart = if (dark) lerp(surface, primary, 0.35f) else primary
        val onCover = readableOn(lerp(coverStart, ink, 0.5f))
        return GenreColors(
            primary = primary,
            onPrimary = readableOn(primary),
            secondary = secondary,
            accent = accent,
            background = background,
            surface = surface,
            text = text,
            mutedText = ensureContrast(lerp(text, background, 0.35f), background, 4.5f),
            badgeContainer = lerp(surface, primary, if (dark) 0.28f else 0.12f),
            badgeContent = ensureContrast(if (dark) text else primary, lerp(surface, primary, if (dark) 0.28f else 0.12f), 4.5f),
            border = primary.copy(alpha = 0.28f),
            gradient = listOf(lerp(background, primary, if (dark) 0.22f else 0.10f), background),
            coverGradient = listOf(coverStart, ink),
            onCover = onCover,
            glow = primary.copy(alpha = 0.35f),
            treatment = CardTreatment.AIRY,
            motif = CoverMotif.LINES,
            ornament = ornament,
        )
    }

    private fun derive(genre: Genre, dark: Boolean): GenreColors {
        val s = genre.seed
        val coverStart = lerp(s.primary, s.secondary, 0.15f)
        val coverEnd = lerp(s.ink, s.primary, 0.12f)
        val onCover = ensureContrast(lerp(s.paper, s.accent, 0.12f), lerp(coverStart, coverEnd, 0.5f), 4.5f)
        return if (dark) {
            val background = s.ink
            val surface = lerp(s.ink, s.primary, 0.16f)
            val primary = withReadableLabel(ensureContrast(lerp(s.primary, s.paper, 0.35f), surface, 3.0f))
            val accent = ensureContrast(lerp(s.accent, s.paper, 0.2f), surface, 3.0f)
            val text = ensureContrast(s.paper, background, 7f)
            val badgeContainer = lerp(s.ink, s.primary, 0.42f)
            GenreColors(
                primary = primary,
                onPrimary = readableOn(primary),
                secondary = lerp(s.secondary, s.paper, 0.25f),
                accent = accent,
                background = background,
                surface = surface,
                text = text,
                mutedText = ensureContrast(lerp(s.paper, s.ink, 0.32f), surface, 4.5f),
                badgeContainer = badgeContainer,
                badgeContent = ensureContrast(s.paper, badgeContainer, 4.5f),
                border = lerp(s.primary, s.accent, 0.4f).copy(alpha = 0.5f),
                gradient = listOf(lerp(s.ink, s.primary, 0.55f), lerp(s.ink, s.primary, 0.18f), s.ink),
                coverGradient = listOf(coverStart, coverEnd),
                onCover = onCover,
                glow = lerp(s.primary, s.accent, 0.3f).copy(alpha = 0.45f),
                treatment = genre.treatment,
                motif = genre.motif,
                ornament = genre.ornament,
            )
        } else {
            val background = s.paper
            val surface = lerp(s.paper, Color.White, 0.55f)
            val primary = withReadableLabel(ensureContrast(s.primary, surface, 4.5f))
            val text = ensureContrast(s.ink, background, 7f)
            val badgeContainer = lerp(s.paper, s.primary, 0.16f)
            GenreColors(
                primary = primary,
                onPrimary = readableOn(primary),
                secondary = s.secondary,
                accent = ensureContrast(s.accent, surface, 2.2f),
                background = background,
                surface = surface,
                text = text,
                mutedText = ensureContrast(lerp(s.ink, s.paper, 0.35f), surface, 4.5f),
                badgeContainer = badgeContainer,
                badgeContent = ensureContrast(lerp(s.primary, s.ink, 0.35f), badgeContainer, 4.5f),
                border = s.primary.copy(alpha = 0.28f),
                gradient = listOf(lerp(s.paper, s.primary, 0.28f), lerp(s.paper, s.secondary, 0.08f), s.paper),
                coverGradient = listOf(coverStart, coverEnd),
                onCover = onCover,
                glow = s.primary.copy(alpha = 0.30f),
                treatment = genre.treatment,
                motif = genre.motif,
                ornament = genre.ornament,
            )
        }
    }
}

/** WCAG contrast ratio between two colours. */
fun contrastRatio(foreground: Color, background: Color): Float {
    val a = foreground.luminance() + 0.05f
    val b = background.luminance() + 0.05f
    return if (a > b) a / b else b / a
}

/** Nudges [foreground] toward black or white until it meets [minimum] contrast on [background]. */
fun ensureContrast(foreground: Color, background: Color, minimum: Float): Color {
    if (contrastRatio(foreground, background) >= minimum) return foreground
    val target = if (background.luminance() > 0.4f) Color.Black else Color.White
    var step = 0.1f
    while (step <= 1f) {
        val candidate = lerp(foreground, target, step)
        if (contrastRatio(candidate, background) >= minimum) return candidate
        step += 0.1f
    }
    return target
}

/**
 * Adjusts a button/fill colour so that its best label colour reaches 4.5:1. Mid-tones that
 * neither black nor white can sit on are pushed lighter or darker as needed.
 */
fun withReadableLabel(fill: Color): Color {
    val label = readableOn(fill)
    return if (contrastRatio(label, fill) >= 4.5f) fill else ensureContrast(fill, label, 4.5f)
}

/** Near-black or near-white, whichever reads better on [background]. */
fun readableOn(background: Color): Color {
    val light = Color(0xFFFFFBF5)
    val dark = Color(0xFF16130F)
    return if (contrastRatio(light, background) >= contrastRatio(dark, background)) light else dark
}

/** Composable entry point: genre colours tuned to the active global theme. */
@Composable
fun genreColors(genre: Genre?): GenreColors {
    val tokens = FableThemeTokens.current
    val scheme = MaterialTheme.colorScheme
    if (genre != null) return GenreThemes.colors(genre, tokens.dark)
    return remember(scheme.primary, scheme.background, tokens.dark, tokens.ornament) {
        GenreThemes.fallback(
            primary = scheme.primary,
            secondary = scheme.secondary,
            accent = tokens.gilt,
            background = scheme.background,
            surface = scheme.surface,
            text = scheme.onBackground,
            dark = tokens.dark,
            // A dash reads as "minus" on a chip, so neutral identities use a quiet ring instead.
            ornament = tokens.ornament.takeUnless { it == "—" } ?: "◦",
        )
    }
}

@Composable
fun rememberGenreMatch(book: Book): GenreMatch = remember(book.id, book.categories) { GenreThemes.resolve(book.categories) }

@Composable
fun rememberGenreMatch(categories: List<String>): GenreMatch = remember(categories) { GenreThemes.resolve(categories) }
