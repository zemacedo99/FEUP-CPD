package membershipService;

import java.io.Serializable;

public class Message implements Serializable{
    private final String msgToSend;

    public Message (String msgToSend){
        this.msgToSend = msgToSend;

    }


    public String getMsgToSend() {
        return msgToSend;
    }
    
}
