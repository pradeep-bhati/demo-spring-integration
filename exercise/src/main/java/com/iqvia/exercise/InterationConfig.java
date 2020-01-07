package com.iqvia.exercise;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.integration.feed.config.FeedInboundChannelAdapterParser;
import org.springframework.integration.feed.dsl.Feed;
import org.springframework.integration.feed.dsl.FeedEntryMessageSourceSpec;
import org.springframework.integration.feed.inbound.FeedEntryMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.metadata.MetadataStore;
import org.springframework.integration.metadata.PropertiesPersistingMetadataStore;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.support.management.MessageSourceManagement;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.integration.annotation.Poller;

import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

@Configuration
public class InterationConfig {
	
	@Value("${feed.url}")
	private Resource feedResource;

	
	   @Bean
	   public IntegrationFlow feedFlow() {
	      return IntegrationFlows
	            .from(Feed.inboundAdapter(this.feedResource, "pubDate")
	                        .metadataStore(metadataStore()),
	                  e -> e.poller(p -> p.fixedDelay(1000).maxMessagesPerPoll(10)))
//	          .transform(Transformers.objectToString())
	            .transform(m -> transformSyndEntryToString(m))
	            .channel(c -> c.queue("entries"))
	            .get();
	   }

	

	
	 @Bean
	    public MetadataStore metadataStore() {
	        PropertiesPersistingMetadataStore metadataStore = new PropertiesPersistingMetadataStore();
	        metadataStore.setBaseDirectory("C:\\Users\\pradeep.bhati\\tmp\\foo");
	        return metadataStore;
	    }
	 
	   @Transformer
	   String transformSyndEntryToString(Object o) {
	      SyndEntry message = (SyndEntry) o;
	      String processedMsg = message.getComments();
	       return processedMsg;
	   }
	
	    @Bean	
	    @ServiceActivator(inputChannel = "entries", poller = @Poller(fixedRate = "5000", maxMessagesPerPoll = "10"))
	    public MessageHandler fileWritingMessageHandler() {
	        FileWritingMessageHandler handler = new FileWritingMessageHandler(new File("C:\\Users\\pradeep.bhati\\tmp\\processed-msgs"));
	        handler.setFileExistsMode(FileExistsMode.REPLACE);
	        handler.setExpectReply(false);
	        return handler;
	    }
	    
	    @Bean(name = PollerMetadata.DEFAULT_POLLER)
	    public PollerMetadata poller() {
	        return Pollers.fixedDelay(1000).get();
	    }

}
