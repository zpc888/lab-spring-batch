
package io.spring.batch.helloworld.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;

@Configuration
public class ChunkBasedStep {
    @Autowired private JobBuilderFactory jobBuilderFactory;
    @Autowired private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job myChunkBasedJob() {
        return this.jobBuilderFactory.get("myChunkBasedJob")
                .start(myChunkBasedStep()).build();
    }

    @Bean
    public Step myChunkBasedStep() {
        return this.stepBuilderFactory.get("myChunkBasedStep")
                .<String, String>chunk(10)
                .reader(itemReader(null))
                .writer(itemWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<String> itemReader(@Value("#{jobParameters['inputFile']}") Resource inputFile) {
        try {
            System.out.println("inputFile = " + inputFile.getURL().toExternalForm());
        } catch (Exception ex) {
            // ignore it
        }
        return new FlatFileItemReaderBuilder<String>()
                .name("itemReader")
                .resource(inputFile)
                .lineMapper(new EchoLineMapper())
                .lineMapper(new PassThroughLineMapper())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<String> itemWriter(@Value("#{jobParameters['outputFile']}") Resource outputFile) {
        try {
            System.out.println("outputFile = " + outputFile.getURL().toExternalForm());
        } catch (Exception ex) {
            // ignore it
        }
        return new FlatFileItemWriterBuilder<String>()
                .name("itemWriter")
                .resource( new FileSystemResource("out-2.txt") )
                .lineAggregator( new LineAggregatorMapper() )
                .build();
    }

    private static class LineAggregatorMapper implements LineAggregator {
        private PassThroughLineAggregator<String> aggregator = new PassThroughLineAggregator<>();

        @Override
        public String aggregate(Object item) {
            String s = (String) item;
            System.out.println("OUT: " + s);
            File f = new File("ok.txt");
            if (!f.exists() && "a14".equals(s)) {
                throw new RuntimeException("on-purpose error out");
            }
            return aggregator.aggregate(s);
        }

    }

    private static class EchoLineMapper implements LineMapper<String> {
        @Override
        public String mapLine(String line, int lineNumber) throws Exception {
            System.out.printf("ECHO BACK: %02d - %s%n", lineNumber, line);
            return line;
        }
    }
}
