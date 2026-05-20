package com.smartnode.app.domain.model

/**
 * Closed set of card types the app understands out of the box, plus a
 * [Custom] catch-all so the schema stays open to anything the user encounters
 * in the field without requiring a code change.
 */
sealed class CardType(val key: String) {
    data object Military : CardType("Military")
    data object Employee : CardType("Employee")
    data object Visitor : CardType("Visitor")
    data object Contractor : CardType("Contractor")
    data class Custom(val raw: String) : CardType(raw)

    companion object {
        fun from(raw: String?): CardType = when (raw?.trim()) {
            null, "" -> Custom("Unknown")
            Military.key -> Military
            Employee.key -> Employee
            Visitor.key -> Visitor
            Contractor.key -> Contractor
            else -> Custom(raw)
        }
    }
}
