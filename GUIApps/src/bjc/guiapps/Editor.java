package bjc.guiapps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Benjamin Culkin
 */
public class Editor extends JFrame {
	private static final long serialVersionUID = -3308383903442100127L;

	public static class AutoIndentAction extends AbstractAction {
		private static final long serialVersionUID = 6881668796439155311L;

		public void actionPerformed(ActionEvent ae) {
			JTextArea comp = (JTextArea) ae.getSource();
			Document doc = comp.getDocument();

			if (!comp.isEditable()) {
				return;
			}

			try {
				int line = comp.getLineOfOffset(comp.getCaretPosition());

				int start = comp.getLineStartOffset(line);
				int end = comp.getLineEndOffset(line);

				String str = doc.getText(start, end - start - 1);
				String whiteSpace = getLeadingWhiteSpace(str);

				doc.insertString(comp.getCaretPosition(),
						'\n' + whiteSpace, null);
			} catch (BadLocationException ex) {
				try {
					doc.insertString(comp.getCaretPosition(), "\n", null);
				} catch (BadLocationException blex) {
					// ignore
				}
			}
		}

		/**
		 * Returns leading white space characters in the specified string.
		 */
		private String getLeadingWhiteSpace(String str) {
			return str.substring(0, getLeadingWhiteSpaceWidth(str));
		}

		/**
		 * Returns the number of leading white space characters in the
		 * specified string.
		 */
		private int getLeadingWhiteSpaceWidth(String str) {
			int whitespace = 0;
			while (whitespace < str.length()) {
				char ch = str.charAt(whitespace);
				if (ch == ' ' || ch == '\t')
					whitespace++;
				else
					break;
			}
			return whitespace;
		}
	}

	private static void editFile(File file) {
		Editor ed = new Editor(file);
		ed.setVisible(true);
	}

	public static void main(String[] argv) {
		editFile(null);
	}

	private String			directory;
	private JFileChooser	fc;
	private File			file;
	private Logger			logger	= LogManager.getLogger();
	private String			text;
	private JTextArea		textArea;

	public Editor() {
		this(null);
	}

