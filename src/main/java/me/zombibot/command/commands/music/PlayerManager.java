package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.zombibot.Config;
import me.zombibot.util.Globals;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

//JDBC
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PlayerManager { //Singleton

    private static PlayerManager INSTANCE;
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    private PlayerManager(){
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public synchronized GuildMusicManager getGuildMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) ->{
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.playerManager, guild);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void loadAndPlay(TextChannel channel, String trackUrl, boolean isAuto){
        final GuildMusicManager musicManager = this.getGuildMusicManager(channel.getGuild());

        this.playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
				
                musicManager.scheduler.queue(track);
                channel.sendMessage("\uD83C\uDFB6 Adding to queue: " + track.getInfo().title + " by " + track.getInfo().author).queue();

                try
                {
                    String filename = Config.get("resource_file_song_history");
                    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
                    fw.write(track.getInfo().title + " by " + track.getInfo().author + "\n");//appends the string to the file
                    fw.close();

                }
                catch(IOException ioe)
                {
                    System.err.println("IOException: " + ioe.getMessage());
                }

            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

                final List<AudioTrack> tracks = playlist.getTracks();
                boolean isSearch = false;
                if (trackUrl.contains("ytsearch:")) {
                    AudioTrack searchTrack = tracks.get(0);
                    musicManager.scheduler.queue(searchTrack);
                    channel.sendMessage("\uD83C\uDFB6")
                            .append("Adding to queue: ")
                            .append((CharSequence) searchTrack.getInfo().title)
                            .append(" by ")
                            .append(searchTrack.getInfo().author)
                            .queue();
                    isSearch = true;


                    //File insertion system
                    if (!isAuto) {
                        try
                        {
                            String filename= Config.get("resource_file_song_history");
                            FileWriter fw = new FileWriter(filename,true); //the true will append the new data
                            fw.write(searchTrack.getInfo().title + " by " + searchTrack.getInfo().author + "\n");//appends the string to the file
                            fw.close();
                        }
                        catch(IOException ioe)
                        {
                            System.err.println("IOException: " + ioe.getMessage());
                        }
                    }
                }

                if (!isSearch) {
                    channel.sendMessage("Adding to queue: ")
                            .append(String.valueOf(tracks.size()))
                            .append(" tracks from playlist ")
                            .append(playlist.getName())
                            .queue();
                }

                if (!isSearch) {
                    for (final AudioTrack track : tracks) {
                        musicManager.scheduler.queue(track);
                    }
                }
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found for: " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not successfully play: " + exception.getMessage()).queue();
            }
        });

    }

    private void play(GuildMusicManager musicManager, AudioTrack track){
        musicManager.scheduler.queue(track);
    }

    private String formatTime(long timeInMillis){
        final long hours = timeInMillis / TimeUnit.HOURS.toMillis(1);
        final long minutes = timeInMillis / TimeUnit.MINUTES.toMillis(1);
        final long seconds = timeInMillis % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static synchronized PlayerManager getInstance(){
        if (INSTANCE == null){
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }
}
