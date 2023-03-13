import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class DNSQuestion {
    // Client request question part

    ArrayList<String> QName = new ArrayList<>();
    short QType;
    short QClass;

    // read a question from input stream
    public static DNSQuestion decodeQuestion(InputStream input, DNSMessage message) throws IOException {
        DNSQuestion question = new DNSQuestion();
        DataInputStream inputStream = new DataInputStream(input);

        // QName: length followed by string
//        int length = inputStream.readByte();
//        String name;
//        question.QName = new ArrayList<>();
//
//        while(length!=0){
//            name = new String(inputStream.readNBytes(length));
//            question.QName.add(name);
//            length = inputStream.readByte();
//        }
        question.QName = message.readDomainName(input);

        // QType, QClass
        question.QType = inputStream.readShort();
        question.QClass = inputStream.readShort();

        return question;
    }

    public void writeBytes(ByteArrayOutputStream output, HashMap<String,Integer> domainNameLocations) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(output);

//        // qname (This part has been moved to DNSMessage.writeDomainName part)
//        String QName_string = String.join(".",QName);
//        if(domainNameLocations.containsKey(QName_string)){
//            // If contains, use compression
//
//            short offset = domainNameLocations.get(QName_string).shortValue();
//            short compressionBytes = (short) ((0xC0)|offset);
//            outputStream.writeShort(compressionBytes);
//
//            // read message compression info from https://www.freesoft.org/CIE/RFC/1035/43.htm
//
//        }
//        else{ // not contain, normally write
//            domainNameLocations.put(QName_string,outputStream.size());
//            for(String name:QName){
//                outputStream.write(name.length());
//                outputStream.writeChars(name);
//            }
//            outputStream.write(0);
//        }
        DNSMessage.writeDomainName(output,domainNameLocations,QName);

        // QType and QClass
        outputStream.writeShort(QType);
        outputStream.writeShort(QClass);
    }

    @Override
    public String toString() {
        String result = "\n" + "QName: " + String.join(".",QName) + "\n" +
                "QType: " + QType + "\n" +
                "QClass: " + QClass + "\n";
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DNSQuestion obj_q = (DNSQuestion) obj;
        return this.QName.equals(obj_q.QName) && (this.QType == obj_q.QType) && (this.QClass == obj_q.QClass);
        // Thanks to Jon!

//        if ((this.QName.equals(obj_q.QName) && (this.QType == obj_q.QType)
//                && (this.QClass == obj_q.QClass))) {
//            return true;
//        }
//        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(QName, QType, QClass);
    }
}