	public Editor(File file) {
		super("JNote");

		this.file = file;

		setBackground(Color.black);
		setForeground(Color.cyan);

		JPanel panel = (JPanel) getContentPane();

		setJMenuBar(createMenu());

		panel.setLayout(new BorderLayout());

		textArea = new JTextArea();
		textArea.registerKeyboardAction(new AutoIndentAction(),
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				JComponent.WHEN_FOCUSED);

		JScrollPane sp = new JScrollPane(textArea);
		sp.setViewportBorder(
				BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		panel.add(sp, BorderLayout.CENTER);

		if (file != null) {
			directory = file.getParent();
			readFile(file);
			updateTitle(file.getName());
			textArea.requestFocus();
		} else {
			makeNewFile();
		}

		setSize(500, 500);
	}

	private void copy() {
		text = textArea.getSelectedText();
	}

	private JMenu createEditMenu() {
		JMenu edit = new JMenu("Edit");

		JMenuItem copy = new JMenuItem("Copy");
		JMenuItem paste = new JMenuItem("Paste");
		JMenuItem cut = new JMenuItem("Cut");

		copy.setMnemonic('C');
		copy.setAccelerator(KeyStroke.getKeyStroke("ctrl c"));
		copy.addActionListener(e -> copy());

		cut.setMnemonic('X');
		cut.setAccelerator(KeyStroke.getKeyStroke("ctrl x"));
		cut.addActionListener(e -> cut());

		paste.setMnemonic('P');
		paste.setAccelerator(KeyStroke.getKeyStroke("ctrl p"));
		paste.addActionListener(e -> paste());

		edit.add(copy);
		edit.add(cut);
		edit.add(paste);

		return edit;
	}

	private JMenu createFileMenu() {
		JMenu file = new JMenu("File");

		file.setMnemonic('F');

		JMenuItem newFile = new JMenuItem("New");
		JMenuItem open = new JMenuItem("Open...");
		JMenuItem save = new JMenuItem("Save");
		JMenuItem saveAs = new JMenuItem("Save As..");
		JMenuItem exit = new JMenuItem("Exit");

		newFile.setMnemonic('N');
		newFile.setAccelerator(KeyStroke.getKeyStroke("ctrl n"));
		newFile.addActionListener(e -> makeNewFile());

		open.setMnemonic('O');
		open.setAccelerator(KeyStroke.getKeyStroke("ctrl o"));
		open.addActionListener(e -> open());

		save.setMnemonic('S');
		save.setAccelerator(KeyStroke.getKeyStroke("ctrl s"));
		save.addActionListener(e -> save());

		saveAs.setMnemonic('A');
		saveAs.setAccelerator(KeyStroke.getKeyStroke("ctrl shift s"));
		saveAs.addActionListener(e -> saveAs());

		exit.setMnemonic('X');
		exit.setAccelerator(KeyStroke.getKeyStroke("ctrl q"));
		exit.addActionListener(e -> exit());

		file.add(newFile);
		file.addSeparator();

		file.add(open);
		file.add(save);
		file.add(saveAs);
		file.addSeparator();

		file.add(exit);

		return file;
	}

	private JMenuBar createMenu() {
		JMenuBar mb = new JMenuBar();

		mb.add(createFileMenu());
		mb.add(createEditMenu());

		return mb;
	}

	private void cut() {
		text = textArea.getSelectedText();
		textArea.replaceRange("", textArea.getSelectionStart(),
				textArea.getSelectionEnd());
	}

	private void exit() {
		setVisible(false);
	}

	private void initFileChooser() {
		if (fc == null) {
			fc = new JFileChooser(directory);

			fc.setBackground(Color.cyan);
			fc.setForeground(Color.black);

			fc.setFileSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
	}

	private void makeNewFile() {
		file = null;

		updateTitle("New file");

		textArea.setText("");
		textArea.requestFocus();
	}

	private void open() {
		initFileChooser();

		fc.setDialogTitle("Open file");

		if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(this)) {
			file = fc.getSelectedFile();

			updateTitle(file.getName());
			readFile(file);

			textArea.requestFocus();
		}
	}

	private void paste() {
		if (text != null) {
			textArea.insert(text, textArea.getCaretPosition());
		}
	}

	private void readFile(final File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			
			byte[] data = new byte[fis.available()];
			
			fis.read(data);
			
			textArea.setText(new String(data));
			textArea.setCaretPosition(0);
			fis.close();
		} catch (FileNotFoundException fnfe) {
			JOptionPane.showMessageDialog(this, "File not found: " + file);
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(this,
					"Error opening file: " + file);
		}
	}

	private void save() {
		if (file == null) {
			saveAs();
		} else {
			writeFile(file);
		}

		requestFocus();
	}

	private void saveAs() {
		initFileChooser();
		
		fc.setDialogTitle("Save file");
		
		if (JFileChooser.APPROVE_OPTION == fc.showSaveDialog(this)) {
			file = fc.getSelectedFile();
			updateTitle(file.getName());
			writeFile(file);
		}
	}

	private void updateTitle(String title) {
		setTitle("JNote - " + title);
	}

	private void writeFile(final File file) {
		try {
			FileWriter fw = new FileWriter(file);
			fw.write(textArea.getText());
			fw.flush();
			fw.close();
		} catch (FileNotFoundException fnfe) {
			JOptionPane.showMessageDialog(Editor.this,
					"File not found: " + file);
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(Editor.this,
					"Error saving file: " + file);
		} catch (Exception x) {
			String msg = "Unexpected error wile saving file: " + file;
			logger.error(msg, x);
			JOptionPane.showMessageDialog(Editor.this, msg);
		}
	}
}
