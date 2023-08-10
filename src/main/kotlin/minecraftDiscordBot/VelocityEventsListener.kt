package minecraftDiscordBot

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger
import java.awt.Color

class VelocityEventsListener(private val server: ProxyServer, private val bot: DiscordBot, private val logger: Logger) {

    @Subscribe
    fun onPlayerConnect (event: ServerConnectedEvent) {
        val player = event.player.username
        val imageUrl = "https://cravatar.eu/helmavatar/${event.player.uniqueId}/128"  // Player skin head url
        bot.sendMessageWithEmbed(player, "**$player** has joined the server.", Color.GREEN, imageUrl)
    }


    @Subscribe
    fun onPlayerDisconnect(event: DisconnectEvent) {
        val player = event.player.username
        val imageUrl = "https://cravatar.eu/helmavatar/${event.player.uniqueId}/128"  // Player skin head url
        bot.sendMessageWithEmbed(player, "**$player** has left the server.", Color.RED, imageUrl)
    }
}