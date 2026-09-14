package com.haiklabs.visaready

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

private val Blue = Color(0xFF1559E8)
private val Navy = Color(0xFF111827)
private val Slate = Color(0xFF536075)
private val Border = Color(0xFFD9E2EF)
private val Canvas = Color(0xFFF8FAFD)
private val PaleBlue = Color(0xFFEAF4FF)
private val Green = Color(0xFF12B981)
private val Amber = Color(0xFFE17A00)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge(); setContent { VisaTheme { VisaApp() } }
    }
}

@Composable private fun VisaTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = lightColorScheme(primary = Blue, background = Canvas, surface = Color.White),
    typography = Typography(), content = content,
)

@Composable fun VisaApp(vm: VisaViewModel = viewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var message by remember { mutableStateOf<String?>(null) }
    val billing = remember { BillingManager(context, vm::purchaseComplete) { message = it } }
    DisposableEffect(Unit) { billing.connect(); onDispose { billing.close() } }
    Surface(Modifier.fillMaxSize(), color = Canvas) {
        when (state.screen) {
            VisaScreen.WELCOME -> Welcome(vm::next)
            VisaScreen.DESTINATION -> FormPage(1, "Where are you planning to travel?", vm::back, vm::next) { Destination(state.profile, vm::update) }
            VisaScreen.TRIP -> FormPage(2, "Tell us about your upcoming trip", vm::back, vm::next) { Trip(state.profile, vm::update) }
            VisaScreen.PERSONAL -> FormPage(3, "What is your current status?", vm::back, vm::next) { Personal(state.profile, vm::update) }
            VisaScreen.FINANCIAL -> FormPage(4, "Verify your financial strength", vm::back, vm::next) { Financial(state.profile, vm::update) }
            VisaScreen.HISTORY -> FormPage(5, "Your historical travel record", vm::back, vm::next) { History(state.profile, vm::update) }
            VisaScreen.DOCUMENTS -> FormPage(6, "Document checklist & ties", vm::back, vm::next, "Review Answers") { Documents(state.profile, vm::update) }
            VisaScreen.REVIEW -> Review(state.profile, vm::back, vm::next)
            VisaScreen.ANALYSIS -> Analysis(vm::next)
            VisaScreen.PAYWALL -> Paywall(vm::back) { billing.launch(context as Activity) }
            VisaScreen.RESULT -> Result(state.profile, vm::back)
        }
    }
    message?.let { AlertDialog(onDismissRequest = { message = null }, confirmButton = { TextButton(onClick = { message = null }) { Text("OK") } }, title = { Text("Google Play checkout") }, text = { Text(it) }) }
}

@Composable private fun Welcome(start: () -> Unit) = Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.SpaceBetween) {
    Column { Spacer(Modifier.height(55.dp)); AssistantBadge(); Spacer(Modifier.height(25.dp)); Title("Check your visa\nreadiness in minutes", 34)
        Text("Evaluate your profile against official Schengen guidelines. Identify missing requirements, secure your travel documents, and strengthen your home ties before you apply.", color = Slate, fontSize = 17.sp, lineHeight = 27.sp)
        Spacer(Modifier.height(20.dp)); Feature(Icons.Outlined.StarOutline, "Personalized Readiness Score", "Get an analytical evaluation of your documents, financials, and strong home ties.")
        Feature(Icons.Outlined.Description, "Tailored Document Checklist", "Know exactly what papers you need based on your employment and trip purpose.")
        Feature(Icons.Outlined.Security, "Private & Highly Secure", "Your answers remain on this device and are used only for your readiness assessment.")
        Info("This assessment evaluates preparation based on consular standards. It does not issue or guarantee visa approvals.", warning = true)
    }
    Column { PrimaryButton("Start Assessment", start); Text("◷ Takes 3–5 minutes · No register required", Modifier.fillMaxWidth().padding(10.dp), color = Slate, textAlign = TextAlign.Center, fontSize = 13.sp) }
}

@Composable private fun FormPage(step: Int, title: String, back: () -> Unit, next: () -> Unit, action: String = "Continue", content: @Composable ColumnScope.() -> Unit) = Column(Modifier.fillMaxSize()) {
    Column(Modifier.padding(horizontal = 26.dp)) { Spacer(Modifier.height(48.dp)); TopBack(back, "STEP $step OF 6"); Title(title, 24); LinearProgressIndicator({ step / 6f }, Modifier.fillMaxWidth().height(4.dp), color = Blue, trackColor = Border) }
    LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(26.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) { item { Column(verticalArrangement = Arrangement.spacedBy(18.dp), content = content) } }
    Surface(shadowElevation = 8.dp) { Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { if (step > 1) SecondaryButton("Previous", back, Modifier.weight(.7f)); PrimaryButton(action, next, Modifier.weight(1.3f)) } }
}

