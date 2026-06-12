package com.magicregan.prankcaller.data

data class CountryInfo(
    val name: String,
    val code: String,
    val flag: String
)

object CountryCodes {
    val countries = listOf(
        CountryInfo("United States", "+1", "🇺🇸"),
        CountryInfo("Canada", "+1", "🇨🇦"),
        CountryInfo("United Kingdom", "+44", "🇬🇧"),
        CountryInfo("Australia", "+61", "🇦🇺"),
        CountryInfo("Germany", "+49", "🇩🇪"),
        CountryInfo("France", "+33", "🇫🇷"),
        CountryInfo("India", "+91", "🇮🇳"),
        CountryInfo("Brazil", "+55", "🇧🇷"),
        CountryInfo("Japan", "+81", "🇯🇵"),
        CountryInfo("South Korea", "+82", "🇰🇷"),
        CountryInfo("China", "+86", "🇨🇳"),
        CountryInfo("Mexico", "+52", "🇲🇽"),
        CountryInfo("Russia", "+7", "🇷🇺"),
        CountryInfo("Italy", "+39", "🇮🇹"),
        CountryInfo("Spain", "+34", "🇪🇸"),
        CountryInfo("Netherlands", "+31", "🇳🇱"),
        CountryInfo("Sweden", "+46", "🇸🇪"),
        CountryInfo("Switzerland", "+41", "🇨🇭"),
        CountryInfo("Ireland", "+353", "🇮🇪"),
        CountryInfo("New Zealand", "+64", "🇳🇿"),
        CountryInfo("South Africa", "+27", "🇿🇦"),
        CountryInfo("Nigeria", "+234", "🇳🇬"),
        CountryInfo("Kenya", "+254", "🇰🇪"),
        CountryInfo("Ghana", "+233", "🇬🇭"),
        CountryInfo("Egypt", "+20", "🇪🇬"),
        CountryInfo("UAE", "+971", "🇦🇪"),
        CountryInfo("Saudi Arabia", "+966", "🇸🇦"),
        CountryInfo("Philippines", "+63", "🇵🇭"),
        CountryInfo("Indonesia", "+62", "🇮🇩"),
        CountryInfo("Thailand", "+66", "🇹🇭"),
        CountryInfo("Vietnam", "+84", "🇻🇳"),
        CountryInfo("Malaysia", "+60", "🇲🇾"),
        CountryInfo("Singapore", "+65", "🇸🇬"),
        CountryInfo("Pakistan", "+92", "🇵🇰"),
        CountryInfo("Bangladesh", "+880", "🇧🇩"),
        CountryInfo("Turkey", "+90", "🇹🇷"),
        CountryInfo("Poland", "+48", "🇵🇱"),
        CountryInfo("Argentina", "+54", "🇦🇷"),
        CountryInfo("Colombia", "+57", "🇨🇴"),
        CountryInfo("Chile", "+56", "🇨🇱"),
        CountryInfo("Peru", "+51", "🇵🇪"),
        CountryInfo("Portugal", "+351", "🇵🇹"),
        CountryInfo("Greece", "+30", "🇬🇷"),
        CountryInfo("Belgium", "+32", "🇧🇪"),
        CountryInfo("Austria", "+43", "🇦🇹"),
        CountryInfo("Norway", "+47", "🇳🇴"),
        CountryInfo("Denmark", "+45", "🇩🇰"),
        CountryInfo("Finland", "+358", "🇫🇮"),
        CountryInfo("Israel", "+972", "🇮🇱"),
        CountryInfo("Jamaica", "+1876", "🇯🇲"),
        CountryInfo("Trinidad", "+1868", "🇹🇹"),
        CountryInfo("Puerto Rico", "+1787", "🇵🇷"),
        CountryInfo("Dominican Rep.", "+1809", "🇩🇴"),
        CountryInfo("Haiti", "+509", "🇭🇹"),
        CountryInfo("Cuba", "+53", "🇨🇺"),
        CountryInfo("Honduras", "+504", "🇭🇳"),
        CountryInfo("Guatemala", "+502", "🇬🇹"),
        CountryInfo("Costa Rica", "+506", "🇨🇷"),
        CountryInfo("Panama", "+507", "🇵🇦"),
        CountryInfo("Venezuela", "+58", "🇻🇪"),
        CountryInfo("Ecuador", "+593", "🇪🇨"),
        CountryInfo("Uruguay", "+598", "🇺🇾"),
        CountryInfo("Paraguay", "+595", "🇵🇾"),
        CountryInfo("Bolivia", "+591", "🇧🇴"),
        CountryInfo("Morocco", "+212", "🇲🇦"),
        CountryInfo("Tanzania", "+255", "🇹🇿"),
        CountryInfo("Ethiopia", "+251", "🇪🇹"),
        CountryInfo("Uganda", "+256", "🇺🇬"),
        CountryInfo("Cameroon", "+237", "🇨🇲"),
        CountryInfo("Zimbabwe", "+263", "🇿🇼"),
        CountryInfo("Zambia", "+260", "🇿🇲"),
        CountryInfo("Mozambique", "+258", "🇲🇿"),
        CountryInfo("Rwanda", "+250", "🇷🇼"),
        CountryInfo("Iraq", "+964", "🇮🇶"),
        CountryInfo("Iran", "+98", "🇮🇷"),
        CountryInfo("Afghanistan", "+93", "🇦🇫"),
        CountryInfo("Sri Lanka", "+94", "🇱🇰"),
        CountryInfo("Nepal", "+977", "🇳🇵"),
        CountryInfo("Myanmar", "+95", "🇲🇲"),
        CountryInfo("Cambodia", "+855", "🇰🇭"),
        CountryInfo("Romania", "+40", "🇷🇴"),
        CountryInfo("Ukraine", "+380", "🇺🇦"),
        CountryInfo("Czech Republic", "+420", "🇨🇿"),
        CountryInfo("Hungary", "+36", "🇭🇺"),
        CountryInfo("Croatia", "+385", "🇭🇷"),
        CountryInfo("Serbia", "+381", "🇷🇸"),
    )

    fun search(query: String): List<CountryInfo> {
        if (query.isBlank()) return countries
        val q = query.lowercase()
        return countries.filter {
            it.name.lowercase().contains(q) || it.code.contains(q)
        }
    }

    data class ParsedNumber(
        val countryCode: String,
        val localNumber: String
    )

    fun parseInternationalNumber(fullNumber: String): ParsedNumber {
        val digits = fullNumber.replace("[^0-9]".toRegex(), "")

        // Try matching longest country codes first (4, 3, 2, 1 digits)
        val allCodes = countries.map { it.code.removePrefix("+") }.distinct().sortedByDescending { it.length }
        for (code in allCodes) {
            if (digits.startsWith(code)) {
                return ParsedNumber("+$code", digits.removePrefix(code))
            }
        }

        return ParsedNumber("+1", digits)
    }
}
