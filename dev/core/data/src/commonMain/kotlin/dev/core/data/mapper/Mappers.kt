package dev.core.data.mapper

import dev.core.data.dto.ClubDto
import dev.core.database.sql.ClubEntity
import dev.core.domain.model.Club

fun ClubDto.toDomain(): Club = Club(
    id = id,
    name = name,
    description = description,
    membersCount = membersCount,
    imageUrl = imageUrl,
)

fun ClubEntity.toDomain(): Club = Club(
    id = id,
    name = name,
    description = description,
    membersCount = membersCount.toInt(),
    imageUrl = imageUrl,
    joined = joined != 0L,
)
