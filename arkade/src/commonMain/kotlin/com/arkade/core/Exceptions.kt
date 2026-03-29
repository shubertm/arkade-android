package com.arkade.core

data class LockedVTXOException(
    override val message: String,
) : Exception(message)

data class SpentVTXOException(
    override val message: String,
) : Exception(message)
