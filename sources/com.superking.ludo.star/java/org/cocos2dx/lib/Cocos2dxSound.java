package org.cocos2dx.lib;

import android.content.Context;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;
import cz.msebera.android.httpclient.HttpStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class Cocos2dxSound {
    private static final int INVALID_SOUND_ID = -1;
    private static final int INVALID_STREAM_ID = -1;
    private static int LOAD_TIME_OUT = HttpStatus.SC_INTERNAL_SERVER_ERROR;
    private static final int MAX_SIMULTANEOUS_STREAMS_DEFAULT = 5;
    private static final int MAX_SIMULTANEOUS_STREAMS_I9100 = 3;
    private static final int SOUND_PRIORITY = 1;
    private static final int SOUND_QUALITY = 5;
    private static final float SOUND_RATE = 1.0f;
    private static final String TAG = "Cocos2dxSound";
    private final Context mContext;
    private float mLeftVolume;
    private final HashMap<String, Integer> mPathSoundIDMap = new HashMap();
    private final HashMap<String, ArrayList<Integer>> mPathStreamIDsMap = new HashMap();
    private ConcurrentHashMap<Integer, SoundInfoForLoadedCompleted> mPlayWhenLoadedEffects = new ConcurrentHashMap();
    private float mRightVolume;
    private SoundPool mSoundPool;

    public class OnLoadCompletedListener implements OnLoadCompleteListener {
        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            if (status == 0) {
                SoundInfoForLoadedCompleted info = (SoundInfoForLoadedCompleted) Cocos2dxSound.this.mPlayWhenLoadedEffects.get(Integer.valueOf(sampleId));
                if (info != null) {
                    info.effectID = Cocos2dxSound.this.doPlayEffect(info.path, sampleId, info.isLoop, info.pitch, info.pan, info.gain);
                    synchronized (info) {
                        info.notifyAll();
                    }
                }
            }
        }
    }

    public class SoundInfoForLoadedCompleted {
        public int effectID = Cocos2dxSound.INVALID_STREAM_ID;
        public float gain;
        public boolean isLoop;
        public float pan;
        public String path;
        public float pitch;

        public SoundInfoForLoadedCompleted(String path, boolean isLoop, float pitch, float pan, float gain) {
            this.path = path;
            this.isLoop = isLoop;
            this.pitch = pitch;
            this.pan = pan;
            this.gain = gain;
        }
    }

    public Cocos2dxSound(Context context) {
        this.mContext = context;
        initData();
    }

    private void initData() {
        if (Cocos2dxHelper.getDeviceModel().contains("GT-I9100")) {
            this.mSoundPool = new SoundPool(MAX_SIMULTANEOUS_STREAMS_I9100, MAX_SIMULTANEOUS_STREAMS_I9100, SOUND_QUALITY);
        } else {
            this.mSoundPool = new SoundPool(SOUND_QUALITY, MAX_SIMULTANEOUS_STREAMS_I9100, SOUND_QUALITY);
        }
        this.mSoundPool.setOnLoadCompleteListener(new OnLoadCompletedListener());
        this.mLeftVolume = 0.5f;
        this.mRightVolume = 0.5f;
    }

    public int preloadEffect(String path) {
        Integer soundID = (Integer) this.mPathSoundIDMap.get(path);
        if (soundID == null) {
            soundID = Integer.valueOf(createSoundIDFromAsset(path));
            if (soundID.intValue() != INVALID_STREAM_ID) {
                this.mPathSoundIDMap.put(path, soundID);
            }
        }
        return soundID.intValue();
    }

    public void unloadEffect(String path) {
        ArrayList<Integer> streamIDs = (ArrayList) this.mPathStreamIDsMap.get(path);
        if (streamIDs != null) {
            Iterator it = streamIDs.iterator();
            while (it.hasNext()) {
                this.mSoundPool.stop(((Integer) it.next()).intValue());
            }
        }
        this.mPathStreamIDsMap.remove(path);
        Integer soundID = (Integer) this.mPathSoundIDMap.get(path);
        if (soundID != null) {
            this.mSoundPool.unload(soundID.intValue());
            this.mPathSoundIDMap.remove(path);
        }
    }

    public int playEffect(String path, boolean loop, float pitch, float pan, float gain) {
        int streamID;
        Integer soundID = (Integer) this.mPathSoundIDMap.get(path);
        if (soundID != null) {
            streamID = doPlayEffect(path, soundID.intValue(), loop, pitch, pan, gain);
        } else {
            soundID = Integer.valueOf(preloadEffect(path));
            if (soundID.intValue() == INVALID_STREAM_ID) {
                return INVALID_STREAM_ID;
            }
            SoundInfoForLoadedCompleted info = new SoundInfoForLoadedCompleted(path, loop, pitch, pan, gain);
            this.mPlayWhenLoadedEffects.putIfAbsent(soundID, info);
            synchronized (info) {
                try {
                    info.wait((long) LOAD_TIME_OUT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            streamID = info.effectID;
            this.mPlayWhenLoadedEffects.remove(soundID);
        }
        return streamID;
    }

    public void stopEffect(int steamID) {
        this.mSoundPool.stop(steamID);
        for (String pPath : this.mPathStreamIDsMap.keySet()) {
            if (((ArrayList) this.mPathStreamIDsMap.get(pPath)).contains(Integer.valueOf(steamID))) {
                ((ArrayList) this.mPathStreamIDsMap.get(pPath)).remove(((ArrayList) this.mPathStreamIDsMap.get(pPath)).indexOf(Integer.valueOf(steamID)));
                return;
            }
        }
    }

    public void pauseEffect(int steamID) {
        this.mSoundPool.pause(steamID);
    }

    public void resumeEffect(int steamID) {
        this.mSoundPool.resume(steamID);
    }

    public void pauseAllEffects() {
        if (!this.mPathStreamIDsMap.isEmpty()) {
            for (Entry<String, ArrayList<Integer>> entry : this.mPathStreamIDsMap.entrySet()) {
                Iterator it = ((ArrayList) entry.getValue()).iterator();
                while (it.hasNext()) {
                    this.mSoundPool.pause(((Integer) it.next()).intValue());
                }
            }
        }
    }

    public void resumeAllEffects() {
        if (!this.mPathStreamIDsMap.isEmpty()) {
            for (Entry<String, ArrayList<Integer>> entry : this.mPathStreamIDsMap.entrySet()) {
                Iterator it = ((ArrayList) entry.getValue()).iterator();
                while (it.hasNext()) {
                    this.mSoundPool.resume(((Integer) it.next()).intValue());
                }
            }
        }
    }

    public void stopAllEffects() {
        if (!this.mPathStreamIDsMap.isEmpty()) {
            for (Entry<String, ArrayList<Integer>> entry : this.mPathStreamIDsMap.entrySet()) {
                Iterator it = ((ArrayList) entry.getValue()).iterator();
                while (it.hasNext()) {
                    this.mSoundPool.stop(((Integer) it.next()).intValue());
                }
            }
        }
        this.mPathStreamIDsMap.clear();
    }

    public float getEffectsVolume() {
        return (this.mLeftVolume + this.mRightVolume) / 2.0f;
    }

    public void setEffectsVolume(float volume) {
        if (volume < 0.0f) {
            volume = 0.0f;
        }
        if (volume > SOUND_RATE) {
            volume = SOUND_RATE;
        }
        this.mRightVolume = volume;
        this.mLeftVolume = volume;
        if (!this.mPathStreamIDsMap.isEmpty()) {
            for (Entry<String, ArrayList<Integer>> entry : this.mPathStreamIDsMap.entrySet()) {
                Iterator it = ((ArrayList) entry.getValue()).iterator();
                while (it.hasNext()) {
                    this.mSoundPool.setVolume(((Integer) it.next()).intValue(), this.mLeftVolume, this.mRightVolume);
                }
            }
        }
    }

    public void end() {
        this.mSoundPool.release();
        this.mPathStreamIDsMap.clear();
        this.mPathSoundIDMap.clear();
        this.mPlayWhenLoadedEffects.clear();
        this.mLeftVolume = 0.5f;
        this.mRightVolume = 0.5f;
        initData();
    }

    public int createSoundIDFromAsset(String path) {
        int soundID;
        try {
            if (path.startsWith("/")) {
                soundID = this.mSoundPool.load(path, 0);
            } else if (Cocos2dxHelper.getObbFile() != null) {
                soundID = this.mSoundPool.load(Cocos2dxHelper.getObbFile().getAssetFileDescriptor(path), 0);
            } else {
                soundID = this.mSoundPool.load(this.mContext.getAssets().openFd(path), 0);
            }
        } catch (Exception e) {
            soundID = INVALID_STREAM_ID;
            Log.e(TAG, "error: " + e.getMessage(), e);
        }
        if (soundID == 0) {
            return INVALID_STREAM_ID;
        }
        return soundID;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    private int doPlayEffect(String path, int soundId, boolean loop, float pitch, float pan, float gain) {
        float leftVolume = (this.mLeftVolume * gain) * (SOUND_RATE - clamp(pan, 0.0f, SOUND_RATE));
        float rightVolume = (this.mRightVolume * gain) * (SOUND_RATE - clamp(-pan, 0.0f, SOUND_RATE));
        int streamID = this.mSoundPool.play(soundId, clamp(leftVolume, 0.0f, SOUND_RATE), clamp(rightVolume, 0.0f, SOUND_RATE), SOUND_PRIORITY, loop ? INVALID_STREAM_ID : 0, clamp(SOUND_RATE * pitch, 0.5f, 2.0f));
        ArrayList<Integer> streamIDs = (ArrayList) this.mPathStreamIDsMap.get(path);
        if (streamIDs == null) {
            streamIDs = new ArrayList();
            this.mPathStreamIDsMap.put(path, streamIDs);
        }
        streamIDs.add(Integer.valueOf(streamID));
        return streamID;
    }

    public void onEnterBackground() {
        this.mSoundPool.autoPause();
    }

    public void onEnterForeground() {
        this.mSoundPool.autoResume();
    }
}
