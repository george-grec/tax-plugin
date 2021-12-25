package main

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TaxHandler(private var econ: Economy) {
    private var percentage = 10

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
                    // usage: /tax collect 10 [playername]
                    if (args.size < 2) {
                        return false
                    }

                    this.collectTaxes()

                    player.sendMessage("Success!")
                }
            }
            "setPercentage" -> {
                if (player.hasPermission("tax.collect")) {
                    if (args.size < 2) {
                        return false
                    }

                    percentage = args[2].toInt()
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