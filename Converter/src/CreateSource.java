import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.Properties;

public class CreateSource {

    public static void main(String[] args) throws Exception {
        parse("../source/Android/2.6/FBReader-en_US.xml", "../source/Android/2.6/FBReader-en_US.ini");
        parse("../source/Android/2.6/zlibrary-en_US.xml", "../source/Android/2.6/zlibrary-en_US.ini");
        parse("../source/Desktop/FBReader-en.xml", "../source/Desktop/FBReader-en.ini");
        parse("../source/Desktop/zlibrary-en.xml", "../source/Desktop/zlibrary-en.ini");
    }

    static void parse(String inFile, String outFile) throws Exception {
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        XMLEventReader rd = inputFactory.createXMLEventReader(new BufferedInputStream(new FileInputStream(inFile)));
        OutputStreamWriter wrIni = new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8");

        Method convertMethod = Properties.class.getDeclaredMethod("saveConvert", String.class, boolean.class,
                boolean.class);
        convertMethod.setAccessible(true);

        String path = "";
        while (rd.hasNext()) {
            XMLEvent e = rd.nextEvent();
            switch (e.getEventType()) {
            case XMLEvent.START_ELEMENT:
                if (e.asStartElement().getName().getLocalPart().equals("node")) {
                    Attribute nameAttr = e.asStartElement().getAttributeByName(QName.valueOf("name"));
                    Attribute conditionAttr = e.asStartElement().getAttributeByName(QName.valueOf("condition"));
                    Attribute valueAttr = e.asStartElement().getAttributeByName(QName.valueOf("value"));
                    path = incPath(path, nameAttr != null ? nameAttr.getValue() : conditionAttr.getValue());
                    if (valueAttr != null) {
                        String key = (String) convertMethod.invoke(new Properties(), path, true, false);
                        String value = (String) convertMethod.invoke(new Properties(), valueAttr.getValue(), false,
                                false);
                        wrIni.write(key + '=' + value + '\n');
                    }
                }
                break;
            case XMLEvent.END_ELEMENT:
                if (e.asEndElement().getName().getLocalPart().equals("node")) {
                    path = decPath(path);
                }
                break;
            }
        }
        if (path.length() > 0) {
            throw new Exception();
        }
        rd.close();
        wrIni.close();
    }

    static String incPath(String path, String add) {
        return path.length() > 0 ? path + '/' + add : add;
    }

    static String decPath(String path) {
        int pos = path.lastIndexOf('/');
        return pos >= 0 ? path.substring(0, pos) : "";
    }
}