@Composable private fun Destination(p: VisaProfile, update: ((VisaProfile) -> VisaProfile) -> Unit) { Picker("Destination Country", p.destination, listOf("France", "Germany", "Italy", "Spain", "Netherlands")) { update { p -> p.copy(destination = it) } }; Label("Select Visa Type"); Options(listOf("Schengen Tourist Visa (Short stay ≤ 90 days)", "Business Visa", "Visitor Visa (Friends & Family)", "Student / Study Visa"), p.visaType) { update { p -> p.copy(visaType = it) } }; Info("For a Schengen Tourist Visa, apply to the consulate of the country where you'll spend the most nights.") }
@Composable private fun Trip(p: VisaProfile, update: ((VisaProfile) -> VisaProfile) -> Unit) { Picker("Primary Purpose of Visit", p.purpose, listOf("Tourism & Sightseeing", "Business", "Visit Family & Friends")) { update { p -> p.copy(purpose = it) } }; Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Box(Modifier.weight(1f)) { Picker("Trip Duration", p.duration, listOf("7 Days", "12 Days", "21 Days")) { update { p -> p.copy(duration = it) } } }; Box(Modifier.weight(1f)) { Picker("Planned Travel Date", p.travelDate, listOf("Oct 14, 2026", "Nov 20, 2026", "Jan 15, 2027")) { update { p -> p.copy(travelDate = it) } } } }; Label("Who is funding this trip?"); Options(listOf("Personally Funded (Self-sponsored)", "Sponsored by an individual / host", "Sponsored by an employing company"), p.funding) { update { p -> p.copy(funding = it) } }; Label("Intended Accommodation"); Options(listOf("Confirmed hotel booking / rental", "Staying with host (official invitation needed)"), p.accommodation) { update { p -> p.copy(accommodation = it) } } }
@Composable private fun Personal(p: VisaProfile, update: ((VisaProfile) -> VisaProfile) -> Unit) { Picker("Your Citizenship", p.citizenship, listOf("India", "United Arab Emirates", "Philippines", "United Kingdom")) { update { p -> p.copy(citizenship = it) } }; Picker("Country of Legal Residence", p.residence, listOf("United Arab Emirates", "India", "Saudi Arabia", "Qatar")) { update { p -> p.copy(residence = it) } }; Picker("Your Age Range", p.age, listOf("18 – 25 years", "26 – 35 years", "36 – 50 years", "51+ years")) { update { p -> p.copy(age = it) } }; Label("Employment Status"); Options(listOf("Employed (Full-time / Private Contract)", "Self-employed / Business Owner", "Student", "Unemployed / Retired"), p.employment) { update { p -> p.copy(employment = it) } }; Picker("Duration of Current Employment", p.employmentDuration, listOf("Under 6 Months", "1 Year", "2 Years, 4 Months", "5+ Years")) { update { p -> p.copy(employmentDuration = it) } } }
@Composable private fun Financial(p: VisaProfile, update: ((VisaProfile) -> VisaProfile) -> Unit) { Info("Consulates require proof of consistent income to justify trip expenses and home ties."); Picker("Monthly Net Salary Range (AED Equivalent)", p.salary, listOf("Under 5,000 AED", "5,000 – 10,000 AED", "15,000 – 20,000 AED", "20,000+ AED")) { update { p -> p.copy(salary = it) } }; Picker("Estimated Savings / Liquid Balance", p.savings, listOf("Under 10,000 AED", "20,000 – 40,000 AED", "45,000 – 60,000 AED", "60,000+ AED")) { update { p -> p.copy(savings = it) } }; Label("Can you provide official 3–6 month bank statements?"); Options(listOf("Yes, fully stamped bank statements", "Yes, but only online digital downloads", "No, I do not have a bank statement"), p.statements) { update { p -> p.copy(statements = it) } } }
@Composable private fun History(p: VisaProfile, update: ((VisaProfile) -> VisaProfile) -> Unit) { Label("Have you traveled to the Schengen Area in the last 59 months?"); Options(listOf("Yes, visited multiple times", "Yes, visited once", "No previous visits"), p.schengenHistory) { update { p -> p.copy(schengenHistory = it) } }; Label("Do you currently hold any other valid visas?"); Options(listOf("Yes, valid US B1/B2 or UK Standard Visitor", "No active global visas"), p.activeVisa) { update { p -> p.copy(activeVisa = it) } }; Label("Have you ever been refused a visa by any country?"); Options(listOf("Yes, previously refused", "No previous refusals"), p.refusal) { update { p -> p.copy(refusal = it) } }; if (p.refusal.startsWith("Yes")) Picker("Refusal Country & Estimated Date", p.refusalDetails, listOf("Schengen (Germany) — Jan 2024", "United Kingdom — 2023", "United States — 2022")) { update { p -> p.copy(refusalDetails = it) } } }
@Composable private fun Documents(p: VisaProfile, update: ((VisaProfile) -> VisaProfile) -> Unit) { Label("▣  Consular Document Checklist"); Checks(listOf("Hotel Booking Proof", "Return Flight Reservation", "Travel Medical Insurance", "No Objection Certificate (NOC)"), p.documents) { update { p -> p.copy(documents = it) } }; Label("⌂  Proof of Ties to Residence Country"); Text("Strong ties prove you intend to return. Select what you can provide:", color = Slate); Checks(listOf("Employment Contract & Paid Payslips", "Family Ties (Spouse/Children staying behind)", "Real Estate Ownership / Rental Agreement"), p.ties) { update { p -> p.copy(ties = it) } } }

