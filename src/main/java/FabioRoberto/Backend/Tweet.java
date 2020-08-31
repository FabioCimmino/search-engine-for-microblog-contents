package FabioRoberto.Backend;

public class Tweet {

	private String tweet;
	private String url;
	private String date;
	private int retweet;
	private int likes;
	
	public Tweet(String tweet, String url, String date,int retweet,int likes) {
		super();
		this.tweet = tweet;
		this.url = url;
		this.date = date;
		this.retweet=retweet;
		this.likes=likes;
		
	}
	
	public int getRetweet() {
		return retweet;
	}

	public void setRetweet(int retweet) {
		this.retweet = retweet;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}


	public String getTweet() {
		return tweet;
	}

	public void setTweet(String tweet) {
		this.tweet = tweet;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	
	
}
