package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
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
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;

public class PadCommand implements ICommand {

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
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();

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

        if (queue.size() >= 20) {
            channel.sendMessage("Queue is already has 20 tracks, no need to pad").queue();
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
            Collections.shuffle(songList);
            set.addAll(songList); //Makes everything unique no duplicates
            noDuplicateSongList.addAll(set);

            //Take 20 songs and add to noDuplicateSongList
            for (int i = queue.size(); i < 20; i++) {
                String input = "ytsearch:" + noDuplicateSongList.get(i);
                PlayerManager.getInstance().loadAndPlay(channel, input, true);
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
        return "pad";
    }

    @Override
    public String getHelp() {
        return "Pads the rest of the queue out adding random songs from play history until reaching 20 tracks";
    }
}
