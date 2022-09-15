package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.zombibot.Config;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import me.zombibot.util.Globals;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class BulkCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {

        final List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();
        final Member self = ctx.getSelfMember();
        final Member member = ctx.getMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        final VoiceChannel memberChannel = memberVoiceState.getChannel();
        AudioManager audioManager = ctx.getGuild().getAudioManager();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(ctx.getGuild());
        AudioPlayer player = musicManager.player;

        //Check permission first
        if (!self.hasPermission(Permission.VOICE_CONNECT)) {
            channel.sendMessage("I don't have permission to join voice channels in this server. You must enable me to do so").queue();
        }

        //Join channel if possible (must not be in another voice channel currently)
        if(self.hasPermission(Permission.VOICE_CONNECT) && !self.getVoiceState().inVoiceChannel() && member.getVoiceState().inVoiceChannel()){
            audioManager.openAudioConnection(memberChannel);
            audioManager.setSelfDeafened(true);
            Globals.isSelfInVoice = true;
            Globals.currentVoiceChannel = memberChannel.getName(); //RECENT: Fixes bug with bot leaving when another channel empties
            channel.sendMessageFormat("Connecting to \uD83D\uDD0A `%s`", memberChannel.getName()).queue();
        }
        else if (self.hasPermission(Permission.VOICE_CONNECT) && !member.getVoiceState().inVoiceChannel()) {
            channel.sendMessage("Please join yourself to a voice channel first before queuing up music").queue();
            return;
        }

        if (self.hasPermission(Permission.VOICE_CONNECT) && self.getVoiceState().inVoiceChannel() && member.getVoiceState()
                .getChannel() != self.getVoiceState().getChannel()) {
            channel.sendMessage("Sorry, I'm currently in another voice channel at the moment").queue();
            return;
        }


        Scanner fileReader = null;
        try {
            File songFile = new File(Config.get("resource_file_song_history"));
            fileReader = new Scanner(songFile);
            ArrayList<String> songList = new ArrayList<String>();
            ArrayList<String> noDuplicateSongList = new ArrayList<String>();
            Set<String> set = new LinkedHashSet<>();

            while (fileReader.hasNext()) {
                String theLine = fileReader.nextLine();
                songList.add(theLine);
            }

            Collections.shuffle(songList);
            set.addAll(songList); //Makes everything unique no duplicates
            noDuplicateSongList.addAll(set);

            int numSongs = 0;
            if (!args.isEmpty()) {
                try {
                    numSongs = Integer.parseInt(args.get(0));
                }
                catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }

            //Take 20 songs and add to noDuplicateSongList by default
            if (args.isEmpty()) {
                for (int i = 0; i < 10; i++) {
                    String input = "ytsearch:" + noDuplicateSongList.get(i);
                    PlayerManager.getInstance().loadAndPlay(channel, input, true);
                }
            }
            else {
                for (int i = 0; i < numSongs; i++) {
                    String input = "ytsearch:" + noDuplicateSongList.get(i);
                    PlayerManager.getInstance().loadAndPlay(channel, input, true);
                }
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
    }

    @Override
    public String getName() {
        return "bulk";
    }

    @Override
    public String getHelp() {
        return "Queues 10 random songs by default based on listening history. OR queues as many songs as the number provided" +
                "\nUsage: `zbulk` OR `zbulk [number of songs]`";
    }

    @Override
    public List<String> getAliases(){
        return Arrays.asList("random", "rand");
    }
}
