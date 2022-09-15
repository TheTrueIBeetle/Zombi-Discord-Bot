package me.zombibot.command.commands.music;

import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import me.zombibot.command.commands.music.GuildMusicManager;
import me.zombibot.command.commands.music.PlayerManager;
import me.zombibot.util.Globals;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

@SuppressWarnings("ConstantConditions")

public class LeaveCommand implements ICommand {

    CommandContext wideCtx;

    @Override
    public void handle(CommandContext ctx){
        this.wideCtx = ctx;
        TextChannel channel = ctx.getChannel();
        AudioManager audioManager = ctx.getGuild().getAudioManager();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(ctx.getGuild());

        if (!audioManager.isConnected()){
            channel.sendMessage("I'm not currently connected to a voice channel").queue();
            return;
        }

        VoiceChannel voiceChannel = audioManager.getConnectedChannel();

        if (!voiceChannel.getMembers().contains(ctx.getMember())){
            channel.sendMessage("You must be in the same voice channel as me in order to let me know to leave").queue();
            return;
        }

        audioManager.closeAudioConnection();
        musicManager.scheduler.getQueue().clear();
        musicManager.player.stopTrack();
        channel.sendMessage("\uD83D\uDEAA Successfully disconnected from your voice channel").queue();

        Globals.isAutoplayToggledOn = false;
        Globals.isSelfInVoice = false;
    }

    @Override
    public String getName(){return "leave";}

    @Override
    public String getHelp(){
        return "Bot leaves the current channel you are in";
    }

    public void letBotCloseConnection(){
        AudioManager audioManager = wideCtx.getGuild().getAudioManager();
        audioManager.closeAudioConnection();
    }
}
