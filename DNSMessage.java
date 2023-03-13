import java.io.*;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
//TODO: Read Domain Name compressed version: Done!
public class DNSMessage {

    DNSHeader header;
    ArrayList<DNSQuestion> questions = new ArrayList<>();
    ArrayList<DNSRecord> answers = new ArrayList<>();
    ArrayList<DNSRecord> authority_records = new ArrayList<>();
    ArrayList<DNSRecord> additional_records = new ArrayList<>();
    byte[] data;
    ByteArrayInputStream inputStream;
    HashMap<String,Integer> domainLocations = new HashMap<>();

    static DNSMessage decodeMessage(byte[] bytes) throws IOException {
        // new message
        DNSMessage message = new DNSMessage();
        message.data = bytes;
        // new input stream
        message.inputStream = new ByteArrayInputStream(bytes);
        // header
        message.header = DNSHeader.decodeHeader(message.inputStream);

        // decode question and add it to array
        for(int i=0; i<message.header.QDcount; i++) {
            message.questions.add(DNSQuestion.decodeQuestion(message.inputStream, message));
        }

        // answer
        for(int i=0; i<message.header.ANcount; i++) {
            message.answers.add(DNSRecord.decodeRecord(message.inputStream, message));
        }

        // authority
        for(int i=0; i<message.header.NScount; i++) {
            message.authority_records.add(DNSRecord.decodeRecord(message.inputStream, message));
        }

        // additional
        for(int i=0; i<message.header.ARcount; i++) {
            message.additional_records.add(DNSRecord.decodeRecord(message.inputStream, message));
        }

        return message;
    }

    //read the pieces of a domain name starting from the current position of the input stream
    ArrayList<String> readDomainName(InputStream input) throws IOException {
        DataInputStream inputStream = new DataInputStream(input);
        ArrayList<String> DomainPieces = new ArrayList<>();

        // Domain Name = QName: length followed by string
        int length = inputStream.readByte();

        String domainPiece;

        while(length!=0){
            domainPiece = new String(inputStream.readNBytes(length));
            DomainPieces.add(domainPiece);
            length = inputStream.readByte();
        }

        return DomainPieces;
    }

    // TODO: Read Compressed Message
    //--same, but used when there's compression, and we need to find the domain from earlier in the message.
    // This method should make a ByteArrayInputStream that starts at the specified byte and call the other version of this method
    ArrayList<String> readDomainName(int firstByte) throws IOException {
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(data));
        ArrayList<String> DomainPieces = new ArrayList<>();

        // Compressed: the first two bit = 11
        // remove 11 and the rest is offset
        // offset = the domain name is located offset bytes from the start of the byte[]
        //short compressed_bit = (short)(firstByte & 0xC000);
        short offset = (short) (firstByte ^ 0xC000);
        //System.out.println("Why offset is negative? ------" + offset);

        // move the cursor of the input stream
        input.readNBytes(offset);

        // normally read the domain name
        DomainPieces = readDomainName(input);

        return DomainPieces;

        //https://spathis.medium.com/how-dns-got-its-messages-on-diet-c49568b234a2
    }

    //--build a response based on the request and the answers you intend to send back.
    static DNSMessage buildResponse(DNSMessage request, ArrayList<DNSRecord> answers) {
        DNSMessage response = new DNSMessage();
        response.header = DNSHeader.buildHeaderForResponse(request,response);
        response.questions = request.questions;

        for(DNSRecord answer: answers){
            response.answers.add(answer);
        }

        response.authority_records = request.authority_records;
        response.additional_records = request.additional_records;

        return response;
    }

    // -- get the bytes to put in a packet and send back
    byte[] toBytes() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        header.writeBytes(outputStream);


        for(DNSQuestion q: questions){
            q.writeBytes(outputStream,domainLocations);
        }

        for(DNSRecord an: answers){
            an.writeBytes(outputStream,domainLocations);
        }

        for(DNSRecord ar: authority_records){
            ar.writeBytes(outputStream,domainLocations);
        }

        for(DNSRecord add_r: additional_records){
            add_r.writeBytes(outputStream,domainLocations);
        }

        return outputStream.toByteArray();

        // https://docs.oracle.com/javase/7/docs/api/java/io/ByteArrayOutputStream.html
        // https://stackoverflow.com/questions/23154009/how-to-convert-outputstream-to-a-byte-array
    }

    // If this is the first time we've seen this domain name in the packet, write it using the DNS encoding
    // (each segment of the domain prefixed with its length, 0 at the end), and add it to the hash map.
    // Otherwise, write a back pointer to where the domain has been seen previously.
    static void writeDomainName(ByteArrayOutputStream output, HashMap<String,Integer> domainLocations, ArrayList<String> domainPieces) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(output);

        // full domain name = qname
        String domainName = joinDomainName(domainPieces);
        if(domainLocations.containsKey(domainName)){
            // If contains, use compression
            System.out.println("Write a compressed query.");
            short offset = domainLocations.get(domainName).shortValue();
            short compressionBytes = (short) (offset|0xC000);
            outputStream.writeShort(compressionBytes);

            // read message compression info from https://www.freesoft.org/CIE/RFC/1035/43.htm
        }
        else{ // not contain, normally write: length + string + 0 at the end
            // TODO: figure out size()
            System.out.println("Write a non-compressed query");
            domainLocations.put(domainName,output.size());

            for(String piece: domainPieces){
                outputStream.writeByte(piece.length());
//                System.out.println(piece.length());
                byte[] piece_bytes = piece.getBytes();
                outputStream.write(piece_bytes);
//                System.out.println(piece);
            }
            outputStream.writeByte(0);
        }
    }

    //-- join the pieces of a domain name with dots ([ "utah", "edu"] -> "utah.edu" )
    static String joinDomainName(ArrayList<String> pieces) {
        return String.join(".",pieces);
    }

    @Override
    public String toString() {
        String result = "\nDNS Message:\n" + "Header: " + header + "\n" +
                "Questions: " + questions + "\n" +
                "Answers: " + answers + "\n" +
                "Authority records: " + authority_records + "\n" +
                "Additional records: " + additional_records + "\n" +
                "Message Byte Array: " + Arrays.toString(data)+ "\n";
        return result;
    }
}
