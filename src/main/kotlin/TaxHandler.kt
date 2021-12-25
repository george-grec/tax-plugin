package main

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TaxHandler(private var econ: Economy) {
    private var percentage = 10.0

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
                    if (args.size < 1) {
                        return false
                    }

                    this.collectTaxes()

                    player.sendMessage("Success!")
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
                }
            }
        }

        return true
    }

    fun collectTaxes() {
        val offlinePlayers = Bukkit.getOfflinePlayers()

        var taxes = 0.0
        var receiverPlayer: Player? = null

        for (offlinePlayer in offlinePlayers) {
            if(offlinePlayer.player!!.hasPermission("tax.collect")) {
                receiverPlayer = offlinePlayer.player
            } else {
                val balance = econ.getBalance(offlinePlayer)
                val taxAmount = balance * percentage / 100

                econ.withdrawPlayer(offlinePlayer, taxAmount)
                taxes += taxAmount
            }
        }

        econ.depositPlayer(receiverPlayer, taxes)
    }
}