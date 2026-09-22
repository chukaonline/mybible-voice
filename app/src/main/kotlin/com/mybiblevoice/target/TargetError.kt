package com.mybiblevoice.target

/** Error categories shared by every [BibleTarget] (design section 14). */
sealed class TargetError(val message: String) {
    /** The target application/service isn't installed or isn't configured at all. */
    class NotAvailable(message: String) : TargetError(message)

    /** Unreachable host, timeout, refused connection. */
    class Network(message: String) : TargetError(message)

    /** Invalid or insufficient-permission credentials. */
    class Authentication(message: String) : TargetError(message)

    /** The requested translation isn't available/mapped on this target - never silently substituted. */
    class TranslationUnavailable(message: String) : TargetError(message)

    /** Missing or invalid target configuration (host, port, token, etc.). */
    class Configuration(message: String) : TargetError(message)

    /** Target-specific/API-level failure that doesn't fit the categories above. */
    class Unexpected(message: String) : TargetError(message)
}