@Composable private fun Review(p: VisaProfile, back: () -> Unit, next: () -> Unit) = Column(Modifier.fillMaxSize()) { Column(Modifier.padding(26.dp)) { Spacer(Modifier.height(40.dp)); TopBack(back); Title("Review your profile details", 24); Info("Almost ready. Review all information before launching the profile readiness analysis.") }; LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 26.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { item { ReviewCard("1. DESTINATION", listOf("Target Country" to p.destination, "Visa Type" to p.visaType.substringBefore(" ("))) }; item { ReviewCard("2. TRIP DETAILS", listOf("Purpose" to p.purpose, "Duration" to "${p.duration} (${p.travelDate})", "Sponsor Source" to p.funding.substringBefore(" ("))) }; item { ReviewCard("3. PERSONAL SITUATION", listOf("Residency Status" to "${p.citizenship} citizen in ${p.residence}", "Employment" to p.employment)) }; item { ReviewCard("4. FINANCIAL SITUATION", listOf("Net Salary Range" to p.salary, "Estimated Savings" to p.savings, "Bank Statements" to p.statements)) }; item { ReviewCard("5. TRAVEL HISTORY", listOf("Previous Schengen Trip" to p.schengenHistory, "Previous Refusal" to if (p.refusal.startsWith("Yes")) p.refusalDetails else "None")) } }; Surface(shadowElevation = 8.dp) { Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { SecondaryButton("Previous", back, Modifier.weight(.7f)); PrimaryButton("Analyze Profile", next, Modifier.weight(1.3f)) } } }

@Composable private fun Analysis(done: () -> Unit) { var progress by remember { mutableFloatStateOf(.12f) }; LaunchedEffect(Unit) { repeat(8) { delay(280); progress += .11f }; delay(250); done() }; Column(Modifier.fillMaxSize().padding(28.dp)) { Spacer(Modifier.height(50.dp)); AssistantBadge(); Spacer(Modifier.height(20.dp)); Title("Analyzing your visa profile", 25); Text("Evaluating preparation strength according to official Schengen guidance.", color = Slate, fontSize = 16.sp); Spacer(Modifier.height(60.dp)); Box(Modifier.align(Alignment.CenterHorizontally).size(135.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(progress = { progress.coerceAtMost(1f) }, Modifier.fillMaxSize(), strokeWidth = 14.dp, color = Blue, trackColor = PaleBlue); Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("${(progress * 100).toInt().coerceAtMost(100)}%", color = Blue, fontSize = 29.sp, fontWeight = FontWeight.Bold); Text("Analyzing", color = Slate) } }; Spacer(Modifier.height(48.dp)); AnalysisRow("Schengen document checklist verified", progress > .3f); AnalysisRow("Employment & financial ties evaluated", progress > .5f); AnalysisRow("Analyzing travel history & previous refusal...", progress > .7f); AnalysisRow("Compiling custom readiness report", progress >= 1f); Spacer(Modifier.height(30.dp)); Info("This review compares your answers with common consular preparation factors to help identify possible gaps.") } }

