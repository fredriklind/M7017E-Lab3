package lab3;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.gstreamer.Bin;
import org.gstreamer.Bus;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.elements.PlayBin2;

public class Audio {
	
	private Boolean shouldStartReceiving = false;
	private Boolean isCurrentlyPlayingGreeting = false;
	private Pipeline receivePipe;
	private Pipeline sendPipe;
	

	public Audio() {
		String[] args = {};
		Gst.init("AudioPlayer", args);
	}
	private  PlayBin2 playbin;

	public void playAudioLocally(Object object){

		 String filepath = "/afs/ltu.se/students/j/o/johsim-0/Desktop/java_test/" + object;
		 playbin = new PlayBin2("AudioPlayer");
		 playbin.setInputFile(new File(filepath));
		 playbin.setVideoSink(ElementFactory.make("fakesink", "videosink"));
		 playbin.setState(State.PLAYING);
	}
	
	public void stop(){
		this.playbin.setState(State.NULL);
	}
	
	public void send_greeting(String ip){

		sendPipe = Pipeline.launch("filesrc location=\"/afs/ltu.se/students/j/o/johsim-0/Desktop/java_test/52cbc26310199.mp3\" ! mad ! audioconvert ! audioresample ! speexenc ! rtpspeexpay ! multiudpsink clients=\"" + ip + ":7078\"" + ip);
		sendPipe.setName("sendPipe");
		sendPipe.setState(State.PLAYING);
		isCurrentlyPlayingGreeting = true;
		
		// Wait for end of mp3 file to start receiving sound.
		Bus.EOS eosSignal = new Bus.EOS() {
            public void endOfStream(GstObject source) {
        		if(shouldStartReceiving){
        			isCurrentlyPlayingGreeting = false;
        			sendPipe = null;
        			System.out.println("Got EOS! BAM.");
        			receive();
        		}
            }
		};
		
		sendPipe.getBus().connect(eosSignal);
	}
	
	
	public void receive(){
		if(isCurrentlyPlayingGreeting){
			shouldStartReceiving = true;
			return;
		}
		
		// Starting a new Gstreamer thing
		String[] args = {};
		Gst.init("AudioPlayer", args);
		System.out.println("Now starting to receive shit.");
		
		String fileName = new SimpleDateFormat("hhmmss'.mp3'").format(new Date());
		fileName = "voice-message-" + fileName;
		System.out.println("And the file name would actually be: " + fileName);
		receivePipe = Pipeline.launch("udpsrc caps=\"application/x-rtp, media=(string)audio, clock-rate=(int)16000, encoding-params=(string)1, payload=(int)96, encoding-name=(string)SPEEX\" port=7078 ! rtpspeexdepay ! speexdec ! audioconvert ! audioresample ! lame ! filesink location=\"/afs/ltu.se/students/j/o/johsim-0/Desktop/java_test/" + fileName + "\"");
		receivePipe.setName("receivePipe");
		receivePipe.setState(State.PLAYING);
	
		System.out.println("Derp! Before shit goes down.");
		//System.out.println("The state of the receiving pipeline is: " + receivePipe.getState());
		
		// Line of oblivion --------------------------------------
		System.out.println("Herp! After shit has gone down.");	
		
		shouldStartReceiving = false;
		// gst-launch-0.10 udpsrc caps="application/x-rtp, media=(string)audio, clock-rate=(int)16000, encoding-params=(string)1, payload=(int)96, encoding-name=(string)SPEEX" port=7078 ! rtpspeexdepay ! speexdec ! audioconvert ! audioresample ! lame ! filesink location='/afs/ltu.se/students/j/o/johsim-0/Desktop/java_test/test.mp3'
	}

	public void terminate() {
		//receivePipe.setState(State.NULL);
		System.out.println("Terminating call");
	}
}