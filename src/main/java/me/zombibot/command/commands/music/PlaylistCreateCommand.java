package me.zombibot.command.commands.music;

import me.zombibot.Config;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PlaylistCreateCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();

        if (args.isEmpty()) {
            channel.sendMessage("Please provide a name for the playlist").queue();
            return;
        }
        else if (args.size() > 1) {
            channel.sendMessage("Please provide only one-word playlist titles for now (no whitespace)").queue();
            return;
        }
        try {

            File newPlaylist = new File(Config.get("playlist_files_init_path") + args.get(0) + ".txt");
            if (newPlaylist.createNewFile()) {
                channel.sendMessage("Successfully created new playlist: " + args.get(0)).queue();
            }
            else {
                channel.sendMessage("This playlist name already exists. Please choose another name").queue();
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "playlistcreate";
    }

    @Override
    public String getHelp() {
        return "Creates a new playlist" +
                "\nUsage: `zplaylistcreate [playlist name]`";
    }

    @Override
    public List<String> getAliases(){
        //return List.of("now", "playing", "np");
        return Arrays.asList("plc", "playlistc");
    }
}
