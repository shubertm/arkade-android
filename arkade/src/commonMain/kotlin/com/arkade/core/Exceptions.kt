package com.arkade.core

class LockedVTXOException(
    override val message: String,
) : Exception(message)

class SpentVTXOException(
    override val message: String,
) : Exception(message)
