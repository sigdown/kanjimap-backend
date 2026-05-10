package com.example.application.usecase.progress

internal fun validateCounters(correctNumber: Int?, wrongNumber: Int?, repetitionLevel: Int?) {
    require(correctNumber == null || correctNumber >= 0) { "correctNumber must be non-negative" }
    require(wrongNumber == null || wrongNumber >= 0) { "wrongNumber must be non-negative" }
    require(repetitionLevel == null || repetitionLevel >= 0) { "repetitionLevel must be non-negative" }
}
