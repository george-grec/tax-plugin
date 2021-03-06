package main

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TaxHandler(private var econ: Economy) {
    private var percentage = 10.0
    private var treasury = 0.0

    fun handleCommand(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) {
            return false
        }

        if (args.isEmpty()) {
            return false
        }

        val player: Player = sender

        when (args[0]) {
            "collect" -> {
                if (player.hasPermission("tax.admin")) {
                    this.collectTaxes()

                    player.sendMessage("Transferred taxes!")
                } else {
                    player.sendMessage("Insufficient permissions!")
                }
            }
            "setPercentage" -> {
                if (player.hasPermission("tax.collect")) {
                    if (args.size < 2 || args[1].toDoubleOrNull() == null) {
                        player.sendMessage("Provide a number for the tax rate argument!")
                        return false
                    }

                    val tempPercentage = args[1].toDouble()
                    if (tempPercentage > 20) {
                        player.sendMessage("This tax rate is above the limit! You must not exceed 20%.")
                    } else if (tempPercentage < 0) {
                        player.sendMessage("This tax rate is below zero! Only tax rates between 0 and 20 are valid.")
                    } else {
                        percentage = tempPercentage
                        player.sendMessage("Success! Tax rate is now $percentage%")
                    }
                } else {
                    player.sendMessage("Insufficient permissions!")
                }
            }
            "help" -> {
                player.sendMessage("Available commands:")
                player.sendMessage("/tax collect | Collect taxes now.")
                player.sendMessage("/tax setPercentage [value] | Sets the tax rate to the given value. Only values between 0 and 20 will be accepted.")
            }
            else -> {
                player.sendMessage("Unknown tax command! See '/tax help' for all commands")
                return false
            }
        }

        return true
    }

    fun collectTaxes() {
        val offlinePlayers = Bukkit.getOfflinePlayers()

        var taxes = 0.0
        var receivingPlayer: Player? = null

        for (offlinePlayer in offlinePlayers) {
            if(offlinePlayer.player?.hasPermission("tax.collect") == true) {
                receivingPlayer = offlinePlayer.player
            } else {
                val balance = econ.getBalance(offlinePlayer)

                if (balance < 0) {
                    continue
                }

                val taxAmount = balance * percentage / 100

                econ.withdrawPlayer(offlinePlayer, taxAmount)
                taxes += taxAmount
            }
        }

        if (receivingPlayer != null) {
            econ.depositPlayer(receivingPlayer, taxes)

            Bukkit.getLogger().info("Completed tax transaction!")
            Bukkit.getLogger().info("Transferred $taxes to ${receivingPlayer.player?.name}")
            if (treasury > 0) {
                econ.depositPlayer(receivingPlayer, treasury)
                Bukkit.getLogger().info("Transferred deposited taxes in the amount of $treasury to ${receivingPlayer.player?.name}")
                treasury = 0.0
            }
        } else {
            treasury += taxes
            Bukkit.getLogger().info("No receiving player available! Deposited taxes in treasury.")
        }
    }
}