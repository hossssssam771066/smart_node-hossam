package com.smartnode.app.data.mapper

import com.smartnode.app.data.local.converter.Converters
import com.smartnode.app.data.local.entity.IdentityEntity
import com.smartnode.app.domain.model.CardType
import com.smartnode.app.domain.model.Identity

/**
 * Hides the JSON-blob mechanics from the rest of the app.
 *
 * On the way out (Entity -> Identity) the JSON payload is parsed back into a
 * typed [Map]; on the way in (Identity -> Entity) the attribute map is
 * re-serialized to a single JSON string column.
 */
internal object IdentityMapper {

    fun IdentityEntity.toDomain(): Identity = Identity(
        uid = uid,
        cardType = CardType.from(cardType),
        holderName = holderName,
        primaryIdentifier = primaryIdentifier,
        attributes = Converters.decodeMap(jsonData),
        createdAt = createdAt,
    )

    fun Identity.toEntity(): IdentityEntity = IdentityEntity(
        uid = uid,
        cardType = cardType.key,
        holderName = holderName,
        primaryIdentifier = primaryIdentifier,
        jsonData = Converters.encodeMap(attributes),
        createdAt = createdAt,
    )
}
