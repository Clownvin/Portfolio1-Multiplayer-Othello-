package com.cs319.client;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import com.cs319.server.othello.Game;

public class PieceComponent extends Component {
	private static final long serialVersionUID = -1335298569192571865L;
	private static final float delta = 0.025f;
	private byte state = 0; // 0 - Not yet used, Black and white are Game.PLAYER_BLACK & Game.PLAYER_WHITE, and bitshift left 2 = outlines.
	private float alphaWhite = 0;
	private float alphaBlack = 0;
	private Timer timer = null;

	//animates the "flipping" animation with swing timers
	public void handleFlip() {
		if (state == Game.PLAYER_BLACK) {
			timer = new Timer(10, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (alphaBlack + delta >= 1) {
						timer.stop();
						return;
					}
					if (alphaWhite - delta >= 0) {
						alphaWhite -= delta;
					}
					alphaBlack += delta;
					repaint();
				}
			});
			timer.start();
		} else if (state == Game.PLAYER_WHITE) {
			timer = new Timer(10, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (alphaWhite + delta >= 1) {
						timer.stop();
						return;
					}
					if (alphaBlack - delta >= 0) {
						alphaBlack -= delta;
					}
					alphaWhite += delta;
					repaint();
				}
			});
			timer.start();
		}
	}

	//paints the pieces, with the appropriate fade for pieces that are currently flipping
	@Override
	public void paint(Graphics g) {
		switch (state) {
		case Game.PLAYER_BLACK:
		case Game.PLAYER_WHITE:
			((Graphics2D) g).setComposite(AlphaComposite.SrcOver.derive(alphaBlack));
			g.drawImage(UI.getBlackImage(), 5, 5, getWidth() - 10, getHeight() - 10, null);
			((Graphics2D) g).setComposite(AlphaComposite.SrcOver.derive(alphaWhite));
			g.drawImage(UI.getWhiteImage(), 5, 5, getWidth() - 10, getHeight() - 10, null);
			break;
		case Game.PLAYER_BLACK << 2:
			g.setColor(Color.BLACK);
			g.drawOval(5, 5, getWidth() - 10, getHeight() - 10);
			break;
		case Game.PLAYER_WHITE << 2:
			g.setColor(Color.WHITE);
			g.drawOval(5, 5, getWidth() - 10, getHeight() - 10);
		}
	}

	public void setState(final byte state) {
		this.state = state;
	}
}