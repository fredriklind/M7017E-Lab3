package lab3;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class main {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				try {
					JFrame f = new Gui();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
