package com.louislepper.waveform;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.Arrays;

public class AudioThread extends Thread{
    private static final double AMPLITUDE = Short.MAX_VALUE;
    private static final int SAMPLE_RATE = 44100;
    private static final int BASE_FREQUENCY = 220;
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
        synchronized (lock) {
            wrapped = new WaveformWrapper(wave);
            examplePh = 0.0;
            exampleStep = wrapped.length * frequency / SAMPLE_RATE;
        }
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
        exampleStep = wrapped.length * frequency / SAMPLE_RATE;

        // start audio
        audioTrack.play();


        while(isRunning) {
            synchronized (lock) {
                for (int i = 0; i < buffer.length; i++) {
                    //TODO: I want to make sure this doesn't skip any samples. Maybe we could change the sample rate if needed?
                    examplePh = examplePh % wrapped.length;
                    buffer[i] = wrapped.get(examplePh);
                    examplePh += exampleStep;
                }
            }

            //This is overly conservative, but I want to block for as little time as possible if we've been stopped.
            if(isRunning) {
                audioTrack.write(buffer, 0, bufferSize);
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

    public static class WaveformWrapper {
        private final short[] waveform;
        private int length;
        private int start;

        public WaveformWrapper(short[] waveform){
            this.waveform = waveform;
            this.length = waveform.length;
            this.start = 0;
        }

        public WaveformWrapper(short[] waveform, SampleInterpolator.StartAndEnd startAndEnd){
            this.waveform = waveform;
            this.length = startAndEnd.getEnd() - startAndEnd.getStart();
            this.start = startAndEnd.getStart();
        }

        public short get(double index){
            if(length == 0) return 0;
            int lowerIndex = (int) Math.floor(index);

            int upperIndex = ((int) Math.ceil(index)) % waveform.length;

            double indexDifference = index - lowerIndex; //Should always be between 0 and 1

            short lower = waveform[lowerIndex];
            short upper = waveform[upperIndex];

            double difference = upper - lower;
            double unroundedResult = ((double) lower) + (difference * indexDifference);
            return (short) unroundedResult;
        }
    }
}
