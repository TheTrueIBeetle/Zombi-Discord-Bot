package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.zombibot.Config;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import me.zombibot.util.Globals;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class PlaylistShowCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();

        if (args.isEmpty()) {
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
            else {
                channel.sendMessage("There is currently no playlists available").queue();
            }

            //Embed building
            int playlistCount = playListNames.size();


            EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                    .setTitle("**Playlists**")
                    .setDescription("**Playlist Count**: " + playlistCount + "\n\n")
                    .setColor(0x672f80)
                    .setFooter(Config.get("latest_update"));


            for (int i = 0; i < playlistCount; i++){
                String playlistName = playListNames.get(i);

                builder.appendDescription("**" + String.valueOf(i + 1) + ". " + "**");
                builder.appendDescription(playlistName + "\n");
            }

            channel.sendMessageEmbeds(builder.build()).queue();
        }
        else { //Read all lines from specific playlist
            if (args.size() > 1) {
                channel.sendMessage("Please provide only one argument: the playlist name").queue();
                return;
            }
            Scanner fileReader = null;
            ArrayList<String> songList = new ArrayList<String>();
            String playlistName = args.get(0);
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

            EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                    .setTitle("**" + playlistName + "**")
                    .setDescription("**Song Count**: " + songList.size() + "\n\n")
                    .setColor(0x672f80)
                    .setFooter(Config.get("latest_update"));


            for (int i = 0; i < songList.size(); i++){
                String songName = songList.get(i);

                builder.appendDescription("**" + String.valueOf(i + 1) + ". " + "**");
                builder.appendDescription(songName + "\n");
            }

            channel.sendMessageEmbeds(builder.build()).setActionRow(Button.success("queue_playlist", "Queue Up")).queue();
            Globals.currentPlaylistName = playlistName;

        }//end if
    }

    @Override
    public String getName() {
        return "playlistshow";
    }

    @Override
    public String getHelp() {
        return "Shows all the playlists created OR shows all songs within a given playlist" +
                "\nUsage: `zplaylistshow` OR `zplaylistshow [playlist name]`";
    }

    @Override
    public List<String> getAliases(){
        //return List.of("now", "playing", "np");
        return Arrays.asList("pls", "playlists", "playlistsshow");
    }
}
