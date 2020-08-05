package org.eu.campelj.vlcampi;

import javax.swing.tree.DefaultMutableTreeNode;

public class DirNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = -7039954917946856790L;
	private String m_location;
	private String[] m_subtitles;
	
	
	public DirNode(String name) {
		super(name);
	}
	
	public DirNode(String name, String location, String[] subtitles) {
		super(name);
		m_location = location;
		m_subtitles = subtitles;
	}
	
	public String getLocation() {
		return m_location;
	}
	
	public String[] getSubtitles() {
		return m_subtitles;
	}
}
