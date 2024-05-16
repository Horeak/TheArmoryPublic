package Core.Util;

import java.util.List;

@FunctionalInterface
public interface AutoComplete{
	List<String> value(String value);
}