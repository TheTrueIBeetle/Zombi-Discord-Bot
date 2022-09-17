package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.zombibot.Config;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

//Java.net stuff
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import net.dv8tion.jda.api.interactions.components.Button;
import org.json.*;

public class NowPlayingCommand implements ICommand {

    private static List<String> list = new ArrayList<>();

    public synchronized static void parseObject(JSONObject json, String key) {
        list.add(json.get(key).toString());
    }

    public synchronized static void getKey(JSONObject json, String key) {
        boolean exists = json.has(key);
        Iterator<?> keys;
        String nextKeys;
        if (!exists) {
            keys = json.keys();
            while (keys.hasNext()) {
                nextKeys = (String)keys.next();
                try {
                    if (json.get(nextKeys) instanceof JSONObject) {
                        if (exists == false) {
                            getKey(json.getJSONObject(nextKeys), key);
                        }
                    }
                    else if (json.get(nextKeys) instanceof JSONArray) {
                        JSONArray jsonarray = json.getJSONArray(nextKeys);
                        for (int i = 0; i < jsonarray.length(); i++) {
                            String str = jsonarray.get(i).toString();
                            JSONObject innerJson = new JSONObject(str);

                            if (exists == false) {
                                getKey(innerJson, key);
                            }
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            parseObject(json, key);
        }
    }

    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();


        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(ctx.getGuild());
        AudioPlayer player = musicManager.player;

        if (player.getPlayingTrack() == null) {
            channel.sendMessage("The player is not currently playing any song").queue();
            return;
        }

        //NOTE: there is 500 api call limit on the api per month (free subscription)
        channel.sendMessage("Give me a second...").queue();
        AudioTrackInfo info = player.getPlayingTrack().getInfo();

        String inputStr = info.title;

        inputStr = inputStr.substring(0, inputStr.length() / 2 - 5);

        String[] tokens = inputStr.split(" ");

        //Construct
        StringBuilder sb = new StringBuilder();
        sb.append("https://shazam.p.rapidapi.com/search?term=");
        for (int i = 0; i < tokens.length; i++) {
            sb.append(tokens[i]);
            if (i == tokens.length - 1) { //Avoids appending final %20
                break;
            }
            sb.append("%20");
        }
        sb.append("&locale=en-US&offset=0&limit=5");
        String uri = sb.toString();
        System.out.println(uri);

        HttpResponse<String> response = null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("X-RapidAPI-Key", Config.get("shazam_api_key"))
                    .header("X-RapidAPI-Host", "shazam.p.rapidapi.com")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (IOException | InterruptedException | IllegalArgumentException ex) {
            ex.printStackTrace();
            EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                    .setTitle("**Playing**: " + "[" + info.title + "]" + "\n**By**: " + "[" + info.author + "]")
                    .setDescription(info.uri + "\n" + (player.isPaused() ? "\u23F8" : "▶") + " " + formatTime(player.getPlayingTrack().getPosition())
                            + " - " + formatTime(player.getPlayingTrack().getDuration()))
                    .setColor(0x672f80)
                    .setFooter(Config.get("latest_update"));
            channel.sendMessageEmbeds(builder.build()).setActionRow(Button.link(info.uri, "See On Youtube")).queue();
            return;
        }


        if (response.body().equals("{}")) {
            System.out.println("Couldn't find data");
            EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                    .setTitle("**Playing**: " + "[" + info.title + "]" + "\n**By**: " + "[" + info.author + "]")
                    .setDescription(info.uri + "\n" + (player.isPaused() ? "\u23F8" : "▶") + " " + formatTime(player.getPlayingTrack().getPosition())
                            + " - " + formatTime(player.getPlayingTrack().getDuration()))
                    .setColor(0x672f80)
                    .setFooter(Config.get("latest_update"));
            channel.sendMessageEmbeds(builder.build()).setActionRow(Button.link(info.uri, "See On Youtube")).queue();
        }
        else {
            JSONObject inputJsonObj = null;
            try {
                inputJsonObj = new JSONObject(response.body());
            }
            catch (JSONException ex) { //Just give default now playing if this catches
                ex.printStackTrace();
                EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                        .setTitle("**Playing**: " + "[" + info.title + "]" + "\n**By**: " + "[" + info.author + "]")
                        .setDescription(info.uri + "\n" + (player.isPaused() ? "\u23F8" : "▶") + " " + formatTime(player.getPlayingTrack().getPosition())
                                + " - " + formatTime(player.getPlayingTrack().getDuration()))
                        .setColor(0x672f80)
                        .setFooter(Config.get("latest_update"));
                channel.sendMessageEmbeds(builder.build()).setActionRow(Button.link(info.uri, "See On Youtube")).queue();
                //Link buttons do not need a listener, they work on their own

                list.clear();
                return; //Stop the handle here
            }

            getKey(inputJsonObj, "subtitle");
            getKey(inputJsonObj, "image");


            //Put the image links in separate container
            List<String> artistArr = new ArrayList<>();
            List<String> linkArr = new ArrayList<>();
            for (String str : list) {
                if (str.startsWith("https")) {
                    linkArr.add(str);
                }
                else {
                    artistArr.add(str);
                }
            }


            String artist = artistArr.get(0);
            String thumbnail = linkArr.get(1);

            EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                    .setTitle("**Playing**: " + "[" + info.title + "]" + "\n**Artist**: " + "[" + artist + "]" +
                            "\n**Author / Releaser**: " + "[" + info.author + "]")
                    .setDescription(info.uri + "\n" + (player.isPaused() ? "\u23F8" : "▶") + " " + formatTime(player.getPlayingTrack().getPosition())
                            + " - " + formatTime(player.getPlayingTrack().getDuration()))
                    .setColor(0x672f80)
                    .setThumbnail(thumbnail)
                    .setFooter(Config.get("latest_update"));
            channel.sendMessageEmbeds(builder.build()).setActionRow(Button.link(info.uri, "See On Youtube")).queue();

            //Cleanup
            list.clear();

        }//end if

    }

    @Override
    public String getName() {
        return "nowplaying";
    }

    @Override
    public String getHelp() {
        return "Shows the currently playing track";
    }

    @Override
    public List<String> getAliases(){
        //return List.of("now", "playing", "np");
        return Arrays.asList("now", "playing", "np", "song");
    }

    public String formatTime(long timeInMillis) {
        final long hours = timeInMillis / TimeUnit.HOURS.toMillis(1);
        final long minutes = timeInMillis / TimeUnit.MINUTES.toMillis(1);
        final long seconds = timeInMillis % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
