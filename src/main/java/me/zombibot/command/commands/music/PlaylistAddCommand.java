package me.zombibot.command.commands.music;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.zombibot.Config;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import me.zombibot.util.Globals;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class PlaylistAddCommand implements ICommand {

    private EventWaiter waiter = null;

    public PlaylistAddCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(ctx.getGuild());
        AudioPlayer player = musicManager.player;

        if (player.getPlayingTrack() == null) {
            channel.sendMessage("There is currently no song playing to add to a playlist").queue();
            return;
        }

        AudioTrackInfo info = player.getPlayingTrack().getInfo();

        if (args.isEmpty()) {
            channel.sendMessage("Please provide a valid playlist name to add to").queue();
            return;
        }

        String playlistName = args.get(0);

        String[] pathnames;
        ArrayList<String> playListNames = new ArrayList<String>();
        File file = new File(Config.get("playlist_files_path"));
        pathnames = file.list();

        if (pathnames != null) {
            for (String p : pathnames) {
                int indexToStop = p.indexOf(".");
                String newStr = p.substring(0, indexToStop);
                playListNames.add(newStr);
            }
        }

        if (!playListNames.contains(playlistName)) {
            channel.sendMessage("There is no such playlist. Please create a new playlist to add music to").queue();
            return;
        }

        //Check if track requested is already within the playlist
        Scanner fileReader = null;
        ArrayList<String> songList = new ArrayList<String>();
        try {
            File playlistFile = new File(Config.get("playlist_files_init_path") + playlistName + ".txt");
            fileReader = new Scanner(playlistFile);

            while (fileReader.hasNext()) {
                String theLine = fileReader.nextLine();
                songList.add(theLine);
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        finally {
            if (fileReader != null) {
                fileReader.close();
            }
        }

        String songName = info.title + " by " + info.author;
        if (songList.contains(songName)) {
            channel.sendMessage("The playlist " + playlistName + " already contains the requested song. Do you still" +
                    " want to add it?").setActionRow(Button.success("btn_add_anyway", "Add It Anyway"),
                                                     Button.danger("btn_cancel_add", "Cancel")).queue((message) -> {
														 
                        this.waiter.waitForEvent(
                                ButtonClickEvent.class, (e) -> e.getMessageIdLong() == message.getIdLong() && !e.getUser().isBot(),
                                (e) -> {
                                    try {
                                        switch (e.getComponentId()) {
                                            case "btn_add_anyway":
                                                try
                                                {
                                                    String filename = Config.get("playlist_files_init_path") + playlistName + ".txt";
                                                    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
                                                    fw.write(info.title + " by " + info.author + "\n");//appends the string to the file
                                                    fw.close();
                                                    e.reply("Successfully added the current song to playlist: " + playlistName).queue();
                                                }
                                                catch(IOException ioe)
                                                {
                                                    System.err.println("IOException: " + ioe.getMessage());
                                                    e.reply("Failed to add current song to playlist. Please try again").queue();
                                                }
                                                break;
                                            case "btn_cancel_add":
                                                e.reply("Ok, I canceled the add request").queue();
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                    catch (IllegalStateException ex) {
                                        System.out.println("Cancel btn has been selected");
                                    }
                                },
                                1L, TimeUnit.MINUTES,
                                () -> channel.sendMessage("You waited too long to respond..").queue()
                        );
            });
            return;
        }

        //Write to file the current track if it does not already contain it
        try
        {
            String filename = Config.get("playlist_files_init_path") + playlistName + ".txt";
            FileWriter fw = new FileWriter(filename,true); //the true will append the new data
            fw.write(info.title + " by " + info.author + "\n");//appends the string to the file
            fw.close();
            channel.sendMessage("Successfully added the current song to playlist: " + playlistName).queue();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
            channel.sendMessage("Failed to add current song to playlist. Please try again").queue();
        }
    }

    @Override
    public String getName() {
        return "playlistadd";
    }

    @Override
    public String getHelp() {
        return "Adds the current song playing to a given playlist" +
                "\nUsage: `zplaylistadd [playlist name]`";
    }

    @Override
    public List<String> getAliases(){
        //return List.of("now", "playing", "np");
        return Arrays.asList("pla", "playlista");
    }

}
