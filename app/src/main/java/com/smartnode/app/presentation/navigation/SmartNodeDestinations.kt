package com.smartnode.app.presentation.navigation

/**
 * Type-safe route catalog for the Compose [androidx.navigation.NavHost].
 *
 * Routes have no arguments yet — when we add per-record screens (Phase 4+) we
 * can extend these with `route + "/{id}"` helpers without leaking string
 * literals into the rest of the codebase.
 */
object SmartNodeDestinations {
    const val HOME = "home"
    const val SCANNER = "scanner"
    const val ADD_IDENTITY = "identities/add"
    const val IDENTITIES = "identities"
    const val LOGS = "logs"
}
