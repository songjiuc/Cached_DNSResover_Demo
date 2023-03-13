import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class DNSServer {

    public static void main(String[] args) throws IOException {

        DatagramSocket socket = new DatagramSocket(8053);
        InetAddress google = InetAddress.getByName("8.8.8.8");
        DNSCache cache = new DNSCache();
        byte[] buff = new byte[1000]; // DNS packet size 512 bytes

        while(true) {

            DatagramPacket received_packet_from_client = new DatagramPacket(buff, buff.length);
            try {
                socket.receive(received_packet_from_client);
            } catch (IOException e) {
                System.out.println("Socket cannot listen to messages.");
                throw new IOException();
            }

            // get data from client request
            byte[] msg = Arrays.copyOfRange(received_packet_from_client.getData(), 0, received_packet_from_client.getLength());
            int port_client = received_packet_from_client.getPort(); // client port
            InetAddress address_client = received_packet_from_client.getAddress(); // client ip address

            // create the DNS message from client request
            DNSMessage request_from_client = DNSMessage.decodeMessage(msg);
            System.out.println("Print client query here:-------------------");
            System.out.println(request_from_client);

            ArrayList<DNSRecord> answers = new ArrayList<>();

            DatagramPacket response_packet_to_client;

            byte[] response_buff;


            DNSQuestion q = request_from_client.questions.get(0);
            boolean if_contain = cache.contains(q);


            // if contain, send response back; if not contain, google and send google response
            if (if_contain) {

//                System.out.println("IN CONTAINS!!!");
                DNSRecord answer = cache.getRecord(q);
                answers.add(answer);
                DNSMessage response_to_client = DNSMessage.buildResponse(request_from_client, answers);
                response_buff = response_to_client.toBytes();

            } else { // ask google then send back

                // transfer client request to google
                // ----------------------------
                DatagramPacket ask_google = new DatagramPacket(msg, msg.length, google, 53);
                socket.send(ask_google);

                // receive from google
                byte[] google_answer_buff = new byte[1000];
                DatagramPacket google_answer = new DatagramPacket(google_answer_buff,google_answer_buff.length);
                socket.receive(google_answer);
                // ----------------------------

                response_buff = Arrays.copyOfRange(google_answer.getData(), 0, google_answer.getLength());
                DNSMessage google_response = DNSMessage.decodeMessage(response_buff);

                System.out.println("Print google response here:---------------");
                System.out.println(google_response);

                cache.putRecordFromMessage(google_response);

                response_buff = google_response.toBytes();
            }
            response_packet_to_client = new DatagramPacket(response_buff, response_buff.length, address_client, port_client);
            socket.send(response_packet_to_client);
        }
    }
}