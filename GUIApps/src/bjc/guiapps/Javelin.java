/*
 *
 * Copyright (C) 2003-2013 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package bjc.guiapps;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Benjamin Culkin
 */

@SuppressWarnings("serial")
public class Javelin extends JFrame {
	private static class IntHolder {
		private int prop;

		public IntHolder(int n) {
			prop = n;
		}

		public void downFive() {
			prop -= 5;
		}

		public int getProp() {
			return prop;
		}

		public void upFive() {
			prop += 5;
		}
	}

	public static void main(String[] args) {
		doInitLog4j();
		Javelin j = new Javelin();
		j.getBounds();
	}

	private static void doInitLog4j() {
		logger = LogManager.getLogger();
	}

	private final List<Javelin>	windows	= new LinkedList<>();
	private static Logger		logger;

	private LinkedList<String>	prevHistory;
	private LinkedList<String>	afterHistory;

	@SuppressWarnings("unused")
	private Map<String, String>	bookMarks;

	private String				curPage;

	private JEditorPane			viewPane;

	private final IntHolder		fontInc	= new IntHolder(0);

	private JTextField			ulBar;

	public Javelin() {
		super("Javelin");

		prevHistory = loadHistory();
		afterHistory = new LinkedList<>();
		bookMarks = loadBookMarks();

		setJMenuBar(getBar());
		JPanel jp = (JPanel) getContentPane();
		jp.setLayout(new BorderLayout());

		jp.add(getTopPane(), BorderLayout.NORTH);

		viewPane = getViewPane();

		jp.add(new JScrollPane(viewPane), BorderLayout.CENTER);

		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				windows.remove(this);
				if (windows.isEmpty()) {
					System.exit(0);
				} else {
					dispose();
				}
			}
		});
		this.setSize(640, 480);
		this.setVisible(true);

		// doGo("http://localhost:80/index.html");
	}

	private void doGo(String ul) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			ulBar.setText(ul);
			viewPane.setPage(ul);

			if (curPage != null) {
				prevHistory.push(curPage);
			}

			curPage = ul;
		} catch (IOException e) {
			logger.error("Could not fetch page", e);
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	private JMenuBar getBar() {
		JMenuBar jmb = new JMenuBar();

		JMenu file = new JMenu("File");
		JMenuItem newWindow = new JMenuItem("New Window");
		newWindow.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Javelin j2 = new Javelin();
				windows.add(j2);
			}
		});
		file.add(newWindow);

		JMenu text = new JMenu("Text");
		JMenuItem sizeUp = new JMenuItem("Font Size +");
		sizeUp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				fontInc.upFive();
				((HTMLDocument) viewPane.getDocument()).getStyleSheet()
						.addRule("" + "body { font-size: "
								+ (100 + fontInc.getProp()) + "% ; }");

			}
		});
		text.add(sizeUp);

		JMenuItem sizeDown = new JMenuItem("Font Size -");
		sizeDown.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				fontInc.downFive();
				((HTMLDocument) viewPane.getDocument()).getStyleSheet()
						.addRule("" + "body { font-size: "
								+ (100 + fontInc.getProp()) + "% ; }");

			}
		});
		text.add(sizeDown);

		JMenuItem injectCSS = new JMenuItem("Inject CSS... ");
		injectCSS.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				fontInc.downFive();
				((HTMLDocument) viewPane.getDocument()).getStyleSheet()
						.addRule(JOptionPane.showInputDialog(
								"CSS String to inject? "));

			}
		});
		text.add(injectCSS);

		jmb.add(file);
		jmb.add(text);
		return jmb;
	}

	private JPanel getTopPane() {
		JPanel jp = new JPanel();
		jp.setLayout(new BoxLayout(jp, BoxLayout.X_AXIS));

		JButton back = new JButton("Back");
		back.setMnemonic('B');
		back.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!prevHistory.isEmpty()) {
					afterHistory.push(curPage);
					curPage = prevHistory.pop();
					doGo(curPage);
				}
			}
		});
		jp.add(back);

		JButton forward = new JButton("Forward");
		forward.setMnemonic('F');
		forward.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!afterHistory.isEmpty()) {
					prevHistory.push(curPage);
					curPage = afterHistory.pop();
					doGo(curPage);
				}
			}
		});
		jp.add(forward);

		ulBar = new JTextField();
		ulBar.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					doGo(ulBar.getText());
				}
				// debugSet();
			}
		});
		;
		jp.add(ulBar);

		return jp;
	}

	private JEditorPane getViewPane() {
		JEditorPane jep = new JEditorPane();

		jep.setEditable(false);
		jep.addHyperlinkListener(new HyperlinkListener() {

			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				e.getEventType();
				if (e.getEventType() == EventType.ACTIVATED) {
					if (e instanceof HTMLFrameHyperlinkEvent) {
						HTMLFrameHyperlinkEvent hfhl = (HTMLFrameHyperlinkEvent) e;
						HTMLDocument document = (HTMLDocument) viewPane
								.getDocument();
						document.processHTMLFrameHyperlinkEvent(hfhl);
					} else {
						doGo(e.getURL().toString());
					}
				}
			}
		});
		return jep;
	}

	private static Map<String, String> loadBookMarks() {
		return new HashMap<>();
	}

	private static LinkedList<String> loadHistory() {
		return new LinkedList<>();
	}
}