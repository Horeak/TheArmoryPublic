package Core.Util;

import Core.Main.Logging;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;

public class FileUtil
{
	public static void addLineToFile(File file, String lineAdd)
	{
		if (file == null) {
			return;
		}

		try {
			ArrayList<String> strings = new ArrayList<>();
			BufferedReader reader = new BufferedReader(new FileReader(file));

			String currentLine;
			while ((currentLine = reader.readLine()) != null) {
				strings.add(currentLine);
			}

			strings.add(lineAdd);
			reader.close();

			FileUtils.writeLines(file, strings);

		} catch (Exception e) {
			if (e instanceof FileNotFoundException) return;

			Logging.exception(e);
		}

	}

	public static File getFile(String path)
	{
		File file = new File(path);
		File folder = new File(file.getPath().replace(file.getName(), ""));

		if (!folder.exists()) {
			folder.mkdirs();
		}

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				Logging.exception(e);
			}
		}

		return file;
	}

	public static File getFolder(String path)
	{
		File file = new File(path);

		if (!file.exists() || file.isFile()) {
			file.mkdirs();
			file.mkdir();
		}

		return file;
	}
}