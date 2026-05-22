package com.example.kwagae.data

import kotlinx.coroutines.delay

/**
 * Multi-university student registry simulation for Gaborone institutions.
 *
 * Seven universities are supported.  Six use a fixed prefix for detection;
 * Botswana Accountancy College uses a course-based format with no fixed prefix:
 *
 *  University                        | Detection      | Format example
 *  ----------------------------------|----------------|-------------------
 *  University of Botswana            | prefix "UB"    | UB20210001
 *  Botho University                  | prefix "BOTHO" | BOTHO210001
 *  Limkokwing University             | prefix "LU"    | LU2021001
 *  Botswana Open University          | prefix "BOU"   | BOU210001
 *  ABM University College            | prefix "ABM"   | ABM210001
 *  BAISAGO University                | prefix "BAI"   | BAI210001
 *  Botswana Accountancy College (BAC)| pattern        | CSE24-099
 *
 * BAC student numbers follow the pattern:
 *   [COURSE_CODE][2-digit year]-[3-digit number]
 *   e.g.  CSE24-099   ACC24-001   BAF23-045   HRM24-010   MKT24-003
 *
 * In a real app this would call a national student registry API.
 * Here the simulation:
 *   1. Identifies the university (prefix match, then BAC pattern fallback)
 *   2. Validates the number against that university's full-format regex
 *   3. Waits 1.5 s (simulated network latency)
 *   4. Looks the number up in a hard-coded valid-number set
 */
object StudentVerifier {

    /**
     * Describes one Gaborone university and its student-number rules.
     *
     * @param shortCode     Fixed prefix (e.g. "UB", "BOTHO"); set to "" for pattern-detected
     *                      universities like BAC.
     * @param detectPattern Only needed when [shortCode] is empty. A partial regex anchored
     *                      at the start of the input, used for live detection as the student
     *                      types.  Example: `^[A-Z]{2,4}\d{2}` detects BAC after the user
     *                      has typed a course code + year.
     * @param format        Full format regex used for final validation.
     * @param example       Representative number shown in hints and error messages.
     * @param validNumbers  Hard-coded set of 50 valid numbers mirroring seeded/test data.
     */
    data class University(
        val name: String,
        val shortCode: String,
        val format: Regex,
        val example: String,
        val validNumbers: Set<String>,
        val detectPattern: Regex? = null   // only needed if shortCode is ""
    )

    val universities: List<University> = listOf(

        // ── Prefix-detected ────────────────────────────────────────────────────

        University(
            name         = "University of Botswana",
            shortCode    = "UB",
            format       = Regex("^UB\\d{8}$"),
            example      = "UB20210001",
            validNumbers = (1..50).map { "UB%08d".format(20210000 + it) }.toSet()
        ),

        University(
            name         = "Botho University",
            shortCode    = "BOTHO",
            format       = Regex("^BOTHO\\d{6}$"),
            example      = "BOTHO210001",
            validNumbers = (1..50).map { "BOTHO%06d".format(210000 + it) }.toSet()
        ),

        University(
            name         = "Limkokwing University",
            shortCode    = "LU",
            format       = Regex("^LU\\d{7}$"),
            example      = "LU2021001",
            validNumbers = (1..50).map { "LU%07d".format(2021000 + it) }.toSet()
        ),

        University(
            name         = "Botswana Open University",
            shortCode    = "BOU",
            format       = Regex("^BOU\\d{6}$"),
            example      = "BOU210001",
            validNumbers = (1..50).map { "BOU%06d".format(210000 + it) }.toSet()
        ),

        University(
            name         = "ABM University College",
            shortCode    = "ABM",
            format       = Regex("^ABM\\d{6}$"),
            example      = "ABM210001",
            validNumbers = (1..50).map { "ABM%06d".format(210000 + it) }.toSet()
        ),

        University(
            name         = "BAISAGO University",
            shortCode    = "BAI",
            format       = Regex("^BAI\\d{6}$"),
            example      = "BAI210001",
            validNumbers = (1..50).map { "BAI%06d".format(210000 + it) }.toSet()
        ),

        // ── Pattern-detected: no fixed prefix ─────────────────────────────────

        University(
            name    = "Botswana Accountancy College",
            shortCode = "",   // no fixed prefix — course code varies per programme
            // Full format: 2–4 uppercase letters (course) + 2-digit year + dash + 3-digit number
            // e.g.  CSE24-099   ACC24-001   BAF23-045   HRM24-010   MKT24-003
            format       = Regex("^[A-Z]{2,4}\\d{2}-\\d{3}$"),
            // Detect as soon as the user has typed the course code + year (before the dash)
            detectPattern = Regex("^[A-Z]{2,4}\\d{2}"),
            example      = "CSE24-099",
            // 50 valid numbers spread across 5 common BAC programmes (10 each)
            // Course codes: CSE · ACC · BAF · HRM · MKT
            validNumbers = buildSet {
                listOf("CSE", "ACC", "BAF", "HRM", "MKT").forEach { course ->
                    (1..10).forEach { n -> add("%s24-%03d".format(course, n)) }
                }
            }
        )
    )

