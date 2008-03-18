package ca.shu.ui.lib;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.KeyEventDispatcher;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.Iterator;

import javax.swing.FocusManager;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import ca.shu.ui.lib.Style.Style;
import ca.shu.ui.lib.actions.ActionException;
import ca.shu.ui.lib.actions.ExitAction;
import ca.shu.ui.lib.actions.ReversableActionManager;
import ca.shu.ui.lib.actions.StandardAction;
import ca.shu.ui.lib.misc.ShortcutKey;
import ca.shu.ui.lib.util.UIEnvironment;
import ca.shu.ui.lib.util.menus.MenuBuilder;
import ca.shu.ui.lib.world.elastic.ElasticWorld;
import ca.shu.ui.lib.world.piccolo.WorldImpl;
import ca.shu.ui.lib.world.piccolo.objects.Window;
import ca.shu.ui.lib.world.piccolo.primitives.PXGrid;
import ca.shu.ui.lib.world.piccolo.primitives.Universe;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.util.PDebug;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.util.PUtil;

/**
 * This class is based on PFrame by Jesse Grosjean
 * 
 * @author Shu Wu
 */
public abstract class AppFrame extends JFrame {
	private static final long serialVersionUID = 2769082313231407201L;

	/**
	 * Name of the directory where UI Files are stored
	 */
	public static final String USER_FILE_DIR = "UIFiles";

	/**
	 * A String which briefly describes some commands used in this application
	 */
	public static final String WORLD_TIPS = "<H3>Keyboard</H3>"
			+ "Interaction modes: Press 's' to switch modes<BR>"
			+ "Searching: Press 'f', then start typing.<BR>"
			+ "Tooltips: Mouse over a node and hold down 'ctrl' to view tooltips<BR>"
			+ "<BR><H3>Mouse</H3>"
			+ "Context menu: Right click opens a context menu on most objects<BR>"
			+ "Zooming: Scroll the mouse wheel or right click and drag";

	private ReversableActionManager actionManager;

	private EventListener escapeFullScreenModeListener;

	private GraphicsDevice graphicsDevice;

	private boolean isFullScreenMode;

	private UserPreferences preferences;

	private ShortcutKey[] shortcutKeys;

	private Window topWindow;

	private Universe universe;

	private MenuBuilder worldMenu;

	protected MenuBuilder editMenu;

