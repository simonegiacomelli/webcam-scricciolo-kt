import kotlinx.datetime.TimeZone

val tzApp: TimeZone
    get() {
        val zoneId = "Europe/Rome"

//    println("----------------------------")
//    println(TimeZone.availableZoneIds)
//        return TimeZone.UTC
     return   TimeZone.of(zoneId)
    }