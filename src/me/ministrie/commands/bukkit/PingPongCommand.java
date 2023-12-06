package me.ministrie.commands.bukkit;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.ministrie.bukkit.BukkitMain;
import me.ministrie.net.impl.IClient;

public class PingPongCommand implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		// TODO Auto-generated method stub
		if(sender.isOp()){
			IClient client = BukkitMain.getService().getClient();
			if(client != null){
				if(client.getConnection() != null){
					client.getConnection().sendData("ping~!~!~!");
					Bukkit.getLogger().log(Level.WARNING, "[SocketFramework] try send to server. messages: ping~!~!~!");
				}else{
					Bukkit.getLogger().log(Level.WARNING, "[SocketFramework] client connection is null.");
				}
			}else{
				Bukkit.getLogger().log(Level.WARNING, "[SocketFramework] can't find NIO Service.");
			}
		}
		return false;
	}

}
