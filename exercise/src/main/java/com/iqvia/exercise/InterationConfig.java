package com.iqvia.exercise;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.support.MessageBuilder;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

@Configuration
public class InterationConfig {
	
	
	
	@Value("${feed.url}")
	private String feedUrl;
	
	
	
	@Bean
	public MessageBuilder<SyndFeed> getMessage() throws Exception
	{
		URL url = new URL(feedUrl);
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(url));
		
		
		System.out.println("pradeep");
//		System.out.println(feed.getAuthor());
//		System.out.println(feed.getDocs());
//		System.out.println(feed.getTitle());
//		System.out.println(feed.getDescription());
		List<SyndEntry>  str = feed.getEntries();
		List<SyndLink> list = feed.getLinks();
		System.out.println("single link"+feed.getLink());
		
//		str.stream().forEach(a -> System.out.println(a.getCategories()));
//		List<List<SyndContent>> list = str.stream().map(a -> a.getContents()).collect(Collectors.toList());
//		list.stream().flatMap(a -> a.)
		System.out.println(feed.getLinks().size());
		System.out.println(feed.getEntries().size());
		for(SyndEntry s : str) {
//			System.out.println("title"+s.getTitle());
//			System.out.println("link"+s.getLink());
//			System.out.println("description"+s.getDescription().getValue());
//			System.out.println("date"+s.getPublishedDate().toString());
//			System.out.println("category size"+s.getCategories().size());
			System.out.println("category size"+s.getContributors().size());
			
			System.out.println("module size:"+s.getModules().size());
			s.getCategories().stream().forEach(a -> System.out.println(a.getName()));
			
//			System.out.println("guid size"+s.getCategories().s);
//			System.out.println("size of authors"+s.getAuthors().size());
//			System.out.println("size of category"+s.getCategories().size());
//			System.out.println("size of contributors"+s.getContributors().size());
			//System.out.println(s.get.size());
//			System.out.println("size of modules"+s.getModules().size());
			//System.out.println(s.getDescription());
//			System.out.println("size of mark up"+s.getForeignMarkup().size());
//			System.out.println("size of closure"+s.getEnclosures().size());
//			System.out.println("size of links"+s.getLinks().size());
//			s.getCategories().stream().forEach(a -> System.out.println("category"+a.getName()));
//			s.getContents().stream().forEach(a -> System.out.println(a.getValue()));
		}
//		str.stream().forEach(a -> System.out.println(a.getC));
//		feed.getEntries().stream().forEach(a -> {a.getContents().stream().forEach(b -> System.out.println(b.getValue()));});
		return MessageBuilder.withPayload(feed);
			
	};
	
	

	@Bean
	public IntegrationFlow getFlow() throws Exception
	{
		return IntegrationFlows.from(getMessage().getPayload().getTitle()).transform(s -> s.toString()).get();
	}
	
}
