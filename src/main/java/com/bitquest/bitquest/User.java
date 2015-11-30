package com.bitquest.bitquest;

import com.google.gson.JsonObject;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;

/**
 * Created by explodi on 11/6/15.
 */
public class User {

    private String clan;
    private Player player;
    public User(Player player) {
        this.player=player;
        loadUserData();
    }
    public void addExperience(int exp) {
        BitQuest.REDIS.incrBy("exp"+this.player.getUniqueId().toString(),exp);
    }
    public String getAddress() {
        return BitQuest.REDIS.get("address"+this.player.getUniqueId().toString());
    }
    public void generateBitcoinAddress() throws IOException, ParseException, org.json.simple.parser.ParseException {

        URL url = new URL("https://api.blockcypher.com/v1/btc/main/addrs");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        // System.out.println("\nSending 'POST' request to URL : " + url);
        // System.out.println("Post parameters : " + urlParameters);
        // System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        JSONParser parser = new JSONParser();
        final JSONObject jsonobj = (JSONObject) parser.parse(response.toString());
        BitQuest.REDIS.set("private"+this.player.getUniqueId().toString(), (String) jsonobj.get("private"));
        BitQuest.REDIS.set("public"+this.player.getUniqueId().toString(), (String) jsonobj.get("public"));
        BitQuest.REDIS.set("address"+this.player.getUniqueId().toString(), (String) jsonobj.get("address"));

    }
    private boolean loadUserData() {
        try {
            if (BitQuest.REDIS.get(player.getUniqueId().toString()) != null) {
                Bukkit.getLogger().info(BitQuest.REDIS.get(player.getUniqueId().toString()));
                return true;
            } else {
                // creates new player data entry and writes it to REDIS
                JsonObject playerData = new JsonObject();
                playerData.addProperty("exp", 0);
                BitQuest.REDIS.set(player.getUniqueId().toString(), playerData.toString());
                return true;
            }
        } catch(final Exception e) {
        	// Log the error.
        	Bukkit.getLogger().warning("Error saving "+player.getName()+"'s data: "+e.getLocalizedMessage());
        	Bukkit.getLogger().warning("Below are the details of the error:");
        	e.printStackTrace();
            return false;
        }
    }
    private boolean setClan(String tag) {
        // TODO: Write user clan info
        return false;
    }
}