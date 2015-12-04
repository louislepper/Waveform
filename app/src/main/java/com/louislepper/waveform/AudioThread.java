package com.louislepper.waveform;

import android.inputmethodservice.KeyboardView;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.levien.synthesizer.core.midi.MidiListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AudioThread extends Thread implements MidiListener{
    private static final double AMPLITUDE = Short.MAX_VALUE;
    private static final int SAMPLE_RATE = 44100;
    private static final double BASE_FREQUENCY = Constants.C1_FREQUENCY;
    private static final int BASE_NOTE = Constants.C1_MIDI_NOTE;

    //We haven't actually resized the wave. This is just an approximate number. This'll be off by a bunch depending on how small the wave is.
    private static final int WAVEFORM_SAMPLES = 2000;
    public static final String TAG = "AudioThread";

    private volatile WaveformWrapper wrapped;

    private boolean isRunning = true;

    private double examplePh;
    private double exampleStep;
    private boolean keyboardMode = false;


    public double setWaveform(short[] processedImageArray, SampleInterpolator.StartAndEnd startAndEnd) {
        short[] wave = Arrays.copyOfRange(processedImageArray, startAndEnd.getStart(), startAndEnd.getEnd() + 1);
        wave = WaveStretcher.normalize(wave);
        wrapped = new WaveformWrapper(wave);

        return exampleStep;
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
                //TODO: Why is this necessary to avoid freezes?
                System.gc();
            }
        }
        //Todo: need to find a way to actually release these.
        audioTrack.pause();
        audioTrack.flush();
        audioTrack.release();
    }

    private double getStepForFrequency(double frequency) {
        return ((double) WAVEFORM_SAMPLES) * frequency / (double) SAMPLE_RATE;
    }

    private int populateBuffer(short[] buffer, int currentBasePlaybackLocation) {
        for (int i = 0; i < buffer.length; i++, currentBasePlaybackLocation++) {
            //This line is probably unneeded, but I don't want the value overflowing and throwing an exception (does java do that?)
            //examplePh = examplePh % WAVEFORM_SAMPLES;
            //                    examplePh = examplePh % Integer.MAX_VALUE - 5;
            if (examplePh > 100000.0) {
                //This line is probably meaningless, can probably just set it to zero.
                examplePh = Constants.moduloLowerAndUpperBound(examplePh, wrapped.size(), 0);
            }

            if (!keyboardMode) {
                buffer[i] = wrapped.get(((double) currentBasePlaybackLocation) * getStepForFrequency(Constants.getFrequencyByIndex(57)));
            } else {
                buffer[i] = getSample(currentBasePlaybackLocation,wrapped,currentNotes);
            }
        }
        return currentBasePlaybackLocation;
    }
    private short getSample(int currentBasePlaybackLocation, WaveformWrapper wrapped, List<Note> notesToPlay) {
        double finalSample = 0;
        synchronized (notesToPlay) {
            int numNotes = notesToPlay.size();
            for(Note n: notesToPlay) {
                short sample = wrapped.get(((double) currentBasePlaybackLocation) * getStepForFrequency(Constants.getFrequencyByIndex(n.note)));
                double velocityProportion = ((double) n.velocity) / 128.0;
                finalSample += (sample * velocityProportion)/numNotes;
            }
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

    public void toggleKeyboardMode() {
        //Todo: Should maybe do something to ensure this persists past AudioThread being killed.
        keyboardMode = !keyboardMode;
    }

    public void keyboardOn() {
        keyboardMode = true;
    }

    public void keyboardOff() {
        keyboardMode = false;
    }

    private ArrayList<Note> unsafeNotes = new ArrayList<>(10);
    final List<Note> currentNotes = Collections.synchronizedList(unsafeNotes);

    public void onNoteOff(int channel, int note, int velocity) {
        synchronized (currentNotes) {
            currentNotes.remove(new Note(note + 24, velocity));
        }
        Log.d(TAG,"onNoteOff - channel = " + channel + ". note = " + note + ". velocity = " + velocity);
    }

    @Override
    public void onNoteOn(int channel, int note, int velocity) {
        synchronized (currentNotes) {
            currentNotes.add(new Note(note + 24, velocity));
        }
//        ArrayList<Note> newList = new ArrayList<>(10);
//        newList.addAll(currentNotes);
//        currentNotes = newList;
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
