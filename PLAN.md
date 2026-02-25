# Project Plan: Ark Android SDK

This project aims to deliver a high-performance Android SDK for the Ark protocol
by porting the existing [rust-sdk](https://github.com/arkade-os/rust-sdk) while maintaining its cryptographic integrity
through a native Rust core.

## Phase 1: Foundation & Core Logic

### Goal: Establish the JNI (Java Native Interface) bridge and port fundamental Ark primitives.

#### Technical Tasks:
- Setup code style, tests, CI and pre-commit hooks.
- Implement the FFI (Foreign Function Interface) layer using the jni-rs library
  to expose ark-core types.
- Create the initial Kotlin wrapper classes for VTXO and transaction models.

##### Milestone: Successful compilation of all Ark primitives and no code quality warnings

## Phase 2: Client Communication (ark-client)
### Goal: Enable the Android SDK to communicate with Ark service providers (ASPs).
#### Technical Tasks:
- Port the gRPC/Rest client logic from the ark-client crate.
- Use Kotlin Coroutines for non-blocking UI calls.

##### Milestone: Successful "ping" or "status check" from an Android Emulator to an Ark backend.

## Phase 3: Fee Management (ark-fees)
### Goal: Integrate dynamic fee estimation and calculation.
#### Technical Tasks:
- Port the ark-fees logic to handle out-of-band and in-protocol fee structures.
- Expose fee-rate providers to the Kotlin layer for real-time UI updates.

##### Milestone: Successful integration tests' fees estimation and calculation.

## Phase 4: Wallet Integration (ark-bdk-wallet)
### Goal: Full wallet functionality integrating on-chain Bitcoin via BDK.
#### Technical Tasks:
- Link the Bitcoin Development Kit (BDK) Rust logic to the Android environment.
- Implement persistence logic to store wallet descriptors securely using Android EncryptedSharedPreferences.

##### Final Milestone: A "Send/Receive" end-to-end test within a sample Android app.

## Tech Stack
- Logic: Rust (via Ark SDK).
- Frontend: Kotlin.
- Bridge: JNI with jni-rs.
- Build Tooling: Gradle and Cargo.