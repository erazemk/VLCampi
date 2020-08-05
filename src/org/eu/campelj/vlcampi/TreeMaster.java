package org.eu.campelj.vlcampi;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TreeMaster {
	private static final String username = "";
	private static final String password = "";
	
	private WindowFrame m_frame;
	
	private DirNode m_rootNode;
	private DefaultTreeModel m_treeModel;
	private JTree m_tree;
	
	public TreeMaster(WindowFrame frame) {
		m_frame = frame;
		getFilms();
	}
	
	public JTree getJTree() {
		return m_tree;
	}
	
	public static String getUsername() {
		return username;
	}
	
	public static String getPassword() {
		return password;
	}
	
	public static String getEncodedUsernamePassword() {
		return Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
	}
	
	public void getFilms() {
		String encodedUsernamePassword = getEncodedUsernamePassword();
		
		try {
			URL url = new URL("https://campelj.eu.org/filmi/filmi/SeagateBackUp/disks.json");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization", "Basic " + encodedUsernamePassword);
			
			con.setDoOutput(true);
			
			int responseCode = con.getResponseCode();
			
			//System.out.println(con.getResponseCode() + ": " + con.getResponseMessage());
			
			if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				System.out.println("Ti idiot si pozabil geslo!");
				return;
			}
			
			if(responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String line = "";
				String site = "";
				
				while((line = in.readLine()) != null) {
					site += line;
				}
				
				in.close();
				
				//Document doc = Jsoup.parse(site);
				//Elements body = doc.select("body");
				//System.out.println(body.get(0).text());
				parseDirObjects(site);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void parseDirObjects(String text){
		JSONArray files = new JSONArray(text);
		
		m_rootNode = new DirNode("Filmi");
		m_treeModel = new DefaultTreeModel(m_rootNode);
		
		m_tree = new JTree(m_treeModel);
		m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		m_tree.setShowsRootHandles(true);

		ImageIcon leafIcon = null;
		try {
			leafIcon = new ImageIcon((Image) ImageIO.read(getClass().getResourceAsStream("/vlc.png")));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(leafIcon != null) {
			Image image = leafIcon.getImage(); // transform it 
			Image newimg = image.getScaledInstance(15, 15,  Image.SCALE_SMOOTH); // scale it the smooth way  
			leafIcon = new ImageIcon(newimg);  // transform it back
			
			if (leafIcon != null) {
			    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
			    renderer.setLeafIcon(leafIcon);
			    m_tree.setCellRenderer(renderer);
			}
		}
		//Poslušalec za ogled
		m_tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DirNode node = (DirNode) m_tree.getLastSelectedPathComponent();
				if(node == null) {
					return;
				}
				
				//èe ni zadnji element (ima "otroke")
				if(!node.isLeaf()) {
					return;
				}
				
				//System.out.println(node.getLocation());
				m_frame.play(node.getLocation(), node.getSubtitles());
			}
		});
		
		for(int i = 0; i < files.length(); i++) {
			JSONObject obj = files.getJSONObject(i);
			DirNode disk = subDir(obj.getString("name"), obj.getJSONArray("files"));
			m_treeModel.insertNodeInto(disk, m_rootNode, m_rootNode.getChildCount());
		}
		
		//System.out.println(m_rootNode.getChildCount());
	}
	
	private DirNode subDir(String name, JSONArray dir) {
		DirNode dirObj = new DirNode(name);
		
		for(int i = 0; i < dir.length(); i++) {
			JSONObject obj = dir.getJSONObject(i);
			try {
				JSONArray subDirDir = obj.getJSONArray("files");
				if(!subDirDir.equals(null) && !subDirDir.isEmpty()) {
					//Èe ni direktorij ampak samo ena datoteka bo null
					
					DirNode subDirObj = subDir(obj.getString("name"), subDirDir);
					if(subDirObj != null) {
						//Problem delajo prazni direktoriji (npr.: podnapisi nobene datoteke avi, mkv, ...)
						m_treeModel.insertNodeInto(subDirObj, dirObj, dirObj.getChildCount());
					}
				}
			} catch (JSONException e) {
				String location = obj.getString("files");
				String label = obj.getString("name");
				String[] subtitles = new String[obj.getJSONArray("subs").length()];
				
				for(int j = 0; j < subtitles.length; j++) {
					subtitles[j] = "https://campelj.eu.org" + obj.getJSONArray("subs").getString(j).substring("/var/www/html/".length() - 1);
				}
				
				//Popravi location
				// odstrani /var/www/html/
				location = "https://campelj.eu.org" + location.substring("/var/www/html/".length() - 1);
				
				//System.out.println(name + ", " + location);
				
				DirNode subDirObj = new DirNode(label, location, subtitles);
				m_treeModel.insertNodeInto(subDirObj, dirObj, dirObj.getChildCount());
			}
		}
		
		return dirObj;
	}
}
