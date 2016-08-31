package bjc.guiapps.redesign;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Javelin {
	public static void main(String[] args) {
		JFrame javelinFrame = new JFrame("Javelin Browser");

		javelinFrame.setLayout(new BorderLayout());

		JPanel addressPanel = new JPanel();

		javelinFrame.add(addressPanel, BorderLayout.PAGE_START);

		javelinFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		javelinFrame.setVisible(true);
	}
}
