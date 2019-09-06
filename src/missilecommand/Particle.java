package missilecommand;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class Particle {
	
	private Vector2f pos;
        private Vector2f nextSegment;
	private Vector2f curPos;
	private Vector2f vel;
	private Vector2f curVel;
	private Color color;
	private float lifeSpan;
	private float time;
	private float radius = 0f;

	public Particle(Vector2f pos) {
            nextSegment = pos;
	}

	public void setPosition(Vector2f pos) {
            
		this.pos = pos;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public void setVector(float angle, float r) {
		vel = Vector2f.polar(angle, r);
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setLifeSpan(float lifeSpan) {
		this.lifeSpan = lifeSpan;
	}

	public void update(float delta) {
		time += delta;
		curVel = vel.mul(time);
		curPos = pos.add(curVel);
	}

	public void draw(Graphics2D g, Matrix3x3f view) {
            Random random = new Random();
            switch (random.nextInt(5)) {
		case 0:
			g.setColor(Color.WHITE);
			break;
		case 1:
			g.setColor(Color.RED);
			break;
		case 2:
			g.setColor(Color.YELLOW);
			break;
		case 3:
			g.setColor(Color.ORANGE);
			break;
		case 4:
			g.setColor(Color.PINK);
			break;
		}
		Vector2f topLeft = new Vector2f(nextSegment.x - radius, nextSegment.y + radius);
		topLeft = view.mul(topLeft);
		Vector2f bottomRight = new Vector2f(nextSegment.x + radius, nextSegment.y - radius);
		bottomRight = view.mul(bottomRight);
		int circleX = (int) topLeft.x;
		int circleY = (int) topLeft.y;
//		int circleWidth = (int) (bottomRight.x - topLeft.x);
//		int circleHeight = (int) (bottomRight.y - topLeft.y);
                int circleWidth = 2;
		int circleHeight = 2;
                nextSegment = new Vector2f(nextSegment.x + .9f, nextSegment.y);
                g.fillOval((int)nextSegment.x, (int)nextSegment.y, circleWidth, circleHeight);
	}

	public boolean hasDied() {
		return false;
	}
}