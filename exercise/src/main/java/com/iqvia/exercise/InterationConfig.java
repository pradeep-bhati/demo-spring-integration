package com.iqvia.exercise;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.feed.dsl.Feed;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.metadata.MetadataStore;
import org.springframework.integration.metadata.PropertiesPersistingMetadataStore;
import org.springframework.messaging.Message;
import com.rometools.rome.feed.synd.SyndEntry;

@Configuration
public class InterationConfig {
	
	@Value("${feed.url}")
	private Resource feedResource;
	
	@Value("${root.folder}")
	private String rootFolder;

	
	   @Bean
	   public IntegrationFlow feedFlow() {
	      return IntegrationFlows
	            .from(Feed.inboundAdapter(this.feedResource, "pubDate")
	                        .metadataStore(metadataStore()),
	                  e -> e.poller(p -> p.fixedDelay(100)))
//	          .transform(Transformers.objectToString())
	           .enrichHeaders(h -> h.headerExpression("fileName","payload.uri.toString()"))
	           .enrichHeaders(h -> h.headerFunction("dir", m -> parseDirectory(m)))
	           .enrichHeaders(h -> h.headerFunction("logData", m -> logData(m)))
//	           .log(LoggingHandler.Level.INFO,"TEST_LOGGER",m -> m.getHeaders().get("logData"))
	           .transform(m -> transformSyndEntryToXmlString(m)) 
	           
	           .handle(Files.outboundAdapter(m -> m.getHeaders().get("dir"))
                       .fileNameGenerator(m -> m.getHeaders().get("fileName").toString()+".xml")
                       .autoCreateDirectory(true))
	           .transform(m -> m.toString())
	           .log(LoggingHandler.Level.INFO,"TEST_LOGGER",m -> m.getHeaders().get("logData"))
//	           .logAndReply(LoggingHandler.Level.INFO,"TEST_LOGGER",m -> m.getHeaders().get("logData"));
//	            .channel(c -> c.queue("entries"))	          	           
	            .get();
	   }

	   
	   String logData(Message<Object> m){
	        SyndEntry message = (SyndEntry) m.getPayload();
	        return message.getLink();
	   }
	
	   String parseDirectory(Message<Object> m){
	        SyndEntry message = (SyndEntry) m.getPayload();
	        String direcoryStructure = createDirectoryStructure(message);
	        return direcoryStructure;       
	    }
	   
	   String createDirectoryStructure(SyndEntry message) {
		   if(message.getPublishedDate()!=null)
	        {
	        	SimpleDateFormat smpl = new SimpleDateFormat("yyyy-MM-dd");
	        	String date = smpl.format(message.getPublishedDate());
	        	if(message.getCategories()!=null) {
	        		String category = message.getCategories().get(0).getName();
	        		return rootFolder+"/"+date+"/"+category;
	        	}
	        	else {
	        	return rootFolder+"/"+date+"categoryNotPresent";
	        	}
	        }
	        else
	        	{return rootFolder+"/"+"dateNotThere";}
		   
	   }

	
	 @Bean
	   public MetadataStore metadataStore() {
	        PropertiesPersistingMetadataStore metadataStore = new PropertiesPersistingMetadataStore();
	        metadataStore.setBaseDirectory("C:\\Users\\pradeep.bhati\\tmp\\foo");
	        return metadataStore;
	    }
	 
	   @Transformer
	   String transformSyndEntryToXmlString(Object o)  {
		  SyndEntry message = (SyndEntry) o;
		  Item item = populateItem(message);
		  String ItemXml = doMarshall(item);
		  return ItemXml;	             
	   }
	   
	   Item populateItem(SyndEntry message)
	   {
		   	Item item = new Item();
			item.setLink(message.getLink());
			item.setTitle(message.getTitle());
			if(message.getCategories()!=null) {
	      		String category = message.getCategories().get(0).getName();
	      		item.setCategory(category);
	      	}
			if(message.getPublishedDate()!=null) {
			  item.setPubDate(message.getPublishedDate().toString());
			}
			if(message.getDescription()!=null) {
			  item.setDescription(message.getDescription().getValue());
			}
			item.setGuid(message.getUri());
			item.setComment(message.getComments());
			return item;
	   }
	   
	   
	   String doMarshall(Item item) {
		   JAXBContext contextObj;
		   StringWriter sw = new StringWriter();
			try {
				contextObj = JAXBContext.newInstance(Item.class);
				Marshaller marshallerObj =  contextObj.createMarshaller();
				marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); 
				marshallerObj.marshal(item, sw);	
				
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 				  
		return sw.toString();	   
	   }
	
//	    @Bean	
//	    @ServiceActivator(inputChannel = "entries", poller = @Poller(fixedRate = "5000", maxMessagesPerPoll = "10"))
//	    public MessageHandler fileWritingMessageHandler() {
//	    	
//	    	Expression directoryExpression = new SpelExpressionParser().parseExpression("headers.dir");
//	        FileWritingMessageHandler handler = new FileWritingMessageHandler(directoryExpression);
//	        handler.setFileExistsMode(FileExistsMode.REPLACE);  
//	        handler.setExpectReply(false);
//	        handler.setFileNameGenerator(a -> a.getHeaders().getId().toString());
//	        handler.setAutoCreateDirectory(true);
//	        
//	        
//	        return handler;
//	    }
//	    
//	    @Bean(name = PollerMetadata.DEFAULT_POLLER)
//	    public PollerMetadata poller() {
//	        return Pollers.fixedDelay(1000).get();
//	    }

}
