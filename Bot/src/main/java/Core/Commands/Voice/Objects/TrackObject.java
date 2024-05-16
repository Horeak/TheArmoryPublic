package Core.Commands.Voice.Objects;

public class TrackObject
{
	public PlayListInfo playlistInfo;
	public Track[] tracks = new Track[0];
	public TrackException exception;
	
	public boolean isPlayList(){
		return playlistInfo != null && playlistInfo.name != null;
	}
	
	public static class PlayListInfo{
		public String name;
	}
	
	public class Track{
		public String track;
		public TrackInfo info;
	}
	
	
	public static class TrackInfo{
		public String author;
		public Long length;
		public String title;
		public String uri;
	}
	
	public static class TrackException{
		public String message;
	}
}
