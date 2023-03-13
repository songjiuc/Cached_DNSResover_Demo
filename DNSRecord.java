import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DNSRecord {
    // Everything after the header and question parts of the DNS message are stored as records

    ArrayList<String> RName = new ArrayList<>();
    short RType;
    short RClass;
    int ttl;
    short RDLength;
    byte[] RData;
    long creation_time;
    long expired_time;
    String IP;

    static DNSRecord decodeRecord(InputStream input, DNSMessage message) throws IOException {
        DNSRecord record = new DNSRecord();
        DataInputStream inputStream = new DataInputStream(input);

        // Read Domain Name:
        // 1. Compressed
        // check compressed and call compressed readDomainName
        // mark the cursor location and get back here when call reset()
        inputStream.mark(0);
        short bit_offset_2_bytes = inputStream.readShort();

        // offset = the domain name is located offset bytes from the start of the byte[]
        short compressed_bit = (short) (bit_offset_2_bytes & 0xC000);

//        System.out.println("compression bits =" + compressed_bit);
//        System.out.println(Integer.toBinaryString(bit_offset_2_bytes));
//        System.out.println(Integer.toBinaryString(compressed_bit));
//        System.out.println(Integer.toBinaryString(0xC000));
//        System.out.println(compressed_bit + " and " + (short) 0xC000);
//        System.out.println(compressed_bit == 0xC000);
        System.out.println("\nif compressed: " + (compressed_bit == (short)0xC000));

        if(compressed_bit == (short) 0xC000){
            record.RName = message.readDomainName(bit_offset_2_bytes);
        }
        // 2. Not compressed
        else {
            inputStream.reset();
            System.out.println("input stream is reset here.\n");
            record.RName = message.readDomainName(inputStream);
        }
        record.RType = inputStream.readShort();
        record.RClass = inputStream.readShort();
        record.ttl = inputStream.readInt();
        record.RDLength = inputStream.readShort();
        record.RData = inputStream.readNBytes(record.RDLength);
        record.creation_time = Instant.now().getEpochSecond();
        record.expired_time = record.creation_time + record.ttl; // Thanks to Jon!

        return record;
    }
    void writeBytes(ByteArrayOutputStream output, HashMap<String, Integer> domainLocations) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(output);

        DNSMessage.writeDomainName(output,domainLocations,RName);
        outputStream.writeShort(RType);
        outputStream.writeShort(RClass);
        outputStream.writeInt(ttl);
        outputStream.writeShort(RDLength);
        outputStream.write(RData);
    }


    // return whether the creation date + the time to live is after the current time.
    // The Date and Calendar classes will be useful for this.
    // Current time > expired time
    boolean isExpired(){
        return (Instant.now().getEpochSecond() > expired_time);
    }

    String IP_string(){
        String[] ip_Pieces = new String[RDLength];
        for (int i = 0; i < RDLength; i++) {
            //int piece_int = record.RData[0];
            ip_Pieces[i] = String.valueOf(RData[i] & 0xff);
        }
        IP = String.join(".",ip_Pieces);
        return IP;
    }


    @Override
    public String toString() {
        String result = "\n" + "RName: " + String.join(".",RName) + "\n" +
                "RType: " + RType + "\n" +
                "RClass: " + RClass + "\n" +
                "Time to live: " + ttl + "\n" + "RData Length: " + RDLength + "\n" +
                "RData: " + this.IP_string() + "\n" + "Creation Time: " + creation_time + "\n" +
                "Expired Time: " + expired_time + "\n";
        return result;
    }

}
