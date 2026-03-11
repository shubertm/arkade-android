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

  Timeline: 01 March 2026 - 25 March 2026

- **Milestone 2: Successful "ping" or "status check" from an Android Emulator to an Ark backend**

  Timeline: 03 April 2026 - 28 May 2026

- **Milestone 3: Successful integration tests' fees estimation and calculation**

  Timeline: 05 June 2026 - 25 June 2026

- **Milestone 4: A "Send/Receive" end-to-end test within a sample Android app**

  Timeline: 01 July 2026 - 28 August 2026

- **Milestone 5: A successful Ark trip from the hardware wallet via a sample Android app**

  Timeline: 01 September 2026 - 20 October 2026

- **Final Milestone: A successful Ark trip within Sparrow wallet**

  Timeline: 23 October 2026 - 28 November 2026

#### Time commitment: Part time (75%)

### Project Budget

#### Costs and Proposed Budget
- Regular contributor incentives (hourly rate)
    - shubertm: $15/hr

- Community engagement (hourly rate)
    - shubertm: $0/hr

- Security audit (estimated cost)

  `$10000 - $25000`

- **Total (approx.): $50000**

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
I knew about Bitcoin in 2022, and it convicted me to contribute and use this P2P money that just works.