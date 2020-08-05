package org.eu.campelj.vlcampi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.json.JSONObject;

public class WindowFrame extends JFrame {
	private static final long serialVersionUID = 6059604276571332488L;

	private MasterPanel m_masterPanel;
	private TreeMaster m_treeMaster;
	private JMenuBar m_menuBar;
	private JPanel m_settingsHolder;
	
	private int m_width;
	private int m_height;

	private String m_vlcProg;
	private String m_configFileName = "vlcampi.json";
	private String m_tempSubFileLoc = ".";
	
	public WindowFrame(String title, int width, int height) {
		super(title);
		
		try {	
			setIconImage((Image) ImageIO.read(getClass().getResourceAsStream("/vlcampi.png")));
		} catch (Exception e) {
			//Logging ne more najti ikone
			System.out.println("Tole ne gre!");
			e.printStackTrace();
		}
				
		m_width = width;
		m_height = height;
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			
		}
		
		/* Konfiguracija */
		
		File conf = new File(m_configFileName);
		if(conf.exists() && conf.length() > 0) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(conf));
				String line = "";
				String confString = "";
				
				while((line = in.readLine()) != null) {
					confString += line;
				}
				
				in.close();
				
				JSONObject jsonConf = new JSONObject(confString);
				

				m_vlcProg = jsonConf.getString("vlc_prog");
				m_tempSubFileLoc = jsonConf.getString("tempSub");
				
				//System.out.println(m_vlc_dir);
				//System.out.println(tempSubFileLoc);
				
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
		} else {		
			JFileChooser vlcChooser = new JFileChooser();
			vlcChooser.setCurrentDirectory(new File("."));
			vlcChooser.setDialogTitle("Izberite program vlc!");
			vlcChooser.setAcceptAllFileFilterUsed(false);
			
			if(vlcChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				m_vlcProg = vlcChooser.getSelectedFile().toString();
			} else {
				JOptionPane.showMessageDialog(null, "Niste izbrali programa vlc!", "Napaka", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
			
			JFileChooser tempSrtChooser = new JFileChooser();
			tempSrtChooser.setCurrentDirectory(new File("."));
			tempSrtChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			tempSrtChooser.setDialogTitle("Izberi DIREKTORIJ, kjer bo shranjen temp.srt");
			tempSrtChooser.setAcceptAllFileFilterUsed(false);
			
			if(tempSrtChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				      m_tempSubFileLoc = tempSrtChooser.getSelectedFile().toString();
			} else {
				JOptionPane.showMessageDialog(null, "Niste izbrali direktorija, v katerem bodo\nshranjeni podnapisi pri vsakem ogledu filma!", "Napaka", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
			
			saveConfiguration();
		}
		
		m_treeMaster = new TreeMaster(this);
		
		m_menuBar = new JMenuBar();
		
		m_settingsHolder = new JPanel();
		
		JLabel vlcProgLabel = new JLabel("Program vlc: " + m_vlcProg);
		JLabel tempSubFileLabel = new JLabel("Direktorij za temp.srt: " + m_tempSubFileLoc);
		
		JButton vlcProgBtn = new JButton("Spremeni!");
		JButton tempSubFileBtn = new JButton("Spremeni!");
		
		WindowFrame wf = this;
		
		vlcProgBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				JFileChooser vlcDirChooser = new JFileChooser();
				vlcDirChooser.setCurrentDirectory(new File("."));
				vlcDirChooser.setDialogTitle("Izberite program vlc!");
				vlcDirChooser.setAcceptAllFileFilterUsed(false);
				
				if(vlcDirChooser.showOpenDialog(wf) == JFileChooser.APPROVE_OPTION) {
				      m_vlcProg = vlcDirChooser.getSelectedFile().toString();
				      saveConfiguration();
				}
			}
		});
		
		tempSubFileBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				JFileChooser tempSrtChooser = new JFileChooser();
				tempSrtChooser.setCurrentDirectory(new File("."));
				tempSrtChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				tempSrtChooser.setDialogTitle("Izberi DIREKTORIJ, kjer bo shranjen temp.srt!");
				tempSrtChooser.setAcceptAllFileFilterUsed(false);
				
				if(tempSrtChooser.showOpenDialog(wf) == JFileChooser.APPROVE_OPTION) {
				      m_tempSubFileLoc = tempSrtChooser.getSelectedFile().toString();
				      saveConfiguration();
				}
			}
		});
		
		m_settingsHolder.setLayout(new GridLayout(2,2,5,5));
		
		m_settingsHolder.add(vlcProgLabel);
		m_settingsHolder.add(vlcProgBtn);
		m_settingsHolder.add(tempSubFileLabel);
		m_settingsHolder.add(tempSubFileBtn);
		
		JMenu settings = new JMenu("Nastavitve");
		JMenuItem settingsItem = new JMenuItem("Nastavitve");
		settingsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				int result = JOptionPane.showConfirmDialog(null, m_settingsHolder, "Nastavitve VLCampi", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if(result == JOptionPane.OK_OPTION) {
					saveConfiguration();
				}
			}
		});
		
		settings.add(settingsItem);
		m_menuBar.add(settings);
		
		setJMenuBar(m_menuBar);
		
		setVisible(true);
		setSize(m_width, m_height);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		
		m_masterPanel = new MasterPanel(m_width, m_height, 20, m_treeMaster.getJTree());
		add(m_masterPanel, BorderLayout.CENTER);
		
		pack();
		
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				validate();
				int newWidth = e.getComponent().getWidth() ;
				int newHeight = e.getComponent().getHeight();
				
				m_masterPanel.setPreferredSize(new Dimension(newWidth, newHeight));
				validate();
			}
		});
		
	}
	
	public void play(String source, String[] subtitles) {
		source = source.replaceAll(" ", "%20");
		source = source.replaceAll("\\[", "%5b");
		source = source.replaceAll("\\]", "%5d");
		String subtitleFile = null;
		
		source = "https://" + TreeMaster.getUsername() + ":" + TreeMaster.getPassword() + "@" + source.substring(8);	
		
		/* Pridobitev podnapisov */
		
		if(subtitles.length != 0) {			
			if(subtitles.length == 1) {
				subtitleFile = getSubtitles(subtitles[0]);
				//System.out.println(subtitleFile);
			} else {
				//Uporabnik ima na izbiro veè podnapisov
				
				String[] names = new String[subtitles.length];
				
				for(int i = 0; i < subtitles.length; i++) {
					String[] name = subtitles[i].split("/");
					names[i] = name[name.length - 1];
				}
				
				String selectedValue = (String) JOptionPane.showInputDialog(null, "Izberite željene podnapise!", "Podnapisi", JOptionPane.INFORMATION_MESSAGE, null, names, names[0]);
				selectedValue = subtitles[Arrays.asList(names).indexOf(selectedValue)];
				
				if(selectedValue != null) {
					subtitleFile = getSubtitles(selectedValue);
				}
			}
		}
		
		try {
			if(subtitleFile != null) {
				Runtime.getRuntime().exec(m_vlcProg + " --network-caching=1000  --sub-file=" + subtitleFile + " " + source);			
			} else {
				Runtime.getRuntime().exec(m_vlcProg + " --network-caching=1000  " + source);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "VLC", "Program VLC se ni mogel zagnati!", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
			//e.printStackTrace();
		}
	}
	
	private String getSubtitles(String source) {
		String name = "temp." + source.substring(source.length() - 3);
		try {
			source = source.replaceAll(" ", "%20");
			source = source.replaceAll("\\[", "%5b");
			source = source.replaceAll("\\]", "%5d");
			
			URL url = new URL(source);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization", "Basic " + TreeMaster.getEncodedUsernamePassword());
			System.setProperty("http.agent", "");
			con.setDoOutput(true);
			
			int responseCode = con.getResponseCode();
			
			//System.out.println(con.getResponseCode() + ": " + con.getResponseMessage());
			if(responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				PrintWriter out = new PrintWriter(new File(m_tempSubFileLoc + "/" + name));
				String line = "";
				
				while((line = in.readLine()) != null) {
					out.println(line);
				}
				
				in.close();
				out.flush();
				out.close();
				
				return m_tempSubFileLoc + "/" + name;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void saveConfiguration() {
		try {

			JSONObject json = new JSONObject();
			
			json.put("vlc_prog", m_vlcProg);
			json.put("tempSub", m_tempSubFileLoc);
			
			File conf = new File(m_configFileName);
			
			FileWriter fw = new FileWriter(conf);
			fw.write(json.toString());
			//System.out.println(json.toString());
			fw.flush();
			fw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
