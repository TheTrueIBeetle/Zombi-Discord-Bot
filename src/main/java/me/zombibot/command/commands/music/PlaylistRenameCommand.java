package me.zombibot.command.commands.music;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.zombibot.Config;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class PlaylistRenameCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();

        if (args.size() < 2) {
            channel.sendMessage("Please provide both the old playlist name, as well as the new name").queue();
            return;
        }
        else if (args.size() > 2) {
            channel.sendMessage("Please do not use whitespace in the playlist name.").queue();
            return;
        }

        String playlistName = args.get(0);
        String newPlaylistName = args.get(1);


        Scanner fileReader = null;
        try {
            File songFile = new File(Config.get("playlist_files_init_path") + playlistName + ".txt");
            fileReader = new Scanner(songFile);
            ArrayList<String> songList = new ArrayList<String>();

            while (fileReader.hasNext()) {
                String theLine = fileReader.nextLine();
                songList.add(theLine);
            }

            fileReader.close();

            if (songFile.delete()) {
                File newPlaylist = new File(Config.get("playlist_files_init_path") + newPlaylistName + ".txt");
                if (newPlaylist.createNewFile()) {
                    FileWriter fw = new FileWriter(newPlaylist,true); //the true will append the new data

                    for (String song : songList) {
                        fw.write(song + "\n");//appends the string to the file
                    }
                    fw.close();
                    channel.sendMessage("Successfully renamed the playlist: " + playlistName + " to: " + newPlaylistName).queue();
                }
                else {
                    channel.sendMessage("Something went wrong when trying to rename the playlist. Please try again").queue();
                }
            }
            else {
                channel.sendMessage("Something went wrong when trying to rename the playlist. Please try again").queue();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    @Override
    public String getName() {
        return "playlistrename";
    }

    @Override
    public String getHelp() {
        return "Renames a specified playlist to given name" +
                "\nUsage: `zplaylistrename [existing playlist name] [new playlist name]`";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("plrn", "playlistrn");
    }
}
