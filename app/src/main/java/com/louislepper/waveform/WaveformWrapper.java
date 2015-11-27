package com.louislepper.waveform;

public class WaveformWrapper {
    private final short[] waveform;

    public int size() {
        return length;
    }

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

    private short getWrapped(int index) {
        index = Constants.moduloLowerAndUpperBound(index, this.start + this.length, this.start);
        return waveform[index];
    }

    public short get(double index){
        if(length == 0) return 0;
        index = index - (double) start;
        int lowerIndex = (int) Math.floor(index);

        int upperIndex = ((int) Math.ceil(index));

        double indexDifference = index - lowerIndex; //Should always be between 0 and 1

        short lower = getWrapped(lowerIndex);
        short upper = getWrapped(upperIndex);

        double difference = upper - lower;
        double unroundedResult = ((double) lower) + (difference * indexDifference);
        return (short) unroundedResult;
    }
}