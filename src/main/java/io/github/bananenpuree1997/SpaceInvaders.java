package io.github.bananenpuree1997;

import org.davic.resources.ResourceClient;
import org.davic.resources.ResourceProxy;
import org.havi.ui.HContainer;
import org.havi.ui.HDefaultTextLayoutManager;
import org.havi.ui.HGraphicButton;
import org.havi.ui.HScene;
import org.havi.ui.HSceneFactory;
import org.havi.ui.HScreen;
import org.havi.ui.HStaticIcon;
import org.havi.ui.HText;
import org.havi.ui.HVisible;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.tv.xlet.Xlet;
import javax.tv.xlet.XletContext;
import javax.tv.xlet.XletStateChangeException;

public class SpaceInvaders implements Xlet, KeyListener, ResourceClient {

    // The xlet context
    private XletContext context;

    // The screen
    private HScreen screen;

    // The scene
    private HScene scene;

    // The image
    private BufferedImage backgroundImage;
    private BufferedImage buttonImage;
    private Image playerShipImage;
    private Image playerShipHealthImage;
    private Image playerShipLaserImage;
    private Image enemyShipImage;
    private Image enemyShipLaserImage;

    // The assets.font
    private Font font;

    // The menu container
    private HContainer menuContainer;

    private SoundEffect zapSound;
    private SoundEffect laser1Sound;
    private SoundEffect laser2Sound;
    private SoundEffect loseSound;
    private SoundEffect backgroundSound;

    // The health bar of the player, change the array size to increase/decrease health
    private final HStaticIcon[] healthBar = new HStaticIcon[3];

    private Space space;

    // The states of the buttons
    private ButtonStates buttons = new ButtonStates();
    private ButtonStates nextButtons = new ButtonStates(); // Button states for the next cycle

    private final Random random = new Random();

    private static class ButtonStates {

        private boolean left;
        private boolean right;
        private boolean shoot;
    }

    // @Override
    public void destroyXlet(boolean unconditional) throws XletStateChangeException {
        System.out.println("State: DESTROY");

        // Cleanup images
        this.backgroundImage.flush();
        this.backgroundImage = null;
        this.enemyShipLaserImage.flush();
        this.enemyShipLaserImage = null;
        this.enemyShipImage.flush();
        this.enemyShipImage = null;
        this.buttonImage.flush();
        this.buttonImage = null;
        this.playerShipHealthImage.flush();
        this.playerShipHealthImage = null;
        this.playerShipLaserImage.flush();
        this.playerShipLaserImage = null;
        this.playerShipImage.flush();
        this.playerShipImage = null;

        // Cleanup sounds
        this.backgroundSound.dispose();
        this.backgroundSound = null;
        this.loseSound.dispose();
        this.loseSound = null;
        this.zapSound.dispose();
        this.zapSound = null;
        this.laser1Sound.dispose();
        this.laser1Sound = null;
        this.laser2Sound.dispose();
        this.laser2Sound = null;

        // Cleanup the scene
        this.scene.setVisible(false);
        this.scene.removeAll();
        this.scene = null;

        // Notify the context
        this.context.notifyDestroyed();
    }

