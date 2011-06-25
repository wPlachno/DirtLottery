package me.wPlachno.DirtLottery;

import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerListener;

public class DirtLotteryPlayerListener extends PlayerListener{
	
	public void onPlayerDropItem(PlayerDropItemEvent curEvent){
		if (curEvent.getItemDrop().getItemStack().getTypeId() == DirtLottery.tokenID){
			DirtLottery.tokens.add(new DirtLotteryTimerTask(curEvent.getItemDrop(), DirtLottery.delay, DirtLottery.tokens.size()));
		}
	}

}
