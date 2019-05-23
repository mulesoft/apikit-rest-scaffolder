package org.mule.tools.apikit.model;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.DOMOutputter;
import org.mule.tools.apikit.input.parsers.HttpListenerConfigParser;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MuleConfig implements NamedContent, WithConstructs, WithConfigs {

  private String name;
  private Document content;
  private List<HttpListenerConfig> configurations;
  private List<Flow> flows;
  private List<Test> tests;

  private MuleConfig(Document content, List<HttpListenerConfig> configurations, List<Flow> flows, List<Test> test) {
    this.content = content;
    this.configurations = configurations;
    this.flows = flows;
    this.tests = test;
  }

  public static MuleConfig fromStream(InputStream input) throws Exception {
    SAXBuilder builder = new SAXBuilder();
    Document inputAsDocument = builder.build(input);
    return fromDoc(inputAsDocument);
  }

  private static MuleConfig fromDoc(Document doc) {
    HttpListenerConfigParser httpConfigParser = new HttpListenerConfigParser();
    List<HttpListenerConfig> configurations = httpConfigParser.parse(doc);

    List<Flow> flowsInConfig = new ArrayList<>();
    List<Test> testsInConfig = new ArrayList<>();

    for (int i = 0; i < doc.getRootElement().getContentSize(); i++) {
      Content content = doc.getRootElement().getContent(i);
      if (content instanceof Element && "flow".equals(((Element) content).getName())) {
        flowsInConfig.add(Flow.fromDocument(content.getDocument()));
      }

      if (content instanceof Element && "munit:test".equals(((Element) content).getName())) {
        testsInConfig.add(Test.fromDocument(content.getDocument()));
      }
    }

    return new MuleConfig(doc, configurations, flowsInConfig, testsInConfig);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public InputStream getContent() {
    try {
      org.w3c.dom.Document outputDoc = new DOMOutputter().output(content);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      Source xmlSource = new DOMSource(outputDoc);
      Result outputTarget = new StreamResult(outputStream);
      TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
      InputStream contentAsInputStream = new ByteArrayInputStream(outputStream.toByteArray());
      return contentAsInputStream;
    } catch (Exception e) {
      // todo handle exception
    }
    return null;
  }

  @Override
  public List<HttpListenerConfig> getConfigs() {
    return configurations;
  }

  @Override
  public List<Flow> getFlows() {
    return flows;
  }

  @Override
  public List<Test> getTests() {
    return tests;
  }

}
