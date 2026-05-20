package com.smartnode.app.domain.model

/**
 * Domain-layer representation of a card holder.
 *
 * The persistence layer keeps everything beyond [uid] / [cardType] / [holderName]
 * / [primaryIdentifier] in a JSON blob, but the domain layer surfaces it as a
 * typed [attributes] map so UI / use-case code never touches raw JSON.
 *
 * Unknown keys round-trip unchanged so a card type the app didn't know about
 * at write time can still be displayed and re-saved without losing data.
 */
data class Identity(
    val uid: String,
    val cardType: CardType,
    val holderName: String,
    val primaryIdentifier: String,
    val attributes: Map<String, Any?>,
    val createdAt: Long,
) {
    fun attr(key: String): String? = attributes[key]?.toString()
}
