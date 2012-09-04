package net.ftb.gui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JRadioButton;
import javax.swing.JButton;

import net.ftb.data.LoginResponse;
import net.ftb.data.PasswordSettings;
import net.ftb.data.Settings;
import net.ftb.workers.GameUpdateWorker;
import net.ftb.workers.LoginWorker;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JCheckBox;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Color;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.wb.swing.FocusTraversalOnArray;

public class LaunchFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	JRadioButton modPack1RB;
	JRadioButton modPack2RB;
	JRadioButton modPack3RB;
	JRadioButton modPack4RB;
	JPanel loginPanel;
	JButton btnPlayOffline;
	private PasswordSettings passwordSettings;
	LoginResponse RESPONSE;
	JCheckBox chckbxRemember;
	JButton btnOptions;
	JLabel lblError;
	JButton btnLogin;
	private JPanel contentPane;
	private JTextField usernameField;
	private JPasswordField passwordField;
	public static String sysArch;

	/**
	 * Launch the application.
	 */

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {

				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Load settings
				try {
					Settings.initSettings();
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null,
							"Failed to load config file: " + e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}

				// Create the install directory if it does not exist.
				File installDir = new File(Settings.getSettings()
						.getInstallPath());
				if (!installDir.exists())
					installDir.mkdirs();

				try {
					LaunchFrame frame = new LaunchFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});

		if(Integer.parseInt(System.getProperty("sun.arch.data.model")) == 64) {
			System.out.println("64");
			sysArch = "64";
		} else if (Integer.parseInt(System.getProperty("sun.arch.data.model")) == 32) {
			System.out.println("32");
			sysArch = "32";
		} else {
			System.out.println("Unknown");
			sysArch = "Unknown";
		}
	}

	/**
	 * Create the frame.
	 */

	public LaunchFrame() {
		setFont(new Font("a_FuturaOrto", Font.PLAIN, 12));
		setResizable(false);
		setTitle("Feed the Beast Launcher");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		passwordSettings = new PasswordSettings(new File(Settings.getSettings()
				.getInstallPath(), "loginData"));

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 821, 480);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		loginPanel = new JPanel();
		loginPanel.setBounds(500, 15, 305, 139);
		contentPane.add(loginPanel);
		loginPanel.setLayout(null);

		chckbxRemember = new JCheckBox("Remember Password");
		chckbxRemember.setBounds(86, 101, 125, 23);
		if (passwordSettings.getUsername() != "") {
			chckbxRemember.setSelected(true);
		} else {

		}
		loginPanel.add(chckbxRemember);

		btnOptions = new JButton("Options");
		btnOptions.setBounds(226, 39, 69, 23);
		loginPanel.add(btnOptions);
		btnOptions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OptionsDialog optionsDlg = new OptionsDialog();
				optionsDlg.setVisible(true);
			}
		});

		btnLogin = new JButton("Login");
		btnLogin.setBounds(226, 72, 69, 23);
		loginPanel.add(btnLogin);
		btnLogin.setEnabled(true);
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chckbxRemember.isSelected()) {
					passwordSettings.storeUP(usernameField.getText(),
							new String(passwordField.getPassword()));
				} else {
					try {
						passwordSettings.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				if (e.getActionCommand().equalsIgnoreCase("login")) {
					doLogin();
				}
			}
		});

		btnPlayOffline = new JButton("Play Offline");
		btnPlayOffline.setBounds(199, 11, 96, 23);
		btnPlayOffline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					launchMinecraft(new File(Settings.getSettings()
							.getInstallPath()).getPath()
							+ "\\"
							+ getSelectedModPack() + "\\.minecraft",
							"OFFLINE", "1");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});


		lblError = new JLabel();
		lblError.setBounds(14, 15, 175, 14);
		loginPanel.add(lblError);
		lblError.setHorizontalAlignment(SwingConstants.LEFT);
		lblError.setForeground(Color.RED);

		usernameField = new JTextField("", 17);
		usernameField.setBounds(76, 39, 144, 22);
		usernameField.setText(passwordSettings.getUsername());
		loginPanel.add(usernameField);

		passwordField = new JPasswordField("", 17);
		passwordField.setBounds(76, 72, 144, 22);
		passwordField.setText(passwordSettings.getPassword());
		loginPanel.add(passwordField);

		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setBounds(14, 43, 52, 14);
		loginPanel.add(lblUsername);
		lblUsername.setDisplayedMnemonic('u');

		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(16, 76, 50, 14);
		loginPanel.add(lblPassword);
		lblPassword.setDisplayedMnemonic('p');

		JScrollPane newsPane = new JScrollPane();
		newsPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		newsPane.setBounds(230, 45, 260, 274);
		contentPane.add(newsPane);

		JTextArea txtrNews = new JTextArea();
		txtrNews.setWrapStyleWord(true);
		txtrNews.setLineWrap(true);
		txtrNews.setEditable(false);
		txtrNews.setText("Hello world, these are the news! And this is just a test to see if the text can be scrolled down as needed, when the news are too long, which they will maybe be. I think this is enough");
		newsPane.setViewportView(txtrNews);

		JScrollPane modPacksPane = new JScrollPane();
		modPacksPane
		.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		modPacksPane.setBounds(10, 15, 210, 426);
		contentPane.add(modPacksPane);

		JPanel panel = new JPanel();
		modPacksPane.setViewportView(panel);
		panel.setLayout(null);

		modPack1RB = new JRadioButton("");
		modPack1RB.setBounds(182, 27, 20, 21);
		modPack1RB.setSelected(true);
		panel.add(modPack1RB);

		modPack2RB = new JRadioButton("");
		modPack2RB.setBounds(182, 86, 20, 21);
		panel.add(modPack2RB);

		JLabel lblModPack2 = new JLabel("FTB (standard pack for new worlds)");
		lblModPack2.setBackground(Color.YELLOW);
		lblModPack2.setBounds(10, 72, 175, 50);
		panel.add(lblModPack2);

		JLabel lblModPack1 = new JLabel("FTB Classic (for use with FTB Maps)");
		lblModPack1.setBackground(Color.YELLOW);
		lblModPack1.setBounds(10, 11, 175, 50);
		panel.add(lblModPack1);

		JLabel lblModPack3 = new JLabel(
				"Direwolf20(for use with Direwolf's maps)");
		lblModPack3.setBackground(Color.YELLOW);
		lblModPack3.setBounds(10, 133, 175, 50);
		panel.add(lblModPack3);

		modPack3RB = new JRadioButton("");
		modPack3RB.setBounds(182, 147, 20, 21);
		panel.add(modPack3RB);

		JLabel lblModPack4 = new JLabel(
				"FTB Lite(stripped down version of the standard FTB pack)");
		lblModPack4.setBackground(Color.YELLOW);
		lblModPack4.setBounds(10, 194, 175, 50);
		panel.add(lblModPack4);

		modPack4RB = new JRadioButton("");
		modPack4RB.setBounds(182, 208, 20, 21);
		panel.add(modPack4RB);

		JPanel sponsorPanel = new JPanel();
		sponsorPanel.setBounds(500, 170, 305, 271);
		contentPane.add(sponsorPanel);
		sponsorPanel.setLayout(null);

		JLabel lblNewLabel = new JLabel("Whatever slowpoke wants here");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(0, 5, 305, 266);
		sponsorPanel.add(lblNewLabel);

		JLabel lblTexturePacks = new JLabel("Texture packs");
		lblTexturePacks.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblTexturePacks.setBounds(230, 330, 126, 19);
		contentPane.add(lblTexturePacks);

		JLabel lblWorldPacks = new JLabel("World packs");
		lblWorldPacks.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblWorldPacks.setBounds(365, 330, 91, 19);
		contentPane.add(lblWorldPacks);

		JLabel lblNews = new JLabel("News");
		lblNews.setFont(new Font("Tahoma", Font.BOLD, 17));
		lblNews.setBounds(230, 15, 113, 19);
		contentPane.add(lblNews);

		ButtonGroup group = new ButtonGroup();
		group.add(modPack1RB);
		group.add(modPack2RB);
		group.add(modPack3RB);
		group.add(modPack4RB);

		setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] {
				usernameField, passwordField, chckbxRemember, btnLogin,
				btnOptions, txtrNews }));
	}

	public void doLogin() {
		btnLogin.setEnabled(false);
		btnOptions.setEnabled(false);
		usernameField.setEnabled(false);
		passwordField.setEnabled(false);
		chckbxRemember.setEnabled(false);

		lblError.setForeground(Color.black);
		lblError.setText("Logging in...");

		LoginWorker loginWorker = new LoginWorker(usernameField.getText(),
				new String(passwordField.getPassword())) {
			@Override
			public void done() {
				lblError.setText("");

				btnOptions.setEnabled(true);
				usernameField.setEnabled(true);
				passwordField.setEnabled(true);
				chckbxRemember.setEnabled(true);

				String responseStr;
				try {
					responseStr = get();
				} catch (InterruptedException err) {
					err.printStackTrace();
					return;
				} catch (ExecutionException err) {
					err.printStackTrace();
					if (err.getCause() instanceof IOException) {
						lblError.setForeground(Color.red);
						lblError.setText("Login failed: "
								+ err.getCause().getMessage());
					} else if (err.getCause() instanceof MalformedURLException) {
						lblError.setForeground(Color.red);
						lblError.setText("Error: Malformed URL");
					}
					return;
				}

				LoginResponse response;
				try {
					response = new LoginResponse(responseStr);
					RESPONSE = response;

				} catch (IllegalArgumentException e) {

					lblError.setForeground(Color.red);

					if (responseStr.contains(":")) {
						lblError.setText("Received invalid response from server.");
					} else {
						if (responseStr.equalsIgnoreCase("bad login")) {
							lblError.setText("Invalid username or password.");
							btnLogin.setEnabled(true);
							loginPanel.add(btnPlayOffline);
							loginPanel.revalidate();
							loginPanel.repaint();
						} else if (responseStr.equalsIgnoreCase("old version"))
							lblError.setText("Outdated launcher.");
						else
							lblError.setText("Login failed: " + responseStr);
					}
					return;
				}

				lblError.setText("Login complete.");
				runGameUpdater(response);
			}
		};
		loginWorker.execute();
	}

	public String getSelectedModPack() {
		if (modPack1RB.isSelected()) {
			return "FTBCLASSIC";
		} else if (modPack2RB.isSelected()) {
			return "FTB";
		} else if (modPack3RB.isSelected()) {
			return "DIREWOLF20";
		} else if (modPack4RB.isSelected()) {
			return "FTBLITE";
		}
		return null;
	}

	public void runGameUpdater(final LoginResponse response) {
		if (!new File(Settings.getSettings().getInstallPath() + "\\.minecraft\\bin\\minecraft.jar").exists()) {
			btnLogin.setEnabled(false);
			btnOptions.setEnabled(false);
			usernameField.setEnabled(false);
			passwordField.setEnabled(false);
			chckbxRemember.setEnabled(false);

			final ProgressMonitor progMonitor = new ProgressMonitor(this, "Downloading minecraft...", "", 0, 100);

			final GameUpdateWorker updater = new GameUpdateWorker(
					RESPONSE.getLatestVersion(), "minecraft.jar", new File(Settings.getSettings().getInstallPath(), ".minecraft//bin").getPath(), false) {
				public void done() {

					btnLogin.setEnabled(true);
					btnOptions.setEnabled(true);
					usernameField.setEnabled(true);
					passwordField.setEnabled(true);
					chckbxRemember.setEnabled(true);

					progMonitor.close();
					try {
						if (get() == true) {
							// Success
							lblError.setForeground(Color.black);
							lblError.setText("Game update complete.");

							// try {
							System.out.println(getSelectedModPack());
							doLogin();
							// launchMinecraft(new
							// File(Settings.getSettings().getInstallPath()).getPath()
							// + "\\" + getSelectedModPack() + "\\.minecraft",
							// RESPONSE.getUsername(), RESPONSE.getSessionID());
							// } catch (IOException e) {
							// e.printStackTrace();
							// }

						} else {
							lblError.setForeground(Color.red);
							lblError.setText("Error downloading game.");
						}
					} catch (CancellationException e) {
						lblError.setForeground(Color.black);
						lblError.setText("Game update cancelled...");
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
						lblError.setForeground(Color.red);
						lblError.setText("Failed to download game: "
								+ e.getCause().getMessage());
						return;
					}
				}
			};

			updater.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (progMonitor.isCanceled()) {
						updater.cancel(false);
					}

					if (!updater.isDone()) {
						int prog = updater.getProgress();
						if (prog < 0)
							prog = 0;
						else if (prog > 100)
							prog = 100;
						progMonitor.setProgress(prog);
						progMonitor.setNote(updater.getStatus());
					}
				}
			});
			updater.execute();
		} else {
			try {
				System.out.println(getSelectedModPack());
				System.out.println("Installed jar mods");
				installMods(getSelectedModPack());
				launchMinecraft(new File(Settings.getSettings()
						.getInstallPath()).getPath()
						+ "\\"
						+ getSelectedModPack() + "\\.minecraft",
						RESPONSE.getUsername(), RESPONSE.getSessionID());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected String getVersionMD5(String modPackName) {
		InputStream is = null;
		MessageDigest md = null;
		File f = new File(Settings.getSettings().getInstallPath() + "\\"
				+ modPackName + "\\.minecraft\\bin\\minecraft.jar");
		if (f.exists()) {
			try {
				md = MessageDigest.getInstance("MD5");
				is = new FileInputStream(Settings.getSettings()
						.getInstallPath()
						+ "\\"
						+ modPackName
						+ "\\.minecraft\\bin\\minecraft.jar");
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				is = new DigestInputStream(is, md);
				// read stream to EOF as normal...
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			String result = "";
			byte[] digest = md.digest();
			for (int i = 0; i < digest.length; i++) {
				result += Integer.toString((digest[i] & 0xff) + 0x100, 16)
						.substring(1);
			}
			return result;
		}
		return "0";
	}

	@SuppressWarnings("deprecation")
	protected void launchMinecraft(String workingDir, String username,
			String password) throws IOException {
		try {
			System.out.println("Loading jars...");
			String[] jarFiles = new String[] { "minecraft.jar", "lwjgl.jar",
					"lwjgl_util.jar", "jinput.jar" };

			URL[] urls = new URL[jarFiles.length];

			for (int i = 0; i < urls.length; i++) {
				try {
					File f = new File(new File(workingDir, "bin"), jarFiles[i]);
					urls[i] = f.toURI().toURL();
					System.out.println("Loading URL: " + urls[i].toString());
				} catch (MalformedURLException e) {
					// e.printStackTrace();
					System.err
					.println("MalformedURLException, " + e.toString());
					System.exit(5);
				}
			}

			System.out.println("Loading natives...");
			String nativesDir = new File(new File(workingDir, "bin"), "natives")
			.toString();

			System.setProperty("org.lwjgl.librarypath", nativesDir);
			System.setProperty("net.java.games.input.librarypath", nativesDir);

			System.setProperty("user.home", new File(workingDir).getParent());

			String[] mcArgs = new String[2];
			mcArgs[0] = username;
			mcArgs[1] = password;

			System.out.println("Ram Min = " + OptionsDialog.ramMin + " Ram Max = " + OptionsDialog.ramMax);

			Runtime openMinecraft = Runtime.getRuntime();

			openMinecraft.exec(new String[] {"cd", Settings.getSettings().getInstallPath()+ "\\"+ getSelectedModPack()+ "\\.minecraft\\bin\\"});
			openMinecraft.exec(new String[]{"java", "-Xms" + OptionsDialog.ramMin + "M", "-Xmx" + OptionsDialog.ramMax + "M","-jar", "minecraft.jar"});

		} finally {

		}
	}

	/*	@SuppressWarnings("deprecation")
	protected void launchMinecraft(String workingDir, String username,
			String password) throws IOException {
		try {
			System.out.println("Loading jars...");
			String[] jarFiles = new String[] { "minecraft.jar", "lwjgl.jar",
					"lwjgl_util.jar", "jinput.jar" };

			URL[] urls = new URL[jarFiles.length];

			for (int i = 0; i < urls.length; i++) {
				try {
					File f = new File(new File(workingDir, "bin"), jarFiles[i]);
					urls[i] = f.toURI().toURL();
					System.out.println("Loading URL: " + urls[i].toString());
				} catch (MalformedURLException e) {
					// e.printStackTrace();
					System.err
							.println("MalformedURLException, " + e.toString());
					System.exit(5);
				}
			}

			System.out.println("Loading natives...");
			String nativesDir = new File(new File(workingDir, "bin"), "natives")
					.toString();

			System.setProperty("org.lwjgl.librarypath", nativesDir);
			System.setProperty("net.java.games.input.librarypath", nativesDir);

			System.setProperty("user.home", new File(workingDir).getParent());

			URLClassLoader cl = new URLClassLoader(urls,
					LaunchFrame.class.getClassLoader());

			// Get the Minecraft Class.
			Class<?> mc = cl.loadClass("net.minecraft.client.Minecraft");
			Field[] fields = mc.getDeclaredFields();

			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];
				if (f.getType() != File.class) {
					// Has to be File
					continue;
				}
				if (f.getModifiers() != (Modifier.PRIVATE + Modifier.STATIC)) {
					// And Private Static.
					continue;
				}
				f.setAccessible(true);
				f.set(null, new File(workingDir));
				// And set it.
				this.hide();
				System.out.println("Fixed Minecraft Path: Field was "
						+ f.toString());
			}

			String[] mcArgs = new String[2];
			mcArgs[0] = username;
			mcArgs[1] = password;

			String mcDir = mc.getMethod("a", String.class)
					.invoke(null, (Object) "minecraft").toString();

			System.out.println("MCDIR: " + mcDir);

			mc.getMethod("main", String[].class).invoke(null, (Object) mcArgs);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			System.exit(2);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.exit(2);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			System.exit(3);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			System.exit(3);
		} catch (SecurityException e) {
			e.printStackTrace();
			System.exit(4);
		}
	}*/

	protected void downloadModPack(String modPackName) {
		URL website;
		try {
			website = new URL("TODO!!!!!!!SERVER/" + modPackName + ".zip");
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(Settings.getSettings()
					.getInstallPath() + "\\temp\\" + modPackName + ".zip");
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		extractZip(Settings.getSettings().getInstallPath() + "\\temp\\"
				+ modPackName + ".zip");
	}

	public void extractZip(String zipLocation) {
		try {
			byte[] buf = new byte[1024];
			ZipInputStream zipinputstream = null;
			ZipEntry zipentry;
			zipinputstream = new ZipInputStream(
					new FileInputStream(zipLocation));

			zipentry = zipinputstream.getNextEntry();
			while (zipentry != null) {
				// for each entry to be extracted
				String entryName = zipentry.getName();
				System.out.println("entryname " + entryName);
				int n;
				FileOutputStream fileoutputstream;
				File newFile = new File(entryName);
				String directory = newFile.getParent();

				if (directory == null) {
					if (newFile.isDirectory()) {
						break;
					}
				}

				fileoutputstream = new FileOutputStream(zipLocation);

				while ((n = zipinputstream.read(buf, 0, 1024)) > -1){
					fileoutputstream.write(buf, 0, n);
				}

				fileoutputstream.close();
				zipinputstream.closeEntry();
				zipentry = zipinputstream.getNextEntry();

			}// while

			zipinputstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void extractZipTo(String zipLocation, String outputLocation)
			throws IOException {
		try {
			File fSourceZip = new File(zipLocation);
			String zipPath = outputLocation;
			File temp = new File(zipPath);
			temp.mkdir();
			System.out.println(zipPath + " created");
			ZipFile zipFile = new ZipFile(fSourceZip);
			Enumeration e = zipFile.entries();

			while (e.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				File destinationFilePath = new File(zipPath, entry.getName());
				destinationFilePath.getParentFile().mkdirs();
				if (entry.isDirectory()) {
					continue;
				} else {
					System.out.println("Extracting " + destinationFilePath);
					BufferedInputStream bis = new BufferedInputStream(
							zipFile.getInputStream(entry));

					int b;
					byte buffer[] = new byte[1024];

					FileOutputStream fos = new FileOutputStream(
							destinationFilePath);
					BufferedOutputStream bos = new BufferedOutputStream(fos,
							1024);

					while ((b = bis.read(buffer, 0, 1024)) != -1) {
						bos.write(buffer, 0, b);
					}

					bos.flush();
					bos.close();
					bis.close();
				}
			}
		} catch (IOException ioe) {
			System.out.println("IOError :" + ioe);
		}

	}

	public static void copyFolder(File src, File dest) throws IOException {

		if (src.isDirectory()) {

			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
				System.out.println("Directory copied from " + src + "  to "
						+ dest);
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyFolder(srcFile, destFile);
			}

		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			if (src.exists()) {
				InputStream in = new FileInputStream(src);
				OutputStream out = new FileOutputStream(dest);

				byte[] buffer = new byte[1024];

				int length;
				// copy the file content in bytes
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}

				in.close();
				out.close();
				System.out.println("File copied from " + src + " to " + dest);
			}
		}
	}


	public static void copyFile(File src, File dest) throws IOException {
		if (src.exists()) {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
			System.out.println("File copied from " + src + " to " + dest);
		}
	}

	public static boolean delete(File resource) throws IOException {
		if (resource.isDirectory()) {
			File[] childFiles = resource.listFiles();
			for (File child : childFiles) {
				delete(child);
			}
		}
		return resource.delete();

	}

	protected void installMods(String modPackName) throws IOException {
		new File(Settings.getSettings().getInstallPath() + "\\"+ getSelectedModPack() + "\\.minecraft").mkdirs();
		System.out.println("dirs mk'd");
		copyFolder(new File(Settings.getSettings().getInstallPath()+ "\\.minecraft\\bin\\"), new File(Settings.getSettings().getInstallPath()+ "\\"+ getSelectedModPack()+ "\\.minecraft\\bin"));
		File minecraft = new File(Settings.getSettings().getInstallPath()+ "\\.minecraft\\bin\\minecraft.jar");
		File mcbackup = new File(Settings.getSettings().getInstallPath() + "\\"+ modPackName + "\\.minecraft\\bin\\mcbackup.jar");
		//		minecraft.renameTo(new File(Settings.getSettings().getInstallPath()+ "\\" + modPackName + "\\.minecraft\\bin\\mcbackup.jar"));
		//		System.out.println("Renamed minecraft.jar to mcbackup.jar");
		JarFile packMinecraft = new JarFile(Settings.getSettings().getInstallPath()+ "\\"+ getSelectedModPack()+ "\\.minecraft\\bin\\minecraft.jar");
		copyFile(minecraft, mcbackup);
	}
}