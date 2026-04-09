package com.arkade.network.grpc

import com.squareup.wire.GrpcClient
import okhttp3.OkHttpClient
import okhttp3.Protocol

actual fun gRPCClient(baseUrl: String): GrpcClient =
    GrpcClient
        .Builder()
        .client(
            OkHttpClient
                .Builder()
                .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
                .build(),
        ).baseUrl(baseUrl)
        .build()
