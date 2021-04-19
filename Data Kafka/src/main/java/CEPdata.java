import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

public class CEPdata {

    // Name of the file with the wanted data
    static String fileName;
    // Name of the Kafka topic to send the data
    static String topicName;

    public static void main (String[] args) throws Exception {

        if(args.length == 2){
            givenArgs(args);
        }else {
            defaultArgs();
        }

        // Create instance for properties to access producer configs
        Properties props = new Properties();
        // Assign host id
        props.put("bootstrap.servers", "83.212.78.117:9092");
        // Set acknowledgments for producer requests.
        props.put("acks", "all");
        // If the request fails, the producer can automatically retry,
        props.put("retries", 0);
        // Specify buffer size in config
        props.put("batch.size", 16384);

        // Reduce the no of requests less than 0
        props.put("linger.ms", 0);

        // The buffer.memory controls the total amount of memory available to the
        // producer for buffering.
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer<String, String>(props);

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = br.readLine();
        int nLines = 0;
        while (line != null) {
            nLines++;
            String[] words = line.split(",");
            producer.send(new ProducerRecord<String, String>(topicName, words[0],line));
            line = br.readLine();
        }
        System.out.println("File Name: " + fileName);
        System.out.println("Topic Name: " + fileName);
        System.out.println("Number of events: " + nLines);
        br.close();
        producer.close();
    }

    private static void defaultArgs() {
        fileName = "data.txt";
        topicName = "CEPdata";
    }

    private static void givenArgs(String [] args){
        fileName = args[0];
        topicName = args[1];
    }
}
