package drum;

import instruments.IKinectInstrument;

import java.util.ArrayList;
import java.util.List;

import main.Main;
import midi.MidiMain;
import player.Player;
import player.VectorHelper;
import processing.core.PGraphics;
import processing.core.PVector;

public class Drums implements IKinectInstrument {
	List<DrumSingle> drumsList;
	private Main p;
	private float marginDrums;
	private float widthDrums;
	private float heightDrums;
	private MidiMain midi;

	private boolean hitMode = true;

	public Drums(Main p, float numberDrums, float marginDrums, float widthDrums, float heightDrums,
			boolean hitmode, MidiMain midi) {

		this.p = p;
		this.marginDrums = marginDrums + widthDrums;
		this.widthDrums = widthDrums;
		this.heightDrums = heightDrums;
		this.midi = midi;
		this.hitMode = hitmode;
		drumsList = new ArrayList<>();

		// Drums generate
		generateDrums(numberDrums);
	}

	private void generateDrums(float numberDrums) {
		drumsList.clear();
		if (numberDrums < 1)
			return;
		float padding = -(numberDrums - 1) / 2;
		for (int i = 0; i < numberDrums; i++) {
			drumsList.add(new DrumSingle(padding, i));
			padding += 1;
		}
	}

	public void update(Player player) {
		// Richtungsvektor COM zu Neck
		// PVector rv = player.getNeckToTorso(true);
		// PVector rv = VectorHelper.roundedVector(player.getNeckToTorso(false),
		// true);
		PVector rv = new PVector(0, player.getNeckToTorso(true).y);

		// Ortsvektor zu RV
		PVector ov = VectorHelper.orthogonalVector(rv, true);
		// ov = new PVector(1,10);

		// Position Drum Start
		PVector startPos = ov.get();
		startPos.mult(widthDrums / 2);

		// Position Drum Ende
		PVector endPos = ov.get();
		endPos.mult(-widthDrums / 2);

		for (DrumSingle myDrum : drumsList) {
			// Verschiebungsvektor vom COM
			PVector translation = new PVector(ov.x, ov.y);
			translation.mult(myDrum.padding * marginDrums);

			// Verschiebungsvektor 2 vom COM
			PVector translationHalf = new PVector(ov.x, ov.y);
			translationHalf.mult((myDrum.padding * marginDrums) - widthDrums / 2);

			// Start und Ende verschieben
			myDrum.start().set(startPos);
			myDrum.start().add(translation);

			myDrum.end().set(endPos);
			myDrum.end().add(translation);

			// Mittelvector
			myDrum.center().set(startPos);
			myDrum.center().add(translationHalf);
		}

		// Draw Overlay
		draw(player);

		// Check Drums
		checkDrums(player);

		// System.out.println(rv);
	}

	private void checkDrums(Player player) {
		PVector handLeft = player.getHandLeftAbsolute().get();
		PVector handRight = player.getHandRightAbsolute().get();

		for (DrumSingle myDrum : drumsList) {

			if (hitMode) {
				checkHit(handLeft, myDrum, player, true);
				checkHit(handRight, myDrum, player, false);
			} else {
				checkCollision(handLeft, myDrum, player, true);
				checkCollision(handRight, myDrum, player, false);
			}

		}
	}

	private void checkCollision(PVector handAbsolute, DrumSingle myDrum, Player player, boolean left) {

		PVector handAbsoluteNormalized = handAbsolute.get();
		handAbsoluteNormalized.normalize();
		PVector ov = myDrum.ov(true);

		// Dot Product
		float dotProduct = handAbsolute.dot(ov);

		// Entfernung berechnen
		float distance = handAbsolute.dist(myDrum.center());
		float maxDistance = marginDrums / 2;

		if (distance <= maxDistance) {
			// Crap
			float velocity = player.getVelocityLeft();

			if (left) {

				if (myDrum.dotProductLeft < 0 && dotProduct > 0) {
					System.out.println("Down Left " + myDrum.id);
					// midi.playMidi(myDrum.id, myDrum.id, true, velocity, 1);
					midi.playMidiDrum(player.getVelocityLeft(), 1, myDrum.id);

				} // else if (myDrum.dotProductLeft > 0 && dotProduct < 0) {
					// }
			} else {
				if (myDrum.dotProductRight < 0 && dotProduct > 0) {
					// System.out.println("Down Right " + myDrum.id);
					// midi.playMidi(myDrum.id, myDrum.id, true);

				} else if (myDrum.dotProductRight > 0 && dotProduct < 0) {
					System.out.println("Down Right " + myDrum.id);
					// midi.playMidi(myDrum.id, myDrum.id, true, velocity, 1);
					midi.playMidiDrum(player.getVelocityLeft(), 1, myDrum.id);
				}
			}

		}

		// Neues Dot Product speichern
		if (left) {
			myDrum.dotProductLeft = dotProduct;
		} else {
			myDrum.dotProductRight = dotProduct;
		}
	}

	private void checkHit(PVector handAbsolute, DrumSingle myDrum, Player player, boolean left) {

		PVector handAbsoluteNormalized = handAbsolute.get();
		handAbsoluteNormalized.normalize();
		PVector ov = myDrum.ov(true);

		// Entfernung berechnen
		float distance = handAbsolute.dist(myDrum.center());
		float maxDistance = marginDrums / 2;

		if (distance <= maxDistance) {

			if (left) {
				if (player.getHitLeft()) {
					System.out.println("Down Left " + myDrum.id);
					// midi.playMidi(myDrum.id, myDrum.id, true,
					// player.getVelocityLeft(), 1);
					midi.playMidiDrum(player.getVelocityLeft(), 1, myDrum.id);
				}

			} else {
				if (player.getHitRight()) {
					System.out.println("Down Right " + myDrum.id);
					// midi.playMidi(myDrum.id, myDrum.id, true,
					// player.getVelocityRight(),1);
					midi.playMidiDrum(player.getVelocityLeft(), 1, myDrum.id);
				}
			}

		}

	}

	private void draw(Player player) {
		p.pushMatrix();
		p.noStroke();
		p.fill(255, 0, 255, 125);

		// Translate zum COM
		p.translate(player.getTorso().x, player.getTorso().y);

		for (DrumSingle myDrum : drumsList) {

			p.rect(myDrum.start().x, myDrum.start().y, widthDrums, heightDrums);
		}

		p.fill(255);
		p.popMatrix();

	}
}
