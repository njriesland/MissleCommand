/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package missilecommand;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author nicr
 */
public class MissileCommand extends JFrame implements Runnable {

    private BufferStrategy bs;
    int x = 10;
    int y = 10;
    int prevX = x;
    int prevY = y;
    private Canvas canvas;
    private volatile boolean running;
    private Thread gameThread;
    protected int vx;
    protected int vy;
    protected int vw;
    protected int vh;
    protected FrameRate frameRate;
    protected long appSleep = 10L;
    protected Color appFPSColor = Color.GREEN;
    protected Font appFont = new Font("Courier New", Font.PLAIN, 14);
    protected String appTitle = "TBD-Title";
    protected int textPos = 0;
    protected Color appBackground = Color.BLACK;
    protected Color appBorder = Color.LIGHT_GRAY;
    protected float appBorderScale = 0.8f;
    protected int appWidth = 640;
    protected int appHeight = 640;
    protected float appWorldWidth = 1.0f;
    protected float appWorldHeight = 1.0f;
    protected boolean appMaintainRatio = false;
    protected boolean appDisableCursor = false;
    private Vector2f position;
    private Vector2f velocity;
    private float angle = 1.15f;
    private ArrayList<Missle> bullets;
    private ArrayList<Ball> balls;
    private ArrayList<Ship> ships;
    private PolygonWrapper wrapper;
    private StateController controller;
    private ShipFactory factory;

    public MissileCommand() {
        bullets = new ArrayList<Missle>();
        balls = new ArrayList<Ball>();
        ships = new ArrayList<Ship>();
        wrapper = new PolygonWrapper(appWorldWidth, appWorldHeight);
        controller = new StateController();
    }

    protected void createFramework() {
        MouseHandle mh = new MouseHandle();
        canvas = new Canvas();
        canvas.addMouseListener(mh);
        canvas.setBackground(appBackground);
        canvas.setIgnoreRepaint(true);
        getContentPane().add(canvas);
        setLocationByPlatform(true);
        if (appMaintainRatio) {
            getContentPane().setBackground(appBorder);
            setSize(appWidth, appHeight);
            setLayout(null);
            getContentPane().addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    onComponentResized(e);
                }
            });
        } else {
            canvas.setSize(appWidth, appHeight);
            pack();
        }
        setTitle(appTitle);
        //       setupInput(canvas);
        setVisible(true);
        createBufferStrategy(canvas);
        canvas.requestFocus();
        Ball ball = launchBall();
        balls.add(ball);
