package me.zombibot.event.events;

import me.zombibot.command.commands.music.GuildMusicManager;
import me.zombibot.command.commands.music.PlayerManager;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.annotation.Nonnull;

public class UserLeaveVoice extends ListenerAdapter {

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        AudioManager audioManager = event.getGuild().getAudioManager();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(event.getGuild());

        if (event.getChannelLeft().getMembers().size() == 1) {
            musicManager.scheduler.getQueue().clear();
            musicManager.player.stopTrack();
            audioManager.closeAudioConnection();
        }
    }
}
