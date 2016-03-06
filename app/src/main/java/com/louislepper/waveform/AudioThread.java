package com.louislepper.waveform;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.levien.synthesizer.core.midi.MidiListener;

import java.util.Arrays;

public class AudioThread extends Thread implements MidiListener{
    private static final double AMPLITUDE = Short.MAX_VALUE;
    private static final int SAMPLE_RATE = 44100;
    private static final int NOTES_IN_SCALE = 12;

    //We haven't actually resized the wave. This is just an approximate number.
    //This will be off by a certain amount depending on how small the wave is.
    private static final int WAVEFORM_SAMPLES = 2000;

    public static final String TAG = "AudioThread";

    private volatile WaveformWrapper wrapped;

    private boolean isRunning = true;

    private boolean keyboardMode = false;


    public void setWaveform(short[] processedImageArray, SampleInterpolator.StartAndEnd startAndEnd) {
        short[] wave = Arrays.copyOfRange(processedImageArray, startAndEnd.getStart(), startAndEnd.getEnd() + 1);
        wave = WaveStretcher.normalize(wave);
        wrapped = new WaveformWrapper(wave);
    }

    public void stopAudio(){
        isRunning = false;
    }

    @Override
    public void run() {
        setPriority(Thread.MAX_PRIORITY);
        int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        // create an audiotrack object
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM);

        short buffer[] = new short[bufferSize];
        wrapped = new WaveformWrapper(createSineWaveform(WAVEFORM_SAMPLES));

        // start audio
        audioTrack.play();
        //Pointers to all frequency play points in the waveform are based off of this base counter.
        int currentBasePlaybackLocation = 0;
        while(isRunning) {
            currentBasePlaybackLocation = populateBuffer(buffer, currentBasePlaybackLocation);

            //This is overly conservative, but I want to block for as little time as possible if we've been stopped.
            if(isRunning) {
                audioTrack.write(buffer, 0, bufferSize);

                //For some reason the app was freezing if I didn't garbage collect here.
                //I can't seem to reproduce that on latest Android, but I'll leave it just to be conservative
                //It probably does slow the app down considerably.
                System.gc();
            }
        }

        audioTrack.pause();
        audioTrack.flush();
        audioTrack.release();
    }

    private double getStepForFrequency(double frequency) {
        return ((double) WAVEFORM_SAMPLES) * frequency / (double) SAMPLE_RATE;
    }

    private int populateBuffer(short[] buffer, int currentBasePlaybackLocation) {
        for (int i = 0; i < buffer.length; i++, currentBasePlaybackLocation++) {
            //Resetting currentBasePlaybackLocation if it gets too large.
            if (currentBasePlaybackLocation > 1000000) {

                //This is me attempting to avoid a click sound when I reset currentBasePlaybackLocation, but I haven't tested this.
                currentBasePlaybackLocation = Constants.moduloLowerAndUpperBound(currentBasePlaybackLocation, wrapped.size(), 0);
            }

            if (!keyboardMode) {
                buffer[i] = wrapped.get(((double) currentBasePlaybackLocation) * getStepForFrequency(Constants.getFrequencyByIndex(57)));
            } else {
                buffer[i] = getSample(currentBasePlaybackLocation, wrapped);
            }
        }
        return currentBasePlaybackLocation;
    }

    private short getSample(int currentBasePlaybackLocation, WaveformWrapper wrapped) {
        Note[] notesToPlay = currentNotes;
        double finalSample = 0;
        int numNotes = notesToPlay.length;
        for(Note n: notesToPlay) {
            short sample = wrapped.get(((double) currentBasePlaybackLocation) * getStepForFrequency(Constants.getFrequencyByIndex(n.note)));
            double velocityProportion = ((double) n.velocity) / 128.0;
            finalSample += (sample * velocityProportion)/numNotes;
        }
        return (short) finalSample;
    }

    private static short[] createSineWaveform(int waveformLength) {
        short[] waveform = new short[waveformLength];
        for (int i = 0; i < waveformLength; ++i) {
            double phase = ((double) i) / waveformLength;
            waveform[i] = (short) (AMPLITUDE * Math.sin(2 * Math.PI * phase));
        }
        return waveform;
    }

    public void keyboardOn() {
        keyboardMode = true;
    }

    public void keyboardOff() {
        keyboardMode = false;
    }

    Note[] currentNotes = new Note[0];

    public void setOctave(int octave) {
        scaleOffset = NOTES_IN_SCALE * octave;
    }

    private int scaleOffset = 24;

    public void onNoteOff(int channel, int note, int velocity) {
        note += scaleOffset;
        Note[] newNotes = new Note[currentNotes.length - 1];
        for(int i = 0, o = 0; i < currentNotes.length; i++) {
            if(currentNotes[i].note != note) {
                newNotes[o] = currentNotes[i];
                o++;
            }
        }
        currentNotes = newNotes;
        Log.d(TAG,"onNoteOff - channel = " + channel + ". note = " + note + ". velocity = " + velocity);
    }

    @Override
    public void onNoteOn(int channel, int note, int velocity) {
        Note[] newNotes = new Note[currentNotes.length + 1];
        System.arraycopy(currentNotes, 0, newNotes, 0, currentNotes.length);
        newNotes[currentNotes.length] = new Note(note + scaleOffset, velocity);
        currentNotes = newNotes;
        Log.d(TAG,"onNoteOn - channel = " + channel + ". note = " + note + ". velocity = " + velocity);
    }

    private class Note {
        int note;
        int velocity;

        public Note(int note, int velocity) {
            this.note = note;
            this.velocity = velocity;
        }
        @Override
        public boolean equals(Object a) {
            return a instanceof Note && ((Note) a).note == note;
        }
    }

    @Override
    public void onNoteAftertouch(int channel, int note, int aftertouch) {
        Log.d(TAG,"onNoteAftertouch - channel = " + channel + ". note = " + note + ". aftertouch = " + aftertouch);
    }

    @Override
    public void onController(int channel, int control, int value) {

    }

    @Override
    public void onProgramChange(int channel, int program) {

    }

    @Override
    public void onChannelAftertouch(int channel, int aftertouch) {
        Log.d(TAG,"onChannelAftertouch - channel = " + channel + ". aftertouch = " + aftertouch);
    }

    @Override
    public void onPitchBend(int channel, int value) {

    }

    @Override
    public void onTimingClock() {

    }

    @Override
    public void onActiveSensing() {

    }

    @Override
    public void onSequenceNumber(int sequenceNumber) {

    }

    @Override
    public void onText(byte[] text) {

    }

    @Override
    public void onCopyrightNotice(byte[] text) {

    }

    @Override
    public void onSequenceName(byte[] text) {

    }

    @Override
    public void onInstrumentName(byte[] text) {

    }

    @Override
    public void onLyrics(byte[] text) {

    }

    @Override
    public void onMarker(byte[] text) {

    }

    @Override
    public void onCuePoint(byte[] text) {

    }

    @Override
    public void onChannelPrefix(int channel) {

    }

    @Override
    public void onPort(byte[] data) {

    }

    @Override
    public void onEndOfTrack() {

    }

    @Override
    public void onSetTempo(int microsecondsPerQuarterNote) {

    }

    @Override
    public void onSmpteOffset(byte[] data) {

    }

    @Override
    public void onTimeSignature(int numerator, int denominator, int metronomePulse, int thirtySecondNotesPerQuarterNote) {

    }

    @Override
    public void onKeySignature(int key, boolean isMinor) {

    }

    @Override
    public void onSequencerSpecificEvent(byte[] data) {

    }

    @Override
    public void onSysEx(byte[] data) {

    }
}
