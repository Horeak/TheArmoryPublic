package Core.Commands.Destiny.Models;

public abstract class BaseStatObject
{
	public int minimum;
	public int maximum;
	public Long statHash;
	public int value;
	public int displayMaximum;
	public int oldValue = -1;
	public boolean displayAsNumeric = false;
	
	public abstract String getName();
	public abstract String getDescription();
}
