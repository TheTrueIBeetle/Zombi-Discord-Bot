package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import me.zombibot.command.CommandContext;
import me.zombibot.util.Globals;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {

    //Static hashmap to hold numbers for songs
    private HashMap<Integer, AudioTrack> queueCoordinator;
    private int count = 0;

    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    public boolean repeating = false;

    //Extra
    private Guild guild;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player, Guild guild) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        queueCoordinator = new HashMap<>();
        this.guild = guild;
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            count++;
            this.queue.offer(track);

            //Add to hashmap
            queueCoordinator.put(count, track);
        }
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public boolean nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        return this.player.startTrack(queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            if (this.repeating) {
                this.player.startTrack(track.makeClone(), false);
            }
            nextTrack();
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        Globals.isNewTrackPlaying = true;
    }

    public HashMap<Integer, AudioTrack> getQueueCoordinator(){
        return this.queueCoordinator;
    }

}