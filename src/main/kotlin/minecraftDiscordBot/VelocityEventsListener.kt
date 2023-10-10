package minecraftDiscordBot

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import java.awt.Color

class VelocityEventsListener(private val bot: DiscordBot) {

    @Subscribe
    fun onPlayerConnect (event: ServerConnectedEvent) {
        val player = event.player.username
        val imageUrl = "https://cravatar.eu/helmavatar/${event.player.uniqueId}/128"  // Player skin head url
        bot.sendMessageWithEmbed("$player has joined the server.", Color.GREEN, imageUrl)
    }


    @Subscribe
    fun onPlayerDisconnect(event: DisconnectEvent) {
        val player = event.player.username
        val imageUrl = "https://cravatar.eu/helmavatar/${event.player.uniqueId}/128"  // Player skin head url
        bot.sendMessageWithEmbed("$player has left the server.", Color.RED, imageUrl)
    }
}

