package Core.Commands.Destiny.system;

import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Fields.Save;
import Core.Objects.Annotation.Method.Interval;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.Annotation.Method.Startup.PostInit;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DestinyUpdateSystem
{
	private static final String folderName = "destiny";
	private static final String name_d2 = "world_d2.db";
	public static File infoFile_d2;
	
	@Save( folderName + "/destinyInfo.json")
	public static String destiny_manifest;
	
	@Save( folderName + "/destinyInfo.json")
	public static String destiny_version;
	
	@Save( folderName + "/destinyInfo.json")
	public static Long last_destiny_update;
	private static boolean updatedQueued = false;

	@Init
	public static void DestinyItemCommandInit()
	{
		File folderFe = new File(Startup.FilePath + File.separator + folderName + File.separator);
		folderFe.mkdirs();
		
		infoFile_d2 = new File( folderFe, File.separator + name_d2);

		if (!updatedQueued) {
			updatedQueued = true;
		}
	}

	@PostInit
	@Interval(time_interval = 1, time_unit = TimeUnit.HOURS, initial_delay = 1)
	public static void updateCheck()
	{
		if(!updatedQueued){
			DestinyItemCommandInit();
		}

		boolean d2System = false;

		try {
			JSONObject destiny_object = DestinySystem.getResponse("https://www.bungie.net/Platform/Destiny2/Manifest/");

			if (destiny_object.has("Response")) {
				JSONObject ob2 = destiny_object.getJSONObject("Response");
				String version = ob2.getString("version");

				if (ob2.has("mobileWorldContentPaths")) {
					JSONObject object = ob2.getJSONObject("mobileWorldContentPaths");

					if(object.has("en")) {
						String manifest = object.get("en").toString();

						if (manifest != null) {
							if (destiny_manifest == null || !destiny_manifest.equals(manifest) && (destiny_version == null || !destiny_version.equalsIgnoreCase(version))) {
								System.out.println("New D2 manifest version: " + version + (destiny_version != null ? ", Old: " + destiny_version : ""));

								updateManifest(manifest);
								
								if(version != null) {
									destiny_version = version;
								}
								
								d2System = true;
							}
						}
					}
				}
			}

		} catch (IOException e) {
			Logging.exception(e);
		}

		try {
			if (!d2System) {
				DestinyItemSystem.init();
			}
		} catch (Exception e) {
			Logging.exception(e);
		}
	}

	private static void updateManifest(String manifest)
	throws IOException
	{
		System.out.println("Destiny 2 manifest update begin");
		
		File folder = new File(Startup.FilePath + File.separator + folderName);
		File tempZipFile = new File(folder, "temp.zip");
		
		folder.mkdirs();
		tempZipFile.createNewFile();

		URL website = new URL(DestinySystem.BASE_RESOURCE_URL + manifest);
		FileUtils.copyURLToFile(website, tempZipFile);
		
		unZipIt(tempZipFile, infoFile_d2);
		tempZipFile.delete();
		
		destiny_manifest = manifest;
		last_destiny_update = System.currentTimeMillis();

		DestinyItemSystem.reInit();

		System.out.println("Destiny 2 manifest update done");
	}
	
	public static void unZipIt(File in, File out) {
		if (in == null || out == null) {
			return;
		}
		
		byte[] buffer = new byte[1024];
		
		try (
				FileInputStream fileInputStream = new FileInputStream(in);
				ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
		) {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			
			while (zipEntry != null) {
				try (FileOutputStream fileOutputStream = new FileOutputStream(out)) {
					int len;
					while ((len = zipInputStream.read(buffer)) > 0) {
						fileOutputStream.write(buffer, 0, len);
					}
				}
				zipEntry = zipInputStream.getNextEntry();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}