	/**
	 * @param title
	 *            Title of application
	 */
	public AppFrame() {
		super(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration());

		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						initialize();
					}
				});
			} catch (InvocationTargetException e) {
				e.getTargetException().printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			initialize();
		}

	}

	/**
	 * Initializes the menu
	 */
	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBorder(null);
		Style.applyMenuStyle(menuBar, true);

		MenuBuilder fileMenu = new MenuBuilder("File");
		fileMenu.getJMenu().setMnemonic(KeyEvent.VK_S);
		initFileMenu(fileMenu);
		fileMenu.addAction(new ExitAction(this, "Quit"), KeyEvent.VK_P);
		menuBar.add(fileMenu.getJMenu());

		editMenu = new MenuBuilder("Edit");
		editMenu.getJMenu().setMnemonic(KeyEvent.VK_E);

		menuBar.add(editMenu.getJMenu());

		initViewMenu(menuBar);

		worldMenu = new MenuBuilder("Options");
		worldMenu.getJMenu().setMnemonic(KeyEvent.VK_O);
		menuBar.add(worldMenu.getJMenu());

		updateWorldMenu();
		updateEditMenu();

		MenuBuilder helpMenu = new MenuBuilder("Help");
		helpMenu.getJMenu().setMnemonic(KeyEvent.VK_H);
		menuBar.add(helpMenu.getJMenu());
		helpMenu.addAction(new ShortcutKeysHelpAction("Shortcuts"), KeyEvent.VK_S);
		helpMenu.addAction(new TipsAction("Tips and Commands", false), KeyEvent.VK_T);
		helpMenu.addAction(new AboutAction("About"), KeyEvent.VK_A);

		menuBar.setVisible(true);
		this.setJMenuBar(menuBar);
	}

	protected void chooseBestDisplayMode(GraphicsDevice device) {
		DisplayMode best = getBestDisplayMode(device);
		if (best != null) {
			device.setDisplayMode(best);
		}
	}

	protected PCamera createDefaultCamera() {
		return PUtil.createBasicScenegraph();
	}

	protected ElasticWorld createWorld() {
		return new ElasticWorld("Top world");
	}

	@SuppressWarnings("unchecked")
	protected DisplayMode getBestDisplayMode(GraphicsDevice device) {
		Iterator itr = getPreferredDisplayModes(device).iterator();
		while (itr.hasNext()) {
			DisplayMode each = (DisplayMode) itr.next();
			DisplayMode[] modes = device.getDisplayModes();
			for (DisplayMode element : modes) {
				if (element.getWidth() == each.getWidth()
						&& element.getHeight() == each.getHeight()
						&& element.getBitDepth() == each.getBitDepth()) {
					return each;
				}
			}
		}

		return null;
	}

	/**
	 * By default return the current display mode. Subclasses may override this
	 * method to return other modes in the collection.
	 */
	@SuppressWarnings("unchecked")
	protected Collection getPreferredDisplayModes(GraphicsDevice device) {
		ArrayList<DisplayMode> result = new ArrayList<DisplayMode>();

		result.add(device.getDisplayMode());
		/*
		 * result.add(new DisplayMode(640, 480, 32, 0)); result.add(new
		 * DisplayMode(640, 480, 16, 0)); result.add(new DisplayMode(640, 480,
		 * 8, 0));
		 */

		return result;
	}

	protected ShortcutKey[] getShortcutKeys() {
		return shortcutKeys;
	}

	/**
	 * Use this function to add menu items to the frame menu bar
	 * 
	 * @param menuBar
	 *            is attached to the frame
	 */
	protected void initFileMenu(MenuBuilder menu) {

	}

	protected void initialize() {
		/*
		 * Initialize shortcut keys
		 */
		FocusManager.getCurrentManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			public boolean dispatchKeyEvent(KeyEvent e) {
				if (getShortcutKeys() != null && e.getID() == KeyEvent.KEY_PRESSED) {
					for (ShortcutKey shortcutKey : getShortcutKeys()) {
						if (shortcutKey.getModifiers() == e.getModifiers()) {
							if (shortcutKey.getKeyCode() == e.getKeyCode()) {
								shortcutKey.getAction().doAction();
								return true;
							}
						}
					}
				}
				return false;
			}
		});

		graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		loadPreferences();
		UIEnvironment.setInstance(this);

		if (preferences.isWelcomeScreen()) {
			preferences.setWelcomeScreen(false);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

					(new TipsAction("", true)).doAction();
				}
			});
		}

		restoreDefaultTitle();

		actionManager = new ReversableActionManager(this);
		getContentPane().setLayout(new BorderLayout());

		universe = new Universe();
		universe.setMinimumSize(new Dimension(200, 200));
		universe.setPreferredSize(new Dimension(400, 400));
		universe.initialize(createWorld());
		universe.setFocusable(true);

		// getContentPane().add(canvas);
		// canvas.setPreferredSize(new Dimension(200, 200));

		initLayout(universe);

		setBounds(new Rectangle(100, 100, 800, 600));
		setBackground(null);
		addWindowListener(new MyWindowListener());

		try {
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		universe.setSelectionMode(false);

		initMenu();
		validate();
		setFullScreenMode(false);

	}

	protected void initLayout(Universe canvas) {
		Container cp = getContentPane();

		cp.add(canvas);

		canvas.requestFocus();
	}

	/**
	 * Use this function to add menu items to the frame menu bar
	 * 
	 * @param menuBar
	 *            is attached to the frame
	 */
	protected void initViewMenu(JMenuBar menuBar) {

	}

	/**
	 * Loads saved preferences related to the application
	 */
	protected void loadPreferences() {
		File preferencesFile = new File(getUserFileDirectory(), "userSettings");

		if (preferencesFile.exists()) {
			FileInputStream fis;
			try {
				fis = new FileInputStream(preferencesFile);

				ObjectInputStream ois = new ObjectInputStream(fis);
				try {
					preferences = (UserPreferences) ois.readObject();
				} catch (ClassNotFoundException e) {
					System.out.println("Could not load preferences");
				}
			} catch (IOException e1) {
				System.out.println("Could not read preferences file");
			}
		}

		if (preferences == null) {
			preferences = new UserPreferences();

		}
		preferences.apply(this);
	}

	/**
	 * Save preferences to file
	 */
	protected void savePreferences() {
		File file = new File(getUserFileDirectory());
		if (!file.exists())
			file.mkdir();

		File preferencesFile = new File(getUserFileDirectory(), "userSettings");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);

			oos.writeObject(preferences);

			FileOutputStream fos = new FileOutputStream(preferencesFile);
			fos.write(bos.toByteArray());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates the menu 'edit'
	 */
	protected void updateEditMenu() {
		editMenu.reset();

		editMenu.addAction(new UndoAction(), KeyEvent.VK_Z, KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				ActionEvent.CTRL_MASK));

		editMenu.addAction(new RedoAction(), KeyEvent.VK_Y, KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				ActionEvent.CTRL_MASK));

	}

	/**
	 * Updates the menu 'world'
	 */
	protected void updateWorldMenu() {
		worldMenu.reset();
		worldMenu.addAction(new MinimizeAllWindows(), KeyEvent.VK_M);

		if (!isFullScreenMode) {
			// worldMenu.addAction(new TurnOnFullScreen(), KeyEvent.VK_F);
		} else {
			// worldMenu.addAction(new TurnOffFullScreen(), KeyEvent.VK_F);
		}

		if (!preferences.isEnableTooltips()) {
			worldMenu.addAction(new TurnOnTooltips(), KeyEvent.VK_T);
		} else {
			worldMenu.addAction(new TurnOffTooltips(), KeyEvent.VK_T);
		}

		if (!PXGrid.isGridVisible()) {
			worldMenu.addAction(new TurnOnGrid(), KeyEvent.VK_G);
		} else {
			worldMenu.addAction(new TurnOffGrid(), KeyEvent.VK_G);
		}

		if (!universe.isSelectionMode()) {
			worldMenu.addAction(new SwitchToSelectionMode(), KeyEvent.VK_S);
		} else {
			worldMenu.addAction(new SwitchToNavigationMode(), KeyEvent.VK_S);
		}

		MenuBuilder qualityMenu = worldMenu.addSubMenu("Rendering Quality");

		qualityMenu.getJMenu().setMnemonic(KeyEvent.VK_Q);

		qualityMenu.addAction(new LowQualityAction(), KeyEvent.VK_L);
		qualityMenu.addAction(new MediumQualityAction(), KeyEvent.VK_M);
		qualityMenu.addAction(new HighQualityAction(), KeyEvent.VK_H);

		MenuBuilder debugMenu = worldMenu.addSubMenu("Debug");
		debugMenu.getJMenu().setMnemonic(KeyEvent.VK_E);

		if (!PDebug.debugPrintUsedMemory) {
			debugMenu.addAction(new ShowDebugMemory(), KeyEvent.VK_S);
		} else {
			debugMenu.addAction(new HideDebugMemory(), KeyEvent.VK_H);
		}
	}

	public boolean addActivity(PActivity activity) {
		return universe.getRoot().addActivity(activity);
	}

	/**
	 * This method adds a key listener that will take this PFrame out of full
	 * screen mode when the escape key is pressed. This is called for you
	 * automatically when the frame enters full screen mode.
	 */
	public void addEscapeFullScreenModeListener() {
		removeEscapeFullScreenModeListener();
		escapeFullScreenModeListener = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent aEvent) {
				if (aEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
					setFullScreenMode(false);
				}
			}
		};
		universe.addKeyListener((KeyListener) escapeFullScreenModeListener);
	}

	public void addWorldWindow(Window window) {
		universe.getWorld().getSky().addChild(window);
	}

	/**
	 * Called when the user closes the Application window
	 */
	public void exitAppFrame() {
		savePreferences();
		System.exit(0);
	}

	/**
	 * @return String which describes what the application is about
	 */
	public abstract String getAboutString();

	/**
	 * @return Action manager responsible for managing actions. Enables undo,
	 *         redo functionality.
	 */
	public ReversableActionManager getActionManager() {
		return actionManager;
	}

	/**
	 * @return Name of the application
	 */
	public abstract String getAppName();

	public abstract String getAppWindowTitle();

	public Window getTopWindow() {
		return topWindow;
	}

	/**
	 * @return Canvas which hold the zoomable UI
	 */
	public Universe getUniverse() {
		return universe;
	}

	/**
	 * @return Name of the directory to store user files
	 */
	public String getUserFileDirectory() {
		return USER_FILE_DIR;
	}

	/**
	 * @return the top-most World associated with this frame
	 */
	public ElasticWorld getWorld() {
		return universe.getWorld();
	}

	/**
	 * This method removes the escape full screen mode key listener. It will be
	 * called for you automatically when full screen mode exits, but the method
	 * has been made public for applications that wish to use other methods for
	 * exiting full screen mode.
	 */
	public void removeEscapeFullScreenModeListener() {
		if (escapeFullScreenModeListener != null) {
			universe.removeKeyListener((KeyListener) escapeFullScreenModeListener);
			escapeFullScreenModeListener = null;
		}
	}

	public void restoreDefaultTitle() {
		setTitle(getAppWindowTitle());
	}

	/**
	 * Called when reversable actions have changed. Updates the edit menu.
	 */
	public void reversableActionsUpdated() {
		updateEditMenu();
	}

	/**
	 * @param fullScreenMode
	 *            sets the screen to fullscreen
	 */
	public void setFullScreenMode(boolean fullScreenMode) {
		this.isFullScreenMode = fullScreenMode;
		if (fullScreenMode) {
			addEscapeFullScreenModeListener();

			if (isDisplayable()) {
				dispose();
			}

			setUndecorated(true);
			setResizable(false);
			graphicsDevice.setFullScreenWindow(this);

			if (graphicsDevice.isDisplayChangeSupported()) {
				chooseBestDisplayMode(graphicsDevice);
			}
			validate();
		} else {
			removeEscapeFullScreenModeListener();

			if (isDisplayable()) {
				dispose();
			}

			setUndecorated(false);
			setResizable(true);
			graphicsDevice.setFullScreenWindow(null);
			validate();
			setVisible(true);
		}
	}

	public void setShortcutKeys(ShortcutKey[] shortcutKeys) {
		this.shortcutKeys = shortcutKeys;
	}

	public void setTopWindow(Window window) {
		topWindow = window;
		if (topWindow != null) {
			setTitle(window.getName() + " - " + getAppWindowTitle());
		} else {
			UIEnvironment.getInstance().restoreDefaultTitle();
		}
	}

	/**
	 * Action to set rendering mode to high quality.
	 * 
	 * @author Shu Wu
	 */
	protected class HighQualityAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		public HighQualityAction() {
			super("High Quality");
		}

		@Override
		protected void action() throws ActionException {
			getUniverse().setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
			getUniverse().setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
			getUniverse().setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
			updateWorldMenu();
		}

	}

	/**
	 * Action to show the 'about' dialog
	 * 
	 * @author Shu Wu
	 */
	class AboutAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		public AboutAction(String actionName) {
			super("About", actionName);
		}

		@Override
		protected void action() throws ActionException {
			JLabel editor = new JLabel("<html>" + getAboutString() + "</html>");
			JOptionPane.showMessageDialog(UIEnvironment.getInstance(), editor, "About "
					+ getAppName(), JOptionPane.PLAIN_MESSAGE);
		}

	}

	/**
	 * Action to hide debug memory messages printed to the console.
	 * 
	 * @author Shu Wu
	 */
	class HideDebugMemory extends StandardAction {

		private static final long serialVersionUID = 1L;

		public HideDebugMemory() {
			super("Stop printing Memory Used to console");
		}

		@Override
		protected void action() throws ActionException {
			PDebug.debugPrintUsedMemory = false;
			updateWorldMenu();
		}

	}

	/**
	 * Action to set rendering mode to low quality.
	 * 
	 * @author Shu Wu
	 */
	class LowQualityAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		public LowQualityAction() {
			super("Low Quality");
		}

		@Override
		protected void action() throws ActionException {
			getUniverse().setDefaultRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
			getUniverse().setAnimatingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
			getUniverse().setInteractingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
			updateWorldMenu();
		}

	}

	/**
	 * Action to set rendering mode to medium quality.
	 * 
	 * @author Shu Wu
	 */
	class MediumQualityAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		public MediumQualityAction() {
			super("Medium Quality");
		}

		@Override
		protected void action() throws ActionException {
			getUniverse().setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
			getUniverse().setAnimatingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
			getUniverse().setInteractingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
			updateWorldMenu();
		}

	}

	/**
	 * Minimizes all windows in the top-level world
	 * 
	 * @author Shu Wu
	 */
	class MinimizeAllWindows extends StandardAction {

		private static final long serialVersionUID = 1L;

		public MinimizeAllWindows() {
			super("Minimize all windows");
		}

		@Override
		protected void action() throws ActionException {
			getWorld().minimizeAllWindows();

		}

	}

	/**
	 * Listener which listens for Application window close events
	 * 
	 * @author Shu Wu
	 */
	class MyWindowListener implements WindowListener {

		public void windowActivated(WindowEvent arg0) {
		}

		public void windowClosed(WindowEvent arg0) {

		}

		public void windowClosing(WindowEvent arg0) {
			AppFrame.this.exitAppFrame();
		}

		public void windowDeactivated(WindowEvent arg0) {
		}

		public void windowDeiconified(WindowEvent arg0) {
		}

		public void windowIconified(WindowEvent arg0) {
		}

		public void windowOpened(WindowEvent arg0) {
		}

	}

	/**
	 * Action to redo the last reversable action
	 * 
	 * @author Shu Wu
	 */
	class RedoAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		public RedoAction() {
			super("Redo: " + actionManager.getRedoActionDescription());
			if (!actionManager.canRedo()) {
				setEnabled(false);
			}
		}

		@Override
		protected void action() throws ActionException {
			actionManager.redoAction();

		}

	}

	/**
	 * Show shortcut keys
	 * 
	 * @author Shu Wu
	 */
	/**
	 * Action to show the 'about' dialog
	 * 
	 * @author Shu Wu
	 */
	class ShortcutKeysHelpAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		public ShortcutKeysHelpAction(String actionName) {
			super("Short shortcut keys", actionName);
		}

		@Override
		protected void action() throws ActionException {
			StringBuilder shortcutKeysString = new StringBuilder(400);

			shortcutKeysString.append("<h3>" + getAppName() + " Shortcuts</h3>");
			if (getShortcutKeys().length == 0) {
				shortcutKeysString.append("No shortcuts available");
			} else {
				for (ShortcutKey shortcutKey : getShortcutKeys()) {
					if ((shortcutKey.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
						shortcutKeysString.append("CTRL ");
					}
					if ((shortcutKey.getModifiers() & KeyEvent.ALT_MASK) != 0) {
						shortcutKeysString.append("ALT ");
					}
					if ((shortcutKey.getModifiers() & KeyEvent.SHIFT_MASK) != 0) {
						shortcutKeysString.append("SHIFT ");
					}

					shortcutKeysString.append((char) shortcutKey.getKeyCode());
					shortcutKeysString.append(" >> ");
					shortcutKeysString.append(shortcutKey.getAction().getDescription());
					shortcutKeysString.append("<br>");
				}
			}

			JLabel editor = new JLabel("<html>" + shortcutKeysString.toString() + "</html>");
			JOptionPane.showMessageDialog(UIEnvironment.getInstance(), editor, "Shortcuts - "
					+ getAppName(), JOptionPane.PLAIN_MESSAGE);
		}

	}

	/**
	 * Action to enable the printing of memory usage messages to the console
	 * 
	 * @author Shu Wu
	 */
	class ShowDebugMemory extends StandardAction {

		private static final long serialVersionUID = 1L;

		public ShowDebugMemory() {
			super("Print Memory Used to console");
		}

		@Override
		protected void action() throws ActionException {
			PDebug.debugPrintUsedMemory = true;
			updateWorldMenu();
		}

	}

	/**
	 * Action to switch to navigation mode
	 * 
	 * @author Shu Wu
	 */
	class SwitchToNavigationMode extends StandardAction {

		private static final long serialVersionUID = 1L;

		public SwitchToNavigationMode() {
			super("Switch to Navigation Mode");
		}

		@Override
		protected void action() throws ActionException {
			universe.setSelectionMode(false);
			updateWorldMenu();
		}

	}

	/**
	 * Action to switch to selection mode
	 * 
	 * @author Shu Wu
	 */
	class SwitchToSelectionMode extends StandardAction {

		private static final long serialVersionUID = 1L;

		public SwitchToSelectionMode() {
			super("Switch to Selection Mode");
		}

		@Override
		protected void action() throws ActionException {
			universe.setSelectionMode(true);
			updateWorldMenu();
		}

	}

	/**
	 * Action which shows the tips dialog
	 * 
	 * @author Shu Wu
	 */
	class TipsAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		private boolean welcome;

		public TipsAction(String actionName, boolean isWelcomeScreen) {
			super("Show UI tips", actionName);

			this.welcome = isWelcomeScreen;
		}

		@Override
		protected void action() throws ActionException {
			JLabel editor;
			if (welcome) {
				String appendum = "To show this message again, click <b>Help -> Tips and Commands</b>";
				editor = new JLabel("<html><H2>Welcome to " + getAppName() + "</H2>" + WORLD_TIPS
						+ "<BR><BR>" + appendum + "</html>");
			} else {
				editor = new JLabel("<html>" + WORLD_TIPS + "</html>");
			}

			JOptionPane.showMessageDialog(UIEnvironment.getInstance(), editor, getAppName()
					+ " Tips", JOptionPane.PLAIN_MESSAGE);
		}
	}

	/**
	 * Action to turn off full screen mode
	 * 
	 * @author Shu Wu
	 */
	class TurnOffFullScreen extends StandardAction {

		private static final long serialVersionUID = 1L;

		public TurnOffFullScreen() {
			super("Full screen off");
		}

		@Override
		protected void action() throws ActionException {
			setFullScreenMode(false);
			updateWorldMenu();
		}

	}

	/**
	 * Action to turn off the grid
	 * 
	 * @author Shu Wu
	 */
	class TurnOffGrid extends StandardAction {

		private static final long serialVersionUID = 1L;

		public TurnOffGrid() {
			super("Grid off");

		}

		@Override
		protected void action() throws ActionException {
			preferences.setGridVisible(false);
			updateWorldMenu();
		}

	}

	/**
	 * Action to turn off tooltips
	 * 
	 * @author Shu Wu
	 */
	class TurnOffTooltips extends StandardAction {

		private static final long serialVersionUID = 1L;

		public TurnOffTooltips() {
			super("Autoshow Tooltips off");
		}

		@Override
		protected void action() throws ActionException {
			preferences.setEnableTooltips(false);
			updateWorldMenu();
		}

	}

	/**
	 * Action to turn on full screen mode
	 * 
	 * @author Shu Wu
	 */
	class TurnOnFullScreen extends StandardAction {

		private static final long serialVersionUID = 1L;

		public TurnOnFullScreen() {
			super("Full screen on");
		}

		@Override
		protected void action() throws ActionException {
			setFullScreenMode(true);
			updateWorldMenu();
		}

	}

	/**
	 * Action to turn on the grid
	 * 
	 * @author Shu Wu
	 */
	class TurnOnGrid extends StandardAction {

		private static final long serialVersionUID = 1L;

		public TurnOnGrid() {
			super("Grid on");
		}

		@Override
		protected void action() throws ActionException {
			preferences.setGridVisible(true);

			updateWorldMenu();
		}

	}

	/**
	 * Action to turn on tooltips
	 * 
	 * @author Shu Wu
	 */
	class TurnOnTooltips extends StandardAction {

		private static final long serialVersionUID = 1L;

		public TurnOnTooltips() {
			super("Autoshow Tooltips on");
		}

		@Override
		protected void action() throws ActionException {
			preferences.setEnableTooltips(true);
			updateWorldMenu();
		}

	}

	/**
	 * Action which undos the last reversable action
	 * 
	 * @author Shu Wu
	 */
	class UndoAction extends StandardAction {

		private static final long serialVersionUID = 1L;

		public UndoAction() {
			super("Undo: " + actionManager.getUndoActionDescription());
			if (!actionManager.canUndo()) {
				setEnabled(false);
			}
		}

		@Override
		protected void action() throws ActionException {
			actionManager.undoAction();
		}
	}

}

