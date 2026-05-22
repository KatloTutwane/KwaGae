package com.example.kwagae.data

import android.content.Context
import com.example.kwagae.data.database.AppDatabase
import com.example.kwagae.data.models.Listing
import com.example.kwagae.data.models.User
import java.security.MessageDigest

/**
 * Seeds the local Room database on first install with:
 *   • 50 student users  (role = "student")
 *   • 50 house listings (Gaborone areas, prices in BWP)
 *
 * Call from MainActivity.onCreate() — guarded by a SharedPrefs flag so it
 * only runs once.
 */
object DatabaseSeeder {

    private const val PREF_KEY = "db_seeded_v10"

    // 5 providers — 10 listings each
    private data class ProviderInfo(val uid: String, val name: String, val email: String, val password: String)

    private val PROVIDERS = listOf(
        ProviderInfo("PR001", "KwaGae Housing",         "landlord@kwagae.bw",      "Landlord@1"),
        ProviderInfo("PR002", "Botho Properties",       "botho.props@kwagae.bw",   "Provider@2"),
        ProviderInfo("PR003", "Moeng Estates",          "moeng.estates@kwagae.bw", "Provider@3"),
        ProviderInfo("PR004", "Seretse Rentals",        "seretse@kwagae.bw",       "Provider@4"),
        ProviderInfo("PR005", "Gaborone Premier Homes", "gab.premier@kwagae.bw",   "Provider@5")
    )

