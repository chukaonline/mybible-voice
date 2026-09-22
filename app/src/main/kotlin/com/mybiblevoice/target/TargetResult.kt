package com.mybiblevoice.target

sealed interface TargetResult {
    /** [warning] is non-null when the target succeeded but with a caveat (e.g. MySword falling
     *  back to a generic launch instead of the exact passage) - preserved rather than dropped
     *  when adapting existing per-target results into this shared contract. */
    data class Success(val warning: String? = null) : TargetResult
    data class Failure(val error: TargetError) : TargetResult
}
