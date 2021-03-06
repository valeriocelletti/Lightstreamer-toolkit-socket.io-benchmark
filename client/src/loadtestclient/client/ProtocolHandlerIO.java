/*
Copyright 2013 Weswit s.r.l.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package loadtestclient.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ProtocolHandlerIO extends ProtocolHandler  {
    
    public ProtocolHandlerIO(BasicWSClient basicWSClient, String server) {
        super(basicWSClient,server);
    }

    
    public static URI getNewSocketURI(String server) {
        try {
            URL url = new URL("http://" + server + "/socket.io/1/");
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");

            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.flush();
            wr.close();

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line = rd.readLine();
            String hskey = line.split(":")[0];
            return new URI("ws://" + server + "/socket.io/1/websocket/" + hskey);
        } catch (Exception e) {
            System.out.println("error: " + e);
            return null;
        }
    }

    @Override
    public synchronized void handleMessage(String text) {
        
        switch (text.charAt(0)) {
            case '5':
                String[] messageParts = text.split(":");
                String lastPart = messageParts[messageParts.length - 1];
                String chatPayload = lastPart.substring(1, lastPart.length() - 2);
                
                this.dispatcher.createEvent(EventDispatcher.MESSAGE, chatPayload);
                
                break;
        }
        
        
    }


    public synchronized void sendMessage(String message) {
        //no need to implement
    }

    public void handleSocketOpen() {
        this.openGateway();
    }


    @Override
    public void open() {
        URI uri = getNewSocketURI(server);
        try {
            this.networkHandler.open(uri);
        } catch (Exception e) {
        }
        
    }


    @Override
    public void close() {
        this.networkHandler.close();
    }
    
}