/**
 * Serializable object which contains UI preferences of the application
 * 
 * @author Shu Wu
 */
/**
 * @author Shu
 */
class UserPreferences implements Serializable {

	private static final long serialVersionUID = 1L;
	private boolean enableTooltips = true;
	private boolean gridVisible = true;
	private boolean isWelcomeScreen = true;

	/**
	 * Applies preferences
	 * 
	 * @param applyTo
	 *            The application in which to apply the preferences to
	 */
	public void apply(AppFrame applyTo) {
		setEnableTooltips(enableTooltips);
		setGridVisible(gridVisible);
	}

	public boolean isEnableTooltips() {
		return enableTooltips;
	}

	public boolean isGridVisible() {
		return gridVisible;
	}

	public boolean isWelcomeScreen() {
		return isWelcomeScreen;
	}

	public void setEnableTooltips(boolean enableTooltips) {
		this.enableTooltips = enableTooltips;
		WorldImpl.setTooltipsVisible(this.enableTooltips);
	}

	public void setGridVisible(boolean gridVisible) {
		this.gridVisible = gridVisible;
		PXGrid.setGridVisible(gridVisible);
	}

	public void setWelcomeScreen(boolean isWelcomeScreen) {
		this.isWelcomeScreen = isWelcomeScreen;
	}

}