package edu.ou.engr.engr2002.ideagroupselection;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;

/**
 * Dialog for showing output from the group selection program and writing it
 * to a file
 */
public class OutputDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final String FONT = "<font face=\"sans-serif\">";
	private static final int SPACE = 5;
	private static final int SPACE_L = 10;
	private JEditorPane textPane;
	private JTextField highlightTF = new JTextField(3);
	private FileSelectionPanel outputPnl;
	private DefaultHighlightPainter yellowPainter = 
	        new DefaultHighlightPainter(Color.YELLOW);
	private DefaultHighlightPainter cyanPainter =
			new DefaultHighlightPainter(Color.CYAN);
	
	public OutputDialog(Frame parent, String text) {
		super(parent, "Results", true);

		JPanel mainPnl = new JPanel(new BorderLayout(SPACE, SPACE));
		add(mainPnl);
		mainPnl.setBorder(new EmptyBorder(SPACE_L, SPACE_L, SPACE, SPACE_L));

		// Get ready to display the results formatted with HTML:
		// use a sans-serif font, and replace all line breaks with <br> tags
		text = FONT + text.replaceAll("\r?\n", "<br>\n");
		// Make group and idea names bold
		text = text.replaceAll("(\nGroup \\d+|\nIdea \\d+[^\n]*)", "<b>$1</b>");
		
		textPane = new JEditorPane("text/html", text);
		textPane.setCaretPosition(0); // scroll to top
		mainPnl.add(new JScrollPane(textPane), BorderLayout.CENTER);
		
		JPanel topPnl = new JPanel(new BorderLayout(SPACE, SPACE));
		topPnl.add(new JLabel(
				"<html><b>Make any desired modifications below, then choose "
				+ "an output filename and click \"Write files.\""),
				BorderLayout.NORTH);
		JPanel highlightPnl = new JPanel(
				new FlowLayout(FlowLayout.LEFT, SPACE, SPACE));
		highlightPnl.add(new JLabel("Highlight students who voted for idea #:"));
		highlightPnl.add(highlightTF);
		JButton highlightBtn = new JButton("Highlight");
		highlightPnl.add(highlightBtn);
		highlightBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				highlightIdea();
			}
		});
		JButton clearBtn = new JButton("Clear highlights");
		highlightPnl.add(clearBtn);
		clearBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textPane.getHighlighter().removeAllHighlights();
			}
		});
		topPnl.add(highlightPnl, BorderLayout.CENTER);
		mainPnl.add(topPnl, BorderLayout.NORTH);
		
		outputPnl = new FileSelectionPanel(
				"<html><b>Output file base name:</b><br>(Two files will be "
				+ "created, with suffixes \" - details\" and \" - groups\". "
				+ "The details file is for the instructor and the groups file "
				+ "is for the students.)",
				System.getProperty("user.home") + File.separator + "results", 
				true, false, null);
		JButton writeBtn = new JButton("Write files");
		writeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				writeFile();
			}
		});
		JPanel writePnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
		writePnl.add(writeBtn);
		outputPnl.add(writePnl, BorderLayout.SOUTH);
		mainPnl.add(outputPnl, BorderLayout.SOUTH);
		
		setSize(600, 700);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	private void highlightIdea() {
		// get the idea number from the text field
		int ideaNum;
		try {
			ideaNum = Integer.parseInt(highlightTF.getText());
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(OutputDialog.this,
					"Invalid idea number", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		Highlighter highlighter = textPane.getHighlighter();
		highlighter.removeAllHighlights(); // remove old highlights
		// get the current text
		String text;
		try {
			text = textPane.getDocument().getText(0, 
					textPane.getDocument().getLength());
		} catch (BadLocationException ex) {
			System.err.println(ex);
			return;
		}
		// find lines of student/vote info containing the given idea number
		Pattern pattern = Pattern.compile(
				// "* " followed by non-*s followed by " - "
				"\\* [^\\*]*?( - )"
				// ...followed by non-*s followed by [ 
				// (note: (?=\[) is a lookahead assertion--it looks for [ next
				// but doesn't consume it)
				+ "[^\\*]*?(?=\\[)"
				// ...followed by non-[s followed by not a digit and the 
				// requested number and not a digit (lookahead in case it's a ])
				+ "([^\\]]*?(\\D(" + ideaNum + ")(?=\\D))"
				// ...followed by anything followed by ]
				+ ".*?\\])");
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			// Highlight each match: the number itself will be cyan, and the
			// rest of the line will be yellow
			try {
				highlighter.addHighlight(
						matcher.start(), matcher.start(4), yellowPainter);
				highlighter.addHighlight(
						matcher.start(4), matcher.end(4), cyanPainter);
				highlighter.addHighlight(
						matcher.end(4), matcher.end(), yellowPainter);
			} catch (BadLocationException ex) {
				System.err.println(ex);
			}
		}
		textPane.setCaretPosition(0); // scroll to top
	}
	
	private void writeFile() {
		String fname = outputPnl.getSelectedPath();
		if (fname == null)
			return;
		// remove extensions that might have previously been added
		fname = fname.replaceAll("\\.(html|htm|txt)$", "");
		String text = textPane.getText();
		text = text.replaceAll("\\&quot;", "\"");
		// the text pane will add <html><head></head><body>...</body></html>
		// to the text, so remove everything but the body contents
		text = text.replaceFirst("<html>\\s*<head>\\s*</head>\\s*<body>", "");
		text = text.replaceAll("</body>\\s*</html>", "");
		// font tags could have randomly been added...
		text = text.replaceAll("</?font.*?>", "");
		// replace newline followed by non-space with <br> tag
		text = text.replaceAll("\n(\\S)", "<br>$1");
		// replace all space and/or newline sequences with one space 
		// (the text pane does this weird indenting and line wrapping thing 
		// where it combines the input text into one line, wraps it in HTML
		// as above, and line wraps at a certain character limit and re-indents)
		text = text.replaceAll("\\s+", " ");
		text = text.replaceAll("<br>\\s*<br>\\*", "<br>\\*");
		// put a newline before all <br> tags (for easier reading of the source)
		text = text.replaceAll("<br>", "\n<br>");
		// fix this weird problem
		text = text.replaceAll("\n<br></b>", "</b>\n<br>");
		// re-wrap with HTML stuff
		text = "<html>\n<head>\n<title>Results</title>\n</head>\n<body>\n"
				+ FONT + "\n" + text + "\n</font>\n</body>\n</html>\n";
		
		// Make a short version that can be shown to students:
		// remove all voting data from after students' names in groups
		String shortText = text.replaceAll(
				"(\\* [^\\*]*?)( - [^L]\\S*)( - \\[.*?\\])?", "$1");
		// remove who proposed which idea from the vote totals section
		shortText = shortText.replaceAll(
				"( *\\d+ - +\\d+: \".*?\")([^\n]*?)(\n|<br>)", "$1$3");
		FileWriter writer = null;
		try {
			writer = new FileWriter(fname + " - details.html");
			writer.write(text);
			writer.close();
			writer = new FileWriter(fname + " - groups.html");
			writer.write(shortText);
			dispose();
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Error writing output: " 
					+ ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			try {
				if (writer != null) 
					writer.close();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}
}
