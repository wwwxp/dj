package com.tydic.util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
 
/**
 * XML工具类
 * 
 */
public class XmlTool {
    /**
     * 获取根节点
     * 
     * @param doc
     * @return
     */
    public static Element getRootElement(Document doc) {
        if (doc == null) {
            return null;
        }
        return doc.getRootElement();
    }
 
    /**
     * 获取节点eleName下的文本值，若eleName不存在则返回默认值defaultValue
     * 
     * @param eleName
     * @param defaultValue
     * @return
     */
    public static String getElementValue(Element eleName, String defaultValue) {
        if (eleName == null) {
            return defaultValue == null ? "" : defaultValue;
        } else {
            return eleName.getTextTrim();
        }
    }
 
    public static String getElementValue(String eleName, Element parentElement) {
        if (parentElement == null) {
            return null;
        } else {
            Element element = parentElement.element(eleName);
            if (element !=null) {
                return element.getTextTrim();
            } else {
                try {
                    throw new Exception("找不到节点" + eleName);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }
 
    /**
     * 获取节点eleName下的文本值
     * 
     * @param eleName
     * @return
     */
    public static String getElementValue(Element eleName) {
        return getElementValue(eleName, null);
    }
 
    public static Document read(File file) {
        return read(file, null);
    }
    
    public static Document read(InputStream in) {
    	return read(in, null);
    }
    
    public static Document read(InputStream in, String charset) {
    	if (in == null) {
    		return null;
    	}
    	SAXReader reader = new SAXReader();
    	if (charset != null) {
            reader.setEncoding(charset);
        }
    	Document document = null;
    	try {
    		document = reader.read(in);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
    	return document;
    }
 
    public static Document findCDATA(Document body, String path) {
        return XmlTool.stringToXml(XmlTool.getElementValue(path,
                body.getRootElement()));
    }
 
    /**
     * 
     * @param file
     * @param charset
     * @return
     * @throws DocumentException
     */
    public static Document read(File file, String charset) {
        if (file == null) {
            return null;
        }
        SAXReader reader = new SAXReader();
        if (charset != null) {
            reader.setEncoding(charset);
        }
        Document document = null;
        try {
            document = reader.read(file);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return document;
    }
 
    public static Document read(URL url) {
        return read(url, null);
    }
 
    /**
     * 
     * @param url
     * @param charset
     * @return
     * @throws DocumentException
     */
    public static Document read(URL url, String charset) {
        if (url == null) {
            return null;
        }
        SAXReader reader = new SAXReader();
        if (charset != null) {
            reader.setEncoding(charset);
        }
        Document document = null;
        try {
            document = reader.read(url);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return document;
    }
 
    /**
     * 将文档树转换成字符串
     * 
     * @param doc
     * @return
     */
    public static String xmltoString(Document doc) {
        return xmltoString(doc, null);
    }
 
    /**
     * 
     * @param doc
     * @param charset
     * @return
     * @throws IOException
     */
    public static String xmltoString(Document doc, String charset) {
        if (doc == null) {
            return "";
        }
        if (charset == null) {
            return doc.asXML();
        }
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding(charset);
        StringWriter strWriter = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(strWriter, format);
        try {
            xmlWriter.write(doc);
            xmlWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strWriter.toString();
    }
 
    /**
     * 持久化Document
     * @param doc
     * @param charset
     * @return
     * @throws Exception
     * @throws IOException
     */
    public static void xmltoFile(Document doc, File file, String charset)
            throws Exception {
        if (doc == null) {
            throw new NullPointerException("doc cant not null");
        }
        if (charset == null) {
            throw new NullPointerException("charset cant not null");
        }
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding(charset);
        FileOutputStream os = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(os, charset);
        XMLWriter xmlWriter = new XMLWriter(osw, format);
        try {
            xmlWriter.write(doc);
            xmlWriter.close();
            if (osw != null) {
                osw.close();
            }
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    /**
     * 
     * @param doc
     * @param charset
     * @return
     * @throws Exception
     * @throws IOException
     */
    public static void xmltoFile(Document doc, String filePath, String charset)
            throws Exception {
        xmltoFile(doc, new File(filePath), charset);
    }
 
     
    /**
     * 
     * @param doc
     * @param filePath
     * @param charset
     * @throws Exception
     */
    public static void writDocumentToFile(Document doc, String filePath, String charset)
            throws Exception {
        xmltoFile(doc, new File(filePath), charset);
    }
     
    public static Document stringToXml(String text) {
        try {
            return DocumentHelper.parseText(text);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }
     
    public static Document createDocument() {
        return DocumentHelper.createDocument();
    }
     
    /**
	 * 
	 * @方法功能描述：得到子节点，不使用xpath
	 * @方法名:getChild
	 * @param parent
	 * @param childName
	 * @return @参数描述 :
	 * @返回类型：Element
	 * @时间：2011-4-14下午12:53:22
	 */
	@SuppressWarnings("rawtypes")
	public static Element getChildElement(Element parent,String childName){
		childName = childName.trim();
		if(parent==null)
			return null;
		if(childName==null || childName.equals(""))
			return null;
		Element e = null;
		Iterator it = getIterator(parent);
		while(it!=null && it.hasNext()){
			Element k = (Element)it.next();
			if(k==null)continue;
			if(k.getName().equalsIgnoreCase(childName)){
				e = k;
				break;
			}
		}
		return e;
	}
	
	/**
	 * 
	 * @方法功能描述:得到指定元素的迭代器
	 * @方法名:getIterator
	 * @param parent
	 * @返回类型：Iterator<Element>
	 * @时间：2011-4-14上午11:29:18
	 */
	@SuppressWarnings("unchecked")
	public static Iterator<Element> getIterator(Element parent){
		if(parent == null)
			return null;
		Iterator<Element> iterator = parent.elementIterator();
		return iterator;
	}
	
    public static void main(String[] args) {
    
		
		try {
			Document doc = XmlTool.read(new File("D:/Personal/Desktop/sp_switch.xml"), "UTF-8");
			Element rootElement = XmlTool.getRootElement(doc);
			Element kafka_switch = XmlTool.getChildElement(rootElement, "UP");
			String current = kafka_switch.attribute("current").getValue();
			System.out.println("current:" + current);
			kafka_switch.addAttribute("current", "jack3");
			
			
//			String text = kafka_switch.getText();
//			if(text.equals("0")){
//				kafka_switch.setText("1");
//			}else if(text.equals("1")){
//				kafka_switch.setText("0");
//			}
			XmlTool.writDocumentToFile(doc, "D:/Personal/Desktop/sp_switch.xml", "UTF-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       		
        // System.out.println(XmlTool.xmltoString(Disconnect.getDisconnectDocument(),
        // "UTF-8"));
    }
}
