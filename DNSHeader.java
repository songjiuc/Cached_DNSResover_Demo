import java.io.*;

public class DNSHeader {
    // Store all the data provided by the 12 byte DNS header
    short ID;
    short QR; // 1 bit
    short Opcode; // 4 bits
    short AA; // 1 bit
    short TC; // 1 bit
    short RD; // 1 bit
    short RA; // 1 bit
    short Z; // 1 bit
    short AD; // 1 bit
    short CD; // 1 bit
    short RCode; // 4 bits
    short QDcount;
    short ANcount;
    short NScount;
    short ARcount;


    // read the header from an input stream
    public static DNSHeader decodeHeader(InputStream x) throws IOException {
        DNSHeader header = new DNSHeader();
        DataInputStream inputStream= new DataInputStream(x);
        header.ID = inputStream.readShort();

        byte firstByte = inputStream.readByte();
        header.QR = (short) ((firstByte & 0x80) >>>7);
        header.Opcode = (short) ((firstByte & 0x78) >>>3);
        header.AA = (short) ((firstByte & 0x4) >>>2);
        header.TC = (short) ((firstByte & 0x2) >>>1);
        header.RD = (short) (firstByte & 0x1);

        byte secondByte = inputStream.readByte();
        header.RA = (short) ((secondByte & 0x80) >>>7);
        header.Z = (short) ((secondByte & 0x40) >>>6);
        header.AD = (short) ((secondByte & 0x20) >>>5);
        header.CD = (short) ((secondByte & 0x10) >>>4);
        header.RCode = (short) (secondByte & 0xF);

        header.QDcount = inputStream.readShort();
        header.ANcount = inputStream.readShort();
        header.NScount = inputStream.readShort();
        header.ARcount = inputStream.readShort();

        return header;

        // learned the bitwise manipulation from https://levelup.gitconnected.com/dns-response-in-java-a6298e3cc7d9
    }

    // create the header for the response
    public static DNSHeader buildHeaderForResponse(DNSMessage request, DNSMessage response){
        response.header= request.header;
        response.header.QR = 1; // Query type 1 = response
        response.header.ANcount = 1; // AN count 1 = 1 answer

        return response.header;
    }

    // encode the header to bytes to be sent back to the client (as a part of response)
    public void writeBytes(OutputStream y) throws IOException {
        // write every part of header
        DataOutputStream out = new DataOutputStream(y);

        out.writeShort(ID); // write id = 2 bytes

        // write 1st byte: qr, opcode, aa, tc, rd
        byte firstByte = 0x00;
        if(QR==1){
            firstByte = (byte) 0x80;
        }
        firstByte = (byte) (firstByte | (Opcode<<3));
        firstByte = (byte) (firstByte | (AA<<2));
        firstByte = (byte) (firstByte | (TC<<1));
        firstByte = (byte) (firstByte | RD);

        out.write(firstByte);

        // write 2nd byte: ra, z, ad, cd, rcode
        byte secondByte = 0x00;
        if(RA==1){
            secondByte = (byte) 0x80;
        }
        secondByte = (byte) (secondByte | (Z<<6));
        secondByte = (byte) (secondByte | (AD<<5));
        secondByte = (byte) (secondByte | (CD<<4));
        secondByte = (byte) (secondByte | RCode);

        out.write(secondByte);

        // write count parts
        out.writeShort(QDcount);
        out.writeShort(ANcount);
        out.writeShort(NScount);
        out.writeShort(ARcount);

    }

    // return a human-readable string of a header object
    @Override
    public String toString() {
        String result = "\nTransaction ID: " + Integer.toHexString(ID) + ";\n" + "QueryType: " + QR + ";\n" +
                "Opcode: " + Opcode + ";\n" + "Authoritative: " + AA + ";\n" + "Truncated: " + TC + ";\n"
                + "Recursion Desired: " + RD + ";\n" + "Recursion Available: " + RA + ";\n" + "Z: " + Z + ";\n"
                + "Answer Authenticated: " + AD + ";\n" + "Non-authenticated Data: " + CD + ";\n" + "Reply Code: " + RCode + ";\n"
                + "Question count: " + QDcount + ";\n" + "Answer count: " + ANcount + ";\n"
                + "Authority Record count: " + NScount+ ";\n" + "Additional Record count: " + ARcount + ";\n";
        return result;
    }
}
