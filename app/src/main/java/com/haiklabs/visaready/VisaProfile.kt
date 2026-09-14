package com.haiklabs.visaready

data class VisaProfile(
    val destination: String = "France",
    val visaType: String = "Schengen Tourist Visa (Short stay ≤ 90 days)",
    val purpose: String = "Tourism & Sightseeing",
    val duration: String = "12 Days",
    val travelDate: String = "Oct 14, 2026",
    val funding: String = "Personally Funded (Self-sponsored)",
    val accommodation: String = "Confirmed hotel booking / rental",
    val citizenship: String = "India",
    val residence: String = "United Arab Emirates",
    val age: String = "26 – 35 years",
    val employment: String = "Employed (Full-time / Private Contract)",
    val employmentDuration: String = "2 Years, 4 Months",
    val salary: String = "15,000 – 20,000 AED",
    val savings: String = "45,000 – 60,000 AED",
    val statements: String = "Yes, fully stamped bank statements",
    val schengenHistory: String = "Yes, visited multiple times",
    val activeVisa: String = "Yes, valid US B1/B2 or UK Standard Visitor",
    val refusal: String = "Yes, previously refused",
    val refusalDetails: String = "Schengen (Germany) — Jan 2024",
    val documents: Set<String> = setOf("Hotel Booking Proof", "Return Flight Reservation", "Travel Medical Insurance", "No Objection Certificate (NOC)"),
    val ties: Set<String> = setOf("Employment Contract & Paid Payslips", "Real Estate Ownership / Rental Agreement"),
)

data class VisaAssessment(val score: Int, val strengths: List<String>, val risks: List<String>)

object ReadinessEngine {
    fun assess(profile: VisaProfile): VisaAssessment {
        var score = 35
        val strengths = mutableListOf<String>()
        val risks = mutableListOf<String>()
        if (profile.employment.startsWith("Employed")) { score += 12; strengths += "Stable employment in ${profile.residence}" }
        if (profile.statements.startsWith("Yes")) { score += 12; strengths += "Verifiable bank statements and savings" } else risks += "Bank statement evidence needs strengthening"
        if (profile.documents.size >= 3) { score += 12; strengths += "Core travel documents prepared" } else risks += "Some core travel documents are missing"
        if (profile.ties.isNotEmpty()) { score += 9; strengths += "Evidence of ties to residence country" } else risks += "Limited evidence of intention to return"
        if (profile.schengenHistory.startsWith("Yes")) { score += 8; strengths += "Positive international travel history" }
        if (profile.refusal.startsWith("Yes")) { score -= 10; risks += "Previous refusal (${profile.refusalDetails}) needs a clear explanation" }
        return VisaAssessment(score.coerceIn(20, 95), strengths.take(3), risks.ifEmpty { listOf("No major preparation risks identified") })
    }
}
