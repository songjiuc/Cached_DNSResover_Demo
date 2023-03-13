import java.util.HashMap;

public class DNSCache {

    static HashMap<DNSQuestion, DNSRecord> cache = new HashMap<>();


    // This class should have methods for querying and inserting records into the cache.\

    // When you look up an entry, if it is too old (its TTL has expired),
    // Current time > expired time
    // remove it and return "not found."
    static boolean contains(DNSQuestion q){
//        System.out.println("CHECKING CONTAINS");
        if(cache.containsKey(q)){
//            System.out.println("Contains q");
            DNSRecord record = getRecord(q);
            if(record.isExpired()){
//                System.out.println("Record expired");
//                System.out.println();
                cache.remove(q);
                System.out.println("not found");
            }
            else{
//                System.out.println("CONTAINS SUCCESS");
                return true;
            }
        }
//        System.out.println("Does not contain q");
        return false;

        // Thanks to Jon for debugging!
    }


    static void putRecordFromMessage(DNSMessage msg){
//        System.out.println("Adding to Cache");
//        System.out.println("Question");
//        System.out.println(msg.questions.get(0).toString());
//        System.out.println("Answer");
//        System.out.println(msg.answers.get(0).toString());
        if(msg.answers.size()!=0) {
            cache.put(msg.questions.get(0), msg.answers.get(0));
        }
        else{
            System.out.println("The requested domain name is not found.");
        }

    }

    static DNSRecord getRecord(DNSQuestion q){
        return cache.get(q);
    }

}
