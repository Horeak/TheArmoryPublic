package Core.Data.Parsers;

import Core.Commands.Destiny.Models.DatabaseObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class DestinyDataParser<T extends DatabaseObject> extends TypeAdapter<T>
{
	private Class<T> type;
	
	public DestinyDataParser(Class<T> type)
	{
		this.type = type;
	}
	
	@Override
	public void write(JsonWriter out, T value) throws IOException
	{
	
	}
	
	@Override
	public T read(JsonReader in) throws IOException
	{
		return null;
	}
}
