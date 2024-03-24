package com.example.spotifywrapped;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class JSONParser {
    public static void parseTopArtist(JSONObject jObject) throws JSONException {

        JSONArray jsonArtists = jObject.getJSONArray("items");

        ArrayList<String> artistList = new ArrayList<>();

        for (int i = 0; i < jsonArtists.length(); i++) {
            JSONObject currArtist = jsonArtists.getJSONObject(i);
            artistList.add(currArtist.getString("name"));
            artistList.add(currArtist.getJSONArray("images").getJSONObject(0).getString("url"));
        }
        storeList("artist", artistList);
    }



    private static void storeList(String key, ArrayList<?> value) {
        DatabaseReference fbDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        String newChild = "top ".concat(key).concat("s");

        DatabaseReference currReference = fbDatabase.child("Users").child(auth.getUid().toString()).child(newChild);

        if (key.equals("artist")) {
            for (int i = 0; i < value.size()/2; i++) {
                String currArtist = (String) value.get(i * 2);
                String currImage = (String) value.get((i*2) + 1);

                currReference.child(key.concat(Integer.toString(i))).child("artist").setValue(currArtist);
                currReference.child(key.concat(Integer.toString(i))).child("url").setValue(currImage);
            }

        } else if (key.equals("song")){
            for (int i = 0; i < value.size()/2; i++) {
                ArrayList<Track> songList = (ArrayList<Track>) value;
                String currSong = songList.get(i).getTrackName();
                String currArtist = songList.get((i)).getArtistName();
                String currImage = songList.get((i)).getImageURL();

                currReference.child(key.concat(Integer.toString(i))).child("song").setValue(currSong);
                currReference.child(key.concat(Integer.toString(i))).child("artist").setValue(currArtist);
                currReference.child(key.concat(Integer.toString(i))).child("image").setValue(currImage);
            }

        } else if (key.equals("album")) {
            for (int i = 0; i < value.size(); i++) {
                ArrayList<Track> albumList = (ArrayList<Track>) value;
                String currAlbum = albumList.get(i).getTrackName();
                String currArtist = albumList.get((i)).getArtistName();
                String currImage = albumList.get((i)).getImageURL();
                currReference.child(key.concat(Integer.toString(i))).child("album").setValue(currAlbum);
                currReference.child(key.concat(Integer.toString(i))).child("artist").setValue(currArtist);
                currReference.child(key.concat(Integer.toString(i))).child("url").setValue(currImage);
            }
        }
    }



    public static void parseTopSongs(JSONObject jObject) throws JSONException {

        // all tracks are held in an array under the key "items"
        JSONArray jsonSongs = jObject.getJSONArray("items");

        // I made it so that all songs are stored in even indexes, and songs in odd - they alternate
        // so the song name goes first, then the person who made it
        ArrayList<Track> songList = new ArrayList<>();

        for (int i = 0; i < jsonSongs.length(); i++) {
            JSONObject currTrack = jsonSongs.getJSONObject(i);
            String currSong = currTrack.getString("name");
            String currArtist = currTrack.getJSONArray("artists").getJSONObject(0).getString("name");
            String currImage = currTrack.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");

            Track newSong = new Track(currArtist, currSong, currImage);
            songList.add(newSong);
        }

        storeList("song", songList);
    }

    public static void parseTopAlbums(JSONObject jObject) throws JSONException{

        JSONArray jsonTracks = jObject.getJSONArray("items");

        HashMap<Track, Integer> topTrackMap = new HashMap<>();

        for (int i = 0; i < jsonTracks.length(); i++) {
            JSONObject currTrack = jsonTracks.getJSONObject(i).getJSONObject("album");

            String currAlbum = currTrack.getString("name");
            String currImage = currTrack.getJSONArray("images").getJSONObject(0).getString("url");
            String currArtist = currTrack.getJSONArray("artists").getJSONObject(0).getString("name");

            Track newAlbum = new Track(currArtist, currAlbum, currImage);

            topTrackMap.put(newAlbum, topTrackMap.getOrDefault(newAlbum, 0) + 1);
        }

        PriorityQueue<Map.Entry<Track, Integer>> maxHeap = new PriorityQueue<>(
                (entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        maxHeap.addAll(topTrackMap.entrySet());

        int count = 0;

        ArrayList<Track> topAlbumList = new ArrayList<>();

        while (!maxHeap.isEmpty() && count < 10) {
            Map.Entry<Track, Integer> entry = maxHeap.poll();
            topAlbumList.add(entry.getKey());
            count++;
        }

        storeList("album", topAlbumList);
    }
}