package edu.ou.engr.engr2002.ideagroupselection;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public class FileSelectionDialog extends JDialog {
	private JTextField pathField;
	// will be null until the dialog closes
	private String selectedFile = null;
	private JFileChooser chooser;
	
	/**
	 * 
	 * @param title           dialog title
	 * @param message         instruction message
	 * @param defaultPath     default path to show in box
	 * @param directoriesOnly true to only allow selecting directories
	 * @param fileFilter      Optional file filter (can be null)
	 */
	private FileSelectionDialog(String title, String message, String defaultPath, 
			boolean directoriesOnly, FileFilter fileFilter) {
		super((JDialog)null, title, true);
		chooser = new JFileChooser();
		if (directoriesOnly)
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fileFilter != null) 
			chooser.addChoosableFileFilter(fileFilter);
		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
		mainPanel.setBorder(new EmptyBorder(10,10,10,10));
		
		// top: message
		JLabel messageLbl = new JLabel(message);
		mainPanel.add(messageLbl, BorderLayout.NORTH);

		// center: path and browse button
		pathField = new JTextField(defaultPath, 40);
		mainPanel.add(pathField, BorderLayout.CENTER);
		JButton browseBtn = new JButton("...");
		browseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showFileChooser();
			}
		});
		mainPanel.add(browseBtn, BorderLayout.EAST);
		
		// botton: cancel and OK buttons
		JPanel bottomPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeDialog(false);
			}
		});
		JButton okayBtn = new JButton("OK");
		okayBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeDialog(true);
			}
		});
		JPanel space = new JPanel();
		space.setPreferredSize(new Dimension(5, 5));
		bottomPnl.add(cancelBtn);
		bottomPnl.add(space);
		bottomPnl.add(okayBtn);
		mainPanel.add(bottomPnl, BorderLayout.SOUTH);
		
		add(mainPanel);
		getRootPane().setDefaultButton(okayBtn);
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	private void closeDialog(boolean userAccepted) {
		selectedFile = userAccepted ? pathField.getText() : null;
		dispose();
	}
	
	private void showFileChooser() {
		chooser.setSelectedFile(new File(pathField.getText()));
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) 
			pathField.setText(chooser.getSelectedFile().getAbsolutePath());
	}
	
	/**
	 * 
	 * @param title           dialog title
	 * @param message         instruction message
	 * @param defaultPath     default path to show in box
	 * @param directoriesOnly true to only allow selecting directories
	 * @param fileFilter      Optional file filter (can be null)
	 */
	public static String showDialog(String title, String message, String defaultPath,
			boolean directoriesOnly, FileFilter fileFilter) {
		return new FileSelectionDialog(title, message, defaultPath, 
				directoriesOnly, fileFilter).selectedFile;
	}
}
