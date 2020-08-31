package FabioRoberto.Backend;

import java.util.ArrayList;

public class User {
	private ArrayList <String> sportPref;
	private ArrayList <String> sciencePref;
	private ArrayList <String> cinemaPref;
	private ArrayList <String> musicPref;
	private ArrayList <String> newsPref;
	
	
	public User() {
		super();
	}

	public User(ArrayList<String> sportPref, ArrayList<String> sciencePref, ArrayList<String> cinemaPref,
			ArrayList<String> musicPref, ArrayList<String> newsPref) {
		super();
		this.sportPref = sportPref;
		this.sciencePref = sciencePref;
		this.cinemaPref = cinemaPref;
		this.musicPref = musicPref;
		this.newsPref = newsPref;
	}

	public ArrayList<String> getSportPref() {
		return sportPref;
	}

	public void setSportPref(ArrayList<String> sportPref) {
		this.sportPref = sportPref;
	}

	public ArrayList<String> getSciencePref() {
		return sciencePref;
	}

	public void setSciencePref(ArrayList<String> sciencePref) {
		this.sciencePref = sciencePref;
	}

	public ArrayList<String> getCinemaPref() {
		return cinemaPref;
	}

	public void setCinemaPref(ArrayList<String> cinemaPref) {
		this.cinemaPref = cinemaPref;
	}

	public ArrayList<String> getMusicPref() {
		return musicPref;
	}

	public void setMusicPref(ArrayList<String> musicPref) {
		this.musicPref = musicPref;
	}

	public ArrayList<String> getNewsPref() {
		return newsPref;
	}

	public void setNewsPref(ArrayList<String> newsPref) {
		this.newsPref = newsPref;
	}

}
