package com.haiklabs.visaready

import org.junit.Assert.assertTrue
import org.junit.Test

class ReadinessEngineTest {
    @Test fun defaultProfileProducesBoundedScoreAndExplanation() {
        val result = ReadinessEngine.assess(VisaProfile())
        assertTrue(result.score in 20..95)
        assertTrue(result.strengths.isNotEmpty())
        assertTrue(result.risks.any { it.contains("refusal", ignoreCase = true) })
    }

    @Test fun missingEvidenceLowersScore() {
        val complete = ReadinessEngine.assess(VisaProfile()).score
        val weak = ReadinessEngine.assess(VisaProfile(statements = "No", documents = emptySet(), ties = emptySet(), schengenHistory = "No previous visits")).score
        assertTrue(weak < complete)
    }
}
