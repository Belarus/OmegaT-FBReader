import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import com.sun.xml.internal.stream.events.CommentEvent;

public class CreateTarget {

    public static void main(String[] args) throws Exception {
        parse("../source/Android/1.6.5/FBReader-en.xml", "../target/Android/1.6.5/FBReader-en.ini",
                "../target/Android/1.6.5/FBReader-be.xml");
        parse("../source/Android/1.6.5/zlibrary-en.xml", "../target/Android/1.6.5/zlibrary-en.ini",
                "../target/Android/1.6.5/zlibrary-be.xml");
        parse("../source/Desktop/FBReader-en.xml", "../target/Desktop/FBReader-en.ini",
                "../target/Desktop/FBReader-be.xml");
        parse("../source/Desktop/zlibrary-en.xml", "../target/Desktop/zlibrary-en.ini",
                "../target/Desktop/zlibrary-be.xml");
    }

    static void parse(String inFileXml, String inFileIni, String outFile) throws Exception {
        Properties props = new Properties();
        Reader rdIni = new InputStreamReader(new FileInputStream(inFileIni), "UTF-8");
        props.load(rdIni);
        rdIni.close();

        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEventReader rd = inputFactory.createXMLEventReader(new BufferedInputStream(new FileInputStream(inFileXml)));
        XMLEventWriter wr = outputFactory.createXMLEventWriter(new FileOutputStream(outFile));

        String path = "";
        while (rd.hasNext()) {
            XMLEvent e = rd.nextEvent();
            switch (e.getEventType()) {
            case XMLEvent.START_DOCUMENT:
                wr.add(e);
                wr.add(eventFactory.createCharacters("\n"));
                break;
            case XMLEvent.COMMENT:
                CommentEvent comment = (CommentEvent) e;
                if (comment.getText().contains("English FBReaderJ resources")) {
                    wr.add(eventFactory.createComment(" Belarusian FBReaderJ resources "));
                    wr.add(eventFactory.createCharacters("\n"));
                } else {
                    wr.add(e);
                }
                break;
            case XMLEvent.START_ELEMENT:
                if (e.asStartElement().getName().getLocalPart().equals("node")) {
                    Attribute nameAttr = e.asStartElement().getAttributeByName(QName.valueOf("name"));
                    Attribute conditionAttr = e.asStartElement().getAttributeByName(QName.valueOf("condition"));
                    Attribute valueAttr = e.asStartElement().getAttributeByName(QName.valueOf("value"));
                    path = incPath(path, nameAttr != null ? nameAttr.getValue() : conditionAttr.getValue());
                    if (valueAttr != null) {
                        List<Attribute> attrs = new ArrayList<>();
                        for (Iterator<Attribute> it = e.asStartElement().getAttributes(); it.hasNext();) {
                            Attribute a = it.next();
                            if (a.getName().getLocalPart().equals("value")) {
                                String value = props.getProperty(path);
                                if (value == null) {
                                    throw new Exception("There is no translation for " + path);
                                }
                                a = eventFactory.createAttribute("value", value);
                            }
                            attrs.add(a);
                        }
                        String valueOrig = valueAttr.getValue();
                        e = eventFactory.createStartElement("", null, "node", attrs.iterator(), null);
                    }
                }
                wr.add(e);
                break;
            case XMLEvent.END_ELEMENT:
                if (e.asEndElement().getName().getLocalPart().equals("node")) {
                    path = decPath(path);
                }
                wr.add(e);
                break;
            default:
                wr.add(e);
                break;
            }
        }
        if (path.length() > 0) {
            throw new Exception();
        }
        rd.close();
        wr.close();
    }

    static String incPath(String path, String add) {
        return path.length() > 0 ? path + '/' + add : add;
    }

    static String decPath(String path) {
        int pos = path.lastIndexOf('/');
        return pos >= 0 ? path.substring(0, pos) : "";
    }
}
