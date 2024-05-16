package Core.Commands.Destiny.User.Objects;

public enum AccountTypes
{
	STEAM("Steam"),
	XBOX("XBOX"),
	PSN("PSN");
	
	public final String name;
	AccountTypes(String name)
	{
		this.name = name;
	}
}
