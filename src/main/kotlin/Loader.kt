package main

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import java.util.*


class Loader : JavaPlugin(), Listener, CommandExecutor {
    private var econ: Economy? = null
    private var taxHandler: TaxHandler? = null

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
        setupEconomy()

        // collect taxes weekly
        Bukkit.getScheduler().runTaskTimer(this, collectTaxes(), getTicksUntilSunday(), WEEK_IN_MINECRAFT_TICKS)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            when (command.name) {
                "hallo" -> {
                    sender.sendMessage("Yo, ${sender.name}!")
                }
                "tax" -> {
                    taxHandler?.handleCommand(sender, args)
                }
            }
        }
        return true
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp: RegisteredServiceProvider<Economy> = server.servicesManager.getRegistration(Economy::class.java)
            ?: return false
        econ = rsp.provider
        taxHandler = TaxHandler(econ!!)
        return true
    }

    private fun getTicksUntilSunday(): Long {
        // Sunday 10:00 current timezone
        val dayOfWeek = Calendar.SUNDAY
        val hour = 10
        val minute = 0

        val cal = Calendar.getInstance() // Today, now

        if (cal[Calendar.DAY_OF_WEEK] != dayOfWeek) {
            cal.add(Calendar.DAY_OF_MONTH, (dayOfWeek + 7 - cal[Calendar.DAY_OF_WEEK]) % 7)
        } else {
            val minOfDay = cal[Calendar.HOUR_OF_DAY] * 60 + cal[Calendar.MINUTE]
            if (minOfDay >= hour * 60 + minute) cal.add(Calendar.DAY_OF_MONTH, 7)
        }
        cal[Calendar.HOUR_OF_DAY] = hour
        cal[Calendar.MINUTE] = minute
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0

        val offsetMillis = cal.timeInMillis - System.currentTimeMillis()

        return offsetMillis / 50 // 1 tick = 50 milliseconds
    }

    private fun collectTaxes(): Runnable {
        val r = Runnable {
            taxHandler?.collectTaxes()
        }

        return r
    }

    companion object {
        const val WEEK_IN_MINECRAFT_TICKS = 20 * 60 * 60 * 24 * 7L
    }
}