@Composable private fun Paywall(back: () -> Unit, buy: () -> Unit) = Column(Modifier.fillMaxSize()) { Column(Modifier.weight(1f).padding(28.dp)) { Spacer(Modifier.height(48.dp)); TextButton(onClick = back) { Text("Back", fontSize = 16.sp) }; Title("Unlock Your Detailed Visa Prep Plan", 25); Text("Get actionable steps to maximize your preparation and address any prior refusal.", color = Slate, fontSize = 16.sp, lineHeight = 23.sp); Spacer(Modifier.height(34.dp)); Surface(shape = RoundedCornerShape(18.dp), color = PaleBlue) { Column(Modifier.padding(20.dp)) { Text("★  What's Included in the Report", color = Blue, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("\n• Refusal Mitigation Guide\n\n• Personalized Home-Ties Strengthening Guide\n\n• Custom Schengen-Compliant Cover Letter outline\n\n• Document checklist and recommendations", color = Slate) } }; Spacer(Modifier.height(24.dp)); Surface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), Color.White, border = BorderStroke(1.dp, Border)) { Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("ONE-TIME ASSESSMENT FEE", color = Slate, fontWeight = FontWeight.Bold); Text("$19", color = Navy, fontSize = 45.sp, fontWeight = FontWeight.ExtraBold); Text("Price shown by Google Play at checkout · No subscription", color = Slate, textAlign = TextAlign.Center) } } }; Surface(shadowElevation = 10.dp) { Column(Modifier.padding(20.dp)) { PrimaryButton("Unlock Full Report", buy); Text("Secure checkout powered by Google Play", Modifier.fillMaxWidth().padding(top = 10.dp), textAlign = TextAlign.Center, color = Slate, fontSize = 13.sp) } } }

@Composable private fun Result(p: VisaProfile, back: () -> Unit) { val a = remember(p) { ReadinessEngine.assess(p) }; Column(Modifier.fillMaxSize()) { LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) { item { Spacer(Modifier.height(32.dp)); TextButton(onClick = back) { Text("Back") }; Title("Your Visa Readiness Profile", 25); Text("Based on common preparation guidelines for Schengen short-stay visas.", color = Slate) }; item { Surface(shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, Border)) { Column(Modifier.padding(22.dp)) { Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Column { Text("READINESS SCORE", color = Slate, fontWeight = FontWeight.Bold); Text(if (a.score >= 70) "Strong Profile" else "Needs Attention", color = if (a.score >= 70) Green else Amber, fontWeight = FontWeight.Bold) }; Text("${a.score}/100", color = if (a.score >= 70) Green else Amber, fontSize = 36.sp, fontWeight = FontWeight.Bold) }; Spacer(Modifier.height(20.dp)); Text("Your profile shows useful preparation signals. Review every risk below and verify requirements with the destination consulate before applying.", color = Slate, lineHeight = 24.sp) } } }; item { ResultCard("Key Strengths", Green, a.strengths) }; item { ResultCard("Potential Risks Detected", Amber, a.risks) }; item { Info("Important Guidance: This score reflects preparation readiness compared against common standards. It is not an approval prediction or guarantee.", true) } }; Surface(shadowElevation = 8.dp) { Box(Modifier.padding(18.dp)) { PrimaryButton("Review Assessment", back) } } } }

