package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import me.zombibot.util.Globals;
import me.zombibot.util.WipeThread;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoplayCommand implements ICommand {
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

        //Autoplay stuff
        if (!Globals.isAutoplayToggledOn && args.isEmpty()) {
            Globals.isAutoplayToggledOn = true;
            channel.sendMessage("Starting autoplay..").queue();

            //Start autoplay thread
            Thread t1 = new Thread(new AutoplayThread(playerManager, ctx.getGuild(), channel));
            t1.start();
        }
        else {
            Globals.isAutoplayToggledOn = false;
            channel.sendMessage("Successfully shut down autoplay").queue();
        }

    }//end handle

    @Override
    public String getName() {
        return "autoplay";
    }

    @Override
    public String getHelp() {
        return "Auto-plays music based on the listening history. It will queue up a song after the last one until autoplay is canceled.";
    }

    @Override
    public List<String> getAliases(){
        return Arrays.asList("auto", "atuo");
    }
}
