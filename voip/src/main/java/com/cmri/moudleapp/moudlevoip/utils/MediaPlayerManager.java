package com.cmri.moudleapp.moudlevoip.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;

import com.cmri.moudleapp.moudlevoip.R;
import com.mobile.voip.sdk.api.utils.MyLogger;


/**
 * Created by caizhibiao on 2016/5/11.
 */
public class MediaPlayerManager implements MediaPlayer.OnCompletionListener {
    private static final MyLogger logger = MyLogger.getLogger("MediaPlayerManager");
    /**
     * 声音的播放器
     */
    private static MediaPlayer mMediaPlayer;
    private static Vibrator mVibrator;
    private Context mContext;

    public MediaPlayerManager(Context mContext) {
        this.mContext = mContext;
    }

    public void startInComingMusic(Context mContext) {
        startInComingRingerMode(mContext);
    }

    //平台播放，不上层播放
    public void startOutGoingMusic() {
        stopMediaPlayer();
        createCallingMusic();
        startCallingMusic();
    }

    //拨打电话，需要嘟嘟的响声
    private void createCallingMusic() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(mContext, R.raw.dudu_ringtone);
            mMediaPlayer.setLooping(true);
        }
    }

    private void startCallingMusic() {
        if (mMediaPlayer != null) {
            if (!mMediaPlayer.isPlaying()) {
                try {
                    mMediaPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }


    public void stop() {
        logger.e("stop music");
        stopCallingMusic();
        logger.e("stop music,end");
    }

    /**
     * 停止播放来电铃声
     */
    private void stopCallingMusic() {
        stopMediaPlayer();
    }

    private void stopMediaPlayer() {
        try {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mMediaPlayer = null;
        }

    }

    /**
     * 播放来电铃声
     */
    private void startInComingRingerMode(Context mContext) {
        playInComingRingtone(mContext);
    }

    private void playInComingRingtone(Context mContext) {
        logger.e("playInComingRingtone");
        stopMediaPlayer();
        AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE );
        mMediaPlayer = MediaPlayer.create(mContext, R.raw.ringtone_incoming);
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            logger.e("mMediaPlayer is playing ,stop it first");
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = MediaPlayer.create(mContext, R.raw.ringtone_incoming);
        }

        if(mMediaPlayer != null){
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {//设置重复播放
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    logger.e("mMediaPlayer play completed ,start again");
                    mMediaPlayer.start();
                    mMediaPlayer.setLooping(true);
                }
            });
        }

        }

    /**
     * 铃声是否在响
     */
    public boolean isMediaPlaying(){
        if(mMediaPlayer != null){
            return mMediaPlayer.isPlaying()?true:false;
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stop();
    }
}
