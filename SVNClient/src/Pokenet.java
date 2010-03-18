import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;




public class Pokenet extends JFrame  implements Runnable {
	public static final String SVN_URL = "http://pokenet-release.svn.sourceforge.net/svnroot/pokenet-release";
	public static final String FOLDER_NAME = "pokenet-release";
	
	JTextArea outText;
	JScrollPane scrollPane;
	
	public Pokenet() {
		super("PokeNet Game Launcher and Updater");
		
		
		/* Center the updater */
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((d.getWidth() / 2) - this.getWidth() / 2);
		int y = (int) ((d.getHeight() / 2) - this.getHeight() / 2);
		this.setLocation(x, y);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(null);
		this.setSize(740, 450);
		this.setResizable(false);
		
		outText = new JTextArea();
		outText.setEditable(false);
		outText.append("Console Information:\n");
		
		scrollPane = new JScrollPane(outText);

		
		ImageIcon m_logo;
		JLabel l = null;
		System.out.println("5...");

		try {
			m_logo = new ImageIcon(new URL("http://pokedev.org/header.png"));
			l = new JLabel(m_logo);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		System.out.println("4...");

		
		l.setBounds(0,0,740,190);
		outText.setBounds(0, 190, 740, 440-190);
		scrollPane.setBounds(0,190,740,450-190);
		System.out.println("3...");

//		Insets insets = this.getInsets();
//        Dimension size = l.getPreferredSize();
//        l.setBounds(insets.left,insets.top,
//                     size.width, size.height);
//        size = outText.getPreferredSize();
//        outText.setBounds(insets.left, 190 + insets.top,
//                     size.width, size.height - 190);
//		System.out.println("2...");

        this.add(l);
		this.add(scrollPane);
		System.out.println("Starting...");
		this.setVisible(true);

		new Thread(this).start();

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Pokenet c = new Pokenet();
	}

	@Override
	public void run() {
		/*
		 * TODO:
		 * make sure they have SVN...
		 * if not, install it
		 */
		Process svn;
		Thread t = null;
		String command;

		boolean exists = (new File(FOLDER_NAME)).exists();
		if(!exists) {
			this.outText.append("Installing...\n Please be patient while PokeNet is downloaded...\n");
			System.out.println("Installing...");
			command = "svn co " + SVN_URL;
		} else {
			this.outText.append("Updating...\n");
			System.out.println("Updating...\n");
			
			command = "svn up";
		}
		
		try {
			svn = Runtime.getRuntime().exec(command);
			StreamReader sr = new StreamReader(svn.getInputStream(), "", outText);
			t = new Thread(sr);
			t.start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		while (t.isAlive()) {
			// TODO: progress
			
		}
		
		this.outText.append("Launching...\n");

		/* Launch the game */
		try {
			this.setVisible(false);
			runPokenet();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "An error occured while running the game.");
			System.exit(0);
		}
		
	}
	
	public void runPokenet() throws Exception {
		Process p = Runtime.getRuntime().exec("java -Dres.path="+FOLDER_NAME+"/"
				+ " -Djava.library.path="+FOLDER_NAME+"/lib/native " +
		"-Xmx512m -Xms512m -jar ./"+FOLDER_NAME+"/Pokenet.jar");
		StreamReader r1 = new StreamReader(p.getInputStream(), "OUTPUT");
		StreamReader r2 = new StreamReader(p.getErrorStream(), "ERROR");
		new Thread(r1).start();
		new Thread(r2).start();
	}

}
