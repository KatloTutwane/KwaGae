package com.example.kwagae

/**
 * Simple in-memory transfer object used to pass chat context to [ChatScreen]
 * without encoding listing titles or names into the navigation route string.
 *
 * Set all fields immediately before calling navController.navigate("chat"),
 * then read them inside the composable destination.
 */
object ChatNavArgs {
    var threadId     : String = ""
    var providerId   : String = ""
    var providerName : String = ""
    var listingId    : Long   = 0L
    var listingTitle : String = ""
}
