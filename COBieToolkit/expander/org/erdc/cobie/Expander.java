package org.erdc.cobie;

/******************************************************************************
 * (c) Copyright bimserver.org 2009
 * Licensed under GNU GPLv3
 * http://www.gnu.org/licenses/gpl-3.0.txt
 * For more information mail to license@bimserver.org
 *
 * Bimserver.org is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bimserver.org is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License a 
 * long with Bimserver.org . If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Expander extends JFrame {
	private static final String JAVA_64_BIT_ARCHITECTURE_PROPERTY_VALUE = "64";
	private static final String SUN_ARCH_DATA_MODEL_PROPERTYNAME = "sun.arch.data.model";
	private static final String WINDOW_TITLE = "COBie Toolkit Starter";

	private static final long serialVersionUID = 5356018168589837130L;
	private Process exec;
	private JarSettings jarSettings = JarSettings.readFromFile();
	private JTextField heapSizeField;
	private JTextField permSizeField;
	private JTextField stackSizeField;
	private JButton browserJvm;
	private JTextField jvmField;

	public static void main(String[] args) {
		
		new Expander().start();
	}

	private void start() {
		final JTextArea logField = new JTextArea();
		
		final PrintStream systemOut = System.out;
		
		PrintStream out = new PrintStream(new OutputStream() {
			@Override
			public void write(byte[] bytes, int off, int len) throws IOException {
				String str = new String(bytes, off, len);
				systemOut.append(str);
				logField.append(str);
				logField.setCaretPosition(logField.getDocument().getLength());
			}

			@Override
			public void write(int b) throws IOException {
				String str = new String(new char[] { (char) b });
				systemOut.append(str);
				logField.append(str);
				logField.setCaretPosition(logField.getDocument().getLength());
			}
		});
		System.setOut(out);
		System.setErr(out);

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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(WINDOW_TITLE);
		try {
			setIconImage(ImageIO.read(getClass().getResource("logo_small.png")));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		setSize(640, 500);
		getContentPane().setLayout(new BorderLayout());
		JPanel fields = new JPanel(new SpringLayout());

		JLabel jvmLabel = new JLabel("JVM");
		fields.add(jvmLabel);

		jvmField = new JTextField(jarSettings.getJvm());
		browserJvm = new JButton("Browse...");
		browserJvm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File currentFile = new File(jvmField.getText());
				JFileChooser chooser = new JFileChooser(currentFile.exists() ? currentFile : new File("."));
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int showOpenDialog = chooser.showOpenDialog(Expander.this);
				if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
					jvmField.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		JPanel jvmPanel = new JPanel();
		jvmPanel.setLayout(new BorderLayout());
		jvmPanel.add(jvmField, BorderLayout.CENTER);
		jvmPanel.add(browserJvm, BorderLayout.EAST);
		fields.add(jvmPanel);

		JLabel heapSizeLabel = new JLabel("Max Heap Size");
		fields.add(heapSizeLabel);

		heapSizeField = new JTextField(jarSettings.getHeapsize());
		fields.add(heapSizeField);

		JLabel permSizeLabel = new JLabel("Max Perm Size");
		fields.add(permSizeLabel);

		permSizeField = new JTextField(jarSettings.getPermsize());
		fields.add(permSizeField);

		JLabel stackSizeLabel = new JLabel("Stack Size");
		fields.add(stackSizeLabel);

		stackSizeField = new JTextField(jarSettings.getStacksize());
		fields.add(stackSizeField);

		SpringUtilities.makeCompactGrid(fields, 4, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		final JButton startStopButton = new JButton("Start");

		startStopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (startStopButton.getText().equals("Start")) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							if (jvmField.getText().equalsIgnoreCase("default") || new File(jvmField.getText()).exists()) {
								setComponentsEnabled(false);
								File file = expand();
								startStopButton.setText("Stop");
								start(file, heapSizeField.getText(), stackSizeField.getText(), permSizeField.getText(), jvmField.getText());
								//start(file, heapSizeField.getText(), stackSizeField.getText(), permSizeField.getText(), jvmField.getText());
							} else {
								JOptionPane.showMessageDialog(Expander.this, "JVM field should contain a valid JVM directory, or 'default' for the default JVM");
							}
						}
					}).start();
				} else if (startStopButton.getText().equals("Stop")) {
					if (exec != null) {
						exec.destroy();
						System.out.println("Server has been shut down");
						exec = null;
						startStopButton.setText("Start");
						setComponentsEnabled(true);
					}
				}
			}
		});
		
		DocumentListener documentChangeListener = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				save();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				save();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				save();
			}

			private void save() {
				try {
					jarSettings.setJvm(jvmField.getText());
					jarSettings.setStacksize(stackSizeField.getText());
					jarSettings.setHeapsize(heapSizeField.getText());
					jarSettings.setPermsize(permSizeField.getText());
					jarSettings.save();
				} catch (Exception e) {
					// ignore
				}
			}
		};

		jvmField.getDocument().addDocumentListener(documentChangeListener);
		heapSizeField.getDocument().addDocumentListener(documentChangeListener);
		stackSizeField.getDocument().addDocumentListener(documentChangeListener);
		
		buttons.add(startStopButton);

		logField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		logField.setEditable(true);
		logField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				try {
					exec.getOutputStream().write(e.getKeyChar());
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						exec.getOutputStream().flush();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(logField);
		getContentPane().add(fields, BorderLayout.NORTH);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(buttons, BorderLayout.SOUTH);

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				if (exec != null) {
					exec.destroy();
				}
			}
		}));
		setVisible(true);
		if (!isJavaHome64Bit())
		{
			JavaVersionWarning warningDialog =
					new JavaVersionWarning();
			warningDialog.setVisible(true);
		}
		//printMemoryInfo();
	}

	private void setComponentsEnabled(boolean enabled) {
		heapSizeField.setEditable(enabled);
		stackSizeField.setEditable(enabled);
		permSizeField.setEditable(enabled);
		jvmField.setEditable(enabled);
		browserJvm.setEnabled(enabled);
	}
	


	private File expand() {
		JarFile jar = null;
		String jarFileName = getJarFileNameNew();
		File destDir = new File(jarFileName.substring(0, jarFileName.indexOf(".jar")));
		if (!destDir.isDirectory()) {
			System.out.println("Expanding " + jarFileName);
			try {
				jar = new java.util.jar.JarFile(jarFileName);
				Enumeration<JarEntry> enumr = jar.entries();
				while (enumr.hasMoreElements()) {
					JarEntry file = (JarEntry) enumr.nextElement();
					System.out.println(file.getName());
					File f = new File(destDir, file.getName());
					if (file.isDirectory()) {
						if (!f.getParentFile().exists()) {
							f.getParentFile().mkdir();
						}
						f.mkdir();
						continue;
					}
					InputStream is = jar.getInputStream(file);
					FileOutputStream fos = new FileOutputStream(f);
					byte[] buffer = new byte[1024];
					int red = is.read(buffer);
					while (red != -1) {
						fos.write(buffer, 0, red);
						red = is.read(buffer);
					}
					fos.close();
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
                try {
                    if (jar != null) {
                        jar.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
		} else {
			System.out.println("No expanding necessary");
		}
		return destDir;
	}
	
	private static boolean isJavaHome64Bit()
	{
		boolean is64Bit = false;
		String architectureRunning = System.getProperty(SUN_ARCH_DATA_MODEL_PROPERTYNAME);
		if(architectureRunning.equals(JAVA_64_BIT_ARCHITECTURE_PROPERTY_VALUE))
			is64Bit=true;
		return is64Bit;
	}
	
    private void start(File destDir, String heapsize, String stacksize, String permsize, String jvmPath) {
        List<String> commandAndArgs = new ArrayList<String>();
        try {

            if (jvmPath.equalsIgnoreCase("default")) 
            {
                commandAndArgs.add("java");
            } 
            else 
            {
                File jvm = new File(jvmPath);
                if (jvm.exists()) 
                {
                    File jre = new File(jvm, "jre");
                    if (!jre.exists()) {
                        jre = jvm;
                    }
                    commandAndArgs.add(new File(jre, "bin" + File.separator + "java").getAbsolutePath());
                    
                    File jreLib = new File(jre, "lib");
                   // commandAndArgs.add("-Xbootclasspath:");
                    @SuppressWarnings("unused")
					String xBootClassPath = "";
                    xBootClassPath += "\"" + jreLib.getAbsolutePath() +"\"";
                    for (File file : jreLib.listFiles()) {
                        if (file.getName().endsWith(".jar")) {
                           if (file.getAbsolutePath().contains(" ")) {
                               xBootClassPath += "\"" + file.getAbsolutePath() + "\"" + File.pathSeparator;
                            } else {
                                xBootClassPath += file.getAbsolutePath() + File.pathSeparator;
                           }
                        }
                    }
                    if (jre != jvm) 
                    {
                        File toolsFile = new File(jvm, "lib" + File.separator + "tools.jar");
                       if (toolsFile.getAbsolutePath().contains(" ")) {
                            xBootClassPath += "\"" + toolsFile.getAbsolutePath() + "\"";
                        } else {
                            xBootClassPath += toolsFile.getAbsolutePath();
                        }
                    }
                    //commandAndArgs.add(xBootClassPath);
                } 
                else 
                {
                    System.out.println("Not using selected JVM (directory not found), using default JVM");
                }
            }
            commandAndArgs.add("-Xmx" + heapsize);
            commandAndArgs.add("-Xss" + stacksize);
            commandAndArgs.add("-XX:MaxPermSize=" + permsize);
//          boolean debug = true;
//          if (debug ) {
//              command += " -Xdebug -Xrunjdwp:transport=dt_socket,address=8998,server=y";
//          }
            commandAndArgs.add("-classpath");
            String classPath = "";
            System.out.println(System.getProperty("os.name"));
            boolean escapeCompletePath = System.getProperty("os.name").toLowerCase().contains("mac");
            if (escapeCompletePath) 
            {
                // OSX fucks up with single jar files escaped, so we try to escape the whole thing
                classPath += "\"";
            }
            classPath += "lib" + File.pathSeparator;
            File dir = new File(destDir + File.separator + "lib");
            for (File lib : dir.listFiles()) {
                if (lib.isFile()) {
                    if (lib.getName().contains(" ") && !escapeCompletePath) {
                        classPath += "\"lib" + File.separator + lib.getName() + "\"" + File.pathSeparator;
                    } else {
                        classPath += "lib" + File.separator + lib.getName() + File.pathSeparator;
                    }
                }
            }
            if (classPath.endsWith(File.pathSeparator)) 
            {
                classPath = classPath.substring(0, classPath.length()-1);
            }
            if (escapeCompletePath) 
            {
                // OSX fucks up with single jar files escaped, so we try to escape the whole thing
                classPath += "\"";
            }
            commandAndArgs.add(classPath);
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            String realMainClass = "";
            while (resources.hasMoreElements()) 
            {
                URL url = resources.nextElement();
                Manifest manifest = new Manifest(url.openStream());
                Attributes mainAttributes = manifest.getMainAttributes();
                for (Object key : mainAttributes.keySet()) {
                    if (key.toString().equals("Real-Main-Class")) {
                        realMainClass = mainAttributes.get(key).toString();
                        break;
                    }
                }
            }
            System.out.println("Main class: " + realMainClass);
            commandAndArgs.add(realMainClass);
            System.out.println("Running: ");
            for(String commandEntry : commandAndArgs)
                System.out.print(commandEntry+" ");
            exec = Runtime.getRuntime().exec(commandAndArgs.toArray(new String[commandAndArgs.size()]), null, destDir);
            
            new Thread(new Runnable(){
                @Override
                public void run() {
                    BufferedInputStream inputStream = new BufferedInputStream(exec.getInputStream());
                    byte[] buffer = new byte[1024];
                    int red;
                    try {
                        red = inputStream.read(buffer);
                        while (red != -1) {
                            String s = new String(buffer, 0, red);
                            System.out.print(s);
                            red = inputStream.read(buffer);
                        }
                    } catch (IOException e) {
                    }
                }}).start();
            new Thread(new Runnable(){
                @Override
                public void run() {
                    BufferedInputStream errorStream = new BufferedInputStream(exec.getErrorStream());
                    byte[] buffer = new byte[1024];
                    int red;
                    try {
                        red = errorStream.read(buffer);
                        while (red != -1) {
                            String s = new String(buffer, 0, red);
                            System.out.print(s);
                            red = errorStream.read(buffer);
                        }
                    } catch (IOException e) {
                    }
                }}).start();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	
 	private String getJarFileNameNew() {
		String name = this.getClass().getName().replace(".", "/") + ".class";
		URL urlJar = getClass().getClassLoader().getResource(name);
		String urlString = urlJar.getFile();
		urlString = urlString.substring(urlString.indexOf(":") + 1, urlString.indexOf("!"));
		try {
			return URLDecoder.decode(urlString, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}