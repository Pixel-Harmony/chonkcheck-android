package com.chonkcheck.android.data.mappers

import com.chonkcheck.android.data.api.dto.MilestoneDataDto
import com.chonkcheck.android.data.api.dto.MilestonesResponse
import com.chonkcheck.android.domain.model.MilestoneData
import com.chonkcheck.android.domain.model.MilestoneOutcome
import com.chonkcheck.android.domain.model.MilestoneType
import com.chonkcheck.android.domain.model.PendingMilestones
import java.time.LocalDate

fun MilestoneDataDto.toDomain(): MilestoneData = MilestoneData(
    type = MilestoneType.fromApiValue(type),
    periodStart = LocalDate.parse(periodStart),
    periodEnd = LocalDate.parse(periodEnd),
    periodLabel = periodLabel,
    startWeight = startWeight,
    endWeight = endWeight,
    change = change,
    changeFormatted = changeFormatted,
    totalLost = totalLost,
    totalLostFormatted = totalLostFormatted,
    outcome = MilestoneOutcome.fromApiValue(outcome)
)

fun MilestonesResponse.toDomain(): PendingMilestones = PendingMilestones(
    weekly = weekly?.toDomain(),
    monthly = monthly?.toDomain()
)