@Composable private fun AssistantBadge() = Surface(shape = RoundedCornerShape(24.dp), color = PaleBlue) { Row(Modifier.padding(horizontal = 15.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Shield, null, tint = Blue, modifier = Modifier.size(20.dp)); Text("  AI VISA ASSISTANT", color = Blue, fontWeight = FontWeight.Bold, fontSize = 14.sp) } }
@Composable private fun Title(text: String, size: Int) { Text(text, color = Navy, fontSize = size.sp, fontWeight = FontWeight.ExtraBold, lineHeight = (size + 5).sp, modifier = Modifier.padding(bottom = 10.dp)) }
@Composable private fun Feature(icon: ImageVector, title: String, body: String) = Surface(Modifier.fillMaxWidth().padding(bottom = 12.dp), RoundedCornerShape(17.dp), Canvas, border = BorderStroke(1.dp, Border)) { Row(Modifier.padding(17.dp)) { Surface(shape = CircleShape, color = PaleBlue) { Icon(icon, null, Modifier.padding(11.dp).size(22.dp), tint = Blue) }; Column(Modifier.padding(start = 14.dp)) { Text(title, fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp); Text(body, color = Slate, lineHeight = 20.sp) } } }
@Composable private fun Info(text: String, warning: Boolean = false) = Surface(Modifier.fillMaxWidth(), RoundedCornerShape(13.dp), if (warning) Color(0xFFFFF2C4) else PaleBlue) { Row(Modifier.padding(14.dp)) { Icon(Icons.Outlined.Info, null, tint = if (warning) Amber else Blue, modifier = Modifier.size(20.dp)); Text(text, Modifier.padding(start = 10.dp), color = if (warning) Amber else Slate, fontSize = 14.sp, lineHeight = 19.sp) } }
@Composable private fun TopBack(back: () -> Unit, end: String = "") = Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { TextButton(onClick = back, contentPadding = PaddingValues(0.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null); Text(" Back") }; Text(end, color = Blue, fontWeight = FontWeight.Bold) }
@Composable private fun Label(text: String) = Text(text, color = Slate, fontWeight = FontWeight.Bold, fontSize = 14.sp)
@Composable private fun Picker(label: String, value: String, choices: List<String>, changed: (String) -> Unit) { var open by remember { mutableStateOf(false) }; Column { Label(label); Spacer(Modifier.height(7.dp)); Box { Surface(Modifier.fillMaxWidth().clickable { open = true }, RoundedCornerShape(13.dp), Color.White, border = BorderStroke(1.dp, Border)) { Row(Modifier.padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text(value, Modifier.weight(1f), color = Navy, maxLines = 1); Icon(Icons.Outlined.KeyboardArrowDown, null, tint = Slate) } }; DropdownMenu(open, { open = false }) { choices.forEach { DropdownMenuItem({ Text(it) }, { changed(it); open = false }) } } } } }
@Composable private fun Options(items: List<String>, selected: String, changed: (String) -> Unit) = Column(verticalArrangement = Arrangement.spacedBy(9.dp)) { items.forEach { item -> val on = item == selected; Surface(Modifier.fillMaxWidth().clickable { changed(item) }, RoundedCornerShape(13.dp), if (on) PaleBlue else Color.White, border = BorderStroke(if (on) 1.5.dp else 1.dp, if (on) Blue else Border)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(on, { changed(item) }, colors = RadioButtonDefaults.colors(selectedColor = Blue)); Text(item, color = if (on) Blue else Navy, fontWeight = if (on) FontWeight.Bold else FontWeight.Normal) } } } }
@Composable private fun Checks(items: List<String>, selected: Set<String>, changed: (Set<String>) -> Unit) = Column(verticalArrangement = Arrangement.spacedBy(9.dp)) { items.forEach { item -> val on = item in selected; Surface(Modifier.fillMaxWidth().clickable { changed(if (on) selected - item else selected + item) }, RoundedCornerShape(13.dp), if (on) PaleBlue else Color.White, border = BorderStroke(if (on) 1.5.dp else 1.dp, if (on) Blue else Border)) { Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Checkbox(on, { changed(if (on) selected - item else selected + item) }, colors = CheckboxDefaults.colors(checkedColor = Blue)); Text(item, color = Navy, fontWeight = FontWeight.SemiBold) } } } }
@Composable private fun PrimaryButton(text: String, click: () -> Unit, modifier: Modifier = Modifier) = Button(click, modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue)) { Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp); Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(18.dp)) }
@Composable private fun SecondaryButton(text: String, click: () -> Unit, modifier: Modifier = Modifier) = OutlinedButton(click, modifier.height(56.dp), shape = RoundedCornerShape(28.dp), border = BorderStroke(1.dp, Border)) { Text(text, color = Slate, fontWeight = FontWeight.Bold) }
@Composable private fun ReviewCard(title: String, rows: List<Pair<String, String>>) = Surface(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), Color.White, border = BorderStroke(1.dp, Border)) { Column(Modifier.padding(16.dp)) { Text(title, fontWeight = FontWeight.ExtraBold, color = Navy); Spacer(Modifier.height(10.dp)); rows.forEach { (key, value) -> Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), Arrangement.SpaceBetween) { Text(key, color = Slate, modifier = Modifier.weight(.45f)); Text(value, color = Navy, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(.55f), maxLines = 2) } } } }
@Composable private fun AnalysisRow(text: String, active: Boolean) = Surface(Modifier.fillMaxWidth().padding(bottom = 12.dp), RoundedCornerShape(14.dp), Color.White, border = BorderStroke(1.dp, if (active) Blue else Border)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { if (active) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 3.dp, color = Blue) else Box(Modifier.size(18.dp).background(Border, CircleShape)); Text(text, Modifier.padding(start = 14.dp), color = if (active) Blue else Slate, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal) } }
@Composable private fun ResultCard(title: String, color: Color, items: List<String>) = Surface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), Color.White, border = BorderStroke(1.dp, Border)) { Column(Modifier.padding(20.dp)) { Text("●  $title", color = color, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp); Spacer(Modifier.height(10.dp)); items.forEach { Text("• $it", color = Slate, modifier = Modifier.padding(vertical = 4.dp), lineHeight = 20.sp) } } }
