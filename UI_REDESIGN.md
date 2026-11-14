# Fable — UI/UX Redesign

A full visual and interaction redesign of Fable. **Only the presentation layer changed**: no
ViewModel, repository, Room schema, Firebase or Open Library code was modified, and every
existing route, action and theme preference still works the same way.

## Design system (`presentation/theme`, `presentation/designsystem`)

| File | What it holds |
| --- | --- |
| `theme/FableTheme.kt` | The five global themes (Gothic, Gothic Romance, Dark Academia, Dreamscape, Minimalist): palettes, editorial typography, shapes, atmospheric glows. Theme switches cross-fade. |
| `theme/GenreTheme.kt` | **Genre identities**: 24 genres, each with a seed palette, ornament, motif and card treatment. Colours are derived for light and dark themes and checked against WCAG contrast. Also includes the subject → genre resolver (primary genre plus up to 2 secondary genres, with a graceful fallback for unknown subjects). |
| `theme/FableTokens.kt` | Spacing, elevation, motion specs (springs, easings, durations) and cover proportions. |
| `designsystem/*` | Shared components: icons, buttons, fields, cards, chips, segmented tabs, banners, skeletons, empty/error states, rating input and display, progress ring and bar, avatars, genre badges and tiles, cover motifs, bottom bar, shared-element helpers, haptics. |

### Genre identity: how it's used
`GenreThemes.resolve(book.categories)` returns a `GenreMatch`. Screens call
`genreColors(match.primary)`; no screen hardcodes a genre colour. The identity drives:
- cover glows and typographic fallback covers (title, author and the genre motif)
- genre badges, secondary-genre dots and filter chips
- the book-detail hero, the genre collection header, continue-reading cards
- shelf progress bars and reader-profile banners (from the reader's favourite genre)
- **genre tiles built from real book covers**: each tile fans out covers of books in that genre
  (from Open Library, via the existing `BookRepository`), over a backdrop tinted in the genre's colours.
  Covers are fetched only when a tile is shown, cached for the session, and de-duplicated so
  neighbouring genres don't show the same book. If covers can't load, the tile falls back to its gradient and motif.

## Screens
Splash, Login, Register, Forgot password, Profile setup/edit, Home, Discover, Genre collection,
Search, Book details (with reviews and related books), Library (shelf and list views), Reading
progress, My reviews, Reader profile, Friends, Friend requests, Discover readers and Activity feed.

The **theme-selection screen is intentionally unchanged**: same five choices, same layout and
behaviour. Choosing a theme still recolours the whole app. Screenshots are in
`docs/redesign-screens/`.

## Motion
Shared-element cover transitions (list → book details), spring-based navigation transitions,
floating bottom bar with an expanding selected tab, staggered section entrances, press-scale
feedback, animated progress and charts, animated shelf selection, rating "pop", theme
cross-fade, haptics on meaningful actions.

## Data honesty
All figures come from real data: the reader's library, Fable reviews, friends' activity, or Open
Library. Milestones are thresholds computed from the reader's own statistics. Sections without
data show designed empty states. Nothing is invented to fill them.

## Tests
`GenreThemeTest` covers genre resolution, multi-genre handling, unknown-subject fallback,
route ↔ genre mapping, and WCAG contrast for every genre in both light and dark modes.

## Firebase backend
The app is wired to the Firebase project **fable-reads-261114865**: email/password sign-in, a
Firestore database (asia-south1) and the repo's `firestore.rules`, all defined in `firebase.json`
and deployed with `firebase deploy --only firestore,auth`.

`app/google-services.json` is deliberately **not** in git (see `.gitignore`). To build a version
that can sign in, get that file from the project owner, or download it from the Firebase console
(Project settings → Your apps → Fable Android), and place it in `app/`.
