package missilecommand;

import java.awt.Color;
import java.awt.Graphics2D;

public class Ball {
	
	private Vector2f velocity;
	private Vector2f position;
        private Vector2f startPosition;
        private Vector2f lastPosition;
        Vector2f nextSegment;
	private Color color;
	private float radius;
        float sin;
        float cos;
        float inc = 10.0F;

	public Ball(Vector2f position, Vector2f endPosition, float angle) {
		this.position = position;
                lastPosition = position;
                nextSegment = position;
                this.startPosition = new Vector2f(321,0,1);
		velocity = Vector2f.polar(angle, 1.0f);
		radius = 0.006f;
		color = Color.GREEN;
                sin = (float)Math.sin(angle);
                cos = (float)Math.cos(angle);
	}

	public Vector2f getPosition() {
		return nextSegment;
	}

	public void draw(Graphics2D g, Matrix3x3f view) {

		g.setColor(color);
                nextSegment = new Vector2f(nextSegment.x + .9f * cos, nextSegment.y + .9f * sin);

		int circleWidth = 10;
		int circleHeight = 10;
		g.fillOval((int)nextSegment.x, (int)nextSegment.y, circleWidth, circleHeight);
	}

	public void update(float time) {
//		position = position.add(velocity.mul(time));
	}
}