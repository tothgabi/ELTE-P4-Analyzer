package parser;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parser.p4.P4BaseVisitor;

public class XMLParseTree {
    public static void toFile(Document doc, String path, boolean prettyPrint) throws TransformerException {

            TransformerFactory tFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
            Transformer transformer = tFactory.newTransformer();

            if(prettyPrint){
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
            }

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));
            transformer.transform(source, result);
    }


    public static Document fromParseTree(ParseTree tree) throws ParserConfigurationException {
            XMLCreatorVisitor v = new XMLCreatorVisitor();
            Element root = tree.accept(v);
            v.doc.appendChild(root);
            return v.doc;
    }

    static class XMLCreatorVisitor extends P4BaseVisitor<Element> {
        DocumentBuilderFactory docFactory;
        DocumentBuilder docBuilder;
        Document doc;

        public XMLCreatorVisitor() throws ParserConfigurationException {
            docFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
        }

        @Override
        public Element visitTerminal(TerminalNode node) {
            
            Element elem = doc.createElement("TERM");
            elem.appendChild(doc.createTextNode(node.getText()));
            return elem;
        }

        @Override
        public Element visitChildren(RuleNode node) {
            Element result = doc.createElement(node.getClass().getSimpleName());

            int n = node.getChildCount();
            for (int i=0; i<n; i++) {
                if (!shouldVisitNextChild(node, result)) {
                    break;
                }

                ParseTree c = node.getChild(i);
                Element childResult = c.accept(this);
                result = aggregateResult(result, childResult);
            }

            return result;
        }

        @Override
        protected Element aggregateResult(Element aggregate, Element nextResult) {
            aggregate.appendChild(nextResult);
            return aggregate;
        }
    }
}