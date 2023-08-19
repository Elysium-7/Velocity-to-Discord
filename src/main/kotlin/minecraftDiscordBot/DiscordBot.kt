package minecraftDiscordBot

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import java.awt.Color

class DiscordBot(private val botToken: String, private val channelId: Long, onlineStatusString: OnlineStatus) {
    private val jda: JDA = JDABuilder.createDefault(botToken)
        .setActivity(Activity.playing("Minecraft"))
        .setStatus(onlineStatusString)
        .build()


    fun sendMessage(message: String) {
        jda.getTextChannelById(channelId)?.sendMessage(message)?.queue()
    }

    fun sendMessageWithEmbed(username: String, message: String, color: Color, imageUrl: String) {
        val embed = EmbedBuilder()
            .setColor(color)
            .setAuthor(username, null, imageUrl)
            .setDescription(message)
            .build()

        jda.getTextChannelById(channelId)?.sendMessageEmbeds(embed)?.queue()
    }

    fun sendMessageToChannel(channelId: Long, message: String) {
        jda.getTextChannelById(channelId)?.sendMessage(message)?.queue()
    }

    fun shutdown() {
        jda.shutdown()
    }
}
