package org.eu.campelj.vlcampi;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

public class MasterPanel extends JPanel {
	private static final long serialVersionUID = -2464228062613544710L;
	private JScrollPane m_scroll_pane;
	
	private int m_width;
	private int m_height;
	private int m_padding;
	
	public MasterPanel(int width, int height, int padding, JTree disks) {
		super();
		m_width = width;
		m_height = height;
		m_padding = padding;
		
		setLayout(new BorderLayout());
		m_scroll_pane = new JScrollPane(disks);
		m_scroll_pane.setPreferredSize(new Dimension(m_width - m_padding, m_height - m_padding));
		add(m_scroll_pane, BorderLayout.CENTER);
		
	}	
	
	public void updateDimensions(int width, int height) {
		m_width = width;
		m_height = height;
		//System.out.println("Nastavljam novo širino: " + m_width + " in višino: " + height);
		
		m_scroll_pane.setPreferredSize(new Dimension(m_width - m_padding, m_height - m_padding));
		m_scroll_pane.validate();
	}

}
