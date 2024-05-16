package Core.Commands.Destiny.Models;

public class SocketObject
{
	public final String name;
	public final String description;
	public final int socketGroup;
	public Long hash;
	
	public SocketObject(String name, String description, int socketGroup, Long hash)
	{
		this.name = name;
		this.description = description;
		this.socketGroup = socketGroup;
		this.hash = hash;
	}
	
	public SocketObject(String name, String description, int socketGroup)
	{
		this.name = name;
		this.description = description;
		this.socketGroup = socketGroup;
	}
}