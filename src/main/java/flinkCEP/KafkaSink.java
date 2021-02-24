package flinkCEP;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

public class KafkaSink {

    FlinkKafkaProducer<String> myProducer;


    public KafkaSink(String brokerlist, String outputTopic) {

        myProducer = new FlinkKafkaProducer<>(
                brokerlist,            // broker list
                outputTopic,                  // target topic
                new SimpleStringSchema());

    }
    public FlinkKafkaProducer<String> getProducer() {
        return myProducer;
    }


}