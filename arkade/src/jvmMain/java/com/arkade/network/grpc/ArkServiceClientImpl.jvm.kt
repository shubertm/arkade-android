package com.arkade.network.grpc

import com.squareup.wire.GrpcClient
import okhttp3.OkHttpClient
import okhttp3.Protocol
import kotlin.time.Duration.Companion.seconds

actual fun gRPCClient(baseUrl: String): GrpcClient =
    GrpcClient
        .Builder()
        .client(
            OkHttpClient
                .Builder()
                .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
                .callTimeout(60.seconds)
                .readTimeout(30.seconds)
                .connectTimeout(30.seconds)
                .build(),
        ).baseUrl(baseUrl)
        .build()
