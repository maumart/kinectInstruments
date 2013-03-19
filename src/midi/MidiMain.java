package midi;

import processing.core.PApplet;

public class MidiMain {
	midi.Midi midi;

	public MidiMain(PApplet p) {
		midi = new midi.Midi(p);
	}

	public void playMidi(int myString, int myHead, boolean upAndDown) {
		midi.strumChord(myHead + 1, 3, 1, upAndDown);
	}

	public void playMidi(int myString, int myHead, boolean upAndDown, float velocity) {
		midi.strumChord(myHead + 1, 3, 1, upAndDown);
	}
	
	public void playMidi(int myString, int myHead, boolean upAndDown, float velocity,float acceleration) {
		midi.strumChord(myHead + 1, 3, 1, upAndDown);
	}

}