    // ── Detection ─────────────────────────────────────────────────────────────

    /**
     * Detect the university from a partial or complete student number.
     *
     * Detection strategy (in order):
     *   1. Prefix match  — instant, covers UB / BOTHO / LU / BOU / ABM / BAI
     *   2. Pattern match — covers BAC-style numbers ([course][year]-[number])
     *
     * Safe to call on every keystroke — no delay, no coroutine needed.
     * Returns null when no university can yet be inferred from the input.
     */
    fun detectUniversity(studentNumber: String): University? {
        val upper = studentNumber.trim().uppercase()
        if (upper.isEmpty()) return null

        // 1. Prefix-based (fastest — O(n) over the small university list)
        universities.find { it.shortCode.isNotEmpty() && upper.startsWith(it.shortCode) }
            ?.let { return it }

        // 2. Pattern-based fallback (for BAC-style numbers)
        return universities.find { uni ->
            uni.shortCode.isEmpty() &&
            uni.detectPattern != null &&
            uni.detectPattern.containsMatchIn(upper)
        }
    }

    // ── Verification ──────────────────────────────────────────────────────────

    /**
     * Verify [studentNumber] against the simulated national registry.
     * Must be called from a coroutine — simulates a 1.5 s network round-trip.
     */
    suspend fun verify(studentNumber: String): VerificationResult {
        val normalised = studentNumber.trim().uppercase()

        if (normalised.isBlank()) return VerificationResult.InvalidFormat(null)

        // 1. Identify university
        val uni = detectUniversity(normalised)
            ?: return VerificationResult.UnknownUniversity

        // 2. Full format validation (instant — no network needed)
        if (!normalised.matches(uni.format)) {
            return VerificationResult.InvalidFormat(uni)
        }

        // 3. Simulate network latency
        delay(1500L)

        // 4. Registry lookup
        return if (normalised in uni.validNumbers) {
            VerificationResult.Verified(uni)
        } else {
            VerificationResult.NotFound
        }
    }
}

/** Result returned by [StudentVerifier.verify] */
sealed class VerificationResult {

    /** Number is in the registry — [university] tells you which institution confirmed it. */
    data class Verified(val university: StudentVerifier.University) : VerificationResult()

    /** Number format is valid but no matching record was found in the registry. */
    object NotFound : VerificationResult()

    /**
     * Number does not match the expected format for its university.
     * [university] is non-null when a university was detected but the format is wrong;
     * null when the input was blank or had no recognisable university indicator.
     */
    data class InvalidFormat(val university: StudentVerifier.University?) : VerificationResult()

    /** The input does not match any known Gaborone university prefix or pattern. */
    object UnknownUniversity : VerificationResult()
}
