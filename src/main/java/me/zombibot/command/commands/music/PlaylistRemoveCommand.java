package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.zombibot.Config;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import me.zombibot.util.Globals;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.print.attribute.IntegerSyntax;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class PlaylistRemoveCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(ctx.getGuild());
        AudioPlayer player = musicManager.player;

        if (args.size() < 2) {
            channel.sendMessage("Please provide the playlist name as well as the track number").queue();
            return;
        }
        else if (args.size() > 2) {
            channel.sendMessage("Please only provide the playlist name and track number").queue();
            return;
        }

        //Read from playlist file and put into list
        Scanner fileReader = null;
        ArrayList<String> songList = new ArrayList<String>();
        String playlistName = args.get(0);
        String trackNumStr = args.get(1);
        int trackNum = 0;
        try {
            trackNum = Integer.parseInt(trackNumStr);
        }
        catch (NumberFormatException ex) {
            channel.sendMessage("Please provide a number to represent the track number").queue();
            ex.printStackTrace();
            return;
        }

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

        //Remove from the list and then rewrite the file with new list
        songList.remove(trackNum - 1);
        try
        {
            String filename = Config.get("playlist_files_init_path") + playlistName + ".txt";
            FileWriter fw = new FileWriter(filename,false);
            fw.write("");
            fw.close();
            FileWriter fw2 = new FileWriter(filename, true);
            for (String str : songList) {
                fw2.write(str + "\n");
            }
            fw2.close();
        }
        catch(IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }

        channel.sendMessage("Successfully removed specified track from the playlist " + playlistName).queue();
    }

    @Override
    public String getName() {
        return "playlistremove";
    }

    @Override
    public String getHelp() {
        return "Removes a specified track number from a specified playlist. " +
                "\nUsage: zplaylistremove [playlist name] [track number]";
    }

    @Override
    public List<String> getAliases(){
        return Arrays.asList("plr", "playlistr");
    }
}
