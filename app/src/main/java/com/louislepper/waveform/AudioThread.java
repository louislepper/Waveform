package com.louislepper.waveform;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.Arrays;

public class AudioThread extends Thread{
    private static final double AMPLITUDE = Short.MAX_VALUE;
    private static final int SAMPLE_RATE = 44100;
    private static final int BASE_FREQUENCY = 220;

    //We haven't actually resized the wave. This is just an approximate number. This'll be off by a bunch depending on how small the wave is.
    private static final int WAVEFORM_SAMPLES = 2000;

    private volatile WaveformWrapper wrapped;

    private boolean isRunning = true;

    private int frequency = BASE_FREQUENCY;
    private double examplePh;
    private double exampleStep;
    final private Object lock = new Object();


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

        examplePh = 0.0;
        exampleStep = WAVEFORM_SAMPLES * frequency / SAMPLE_RATE;

        // start audio
        audioTrack.play();

        while(isRunning) {
                for (int i = 0; i < buffer.length; i++) {
                    //This line is probably unneeded, but I don't want the value overflowing and throwing an exception (does java do that?)
                    examplePh = examplePh % WAVEFORM_SAMPLES;
                    buffer[i] = wrapped.get(examplePh);
                    examplePh += exampleStep;
                }

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

    private static short[] createSineWaveform(int waveformLength) {
        short[] waveform = new short[waveformLength];
        for (int i = 0; i < waveformLength; ++i) {
            double phase = ((double) i) / waveformLength;
            waveform[i] = (short) (AMPLITUDE * Math.sin(2 * Math.PI * phase));
        }
        return waveform;
    }
}
