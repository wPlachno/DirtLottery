package me.wPlachno.DirtLottery;

public class DirtLotteryPrize {
	float low;
	float high;
	int typeID;
	public DirtLotteryPrize(float low, float high, int typeID){
		this.low = low;
		this.high = high;
		this.typeID = typeID;
	}
	public float probability(){
		return high - low;
	}
	public boolean isPrize(float f){
		if ((f >= low)&&(f<high)){
			return true;
		}
		return false;
	}
}