    suspend fun seedIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences("kwagae_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean(PREF_KEY, false)) return   // already seeded

        val db = AppDatabase.getDatabase(context)

        // ── 1. Seed 50 students ───────────────────────────────────────────────
        db.userDao().insertAll(buildStudents())

        // ── 2. Seed 5 provider accounts ───────────────────────────────────────
        db.userDao().insertAll(
            PROVIDERS.map { p ->
                User(
                    studentId    = p.uid,
                    fullName     = p.name,
                    email        = p.email,
                    passwordHash = hash(p.password),
                    role         = "provider",
                    university   = "",
                    isVerified   = false,
                    pendingSync  = false
                )
            }
        )

        // ── 3. Seed 50 listings (10 per provider) ─────────────────────────────
        db.listingDao().insertAll(buildListings())

        prefs.edit().putBoolean(PREF_KEY, true).apply()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun hash(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }


    // 50 students distributed across 6 Gaborone universities:

    // Test login:  email = firstname{n}@student.ac.bw   password = Student@{n}

    private fun buildStudents(): List<User> {
        val names = listOf(
            // 1-10: University of Botswana
            "Kabo Molefe", "Thabo Moeng", "Lerato Sebele", "Bontle Kgosi", "Kagiso Moatlhodi",
            "Neo Gaobusi", "Tiro Mosweu", "Mpho Tshiamo", "Refilwe Molefhi", "Lesego Seelo",
            // 11-20: Botho University
            "Tebogo Dikgang", "Oratile Kelesitse", "Boitumelo Ntsha", "Ofentse Segwe", "Keabetswe Ramotswe",
            "Goabaone Morapedi", "Phenyo Gabana", "Masego Motsepe", "Botlhe Kebonang", "Tshepo Nkwe",
            // 21-28: Limkokwing University
            "Obakeng Gaetsiwe", "Nthaloso Molatedi", "Gaone Phiri", "Sethunya Motlhanka",
            "Dimpho Kgosi", "Olerato Mogami", "Baitshepi Rammidi", "Keitumetse Lekwalo",
            // 29-36: Botswana Open University
            "Bogosi Seretse", "Simangele Dube", "Itumeleng Sebata", "Tlhalefo Moilwa",
            "Pako Gaefele", "Koketso Maeto", "Onkemetse Setlhare", "Amogelang Morapedi",
            // 37-43: ABM University College
            "Khumo Kedumetse", "Lorato Mmereki", "Banno Mmoloki", "Batlile Phuthego",
            "Gofaone Dipuo", "Relebogile Mabua", "Oteng Gaoshubelwe",
            // 44-50: BAISAGO University
            "Segolame Ditlhase", "Taboka Ntswane", "Keatlaretse Moagi",
            "Modise Kgwadi", "Sego Modiegi", "Tshegofatso Dibe", "Kekeletso Molebatsi"
        )

        return names.mapIndexed { i, name ->
            val num       = i + 1
            val firstName = name.split(" ").first().lowercase()

            // Assign student number and university based on allocation above
            val (uniName, studentNumber, emailDomain) = when {
                num <= 10 -> Triple(
                    "University of Botswana",
                    "UB%08d".format(20210000 + num),          // UB20210001…UB20210010
                    "student.ub.bw"
                )
                num <= 20 -> Triple(
                    "Botho University",
                    "BOTHO%06d".format(210000 + (num - 10)),  // BOTHO210001…BOTHO210010
                    "student.botho.bw"
                )
                num <= 28 -> Triple(
                    "Limkokwing University",
                    "LU%07d".format(2021000 + (num - 20)),    // LU2021001…LU2021008
                    "student.limkokwing.bw"
                )
                num <= 36 -> Triple(
                    "Botswana Open University",
                    "BOU%06d".format(210000 + (num - 28)),    // BOU210001…BOU210008
                    "student.bou.bw"
                )
                num <= 43 -> Triple(
                    "ABM University College",
                    "ABM%06d".format(210000 + (num - 36)),    // ABM210001…ABM210007
                    "student.abm.bw"
                )
                else -> Triple(
                    "BAISAGO University",
                    "BAI%06d".format(210000 + (num - 43)),    // BAI210001…BAI210007
                    "student.baisago.bw"
                )
            }

            User(
                studentId       = "KW%03d".format(num),
                fullName        = name,
                email           = "$firstName$num@$emailDomain",
                passwordHash    = hash("Student@$num"),
                role            = "student",
                ubStudentNumber = studentNumber,
                university      = uniName,
                isVerified      = true,
                pendingSync     = false
            )
        }
    }

    // ── Listings ──────────────────────────────────────────────────────────────

    private fun buildListings(): List<Listing> = rawListings().mapIndexed { idx, listing ->
        // Listings 0-9 → PR001, 10-19 → PR002, 20-29 → PR003, 30-39 → PR004, 40-49 → PR005
        val p = PROVIDERS[idx / 10]
        // Derive extra image asset names: house1.jpg → house1_b.jpg, house1_c.jpg
        val base = listing.imageUrl.substringAfterLast("/").substringBeforeLast(".")
        val ext  = listing.imageUrl.substringAfterLast(".")
        val dir  = listing.imageUrl.substringBeforeLast("/")
        listing.copy(
            ownerUid     = p.uid,
            providerName = p.name,
            imageUrls    = "${listing.imageUrl}," +
                           "$dir/${base}_b.$ext," +
                           "$dir/${base}_c.$ext"
        )
    }

    @Suppress("FunctionName")
    private fun rawListings(): List<Listing> = listOf(

        // ── 1-10: Houses ─────────────────────────────────────────────────────
        Listing(
            title = "Spacious 3-Bed House in Block 8",
            description = "Well-maintained 3-bedroom house with large garden, secure parking, and easy access to the CBD. Quiet residential area, ideal for postgraduate students or young professionals.",
            location = "Block 8, Gaborone",
            type = "House",
            price = 7500.0,
            depositAmount = 15000.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "3 Bedrooms,2 Bathrooms,Garden,Parking,Security Gate,DSTV Point",
            imageUrl = "file:///android_asset/listings/house1.jpg"
        ),
        Listing(
            title = "Modern 2-Bed House — Phase 2",
            description = "Newly renovated 2-bedroom house in a sought-after Phase 2 location. Walking distance to Riverwalk Mall and University of Botswana.",
            location = "Phase 2, Gaborone",
            type = "House",
            price = 6000.0,
            depositAmount = 12000.0,
            availabilityDate = "15 Jun 2026",
            isAvailable = true,
            amenities = "2 Bedrooms,1 Bathroom,Parking,Borehole Water,Prepaid Electricity",
            imageUrl = "file:///android_asset/listings/house2.jpg"
        ),
        Listing(
            title = "Cosy 3-Bed House — Broadhurst",
            description = "Comfortable family house in Broadhurst. Fully enclosed yard with fruit trees. Close to Broadhurst Mall and primary schools.",
            location = "Broadhurst, Gaborone",
            type = "House",
            price = 6500.0,
            depositAmount = 13000.0,
            availabilityDate = "01 Jul 2026",
            isAvailable = true,
            amenities = "3 Bedrooms,2 Bathrooms,Garden,Parking,Borehole,Pet Friendly",
            imageUrl = "file:///android_asset/listings/house3.jpg"
        ),
        Listing(
            title = "4-Bed Family House — Phakalane",
            description = "Executive 4-bedroom house in prestigious Phakalane Estate. Has a swimming pool, double garage, and 24-hour estate security.",
            location = "Phakalane, Gaborone",
            type = "House",
            price = 12000.0,
            depositAmount = 24000.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "4 Bedrooms,3 Bathrooms,Swimming Pool,Double Garage,Garden,Security Estate",
            imageUrl = "file:///android_asset/listings/house4.jpg"
        ),
        Listing(
            title = "2-Bed House — Tlokweng",
            description = "Affordable 2-bedroom house just across the Tlokweng border, 10 minutes from Gaborone CBD. Ideal for budget-conscious students.",
            location = "Tlokweng, Gaborone",
            type = "House",
            price = 4500.0,
            depositAmount = 9000.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "2 Bedrooms,1 Bathroom,Yard,Prepaid Electricity",
            imageUrl = "file:///android_asset/listings/house5.jpg"
        ),
        Listing(
            title = "3-Bed House — Old Naledi",
            description = "Character house in Old Naledi, Gaborone's oldest township. Close to transport routes and local markets.",
            location = "Old Naledi, Gaborone",
            type = "House",
            price = 3500.0,
            depositAmount = 7000.0,
            availabilityDate = "01 Aug 2026",
            isAvailable = true,
            amenities = "3 Bedrooms,1 Bathroom,Yard,Storage Room",
            imageUrl = "file:///android_asset/listings/house6.jpg"
        ),
        Listing(
            title = "3-Bed House with Solar — Mogoditshane",
            description = "Eco-friendly house with solar panels and rainwater harvesting. Peaceful location in Mogoditshane village.",
            location = "Mogoditshane, Gaborone",
            type = "House",
            price = 5500.0,
            depositAmount = 11000.0,
            availabilityDate = "15 Jul 2026",
            isAvailable = true,
            amenities = "3 Bedrooms,2 Bathrooms,Solar Power,Rainwater Tank,Garden,Parking",
            imageUrl = "file:///android_asset/listings/house7.jpg"
        ),
        Listing(
            title = "2-Bed House — Bontleng",
            description = "Compact 2-bedroom house in Bontleng, walking distance from Gaborone Dam and Princess Marina Hospital.",
            location = "Bontleng, Gaborone",
            type = "House",
            price = 4800.0,
            depositAmount = 9600.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "2 Bedrooms,1 Bathroom,Yard,Parking",
            imageUrl = "file:///android_asset/listings/house8.jpg"
        ),
        Listing(
            title = "Executive 3-Bed — G-West",
            description = "Beautifully designed 3-bedroom house in G-West. Open-plan kitchen, modern finishes, and fibre-ready.",
            location = "G-West, Gaborone",
            type = "House",
            price = 8500.0,
            depositAmount = 17000.0,
            availabilityDate = "01 Jul 2026",
            isAvailable = true,
            amenities = "3 Bedrooms,2.5 Bathrooms,Open Plan Kitchen,Fibre Internet,Parking,Garden",
            imageUrl = "file:///android_asset/listings/house9.jpg"
        ),
        Listing(
            title = "Budget 2-Bed House — Ledumang",
            description = "No-frills 2-bedroom house at an unbeatable price. Great for students needing space. Close to Ledumang Senior Secondary School.",
            location = "Ledumang, Gaborone",
            type = "House",
            price = 3800.0,
            depositAmount = 7600.0,
            availabilityDate = "15 Jun 2026",
            isAvailable = true,
            amenities = "2 Bedrooms,1 Bathroom,Yard,Prepaid Water",
            imageUrl = "file:///android_asset/listings/house10.jpg"
        ),

        // ── 11-20: Apartments ─────────────────────────────────────────────────
        Listing(
            title = "1-Bed Apartment — Gaborone CBD",
            description = "Stylish 1-bedroom apartment in the heart of the CBD. 5th floor with panoramic city views. Access to rooftop pool.",
            location = "Gaborone CBD",
            type = "Apartment",
            price = 5500.0,
            depositAmount = 11000.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "1 Bedroom,1 Bathroom,Rooftop Pool,Security,Elevator,Parking Bay",
            imageUrl = "file:///android_asset/listings/apt1.jpg"
        ),
        Listing(
            title = "2-Bed Apartment — Phase 4",
            description = "Bright 2-bedroom apartment in a secure complex in Phase 4. Shared gym and braai area.",
            location = "Phase 4, Gaborone",
            type = "Apartment",
            price = 6800.0,
            depositAmount = 13600.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "2 Bedrooms,2 Bathrooms,Gym,Braai Area,Secure Parking,Security Guard",
            imageUrl = "file:///android_asset/listings/apt2.jpg"
        ),
        Listing(
            title = "Luxury 2-Bed Apartment — Phakalane",
            description = "Premium apartment in a golf estate complex. Comes fully furnished. Ideal for professionals.",
            location = "Phakalane, Gaborone",
            type = "Apartment",
            price = 9500.0,
            depositAmount = 19000.0,
            availabilityDate = "01 Jul 2026",
            isAvailable = true,
            amenities = "2 Bedrooms,2 Bathrooms,Fully Furnished,Golf Estate,Pool,Fibre Internet",
            imageUrl = "file:///android_asset/listings/apt3.jpg"
        ),
        Listing(
            title = "1-Bed Apartment — Riverwalk Area",
            description = "Modern apartment near Riverwalk Mall. Walking distance to restaurants, Checkers, and UB campus.",
            location = "Riverwalk, Gaborone",
            type = "Apartment",
            price = 4800.0,
            depositAmount = 9600.0,
            availabilityDate = "15 Jun 2026",
            isAvailable = true,
            amenities = "1 Bedroom,1 Bathroom,Parking,Elevator,CCTV Security",
            imageUrl = "file:///android_asset/listings/apt4.jpg"
        ),
        Listing(
            title = "3-Bed Apartment — Block 9",
            description = "Spacious 3-bedroom apartment on Block 9. Great for sharing between students. Includes 2 secure parking bays.",
            location = "Block 9, Gaborone",
            type = "Apartment",
            price = 8000.0,
            depositAmount = 16000.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "3 Bedrooms,2 Bathrooms,2 Parking Bays,Security,DSTV Point,Laundry Room",
            imageUrl = "file:///android_asset/listings/apt5.jpg"
        ),
        Listing(
            title = "Compact 1-Bed Apartment — Block 6",
            description = "Affordable 1-bedroom apartment in Block 6. Recently repainted and fitted with new kitchen units.",
            location = "Block 6, Gaborone",
            type = "Apartment",
            price = 4000.0,
            depositAmount = 8000.0,
            availabilityDate = "01 Aug 2026",
            isAvailable = true,
            amenities = "1 Bedroom,1 Bathroom,Parking,Security Gate,New Kitchen",
            imageUrl = "file:///android_asset/listings/apt6.jpg"
        ),
        Listing(
            title = "2-Bed Apartment — Extension 10",
            description = "Well-located 2-bedroom apartment in Extension 10. Quiet complex, ideal for postgrad students.",
            location = "Extension 10, Gaborone",
            type = "Apartment",
            price = 5800.0,
            depositAmount = 11600.0,
            availabilityDate = "15 Jul 2026",
            isAvailable = true,
            amenities = "2 Bedrooms,1 Bathroom,Parking,Quiet Complex,DSTV Ready",
            imageUrl = "file:///android_asset/listings/apt7.jpg"
        ),
        Listing(
            title = "Ground Floor Apartment — Broadhurst",
            description = "Disability-friendly ground floor apartment with wide doorways and walk-in shower.",
            location = "Broadhurst, Gaborone",
            type = "Apartment",
            price = 4500.0,
            depositAmount = 9000.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "2 Bedrooms,1 Bathroom,Wheelchair Friendly,Parking,Borehole Water",
            imageUrl = "file:///android_asset/listings/apt8.jpg"
        ),
        Listing(
            title = "Penthouse Apartment — CBD",
            description = "Stunning penthouse on the 12th floor with private terrace and 360° views of Gaborone.",
            location = "Gaborone CBD",
            type = "Apartment",
            price = 15000.0,
            depositAmount = 30000.0,
            availabilityDate = "01 Jul 2026",
            isAvailable = true,
            amenities = "3 Bedrooms,3 Bathrooms,Private Terrace,City Views,Concierge,2 Parking Bays",
            imageUrl = "file:///android_asset/listings/apt9.jpg"
        ),
        Listing(
            title = "1-Bed Apartment — Block 7",
            description = "Clean, secure 1-bed apartment. Perfect for a first-year student. Close to bus routes.",
            location = "Block 7, Gaborone",
            type = "Apartment",
            price = 3800.0,
            depositAmount = 7600.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "1 Bedroom,1 Bathroom,Security,Close to Bus Route",
            imageUrl = "file:///android_asset/listings/apt10.jpg"
        ),

        // ── 21-28: Studios ────────────────────────────────────────────────────
        Listing(
            title = "Studio Apartment — Gaborone CBD",
            description = "Compact yet fully functional studio in the CBD. Includes a kitchenette, en-suite bathroom, and Wi-Fi.",
            location = "Gaborone CBD",
            type = "Studio",
            price = 2800.0,
            depositAmount = 5600.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "En-Suite Bathroom,Kitchenette,Wi-Fi,Security,Parking",
            imageUrl = "file:///android_asset/listings/studio1.jpg"
        ),
        Listing(
            title = "Modern Studio — Phase 2",
            description = "Thoughtfully designed studio with built-in wardrobes, a Murphy bed, and fibre internet.",
            location = "Phase 2, Gaborone",
            type = "Studio",
            price = 2500.0,
            depositAmount = 5000.0,
            availabilityDate = "15 Jun 2026",
            isAvailable = true,
            amenities = "En-Suite,Built-in Wardrobes,Fibre Internet,Murphy Bed,Parking",
            imageUrl = "file:///android_asset/listings/studio2.jpg"
        ),
        Listing(
            title = "Budget Studio — Bontleng",
            description = "Simple no-frills studio for the cost-conscious student. All utilities included in rent.",
            location = "Bontleng, Gaborone",
            type = "Studio",
            price = 1800.0,
            depositAmount = 3600.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "Bathroom,Kitchenette,All Utilities Included",
            imageUrl = "file:///android_asset/listings/studio3.jpg"
        ),
        Listing(
            title = "Studio with Balcony — Block 9",
            description = "Studio with a lovely east-facing balcony, ideal for morning coffee. Secure complex.",
            location = "Block 9, Gaborone",
            type = "Studio",
            price = 3200.0,
            depositAmount = 6400.0,
            availabilityDate = "01 Jul 2026",
            isAvailable = true,
            amenities = "Balcony,En-Suite,Parking,Security Fence,Intercom",
            imageUrl = "file:///android_asset/listings/studio4.jpg"
        ),
        Listing(
            title = "Furnished Studio — Riverwalk",
            description = "Fully furnished studio perfect for a student or young professional. Move in with just your clothes!",
            location = "Riverwalk, Gaborone",
            type = "Studio",
            price = 3500.0,
            depositAmount = 7000.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "Fully Furnished,En-Suite,Wi-Fi,Smart TV,Secure Parking",
            imageUrl = "file:///android_asset/listings/studio5.jpg"
        ),
        Listing(
            title = "Garden Studio — Mogoditshane",
            description = "Peaceful detached studio in the garden of a quiet family home. Own entrance, privacy guaranteed.",
            location = "Mogoditshane, Gaborone",
            type = "Studio",
            price = 2000.0,
            depositAmount = 4000.0,
            availabilityDate = "15 Jun 2026",
            isAvailable = true,
            amenities = "Own Entrance,Garden View,En-Suite,Parking",
            imageUrl = "file:///android_asset/listings/studio6.jpg"
        ),
        Listing(
            title = "Luxury Studio — Phakalane",
            description = "High-end studio in an upmarket Phakalane block. Concierge service, gym, and pool access.",
            location = "Phakalane, Gaborone",
            type = "Studio",
            price = 4200.0,
            depositAmount = 8400.0,
            availabilityDate = "01 Aug 2026",
            isAvailable = true,
            amenities = "En-Suite,Gym Access,Pool Access,Concierge,Parking,24-Hour Security",
            imageUrl = "file:///android_asset/listings/studio7.jpg"
        ),
        Listing(
            title = "Compact Studio — Extension 12",
            description = "Recently built studio in a new development in Extension 12. Prepaid meter and independent access.",
            location = "Extension 12, Gaborone",
            type = "Studio",
            price = 2200.0,
            depositAmount = 4400.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "En-Suite,Prepaid Electricity,Own Entrance,Parking",
            imageUrl = "file:///android_asset/listings/studio8.jpg"
        ),

        // ── 29-36: Rooms ──────────────────────────────────────────────────────
        Listing(
            title = "Single Room — Block 8 Shared House",
            description = "Furnished single room in a shared 4-bedroom house. Includes access to shared lounge, kitchen, and Wi-Fi.",
            location = "Block 8, Gaborone",
            type = "Room",
            price = 1200.0,
            depositAmount = 2400.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "Furnished,Shared Kitchen,Wi-Fi,Laundry,Security Gate",
            imageUrl = "file:///android_asset/listings/room1.jpg"
        ),
        Listing(
            title = "En-Suite Room — Phase 2",
            description = "Private en-suite room in a shared house. Ideal for a focused student. Bills included.",
            location = "Phase 2, Gaborone",
            type = "Room",
            price = 1600.0,
            depositAmount = 3200.0,
            availabilityDate = "15 Jun 2026",
            isAvailable = true,
            amenities = "En-Suite Bathroom,Shared Kitchen,Wi-Fi,All Bills Included",
            imageUrl = "file:///android_asset/listings/room2.jpg"
        ),
        Listing(
            title = "Large Room — Broadhurst",
            description = "Extra-large room in a quiet household. Has a dedicated study desk and natural light.",
            location = "Broadhurst, Gaborone",
            type = "Room",
            price = 1400.0,
            depositAmount = 2800.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "Study Desk,Shared Bathroom,Shared Kitchen,Parking",
            imageUrl = "file:///android_asset/listings/room3.jpg"
        ),
        Listing(
            title = "Room in Student House — Bontleng",
            description = "One of 5 rooms in a purpose-built student house. Regular cleaning service and security.",
            location = "Bontleng, Gaborone",
            type = "Room",
            price = 1100.0,
            depositAmount = 2200.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "Shared Bathroom,Shared Kitchen,Wi-Fi,Cleaning Service",
            imageUrl = "file:///android_asset/listings/room4.jpg"
        ),
        Listing(
            title = "Furnished Room — Old Naledi",
            description = "Fully furnished room in a friendly household. Ideal for a first-year university student.",
            location = "Old Naledi, Gaborone",
            type = "Room",
            price = 900.0,
            depositAmount = 1800.0,
            availabilityDate = "01 Aug 2026",
            isAvailable = true,
            amenities = "Furnished Bed,Shared Bathroom,Shared Kitchen",
            imageUrl = "file:///android_asset/listings/room5.jpg"
        ),
        Listing(
            title = "Double Room — Tlokweng",
            description = "Spacious double room, great for a couple or two students sharing. Borehole water supply.",
            location = "Tlokweng, Gaborone",
            type = "Room",
            price = 1300.0,
            depositAmount = 2600.0,
            availabilityDate = "15 Jun 2026",
            isAvailable = true,
            amenities = "Double Bed,Borehole Water,Shared Bathroom,Shared Kitchen",
            imageUrl = "file:///android_asset/listings/room6.jpg"
        ),
        Listing(
            title = "Self-Catering Room — G-West",
            description = "Room with its own mini-kitchen and kitchenette. Shared bathroom with one other tenant.",
            location = "G-West, Gaborone",
            type = "Room",
            price = 1500.0,
            depositAmount = 3000.0,
            availabilityDate = "01 Jul 2026",
            isAvailable = true,
            amenities = "Mini-Kitchen,Shared Bathroom,Parking,Security Fence",
            imageUrl = "file:///android_asset/listings/room7.jpg"
        ),
        Listing(
            title = "Room Near UB Campus — Block 7",
            description = "10-minute walk to UB main gate. Shared house with 3 other students. Study-friendly environment.",
            location = "Block 7, Gaborone",
            type = "Room",
            price = 1350.0,
            depositAmount = 2700.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "Shared Kitchen,Shared Bathroom,Wi-Fi,Study Room,10 Mins from UB",
            imageUrl = "file:///android_asset/listings/room8.jpg"
        ),

        // ── 37-41: Townhouses ─────────────────────────────────────────────────
        Listing(
            title = "3-Bed Townhouse — Phakalane Estate",
            description = "Elegant cluster townhouse in a prestigious estate. Includes a private garden and garage.",
            location = "Phakalane, Gaborone",
            type = "Townhouse",
            price = 10000.0,
            depositAmount = 20000.0,
            availabilityDate = "01 Jul 2026",
            isAvailable = true,
            amenities = "3 Bedrooms,2 Bathrooms,Private Garden,Single Garage,Estate Security,Pool Access",
            imageUrl = "file:///android_asset/listings/town1.jpg"
        ),
        Listing(
            title = "2-Bed Townhouse — Phase 2 Complex",
            description = "Contemporary 2-bedroom townhouse in a gated complex. Fibre-ready and DSTV-ready.",
            location = "Phase 2, Gaborone",
            type = "Townhouse",
            price = 7200.0,
            depositAmount = 14400.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "2 Bedrooms,1.5 Bathrooms,Parking,Gated Complex,Fibre Ready,DSTV Point",
            imageUrl = "file:///android_asset/listings/town2.jpg"
        ),
        Listing(
            title = "Townhouse with Study — G-West",
            description = "3-bedroom townhouse featuring a dedicated study room. Great for postgrad students and academics.",
            location = "G-West, Gaborone",
            type = "Townhouse",
            price = 8500.0,
            depositAmount = 17000.0,
            availabilityDate = "15 Jun 2026",
            isAvailable = true,
            amenities = "3 Bedrooms,2 Bathrooms,Study Room,Parking,Security,Borehole",
            imageUrl = "file:///android_asset/listings/town3.jpg"
        ),
        Listing(
            title = "Pet-Friendly Townhouse — Mogoditshane",
            description = "Two-storey townhouse with a large fenced yard. Perfect for families or pet owners.",
            location = "Mogoditshane, Gaborone",
            type = "Townhouse",
            price = 6000.0,
            depositAmount = 12000.0,
            availabilityDate = "01 Aug 2026",
            isAvailable = true,
            amenities = "3 Bedrooms,2 Bathrooms,Fenced Yard,Pet Friendly,Parking",
            imageUrl = "file:///android_asset/listings/town4.jpg"
        ),
        Listing(
            title = "Luxury Townhouse — CBD Fringe",
            description = "Ultra-modern townhouse on the edge of the CBD. Rooftop deck and smart home features.",
            location = "Gaborone CBD",
            type = "Townhouse",
            price = 13000.0,
            depositAmount = 26000.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "4 Bedrooms,3 Bathrooms,Rooftop Deck,Smart Home,2 Garages,Pool",
            imageUrl = "file:///android_asset/listings/town5.jpg"
        ),

        // ── 42-45: Bachelor Flats ─────────────────────────────────────────────
        Listing(
            title = "Bachelor Flat — Riverwalk",
            description = "Open-plan bachelor flat minutes from Riverwalk Mall. All utilities prepaid.",
            location = "Riverwalk, Gaborone",
            type = "Bachelor Flat",
            price = 2200.0,
            depositAmount = 4400.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "Open Plan,Prepaid Utilities,Security,Parking",
            imageUrl = "file:///android_asset/listings/bach1.jpg"
        ),
        Listing(
            title = "Bachelor Flat — Block 6",
            description = "Neat and compact bachelor flat in Block 6. New bathroom fittings and tile floors.",
            location = "Block 6, Gaborone",
            type = "Bachelor Flat",
            price = 1900.0,
            depositAmount = 3800.0,
            availabilityDate = "15 Jun 2026",
            isAvailable = true,
            amenities = "New Bathroom,Tiled Floors,Security Gate,Parking",
            imageUrl = "file:///android_asset/listings/bach2.jpg"
        ),
        Listing(
            title = "Bachelor Flat — Ledumang",
            description = "Affordable bachelor flat, great for a student on a tight budget. Shared yard space.",
            location = "Ledumang, Gaborone",
            type = "Bachelor Flat",
            price = 1500.0,
            depositAmount = 3000.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "Bathroom,Kitchenette,Shared Yard,Prepaid Electricity",
            imageUrl = "file:///android_asset/listings/bach3.jpg"
        ),
        Listing(
            title = "Bachelor Flat — Extension 10",
            description = "Self-contained bachelor flat with own entrance. Part of a small, well-managed block.",
            location = "Extension 10, Gaborone",
            type = "Bachelor Flat",
            price = 2000.0,
            depositAmount = 4000.0,
            availabilityDate = "01 Jul 2026",
            isAvailable = true,
            amenities = "Own Entrance,Bathroom,Kitchenette,Security,Parking",
            imageUrl = "file:///android_asset/listings/bach4.jpg"
        ),

        // ── 46-48: Duplexes ───────────────────────────────────────────────────
        Listing(
            title = "2-Storey Duplex — Phase 4",
            description = "Stylish 3-bedroom duplex over two floors. Ground floor living area, bedrooms upstairs.",
            location = "Phase 4, Gaborone",
            type = "Duplex",
            price = 9000.0,
            depositAmount = 18000.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "3 Bedrooms,2 Bathrooms,Open Plan Living,Parking,Garden,Fibre Ready",
            imageUrl = "file:///android_asset/listings/duplex1.jpg"
        ),
        Listing(
            title = "Modern Duplex — G-West",
            description = "Brand-new 4-bedroom duplex in G-West. High ceilings, Italian tiles, and energy-efficient design.",
            location = "G-West, Gaborone",
            type = "Duplex",
            price = 11000.0,
            depositAmount = 22000.0,
            availabilityDate = "15 Jul 2026",
            isAvailable = true,
            amenities = "4 Bedrooms,3 Bathrooms,Italian Tiles,Solar Geyser,Double Garage",
            imageUrl = "file:///android_asset/listings/duplex2.jpg"
        ),
        Listing(
            title = "Duplex with Garden — Broadhurst",
            description = "Classic duplex in Broadhurst with a mature garden and large outdoor entertaining area.",
            location = "Broadhurst, Gaborone",
            type = "Duplex",
            price = 8000.0,
            depositAmount = 16000.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "3 Bedrooms,2 Bathrooms,Garden,Outdoor Braai,Parking,Security Fence",
            imageUrl = "file:///android_asset/listings/duplex3.jpg"
        ),

        // ── 49-50: Bedsitters ─────────────────────────────────────────────────
        Listing(
            title = "Bedsitter — Old Naledi",
            description = "Compact bedsitter with a combined bedroom and lounge. Bills paid by landlord.",
            location = "Old Naledi, Gaborone",
            type = "Bedsitter",
            price = 1200.0,
            depositAmount = 2400.0,
            availabilityDate = "01 Jun 2026",
            isAvailable = true,
            amenities = "Kitchenette,Shared Bathroom,All Bills Included",
            imageUrl = "file:///android_asset/listings/bedsit1.jpg"
        ),
        Listing(
            title = "Bedsitter Near BOTA — Block 8",
            description = "Convenient bedsitter near BOTA offices and Block 8 shopping centre. DSTV point included.",
            location = "Block 8, Gaborone",
            type = "Bedsitter",
            price = 1400.0,
            depositAmount = 2800.0,
            availabilityDate = "15 Jun 2026",
            isAvailable = true,
            amenities = "DSTV Point,Shared Bathroom,Kitchenette,Security Gate",
            imageUrl = "file:///android_asset/listings/bedsit2.jpg"
        )
    )
}
