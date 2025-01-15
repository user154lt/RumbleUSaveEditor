package data

sealed class SignatureException(
    message: String?,
    cause: Throwable?,
) : Exception(message, cause)

class BlastSignatureException(
    message: String? = null,
    cause: Throwable? = null,
) : SignatureException(message, cause)

class WorldSignatureException(
    message: String? = null,
    cause: Throwable? = null,
) : SignatureException(message, cause)

class InvalidSignatureException(
    message: String? = null,
    cause: Throwable? = null,
) : SignatureException(message, cause)

class NoZLibSignatureException(
    message: String? = null,
    cause: Throwable? = null,
) : SignatureException(message, cause)