package Core.Commands.Destiny.Models;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class DestinyBaseItemObject extends DatabaseDisplayObject
{
	public String dlc;
	public String source;
	
	public abstract String getName();
	public abstract String getDescription();
	public abstract String getIcon();
	
	public abstract String getImage();
	public abstract int getItemTier();
	public abstract String getItemTierAndType();
	public abstract boolean isEquippable();
	
	public abstract HashMap<Long, BaseStatObject> getStats();
	
	public abstract HashMap<String, String> getInfo();
	
	public abstract HashMap<Integer, ArrayList<SocketObject>> getPerks();
	public abstract void finalizeObject();
}