    // @Override
    public void initXlet(XletContext ctx) throws XletStateChangeException {
        System.out.println("State: INIT");

        this.context = ctx;
        this.screen = HScreen.getDefaultHScreen();

        try {
            this.font = Font.createFont(Font.TRUETYPE_FONT, openStream("assets/font/kenvector_future.ttf"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        // Load the assets.sound effects
        this.zapSound = new SoundEffect("assets/sound/sfx_zap.wav", 100);
        this.laser1Sound = new SoundEffect("assets/sound/sfx_laser1.wav", 100);
        this.laser2Sound = new SoundEffect("assets/sound/sfx_laser2.wav", 100);
        this.loseSound = new SoundEffect("assets/sound/sfx_lose.wav");
        this.backgroundSound = new SoundEffect("assets/sound/background_music.wav");
        this.backgroundSound.setVolume(0.07f);

        // Create a scene which covers the complete screen
        final HSceneFactory hsceneFactory = HSceneFactory.getInstance();
        this.scene = hsceneFactory.getFullScreenScene(HScreen.getDefaultHScreen().getDefaultHGraphicsDevice());
        this.scene.setLayout(null);

        final int width = this.scene.getWidth();
        final int height = this.scene.getHeight();

        // Load the assets.background image and repeat the base image to fill the screen
        this.backgroundImage = loadImage("assets/background/background.png");
        this.backgroundImage = createRepeatedFilledImage(this.backgroundImage, width, height);

        final int menuWidth = (int) ((double) width / 2.3);

        this.buttonImage = loadImage("assets/ui/menu_button.png");

        // Scale the ui height based on the image
        final int buttonHeight = (int) (((double) menuWidth / (double) this.buttonImage.getWidth()) * (double) this.buttonImage.getHeight());
        final int buttonSpacingHeight = (int) (buttonHeight * 0.7);

        final BufferedImage shipHealthImage = loadImage("assets/player/player_life.png");

        final int healthBarIconWidth = (int) ((double) width / 17);
        final int healthBarIconHeight = (int) (((double) healthBarIconWidth / (double) shipHealthImage.getWidth()) * (double) shipHealthImage.getHeight());

        this.playerShipHealthImage = shipHealthImage.getScaledInstance(healthBarIconWidth, healthBarIconHeight, BufferedImage.SCALE_SMOOTH);

        final int healthBarIconSpacing = (int) (healthBarIconWidth * 0.2);
        final int healthBarIconMargin = (int) (healthBarIconWidth * 0.5);
        for (int i = 0; i < this.healthBar.length; i++) {
            this.healthBar[i] = new HStaticIcon(this.playerShipHealthImage, healthBarIconMargin + (healthBarIconWidth + healthBarIconSpacing) * i,
                    healthBarIconMargin, healthBarIconWidth, healthBarIconHeight);
            this.scene.add(this.healthBar[i]);
        }
        updateHealthBar(0);

        final HContainer spaceContainer = new HContainer(0, 0, width, height);
        this.space = new Space(spaceContainer);

        final BufferedImage playerShipImage = loadImage("assets/player/player_ship.png");
        final BufferedImage enemyShipImage = loadImage("assets/invader/invader_ship.png");

        final int shipWidth = (int) ((double) width / 12);
        final int playerShipHeight = (int) (((double) shipWidth / (double) playerShipImage.getWidth()) * (double) playerShipImage.getHeight());
        final int enemyShipHeight = (int) (((double) shipWidth / (double) enemyShipImage.getWidth()) * (double) enemyShipImage.getHeight());

        // Get the rescaled ship images
        this.playerShipImage = playerShipImage.getScaledInstance(shipWidth, playerShipHeight, BufferedImage.SCALE_SMOOTH);
        this.enemyShipImage = enemyShipImage.getScaledInstance(shipWidth, enemyShipHeight, BufferedImage.SCALE_SMOOTH);

        final BufferedImage playerShipLaserImage = loadImage("assets/player/player_laser.png");
        final BufferedImage enemyShipLaserImage = loadImage("assets/invader/invader_laser.png");

        final int laserWidth = shipWidth / 4;
        final int playerLaserHeight = (int) (((double) laserWidth / (double) playerShipLaserImage.getWidth()) * (double) playerShipLaserImage.getHeight());
        final int enemyLaserHeight = (int) (((double) laserWidth / (double) enemyShipLaserImage.getWidth()) * (double) enemyShipLaserImage.getHeight());

        this.playerShipLaserImage = playerShipLaserImage.getScaledInstance(laserWidth, playerLaserHeight, BufferedImage.SCALE_SMOOTH);
        this.enemyShipLaserImage = enemyShipLaserImage.getScaledInstance(laserWidth, enemyLaserHeight, BufferedImage.SCALE_SMOOTH);

        // Create a resized assets.font for the ui text
        final Font buttonFont = newFontWithSize(this.font, (int) ((double) buttonHeight * 0.7));
        final Font titleFont = newFontWithSize(this.font, buttonHeight);

        final List menuComponents = new ArrayList();

        // The start ui
        menuComponents.add(createTextWithoutBackground("Space Invaders", titleFont,
                new Color(255, 204, 0), 0, 0, width, buttonHeight));
        final GraphicalTextButton button = new GraphicalTextButton(this.buttonImage, "Start", buttonFont, menuWidth, buttonHeight);
        button.getButton().addMouseListener(new AbstractMouseListener() {
            // @Override
            public void mouseClicked(MouseEvent e) {
                pressStart();
            }
        });
        menuComponents.add(button);

        final int menuHeight = menuComponents.size() * buttonHeight + (menuComponents.size() - 1) * buttonSpacingHeight;
        final int menuTop = (height - menuHeight) / 3;
        this.menuContainer = new HContainer(0, menuTop, width, menuHeight);

        for (int i = 0; i < menuComponents.size(); i++) {
            final Component component = (Component) menuComponents.get(i);
            component.setBounds((width - component.getWidth()) / 2, i * (buttonHeight + buttonSpacingHeight),
                    component.getWidth(), component.getHeight());
            this.menuContainer.add(component);
        }

        // Add order matters, first added has higher priority
        this.scene.add(this.menuContainer);
        this.scene.add(spaceContainer);

        // Create the assets.background
        final HStaticIcon background = new HStaticIcon(this.backgroundImage, 0, 0, width, height);
        this.scene.add(background);
    }

    // @Override
    public void startXlet() throws XletStateChangeException {
        System.out.println("State: START");

        this.scene.addKeyListener(this);
        this.scene.setVisible(true);
        this.scene.requestFocus();

        this.backgroundSound.loop();
    }

    private void updateHealthBar(int health) {
        // Update the health bar
        for (int i = 0; i < healthBar.length; i++) {
            healthBar[i].setVisible(health > i);
        }
    }

    private void pressStart() {
        // Play a click assets.sound
        this.zapSound.play();
        // Make the menu invisible, let's start the game
        this.menuContainer.setVisible(false);
        // Start this shizzle
        this.space.start();
    }

    private final class Space {

        public final HContainer container;

        // A list with space elements that are currently present
        private final List spaceElements = new ArrayList();

        // The current player player ship, the game is finished when this is null
        private PlayerShip playerShip;

        // Whether the space is running
        private boolean running;

        // The rows with invaders
        private List invaderRows = new ArrayList();

        private double speedModifier = 1;

        private Thread thread;

        private Space(HContainer container) {
            this.container = container;
        }

        public void addElement(SpaceElement element) {
            this.spaceElements.add(element);
            if (element instanceof ComponentSpaceElement) {
                this.container.add(((ComponentSpaceElement) element).component);
            }
        }

        /**
         * Starts the space.
         */
        public void start() {
            final int pulseDelay = 25;
            if (this.thread != null) {
                // Wait for the old thread to stop
                try {
                    this.thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                }
            }
            this.thread = new Thread() {
                // @Override
                public void run() {
                    init();
                    running = true;
                    while (running) {
                        final long startTime = System.currentTimeMillis();
                        scene.repaint();
                        update();
                        final long endTime = System.currentTimeMillis();
                        final long elapsedTime = endTime - startTime;
                        long waitTime = pulseDelay - elapsedTime;
                        if (waitTime < 0) {
                            waitTime = 1;
                        }
                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            throw new RuntimeException();
                        }
                    }
                }
            };
            this.thread.start();
        }

        public Rectangle getAABB() {
            return this.container.getBounds();
        }

        /**
         * Removes all the {@link SpaceElement}s.
         */
        public void clear() {
            this.speedModifier = 1;
            for (int i = 0; i < this.spaceElements.size(); i++) {
                final SpaceElement element = (SpaceElement) this.spaceElements.get(i);
                element.destroy();
            }
        }

        /**
         * Finishes the game.
         */
        public void stop() {
            this.playerShip = null;
            this.running = false;
            loseSound.play();
        }

        private void init() {
            clear();
            waitUntilImageIsLoaded(playerShipImage);
            waitUntilImageIsLoaded(playerShipLaserImage);
            waitUntilImageIsLoaded(enemyShipImage);
            waitUntilImageIsLoaded(enemyShipLaserImage);
            final int height = playerShipImage.getHeight(null);
            final HStaticIcon playerShipIcon = new HStaticIcon(playerShipImage, 100, 100,
                    playerShipImage.getWidth(null), height);
            this.playerShip = new PlayerShip(playerShipIcon);
            this.playerShip.setPosition(new Point2D.Double(scene.getWidth() / 2, (int) (scene.getHeight() - height * 1.5)));
            addElement(this.playerShip);
        }

        private void updateInvaders() {
            final double rowHeight = enemyShipImage.getHeight(null) * 1.6;
            boolean spawnNew = true;
            final Iterator it = this.invaderRows.iterator();
            while (it.hasNext()) {
                final InvaderRow invaderRow = (InvaderRow) it.next();
                if (invaderRow.invaders.isEmpty()) {
                    it.remove();
                    continue;
                }
                invaderRow.update();
                if (invaderRow.aabb.y < rowHeight) {
                    spawnNew = false;
                } else {
                    final InvaderShip invaderShip = (InvaderShip) invaderRow.invaders.get(0);
                    if (invaderShip.getAABB().getMaxY() > scene.getHeight()) {
                        this.playerShip.kill();
                    }
                }
            }
            if (spawnNew) {
                final double rowWidth = scene.getWidth() * 0.8;
                final InvaderRow invaderRow = new InvaderRow(new Rectangle(0, 0, (int) rowWidth, (int) rowHeight), 0.2, 1);
                final double shipWidth = enemyShipImage.getWidth(null);
                final double shipHeight = enemyShipImage.getHeight(null);
                final double reservedShipWidth = shipWidth * 1.3;
                final int ships = (int) (rowWidth / reservedShipWidth);
                final double restShipWidth = rowWidth - ships * reservedShipWidth;
                for (int i = 0; i < ships; i++) {
                    final HStaticIcon playerShipIcon = new HStaticIcon(enemyShipImage, 0, 0, (int) shipWidth, (int) shipHeight);
                    final InvaderShip invaderShip = new InvaderShip(playerShipIcon, invaderRow);
                    invaderShip.setPosition(new Point2D.Double(restShipWidth + reservedShipWidth * i + reservedShipWidth / 2, shipHeight / 2));
                    space.addElement(invaderShip);
                }
                this.invaderRows.add(invaderRow);
            }
        }

        private void update() {
            this.speedModifier += 0.001;
            updateInvaders();
            buttons = nextButtons;
            nextButtons = new ButtonStates();
            for (int i = 0; i < this.spaceElements.size(); i++) {
                final SpaceElement element = (SpaceElement) this.spaceElements.get(i);
                for (int j = 0; j < this.spaceElements.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    final SpaceElement otherElement = (SpaceElement) this.spaceElements.get(j);
                    if (element.getAABB().intersects(otherElement.getAABB())) {
                        element.collideWith(otherElement);
                    }
                }
            }
            for (int i = 0; i < this.spaceElements.size(); i++) {
                final SpaceElement element = (SpaceElement) this.spaceElements.get(i);
                if (!element.isDestroyed()) {
                    element.update();
                }
            }
            final Iterator it = this.spaceElements.iterator();
            while (it.hasNext()) {
                final SpaceElement element = (SpaceElement) it.next();
                if (element.isDestroyed()) {
                    it.remove();
                    if (element instanceof ComponentSpaceElement) {
                        this.container.remove(((ComponentSpaceElement) element).component);
                    }
                }
            }
        }
    }

    private class InvaderRow {

        public final List invaders = new ArrayList();

        private Rectangle aabb;

        private double verticalSpeed;
        private int horizontalMovement;
        private double yPos = 0;

        private InvaderRow(Rectangle aabb, double verticalSpeed, int horizontalSpeed) {
            this.aabb = aabb;
            this.verticalSpeed = verticalSpeed;
            this.horizontalMovement = (random.nextBoolean() ? 1 : -1) * horizontalSpeed;
        }

        void update() {
            final int horizontalMovement = (int) (this.horizontalMovement * space.speedModifier);
            final Rectangle moved = new Rectangle(this.aabb.x + horizontalMovement, this.aabb.y, this.aabb.width, this.aabb.height);
            if (!space.container.getBounds().contains(moved)) {
                this.horizontalMovement *= -1;
                moved.x = this.aabb.x + horizontalMovement;
            }
            this.yPos += this.verticalSpeed * space.speedModifier;
            moved.y = (int) this.yPos;
            this.aabb = moved;
            for (int i = 0; i < this.invaders.size(); i++) {
                final InvaderShip invaderShip = (InvaderShip) this.invaders.get(i);
                if (!invaderShip.isDestroyed()) {
                    final Point2D.Double pos = invaderShip.getPosition();
                    invaderShip.setPosition(new Point2D.Double(pos.x + horizontalMovement, this.yPos));
                }
            }
        }
    }

    private class Ship extends ComponentSpaceElement {

        protected int health;

        protected Ship(Component component, int health) {
            super(component);
            this.health = health;
        }

        // @Override
        public void collideWith(SpaceElement other) {
        }

        // @Override
        public void update() {
            if (this.health <= 0) {
                destroy();
            }
        }
    }

    private final class PlayerShip extends Ship {

        private final int movementSpeed = 7;
        private final int shootDelay = 17;

        private int shootCounter;

        protected PlayerShip(Component component) {
            super(component, healthBar.length);
        }

        // @Override
        public void collideWith(SpaceElement other) {
            super.collideWith(other);
            if (other instanceof InvaderShip) {
                this.health = 0;
            }
        }

        // @Override
        public void update() {
            super.update();

            // Update the health bar
            updateHealthBar(this.health);

            if (buttons.left != buttons.right) {
                final double move;
                if (buttons.left) {
                    move = -movementSpeed;
                } else {
                    move = movementSpeed;
                }
                final Rectangle aabb = getAABB();
                final Rectangle moved = new Rectangle((int) (aabb.x + move), aabb.y, aabb.width, aabb.height);
                if (space.getAABB().contains(moved)) {
                    setAABB(moved);
                }
            }
            this.shootCounter--;
            if (buttons.shoot && this.shootCounter <= 0) {
                final HStaticIcon laserIcon = new HStaticIcon(playerShipLaserImage, 0, 0,
                        playerShipLaserImage.getWidth(null), playerShipLaserImage.getHeight(null));
                final Laser laser = new Laser(laserIcon, -10, PlayerShip.class);
                laser.setPosition(new Point2D.Double(getPosition().x, getAABB().y));
                space.addElement(laser);
                (random.nextBoolean() ? laser1Sound : laser2Sound).play();
                this.shootCounter = this.shootDelay;
            }
        }

        // @Override
        public void destroy() {
            super.destroy();
            kill();
        }

        public void kill() {
            this.health = 0;

            // Update the health bar
            updateHealthBar(this.health);

            // Show death screen
            space.stop();

            // Show menu
            menuContainer.setVisible(true);
        }
    }

    private final class InvaderShip extends Ship {

        private final InvaderRow row;

        private final int shootDelay = 17;
        private int shootCounter;

        private InvaderShip(Component component, InvaderRow row) {
            super(component, 1);
            this.row = row;
            this.row.invaders.add(this);
        }

        // @Override
        public void destroy() {
            super.destroy();
            this.row.invaders.remove(this);
        }

        // @Override
        public void update() {
            super.update();

            this.shootCounter--;
            if (random.nextInt(1000) == 0 && this.shootCounter <= 0) {
                final HStaticIcon laserIcon = new HStaticIcon(enemyShipLaserImage, 0, 0,
                        enemyShipLaserImage.getWidth(null), enemyShipLaserImage.getHeight(null));
                final Laser laser = new Laser(laserIcon, 10, InvaderShip.class);
                laser.setPosition(new Point2D.Double(getPosition().x, getAABB().y + getAABB().height));
                space.addElement(laser);
                this.shootCounter = this.shootDelay;
            }
        }
    }

    /**
     * A laser is a projectile shot by a
     * {@link InvaderShip} or {@link PlayerShip}.
     */
    private final class Laser extends ComponentSpaceElement {

        private final int movementSpeed;
        private final Class friendlyShip;

        private Laser(Component component, int movementSpeed, Class friendlyShip) {
            super(component);
            this.movementSpeed = movementSpeed;
            this.friendlyShip = friendlyShip;
        }

        // @Override
        public void collideWith(SpaceElement other) {
            if (other instanceof Ship && !this.friendlyShip.isInstance(other)) {
                final Ship ship = (Ship) other;
                ship.health--; // Damage the player ship and destroy the laser
                destroy();
            }
        }

        // @Override
        public void update() {
            super.update();
            final Point2D.Double pos = getPosition();
            // The laser goes outside the space we view, destroy it
            if (!space.getAABB().contains(pos) && !space.getAABB().intersects(getAABB())) {
                destroy();
            } else {
                setPosition(new Point2D.Double(pos.x, pos.y + this.movementSpeed));
            }
        }
    }

    private abstract class ComponentSpaceElement extends SpaceElement {

        private final Component component;

        protected ComponentSpaceElement(Component component) {
            this.component = component;
            updatePosition();
        }

        // @Override
        public Rectangle getAABB() {
            return this.component.getBounds();
        }

        // @Override
        protected void setAABB(Rectangle aabb) {
            this.component.setBounds(aabb);
            updatePosition();
        }
    }

    /**
     * Represents a element that exists in space and
     * can collide with another ones.
     */
    private static abstract class SpaceElement {

        private boolean destroyed;
        private Point2D.Double position;

        /**
         * Gets the axis aligned bounding box of this collision element.
         *
         * @return The axis aligned bounding box
         */
        public abstract Rectangle getAABB();

        protected void updatePosition() {
            final Rectangle aabb = getAABB();
            this.position = new Point2D.Double(aabb.getCenterX(), aabb.getCenterY());
        }

        /**
         * Gets the position of the space element. This is usually
         * the center of the {@link #getAABB()}.
         *
         * @return The position
         */
        public Point2D.Double getPosition() {
            return this.position;
        }

        /**
         * Sets the position of the space element. This is usually
         * the center of the {@link #getAABB()}.
         *
         * @param position The position
         */
        public void setPosition(Point2D.Double position) {
            this.position = position;
            final Rectangle aabb = getAABB();
            setAABB(new Rectangle((int) (position.x - (double) aabb.width / 2.0),
                    (int) (position.y - (double) aabb.height / 2), aabb.width, aabb.height));
        }

        protected abstract void setAABB(Rectangle aabb);

        /**
         * Is called when this {@link SpaceElement} collides with the other element.
         *
         * @param other The collision element that is collided with
         */
        public abstract void collideWith(SpaceElement other);

        /**
         * Gets whether this space element is destroyed.
         *
         * @return Is destroyed
         */
        public boolean isDestroyed() {
            return this.destroyed;
        }

        /**
         * Destroys this space element.
         */
        public void destroy() {
            this.destroyed = true;
        }

        public void update() {
        }
    }

    // @Override
    public void pauseXlet() {
        System.out.println("State: PAUSE");
    }

    // @Override
    public void keyTyped(KeyEvent e) {}

    // @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!this.space.running) {
                pressStart();
            }
            this.nextButtons.shoot = true;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            this.nextButtons.left = true;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            this.nextButtons.right = true;
        }
    }

    // @Override
    public void keyReleased(KeyEvent e) {
    }

    // @Override
    public boolean requestRelease(ResourceProxy proxy, Object requestData) {
        return false;
    }

    // @Override
    public void release(ResourceProxy proxy) {}

    // @Override
    public void notifyRelease(ResourceProxy proxy) {}

    private static class GraphicalTextButton extends HContainer {

        private final HGraphicButton button;
        private final HText text;

        public GraphicalTextButton(Image image, String text, Font font, int width, int height) {
            super(0, 0, width, height);

            this.text = new HText(text, 0, 0, width, height);
            this.text.setFont(font);
            this.text.setBackgroundMode(HVisible.NO_BACKGROUND_FILL);
            add(this.text);

            this.button = new HGraphicButton(image.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, width, height);
            add(this.button);
        }

        public void setPosition(int x, int y) {
            super.setBounds(x, y, getWidth(), getHeight());
        }

        public HGraphicButton getButton() {
            return this.button;
        }

        public HText getText() {
            return this.text;
        }
    }

    private static Image getScaledImage(BufferedImage image, int width) {
        final int height = (int) (((double) width / (double) image.getWidth()) * (double) image.getHeight());
        return image.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
    }

    private static InputStream openStream(String path) {
        try {
            // Covert to URL to avoid a XleTView bug
            return new File(path).toURL().openStream();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private static HText createTextWithoutBackground(String text, Font font, Color color, int x, int y, int width, int height) {
        final HText hText = new HText(text, x, y, width, height, font, color, color, new HDefaultTextLayoutManager());
        hText.setBackgroundMode(HVisible.NO_BACKGROUND_FILL);
        return hText;
    }

    private static Font newFontWithSize(Font font, final int fontSize) {
        return new Font(font) {
            {
                this.size = fontSize;
                this.pointSize = fontSize;
            }
        };
    }

    /**
     * Waits for the {@link Image} to be loaded.
     *
     * @param image The image
     */
    private static void waitUntilImageIsLoaded(Image image) {
        final Object lock = new Object();
        // Trigger the image to load
        final int width = image.getWidth(new ImageObserver() {
            // @Override
            public boolean imageUpdate(Image img, int infoFlags, int x, int y, int width, int height) {
                synchronized (lock) {
                    lock.notifyAll();
                }
                return true;
            }
        });
        if (width == -1) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                }
            }
        }
    }

    /**
     * Loads the {@link Image} at the given path.
     *
     * @param path The path
     * @return The image
     */
    private static BufferedImage loadImage(String path) {
        try {
            // Covert to URL to avoid a XleTView bug
            return ImageIO.read(new File(path).toURL());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * Creates a {@link Image} of the given width and height, the input {@link Image}
     * will be repeatedly copied to the output {@link Image} until its filled.
     *
     * @param input The input image
     * @param width The output width
     * @param height The output height
     * @return The output image
     */
    private static BufferedImage createRepeatedFilledImage(Image input, int width, int height) {
        final BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        waitUntilImageIsLoaded(input);
        final int iw = input.getWidth(null);
        final int ih = input.getHeight(null);
        int x = 0;
        int y = 0;
        final Graphics g = output.createGraphics();
        while (y < height) {
            while (x < width) {
                int dx = x + iw;
                if (dx > width) {
                    dx = width;
                }
                int dy = y + ih;
                if (dy > height) {
                    dy = height;
                }
                g.drawImage(input, x, y, dx, dy, 0, 0, dx - x, dy - y, null);
                x += iw;
            }
            x = 0;
            y += ih;
        }
        g.dispose();
        return output;
    }
}
