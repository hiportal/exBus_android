package com.ex.exbus.util;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

public class XMLData extends DataList {
    public static final String ID_NODE = "_NODE_";

    private Node node = null;

    public XMLData(File file) throws XmlPullParserException {
        try {
            node = XMLUtil.parse(file);
        } catch(Exception e) {
            Log.d("","XMLDATA error file =" + e.toString());
            throw new XmlPullParserException("Data Parsing Error!");
        }
    }

    public XMLData(InputStream is) throws XmlPullParserException {
        try {
            node = XMLUtil.parse(is);
        } catch(Exception e) {
            Log.d("","XMLDATA error InputStream =" + e.toString());
            throw new XmlPullParserException("Data Parsing Error!");
        }
    }

    public XMLData(String xmlContent) throws XmlPullParserException {
        try {
            node = XMLUtil.parseContent(xmlContent);
        } catch(Exception e) {
            Log.d("","XMLDATA error String = " + e.toString());
            e.printStackTrace();
            throw new XmlPullParserException("Data Parsing Error!");
        }
    }

    public XMLData(Node node) throws XmlPullParserException {
        //TODO NodeType Check
        if(node == null)
            throw new XmlPullParserException("Node is null!");

        this.node = node;
    }

    public void setList(String xpathExpression) throws XmlPullParserException {
        if(list == null)
            list = new ArrayList<Map<String, Object>>();
        else
            list.clear();

        try {
            NodeList nodeList = null;
            if(xpathExpression.charAt(0) != '/') {
                switch(node.getNodeType()) {
                    case Node.DOCUMENT_NODE :
                        Document document = (Document)node;
                        nodeList = document.getElementsByTagName(xpathExpression);
                        break;
                    case Node.ELEMENT_NODE :
                        Element element = (Element)node;
                        nodeList = element.getElementsByTagName(xpathExpression);
                        break;
                    default :
                        throw new XmlPullParserException("Node-Type is invalid!");
                }
            } else {
                nodeList = XMLUtil.selectNodeList(node, xpathExpression);
            }

            for(int i=0; i<nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Map<String, Object> value = new HashMap<String, Object>();

                value.put(ID_NODE, node);

                NodeList childNodeList = node.getChildNodes();
                for(int j=0; j<childNodeList.getLength(); j++) {
                    Node childNode = childNodeList.item(j);

                    if(childNode.getLocalName() != null)
                        value.put(childNode.getLocalName(), XMLUtil.getTextContent(childNode));
                }

                list.add(value);
            }
        } catch(XmlPullParserException e) {
            throw e;
        } catch(Exception e) {
            throw new XmlPullParserException(e.toString());
        }
    }

    public Node getRoot() {
        return this.node;
    }

    public Node getNode(String xpathExpression) throws XmlPullParserException {
        try {
            if(xpathExpression.charAt(0) != '/') {
                NodeList nodeList = null;
                switch(node.getNodeType()) {
                    case Node.DOCUMENT_NODE :
                        Document document = (Document)node;
                        nodeList = document.getElementsByTagName(xpathExpression);
                        break;
                    case Node.ELEMENT_NODE :
                        Element element = (Element)node;
                        nodeList = element.getElementsByTagName(xpathExpression);
                        break;
                    default :
                        throw new XmlPullParserException("Node-Type is invalid!");
                }

                if(nodeList.getLength() > 0) {
                    return nodeList.item(0);
                } else {
                    return null;
                }
            } else {
                return XMLUtil.getSingleNode(node, xpathExpression);
            }
        } catch(XmlPullParserException e) {
            throw e;
        } catch(Exception e) {
            throw new XmlPullParserException(e.toString());
        }
    }

    public String get(String xpathExpression) throws XmlPullParserException {
        return XMLUtil.getTextContent(getNode(xpathExpression));
    }

    public XMLData getChild(String xpathExpression) throws XmlPullParserException {
        return new XMLData(getNode(xpathExpression));
    }

    public XMLData getChild(int i) throws XmlPullParserException {
        if(list == null)
            throw new XmlPullParserException("List is null!");

        return new XMLData((Node)getObject(i, ID_NODE));
    }

    /*
     * XPath濡? ?삤硫? ?븞?릺怨? ?븯?굹?쓽 ?끂?뱶 ?씠由꾩쑝濡? ???빞 ?븳?떎.
     */
    public XMLData getChild(int i, String nodeName) throws XmlPullParserException {
        XMLData xmlData = getChild(nodeName);
        xmlData.setList(nodeName);
        Map<String, Object> result = xmlData.getListItem(i);
        return (XMLData) result.get(nodeName);
    }

    public String getContent(int i) throws XmlPullParserException {
        if(list == null)
            throw new XmlPullParserException("List is null!");

        String ret = "";
        try {
            ret = XMLUtil.getTextContent((Node)getObject(i, ID_NODE));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

}