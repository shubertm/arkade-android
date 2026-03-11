# Project Plan: Ark Android SDK

This project aims to deliver a high-performance Android SDK for the Ark protocol
based on the [rust-sdk](https://github.com/arkade-os/rust-sdk). We will use [secp256k1-kmp](https://github.com/ACINQ/secp256k1-kmp) for all ECC, as it also 
exposes bitcoin secp256k1 library via JNI already.
Secondly, we aim at building Arkade Satochip firmware to allow users board the Ark from hardware wallets.
In addition to all objectives we need to build a proof of concept Sparrow wallet integration of Arkade Satochip firmware.

## Phase 1: Foundation & Core Logic (ark-core)

### Goal: Implement fundamental Ark primitives.

#### Technical Tasks:
- Setup code style, tests, CI and pre-commit hooks.
- Implement ark core types in Kotlin.
- Create unit tests for the core types.

##### Milestone: Successful compilation of all Ark primitives and no code quality warnings
**Q1 2026**

## Phase 2: Client Communication (ark-client)
### Goal: Enable the Android SDK to communicate with Ark service providers (ASPs).
#### Technical Tasks:
- Implement the gRPC/REST client logic from the ark-client crate in Kotlin.
- Use Kotlin Coroutines for non-blocking UI calls.

##### Milestone: Successful "ping" or "status check" from an Android Emulator to an Ark backend.
**Q1 2026**

## Phase 3: Fee Management (ark-fees)
### Goal: Integrate dynamic fee estimation and calculation.
#### Technical Tasks:
- Translate the ark-fees crate logic into Kotlin to handle out-of-band and in-protocol fee structures.
- Expose fee-rate providers to the Kotlin layer for real-time UI updates.

##### Milestone: Successful integration tests' fees estimation and calculation.
**Q2 2026**

## Phase 4: Wallet Integration (ark-bdk-wallet)
### Goal: Full wallet functionality integrating on-chain Bitcoin via BDK.
#### Technical Tasks:
- Add the Bitcoin Development Kit (BDK) to ark-android.
- Implement persistence logic to store wallet descriptors securely using Android EncryptedSharedPreferences.
    
##### Milestone: A "Send/Receive" end-to-end test within a sample Android app.
**Q2 2026**

## Phase 5: Arkade Hardware Wallet Integration (Satochip)
### Goal: Build Satochip firmware for Arkade integration.
#### Technical Tasks:
- Implement Arkade firmware for Satochip.
    
##### Milestone: A successful Ark trip from the hardware wallet via a sample Android app.
**Q3 2026**

## Final Phase: Arkade Sparrow Wallet Integration
### Goal: Build Arkade access layer in Sparrow wallet.
#### Technical Tasks:
- Integrate Arkade into Sparrow wallet.
    
##### Milestone: A successful Ark trip within Sparrow wallet.
**Q4 2026**

## Tech Stack
- Language: Kotlin and Java.
- Build Tooling: Gradle and Cargo.

***
## OpenSats Proposal

### Project Details
#### Name: Ark Android

#### Description
This project aims at providing an easy to use and high performance Android SDK for the Ark protocol. The primary work
is based on Arkade implementation of the Ark SDK. We refer to the Rust implementation from ArkadeOS
repository.
We are also aiming to integrate Arkade into Satochip hardware wallets by building an Arkade firmware for Satochip hardware wallets.
Using the firmware, we will build a proof of concept Arkade integration in Sparrow wallet.

###### A Little about the Ark

Ark is a Bitcoin layer 2 protocol that enhances transaction speed, privacy and efficiency 
by transaction batching using a Bitcoin virtual execution layer. It uses VTXOs 
(Virtual Transaction Outputs) which are a programmable adaptation of the Bitcoin native UTXOs 
(Unspent Transaction Outputs). VTXOs as an adaptation do not enforce any new consensus rules 
offchain, it uses the same consensus rules as onchain. 
Users can spend their Bitcoins collaboratively with the Arkade Operetor or 
unilaterally with a relative time lock, this design preserves self custody within the Ark.

#### Potential Impact
According to the main objective, the primary impact of this project is that building Ark wallets on
native Android using Kotlin or Java will be faster and easy for developers without compromising performance
and security.
It will also enhance user experience and security for Arkade users with Satochip hardware wallets as native Android
wallets will support them directly.

#### Project Website

### Source code
- [Github](https://github.com/shubertm/ark-android)
- [MIT License](http://www.opensource.org/licenses/mit-license.php)

### Project Timeline

#### Duration: 12 months

#### Projects Timeline and Potential Milestones

- **Milestone 1: Successful compilation of all Ark primitives and no code quality warnings**

    Q1 2026

- **Milestone 2: Successful "ping" or "status check" from an Android Emulator to an Ark backend**

    Q1 2026

- **Milestone 3: Successful integration tests' fees estimation and calculation**

    Q2 2026

- **Milestone 4: A "Send/Receive" end-to-end test within a sample Android app**

    Q2 2026

- **Milestone 5: A successful Ark trip from the hardware wallet via a sample Android app**

    Q3 2026

- **Final Milestone: A successful Ark trip within Sparrow wallet**

    Q4 2026

#### Time commitment: Part time (75%)

### Project Budget

#### Costs and Proposed Budget
- Regular contributor incentives (hourly rate)
    - shubertm: $25/hr

- **Total (approx.): $55000**

### Applicant Details

#### Name: Shubert Munthali
#### Email: shubertm.m@gmail.com
#### Personal Github: [shubertm](https://github.com/shubertm)
#### Other contact details
- [X](https://x.com/shubertmm)
- [Discord](https://discord.com/channels/@shubertm)

### References and Prior Contributions

#### References

#### Prior contributions
- [ARK-Builders](https://github.com/ark-builders)
- [Arkade Script Compiler](https://github.com/arkade-os/compiler)
- [Arkade Typescript SDK](https://github.com/arkade-os/ts-sdk)
- [Arkade Wallet](https://github.com/arkade-os/wallet)

#### Years of developer experience: 4

### Anything else
I am very passionate about Computer Science and very committed to contribute to free and open knowledge base
of humanity as long as I live. Since childhood, the primary barrier I meet is little or no
resources to build my part of the knowledge base important to humanity.
It is because of this passion and commitment that with the minimal resources, I had to study Computer Science 
informally and build global connections to learn and apply the knowledge and skills I get on the journey.
I knew about Bitcoin in 2022, and it convicted me to contribute and use this P2P money because it just works
towards human freedom and sovereignty without borders.