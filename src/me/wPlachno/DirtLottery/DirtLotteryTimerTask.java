package me.wPlachno.DirtLottery;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;

public class DirtLotteryTimerTask extends TimerTask{

	Timer timer;
	Item item;
	Item itemCopy;
	long delay;
	int idx;
	public DirtLotteryTimerTask(Item i, long ms, int idx){
		item = itemCopy = i;
		this.delay = ms;
		this.idx = idx;
		timer.schedule(this, ms);		
	}
	public void run() {
		if(item.getVelocity().equals(new Vector().zero())){
			if (isWater(item.getWorld().getBlockAt(item.getLocation()).getTypeId())){
				item.setItemStack(DirtLottery.getPrizeStack(item.getItemStack().getAmount()));
				List<Entity> Entities = DirtLottery.server.getWorld(item.getWorld().getName()).getEntities();
				for (int i = 0; i < Entities.size();i++){
					if (Entities.contains(itemCopy)){
						DirtLottery.server.getWorld(item.getWorld().getName()).getEntities().remove(itemCopy);
						DirtLottery.server.getWorld(item.getWorld().getName()).getEntities().add(item);
					}
					Entities.indexOf(item);
					
				}
			}
			timer.cancel();
			DirtLottery.tokens.remove(idx);
		} else {
			timer.schedule(this, delay);
		}
	}
	private boolean isWater(int typeID){
		if ((typeID == Material.WATER.getId())||(typeID == Material.STATIONARY_WATER.getId())){
			return true;
		}
		return false;
	}
}
