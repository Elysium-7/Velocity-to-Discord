package minecraftDiscordBot

import org.yaml.snakeyaml.Yaml
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

@Plugin(id = "mc-vtod", name = "Velocity-to-Discord", version = "1.1.1", authors = ["Elysium"])
class Main @Inject constructor(
    private val server: ProxyServer,
    private val logger: Logger,
    @DataDirectory private val dataFolder: Path
) {
    private lateinit var bot: DiscordBot

    private val currentConfigVersion = 1.1

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        if (!Files.exists(dataFolder)) {
            logger.info("Attempting to create data folder: $dataFolder")
            Files.createDirectories(dataFolder)
            logger.info("Data folder created successfully.")
            logger.info("Data folder path: ${dataFolder.toAbsolutePath()}")
        }

        val configFile = dataFolder.resolve("config.yml")
        logger.info("Expected path for config.yml: ${configFile.toAbsolutePath()}")

        if (Files.notExists(configFile) || checkAndUpdateConfig(configFile)) {
            copyDefaultConfigFromResources(configFile)
        }

        val config = Yaml().load<Map<String, Any>>(Files.newBufferedReader(configFile))
        val botToken = config["token"]?.toString() ?: ""
        val channelId = config["channel_id"]?.toString()?.toLong() ?: 0
        val startMessageText = config["start_message_text"]?.toString() ?: "Server has started."
        logger.info("Loaded config content: ${config.toString()}")


        try {
            bot = DiscordBot(botToken, channelId.toLong())
        } catch (e: Exception) {
            if (botToken.isEmpty()) {
                logger.error("ERROR!! In the configuration file, enter the bot token and channel ID!")
            } else {
                logger.error("An unexpected error occurred during bot initialization:", e)
            }
            return
        }

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
        val configFile = dataFolder.resolve("config.yml")
        val config = Yaml().load<Map<String, Any>>(Files.newBufferedReader(configFile))
        val stopMessageText = config["stop_message_text"]?.toString() ?: "Server has stopped"
        bot.sendMessage(":octagonal_sign: **$stopMessageText**")
        bot.shutdown()
    }

    private fun copyDefaultConfigFromResources(configFile: Path) {
        if (Files.notExists(configFile)) {
            javaClass.getResourceAsStream("/config.yml")?.use { defaultConfigStream ->
                Files.copy(defaultConfigStream, configFile)
            } ?: throw RuntimeException("Could not find config.yml in resources!")
        }
    }

    private fun checkAndUpdateConfig(configFile: Path): Boolean {
        val configVersion = (Yaml().load<Map<String, Any>>(Files.newBufferedReader(configFile))["configuration-version"] as Double).toLong()
        return configVersion < currentConfigVersion
    }
}