//        PolygonWrapper wrapper =
//		(PolygonWrapper)controller.getAttribute( "wrapper" );
        
        
        try {
            factory = new ShipFactory( wrapper );
            Element xml = loadXML( "ship.xml" );
            factory.loadFactory( xml );
            Ship ship = factory.createShip();
            ship.setThrusting(true);
            ships.add(ship);
	    controller.setAttribute( "ship-factory", factory );
        } catch (Exception e) {
            e.printStackTrace();
        }
            
    }

    protected void createAndShowGUI(){
        createFramework();
        if (appDisableCursor) {
            disableCursor();
        }
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    private Element loadXML(String path) throws IOException, SAXException,
			ParserConfigurationException {
		InputStream model = ResourceLoader.load(MissileCommand.class,
				"res/assets/xml/" + path, "/xml/" + path);
		Document document = XMLUtility.parseDocument(model);
		return document.getDocumentElement();
	}

    protected void createBufferStrategy(Canvas component) {
        component.createBufferStrategy(2);
        bs = component.getBufferStrategy();
    }

    protected void createBufferStrategy(Window window) {
        window.createBufferStrategy(2);
        bs = window.getBufferStrategy();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MissileCommand app = new MissileCommand();
        app.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                app.shutDown();
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                app.createAndShowGUI();
            }
        });
    }

    @Override
    public void run() {
        running = true;
        initialize();
        long curTime = System.nanoTime();
        long lastTime = curTime;
        double nsPerFrame;
        while (running) {
            curTime = System.nanoTime();
            nsPerFrame = curTime - lastTime;
            gameLoop((float) (nsPerFrame / 1.0E9));
            lastTime = curTime;
        }
        terminate();
    }

    private void gameLoop(float delta) {
//		processInput(delta);
        updateObjects(delta);
        renderFrame();
        sleep(appSleep);
    }

    protected void updateObjects(float delta) {
        ArrayList<Missle> copy = new ArrayList<Missle>(bullets);
        for (Missle bullet : copy) {
            updateBullet(delta, bullet);
        }
        
        ArrayList<Ball> copy2 = new ArrayList<Ball>(balls);
        for (Ball ball : copy2) {
            updateBall(delta, ball);
        }
        
        ArrayList<Ship> copy3 = new ArrayList<Ship>(ships);
        for (Ship ship : copy3) {
            updateShip(delta, ship);
        }

    }

    private void renderFrame() {
        do {
            do {
                Graphics g = null;
                try {
                    g = bs.getDrawGraphics();
                    renderFrame(g);
                } finally {
                    if (g != null) {
                        g.dispose();
                    }
                }
            } while (bs.contentsRestored());
            bs.show();
        } while (bs.contentsLost());
    }

    protected void renderFrame(Graphics g) {
        g.clearRect(0, 0, getScreenWidth(), getScreenHeight());
        render(g);
    }

    protected void initialize() {
        frameRate = new FrameRate();
        frameRate.initialize();
        controller = new StateController();
        controller.setAttribute("app", this);
//        controller.setAttribute("keys", keyboard);
//        controller.setAttribute("ACME", new Acme(this));
        controller.setAttribute("wrapper", 
                new PolygonWrapper(appWorldWidth, appWorldHeight));
        controller.setAttribute("viewport", getViewportTransform());
    }

    protected void terminate() {
    }

    private void sleep(long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException ex) {
        }
    }

    protected void render(Graphics g) {
        g.setFont(appFont);
        g.setColor(appFPSColor);
        frameRate.calculate();
        textPos = Utility.drawString(g, 20, 0, frameRate.getFrameRate() + " " + bullets.size());
        g.setColor(Color.black);
        g.drawLine(400, 0, prevX, prevY);
        g.setColor(Color.red);
//        g.drawLine(400, 0, x, y);
        Matrix3x3f view = getViewportTransform();
        ArrayList<Missle> copy = new ArrayList<Missle>(bullets);
        for (Missle bullet : copy) {
            bullet.draw((Graphics2D) g, view);
        }
        
        ArrayList<Ball> copy2 = new ArrayList<Ball>(balls);
        for (Ball ball : copy2) {
            ball.draw((Graphics2D) g, view);
        }
        
        ArrayList<Ship> copy3 = new ArrayList<Ship>(ships);
        for (Ship ship : copy3) {
            ship.draw((Graphics2D) g, view);
        }
    }

    private void updateBullet(float delta, Missle bullet) {
        bullet.update(delta);
        if (bullet.getPosition().x > 640 || bullet.getPosition().y > 640) {
            bullets.remove(bullet);
        }
        /*else {
			ArrayList<Asteroid> ast = new ArrayList<Asteroid>(asteroids);
			for (Asteroid asteroid : ast) {
				if (asteroid.contains(bullet.getPosition())) {
					remove(asteroid);
					bullets.remove(bullet);
					explosions.add(new AsteroidExplosion(bullet.getPosition()));
					explosion[rand.nextInt(explosion.length)].fire();
				}
			}
		}*/
    }
    
    private void updateBall(float delta, Ball ball) {
        ball.update(delta);
        if (ball.getPosition().x > 640 ) {
            balls.remove(ball);
            Ball newShip = launchBall();
            balls.add(newShip);
        }
        /*else {
			ArrayList<Asteroid> ast = new ArrayList<Asteroid>(asteroids);
			for (Asteroid asteroid : ast) {
				if (asteroid.contains(bullet.getPosition())) {
					remove(asteroid);
					bullets.remove(bullet);
					explosions.add(new AsteroidExplosion(bullet.getPosition()));
					explosion[rand.nextInt(explosion.length)].fire();
				}
			}
		}*/
    }
    
    private void updateShip(float delta, Ship ship) {
        ship.update(delta);
        if (ship.getPosition().x > 640 ) {
            ships.remove(ship);
            Ship newShip = launchShip();
            ships.add(newShip);
        }
        /*else {
			ArrayList<Asteroid> ast = new ArrayList<Asteroid>(asteroids);
			for (Asteroid asteroid : ast) {
				if (asteroid.contains(bullet.getPosition())) {
					remove(asteroid);
					bullets.remove(bullet);
					explosions.add(new AsteroidExplosion(bullet.getPosition()));
					explosion[rand.nextInt(explosion.length)].fire();
				}
			}
		}*/
    }

    protected Matrix3x3f getViewportTransform() {
        return Utility.createViewport(appWorldWidth, appWorldHeight,
                getScreenWidth(), getScreenHeight());
    }

    protected void onComponentResized(ComponentEvent e) {
        Dimension size = getContentPane().getSize();
        setupViewport(size.width, size.height);
        canvas.setLocation(vx, vy);
        canvas.setSize(vw, vh);
    }

    public int getScreenWidth() {
        return canvas.getWidth();
    }

    public int getScreenHeight() {
        return canvas.getHeight();
    }

    protected void setupViewport(int sw, int sh) {
        int w = (int) (sw * appBorderScale);
        int h = (int) (sh * appBorderScale);
        int x = (sw - w) / 2;
        int y = (sh - h) / 2;
        vw = w;
        vh = (int) (w * appWorldHeight / appWorldWidth);
        if (vh > h) {
            vw = (int) (h * appWorldWidth / appWorldHeight);
            vh = h;
        }
        vx = x + (w - vw) / 2;
        vy = y + (h - vh) / 2;
    }

    private void disableCursor() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Image image = tk.createImage("");
        Point point = new Point(0, 0);
        String name = "CanBeAnything";
        Cursor cursor = tk.createCustomCursor(image, point, name);
        setCursor(cursor);
    }

    protected void shutDown() {
        if (Thread.currentThread() != gameThread) {
            try {
                running = false;
                gameThread.join();
                onShutDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(0);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    shutDown();
                }
            });
        }
    }

    protected void onShutDown() {
    }

    public Missle launchBullet(int x, int y) {
        position = new Vector2f();
        angle = angle + .05f;
        if( angle > 2.0)
            angle = 1.15f;
        Vector2f bulletStart = new Vector2f(320,0);
//        Vector2f bulletStart = position.add(Vector2f.polar(angle, 0.0325f));
        Vector2f bulletEnd = new Vector2f(x,y);
        return new Missle(bulletStart, bulletEnd, angle);
    }
    
    public Ball launchBall() {
        position = new Vector2f();
        angle = angle + .05f;
        if( angle > 2.0)
            angle = 1.15f;
        Vector2f shipStart = new Vector2f(0,100);
        Vector2f bulletEnd = new Vector2f(641,100);
        return new Ball(shipStart, bulletEnd,  0);
    }
    
    public Ship launchShip() {
        position = new Vector2f();
        angle = angle + .05f;
        if( angle > 2.0)
            angle = 1.15f;
        Vector2f shipStart = new Vector2f(0,100);
        Vector2f bulletEnd = new Vector2f(641,100);
        Ship ship = factory.createShip();
        ship.setThrusting(true);
        return ship;
    }

    class MouseHandle extends MouseAdapter // second class             
    {

        public void mousePressed(MouseEvent e) {
            x = e.getX();
            y = e.getY();
            System.out.println("You clicked mouse at coordinates " + x + ", " + y);
//            canvas.repaint();
            Missle bull = MissileCommand.this.launchBullet(x,y);
            bullets.add(bull);
        }
    }

}
