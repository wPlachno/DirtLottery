package me.wPlachno.DirtLottery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;


import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class DirtLottery extends JavaPlugin{

	public static Logger log = Logger.getLogger("Minecraft");
	public static Server server;	
	public static String logPrefix = "[DirtLottery] ";
	String version = "0.0.1";
	public static String pluginMainDir = "./plugins/dirtLottery";		
	public static Random generator;
	public static Material mat;
	public static int tokenID;
	public static long delay;
	private File cfgFile;
	public DirtLotteryPlayerListener pListener;
	public static ArrayList<DirtLotteryTimerTask> tokens = new ArrayList<DirtLotteryTimerTask>();
	public static ArrayList<DirtLotteryPrize> prizes = new ArrayList<DirtLotteryPrize>();

	public void onDisable() {
		logIt("Plugin disabled.");
	}

	public void onEnable() {
		server = this.getServer();
		cfgFile = new File(pluginMainDir+"/config.yml");
		cfgFile.getParentFile().mkdirs();
		if (cfgFile.exists()){
			logIt("cfgFile found.");
			ArrayList<DirtLotteryPrize> prizesBuf = new ArrayList<DirtLotteryPrize>();
			boolean loaded = this.loadConfig(cfgFile, prizes);
			if (loaded == false){
				logIt("Unable to load the config file");
				buildDefaults();
			}
			else {
				DirtLottery.prizes = prizesBuf;
				logIt("Successfully loaded the config file.");
			}
		}
		else {
			//load default classes
			logIt("No cfg found. Loading defaults.");
			buildDefaults();
		}
		pListener = new DirtLotteryPlayerListener();
		PluginManager pm = server.getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, pListener, Event.Priority.Normal, this);
		logIt("Plugin enabled. 4 8 16 23 42");
	}
	private void buildDefaults() {
		ArrayList<DirtLotteryPrize> prizesBuf = new ArrayList<DirtLotteryPrize>();
		DirtLotteryPrize curPrize = new DirtLotteryPrize((float)0.0, (float)80.0, 2);
		prizesBuf.add(curPrize);
		curPrize = new DirtLotteryPrize((float) 80.0,(float) 81.5, 266);
		prizesBuf.add(curPrize);
		curPrize = new DirtLotteryPrize((float) 81.5, (float) 88.0, 89);
		prizesBuf.add(curPrize);
		curPrize = new DirtLotteryPrize((float) 88.0, (float) 90.0, 14);
		prizesBuf.add(curPrize);
		curPrize = new DirtLotteryPrize((float) 90.0, (float) 98.0, 35);
		prizesBuf.add(curPrize);
		curPrize = new DirtLotteryPrize((float) 98.0, (float) 99.7, 265);
		prizesBuf.add(curPrize);
		curPrize = new DirtLotteryPrize((float) 99.7, (float) 100.0, 57);
		prizesBuf.add(curPrize);
		DirtLottery.prizes = prizesBuf;
		DirtLottery.tokenID = 3;
		DirtLottery.delay = 750;
		saveConfig(cfgFile);
	}
	private void saveConfig(File config){
		try{
			BufferedWriter bWrite = new BufferedWriter(new FileWriter(config));
			Line("token type ID: " + DirtLottery.tokenID, bWrite);
			Line("water Delay (ms): " + DirtLottery.delay, bWrite);
			for (int i = 0; i < DirtLottery.prizes.size();i++){
				DirtLotteryPrize buffer = DirtLottery.prizes.get(i);
				String Name = Material.getMaterial(buffer.typeID).name();
				Line(i+": "+Name, bWrite);
				Line(Name + " Type ID: " + buffer.typeID, bWrite);
				Line(Name + " Odds: " + buffer.probability(), bWrite);
			}
			bWrite.flush();
			bWrite.close();			
		} catch (IOException ex) {
			logIt("Could not save config file. Meh.");
		}
	}
	private void Line(String msg, BufferedWriter bWrite) throws IOException{
		bWrite.write(msg);
		bWrite.newLine();
	}

	private boolean loadConfig(File cfgFile2, ArrayList<DirtLotteryPrize> prizes2) {
		try{
			BufferedReader bRead = new BufferedReader(new FileReader(cfgFile2));
			ArrayList<Float> rawNums = new ArrayList<Float>();
			ArrayList<Integer> typeIDs = new ArrayList<Integer>();
			tokenID = getNextInt(bRead);
			delay = (long)getNextInt(bRead);
			String buffer = new String();
			buffer = getNextLine(bRead);
			Float total = (float)0.0;
			while (buffer != "END OF FILE"){
				typeIDs.add((Integer)(getNextInt(bRead)));
				Float bufInt = Float.parseFloat(getNextValue(bRead));
				total += bufInt;
				rawNums.add(bufInt);
				buffer = getNextLine(bRead);
			}
			bRead.close();
			float curLow = 0;
			for (int i=0; i < rawNums.size(); i++){
				float curHigh = ((100*((float)rawNums.get(i))/((float)total))+curLow);
				DirtLotteryPrize curPrize = new DirtLotteryPrize(curLow, curHigh, typeIDs.get(i));
				prizes2.add(curPrize);
				curLow = curHigh;			
			}
			saveConfig(cfgFile);
			return true;
		} catch (IOException ex) {
			logIt("Could not load the config file. Loading defaults.");
			buildDefaults();
		}
		return false;
	}
	/**
	 * gets the next line from the given reader. Ignores comment lines and trims whats left
	 * @param bRead: the reader we will be reading from.
	 * @return String: The string representing the line we got from the reader
	 * @throws IOException: This function does use the reader.
	 */
	public String getNextLine(BufferedReader bRead) throws IOException{
		String buffer;
		while ((buffer = bRead.readLine()) != null){
			if((!buffer.contains("#"))&&(buffer.contains(":"))){
				return buffer.trim();
			}
		}
		return "END OF FILE";
	}
	
	/**
	 * Gets the String value of the next line from the reader. Checks for the ':' character
	 * @param bRead: the reader to use when reading the value from
	 * @return String: the string representing whatever was after the ':' char
	 * @throws IOException: See getNextLine
	 */
	public String getNextValue(BufferedReader bRead) throws IOException{
		String buffer;
		buffer = getNextLine(bRead);
		if (buffer == "END OF FILE"){
			throw new IOException();
		}
		else{
			int assignIdx = buffer.indexOf(":");
			return buffer.substring(assignIdx+1);
		}
	}
	
	/**
	 * Gets the int value of the nextline from the reader.
	 * @param bRead: the reader to read from.
	 * @return int: the int we found
	 * @throws IOException: See getNextLine
	 */
	public int getNextInt (BufferedReader bRead) throws IOException{
		String buffer = getNextValue(bRead);
		return Integer.parseInt(buffer.trim());
	}
	

	public void logIt(String msg){
		log.info(logPrefix+msg);
	}

	public static ItemStack getPrizeStack(int amt){
		float rand = generator.nextFloat();
		rand = rand % (float)100;
		int typeID = 0;
		for (int i = 0; i < DirtLottery.prizes.size(); i++){
			if (DirtLottery.prizes.get(i).isPrize(rand)){
				typeID = DirtLottery.prizes.get(i).typeID;
			}
		}
		ItemStack curPrizeStack = new ItemStack(typeID, amt);
		return curPrizeStack;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		 
		 if(cmd.getName().equalsIgnoreCase("dirtlottery")){ // If the player typed /basic then do the following...
		   logIt(DirtLottery.logPrefix + this.version);
		   logIt("Drop some " + Material.getMaterial(DirtLottery.tokenID) + " into water to play the lottery!");
		   logIt("See what kind of prizes you can get!");
		   logIt("I heard a rumor about gold hidden in fountains...");
		   return true;
		 } //If this has happened the function will break and return true. if this hasn't happened the a value of false will be returned.
		 return false; 
		}
}
