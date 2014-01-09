package lab3;

import gov.nist.javax.sip.RequestEventExt;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TooManyListenersException;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransportNotSupportedException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class Sip_Com implements SipListener {

	private String username;
	private SipStack sipStack;
	private SipFactory sipFactory;
	private AddressFactory addressFactory;
	private HeaderFactory headerFactory;
	private MessageFactory messageFactory;
	private SipProvider sipProvider;
	public Audio audio;
	public Boolean shouldSendGreeting = false;
	//private String ip = "130.240.53.164";

	public Sip_Com(String username, String ip, int port) throws PeerUnavailableException, TransportNotSupportedException,
	InvalidArgumentException, ObjectInUseException,
	TooManyListenersException {
		setUsername(username);
		sipFactory = SipFactory.getInstance();
		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "Sip_Com");
		properties.setProperty("javax.sip.IP_ADDRESS", ip);

		properties.setProperty("javax.sip.TRACE_LEVEL", "32");
		properties.setProperty("javax.sip.SERVER_LOG",
				"textclient.txt");
		properties.setProperty("javax.sip.DEBUG_LOG",
				"textclientdebug.log");

		sipStack = sipFactory.createSipStack(properties);
		headerFactory = sipFactory.createHeaderFactory();
		addressFactory = sipFactory.createAddressFactory();
		messageFactory = sipFactory.createMessageFactory();

		ListeningPoint tcp = sipStack.createListeningPoint(port, "tcp");
		ListeningPoint udp = sipStack.createListeningPoint(port, "udp");

		sipProvider = sipStack.createSipProvider(tcp);
		sipProvider.addSipListener(this);
		sipProvider = sipStack.createSipProvider(udp);
		sipProvider.addSipListener(this);

	}

	@Override
	public void processRequest(RequestEvent evt) {	
		
		// 			linphone symmetric, skicka samma port?
		try{
			Request req = evt.getRequest();
//			System.out.println(req.getMethod());
//						byte[] sdp = req.getRawContent();
//						String dec = new String(sdp, "UTF-8");
//			System.out.println(dec);

			//			Create the contact name address.
			SipURI contactURI = addressFactory.createSipURI(getUsername(),getHost());
			contactURI.setPort(sipProvider.getListeningPoint("udp").getPort());

			Response response = null;

			response = messageFactory.createResponse(200, req);

			if(req.getMethod().equals(req.INVITE)){
				Address contactAddress = addressFactory.createAddress(contactURI);
				contactAddress.setDisplayName(getUsername());
				ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);

				String sdpData = "v=0\r\n"
						+ "o=herp 13760799956958020 13760799956958020 IN IP4 130.240.53.164\r\n"
						+ "s=Talk\r\n"
						+ "c=IN IP4 130.240.53.164\r\n"
						+ "t=0 0\r\n" 
						+ "m=audio 7078 RTP/AVP 111\r\n"
						+ "a=rtpmap:111 speex/16000\r\n"
						+ "a=fmtp:111 vbr=on\r\n";
				
//				String sdpData = "v=0\r\n"
//				+ "o=herp 13760799956958020 13760799956958020 IN IP4 130.240.53.164\r\n"
//				+ "s=-\r\n"
//				+ "c=IN IP4 130.240.53.164\r\n"
//				+ "t=0 0\r\n" 
//				+ "m=audio 5004 RTP/AVP 96\r\n"
//				+ "a=rtpmap:96 speex/16000\r\n"
//				+ "a=extmap:1 urn:ietf:params:rtp-hdrext:csrc-audio-level\r\n";

				//System.out.println(sdpData);

				byte[] contents = sdpData.getBytes();

				ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
				response.setContent(contents, contentTypeHeader);

				response.addHeader(contactHeader);

				ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
				toHeader.setTag("888");
				ServerTransaction st = sipProvider.getNewServerTransaction(req);
				st.sendResponse(response);

			}else if(req.getMethod().equals(req.BYE)){
				audio.terminate();
				
			}else if(req.getMethod().equals(req.ACK)){
					
				//Get ip of sender
				RequestEventExt event = (RequestEventExt) evt;
				String ip = event.getRemoteIpAddress();
				int port = event.getRemotePort();
				
				if(shouldSendGreeting){
					audio.send_greeting(ip);
				}
				
				System.out.println("Got ACK, starting to send to " + ip + " on port: " + port);
				audio.receive();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Receiving
	//gst-launch-0.10 udpsrc caps="application/x-rtp, media=(string)audio, clock-rate=(int)16000, encoding-params=(string)1, payload=(int)96, encoding-name=(string)SPEEX" port=7078 ! rtpspeexdepay ! speexdec ! audioconvert ! audioresample ! lame ! filesink location='/afs/ltu.se/students/j/o/johsim-0/Desktop/java_test/test.mp3'

	// Sending

	//gst-launch-0.10 audiotestsrc ! audioconvert ! audioresample ! speexenc ! rtpspeexpay ! udpsink port=7078 host=130.240.109.41


	// gst-launch-0.10 filesrc location='/afs/ltu.se/students/j/o/johsim-0/Desktop/java_test/52cbc26310199.mp3' 
	// caps="application/x-rtp, media=(string)audio, clock-rate=(int)16000, encoding-params=(string)1, payload=(int)96, encoding-name=(string)SPEEX" 
	// ! rtpspeexpay ! speexenc ! audioconvert ! audioresample ! udpsink port=7078 host=130.240.109.41 
	@Override
	public void processResponse(ResponseEvent evt) {
		Response response = evt.getResponse();
		int status = response.getStatusCode();

		if ((status >= 200) && (status < 300)) {
			System.out.println("Success!");
			return;
		}

	}
	

	@Override
	public void processDialogTerminated(DialogTerminatedEvent arg0) {

	}

	@Override
	public void processIOException(IOExceptionEvent arg0) {

	}

	@Override
	public void processTimeout(TimeoutEvent arg0) {

	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent arg0) {

	}

	public void setUsername(String newUsername) {
		username = newUsername;
	}
	public String getUsername() {
		return username;
	}
	public String getHost() {
		int port = sipProvider.getListeningPoint().getPort();
		String host = sipStack.getIPAddress();
		return host;
	}

	public int getPort() {
		int port = sipProvider.getListeningPoint().getPort();
		return port;
	}
}
