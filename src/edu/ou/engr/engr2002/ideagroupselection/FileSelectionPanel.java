package edu.ou.engr.engr2002.ideagroupselection;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

/**
 * Specialized JPanel for selecting a file: has a message at the top and a
 * text field with "..." button (to open file chooser) in the middle.
 * The panel handles showing a file chooser and synchronizing the text field
 * with the chooser. It uses a BorderLayout with an empty south section, so 
 * buttons or other things can be added directly to the panel in that section.
 */
public class FileSelectionPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JLabel msgLabel;
	private JTextField pathField;
	private JButton browseBtn = new JButton("...");
	private JFileChooser chooser;
	private boolean isSaveDialog;
	
	/**
	 * @param message         instruction message
	 * @param defaultPath     default path to show in box
	 * @param isSaveDialog    true if this should be a save (not open) dialog
	 * @param directoriesOnly true to only allow selecting directories
	 * @param fileFilter      Optional file filter (can be null)
	 */
	public FileSelectionPanel(String message, String defaultPath, 
			boolean isSaveDialog, boolean directoriesOnly, 
			FileFilter fileFilter) {
		this.isSaveDialog = isSaveDialog;
		chooser = new JFileChooser();
		if (directoriesOnly)
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fileFilter != null) 
			chooser.addChoosableFileFilter(fileFilter);
		
		setLayout(new BorderLayout(5, 5));
		
		// top: message
		msgLabel = new JLabel(message);
		add(msgLabel, BorderLayout.NORTH);

		// center: path and browse button
		pathField = new JTextField(defaultPath, 40);
		add(pathField, BorderLayout.CENTER);
		browseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showFileChooser();
			}
		});
		add(browseBtn, BorderLayout.EAST);
	}
	
	/** Gets the selected path, or null if the panel is disabled. */
	public String getSelectedPath() {
		if (isEnabled())
			return pathField.getText();
		return null;
	}
	
	/** Sets whether the panel AND its contents should be enabled. */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		msgLabel.setEnabled(enabled);
		pathField.setEnabled(enabled);
		browseBtn.setEnabled(enabled);
	}
	
	private void showFileChooser() {
		chooser.setSelectedFile(new File(pathField.getText()));
		int result = isSaveDialog ? chooser.showSaveDialog(this)
				: chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) 
			pathField.setText(chooser.getSelectedFile().getAbsolutePath());
	}
}
