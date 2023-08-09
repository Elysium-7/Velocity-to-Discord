package minecraftDiscordBot

import com.moandjiezana.toml.Toml
import com.moandjiezana.toml.TomlWriter
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import org.slf4j.Logger
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Plugin(id = "mc-vtod", name = "Velocity-to-Discord", version = "1.1.0", authors = ["Elysium"])
class Main @Inject constructor(
    private val server: ProxyServer,
    private val logger: Logger,
    @DataDirectory private val dataFolder: Path
) {
    private lateinit var bot: DiscordBot

    // 現在の設定ファイルのバージョン
    private val currentConfigVersion = 1.1

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        val configFile = dataFolder.resolve("config.toml")

        if (Files.notExists(configFile) || checkAndUpdateConfig(configFile)) {
            createDefaultConfig(configFile)
        }

        val config = Toml().read(configFile.toFile())
        val botToken = config.getString("bot.token")
        val channelId = config.getLong("bot.channel_id")
        val startMessageText = config.getString("bot.start_message_text")

        bot = DiscordBot(botToken, channelId)
        server.eventManager.register(this, bot)
        server.eventManager.register(this, VelocityEventsListener(server, bot, logger))

        runBlocking {
            launch {
                delay(2000)
                bot.sendMessage(":white_check_mark: **$startMessageText**")
            }
        }
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        val stopMessageText = Toml().read(dataFolder.resolve("config.toml").toFile()).getString("bot.stop_message_text")
        bot.sendMessage(":octagonal_sign: **$stopMessageText**")
        bot.shutdown()
    }

    private fun createDefaultConfig(configFile: Path) {
        val defaultConfig = mapOf(
            "configuration-version" to currentConfigVersion,
            "bot" to mapOf(
                "token" to "",
                "channel_id" to 0,
                "start_message_text" to "Server has started.",
                "stop_message_text" to "Server has stopped."
            )
        )

        TomlWriter().write(defaultConfig, configFile.toFile())
    }

    private fun checkAndUpdateConfig(configFile: Path): Boolean {
        val configVersion = Toml().read(configFile.toFile()).getLong("configuration-version") ?: 0L
        return configVersion < currentConfigVersion
    }
}
