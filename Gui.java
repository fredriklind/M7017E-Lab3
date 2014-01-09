

package lab3;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TooManyListenersException;

import javax.swing.*;
import javax.swing.text.JTextComponent;


public class Gui extends JFrame implements ActionListener, ItemListener{

	/**
	 * @param args
	 */

	static JTextArea txtarea;
	private static JComboBox<String> voicemsgList;
	private Audio a = new Audio();
	private Sip_Com s = new Sip_Com("test", "130.240.53.164", 5060);
	private JCheckBox box;

	public Gui() throws Exception {
		createGui();
		s.audio = a;
	}

	public void Components(Container pane){

		JButton button;
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		//c.fill = GridBagConstraints.HORIZONTAL;
		//c.weightx = 0.5;

		button = new JButton("Listen");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		pane.add(button,c);

		button.addActionListener(this);

		button = new JButton("Stop");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		pane.add(button,c);

		button.addActionListener(this);

		button = new JButton("Refresh");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		pane.add(button,c);

		button.addActionListener(this);

		button = new JButton("Delete");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		pane.add(button,c);

		button.addActionListener(this);

		box = new JCheckBox("Send Greeting");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 3;
		c.gridy = 1;
		pane.add(box,c);

		box.addItemListener((ItemListener) this);

		//		button = new JButton("Call");
		//		c.fill = GridBagConstraints.HORIZONTAL;
		//		c.gridx = 0;
		//		c.gridy = 1;
		//		pane.add(button,c);

		//		txt = new JTextField(15);
		//		c.fill = GridBagConstraints.HORIZONTAL;
		//		c.gridx = 2;
		//		c.gridy = 1;
		//		pane.add(txt,c);

		voicemsgList = new JComboBox<String>();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 3;
		c.gridy = 0;
		pane.add(voicemsgList, c);


	}

	public void itemStateChanged(ItemEvent e){

		if(box.isSelected()){
			s.shouldSendGreeting = true;
		}else{
			s.shouldSendGreeting = false;
		}
	}

	public void actionPerformed(ActionEvent e){

		switch (((JButton) e.getSource()).getText()) {
		case "Listen": a.playAudioLocally(voicemsgList.getSelectedItem());
			break;
		case "Stop": a.stop();
			break;
		case "Refresh": refreshFileList();
			break;
		case "Delete": deleteFile();
			break;
		default:;
			break;
		}
	}

	private void refreshFileList() {
		voicemsgList.removeAllItems();
		files();
	}

	private void deleteFile(){
		String path = "/afs/ltu.se/students/j/o/johsim-0/Desktop/java_test/"+voicemsgList.getSelectedItem();
		File file = new File(path);
		file.delete();
		voicemsgList.removeItem(voicemsgList.getSelectedItem());
		//files();
	}

	public static void files(){
		String path = "/afs/ltu.se/students/j/o/johsim-0/Desktop/java_test";
		String file;
		File folder = new File(path);
		File[] nrfiles = folder.listFiles();

		for (int i = 0; i < nrfiles.length; i++){
			if(nrfiles[i].isFile()){
				file = nrfiles[i].getName();

				if(file.endsWith(".mp3")){
					voicemsgList.addItem(file);
				}
			}
		}
	}

	private void createGui(){

		JFrame frame = new JFrame("Answering Machine");
		Container c = frame.getContentPane();
		Dimension d = new Dimension(400,80);
		c.setPreferredSize(d);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.Components(frame.getContentPane());
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
		files();
	}

}
