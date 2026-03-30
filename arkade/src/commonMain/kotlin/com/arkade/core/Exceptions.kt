package com.arkade.core

class LockedVTXOException(
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause)

class SpentVTXOException(
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause)
