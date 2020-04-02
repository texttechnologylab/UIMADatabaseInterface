package org.texttechnologylab.uimadb.wrapper.virtuoso.java;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VirtuosoHelper {
//
//    public static void main(String[] args){
//        try {
//            virt v = new virt();
//            v.cast_to_db("Test_01", "C:\\Users\\Kilian\\Desktop\\Benchmarks\\3720448.xmi");
//            //v.get_data("Test_00");
//        } catch(Exception e){}
//    }

    public static void cast_to_db(String graph_name, String xmi_path) {



        try {
            Class.forName("virtuoso.jdbc4.Driver");
            Connection conn = DriverManager.getConnection("jdbc:virtuoso://localhost:1111","dba","dba");
            Statement st = conn.createStatement();

            //Get Document and make to Cas
            // xmi_path in form of: "C:\\Temp\\4522153.xmi"
            File xml = new File(xmi_path);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xml);
            doc.getDocumentElement().normalize();
            Element root = doc.getDocumentElement();
            NamedNodeMap NodeMap = root.getAttributes();

            //inital types
            String init_cmd = "SPARQL INSERT IN GRAPH <" + graph_name + "> { ";
            int size = NodeMap.getLength();
            for(int i = 0; i< size; i++) {
                Node n = NodeMap.item(i);
                init_cmd = init_cmd.concat("<xmi:XMI> <").concat(n.getNodeName()).concat("> \"").concat(n.getNodeValue()).concat("\" . ");
            }
            init_cmd = init_cmd.concat("}");
            try {
                st.execute(init_cmd);
            }catch (Exception e){
                System.out.println(init_cmd);
                e.printStackTrace();
            }

            NodeList nl = root.getChildNodes();
            int node_count = nl.getLength() ;

            for(int iEle = 1; iEle < node_count; iEle += 2) {
                String cmd = "SPARQL INSERT IN GRAPH <" + graph_name + "> { ";
                Node curr_element = nl.item(iEle);

                String type = curr_element.getNodeName();
                NodeMap = curr_element.getAttributes();

                //Handle Views
                if(type.equals("cas:View")){
                    Node n_sofa = NodeMap.getNamedItem("sofa");
                    String sofa = n_sofa.getNodeValue();
                    Node n_members = NodeMap.getNamedItem("members");
                    String members = n_members.getNodeValue();
                    cmd = cmd.concat("<View> <").concat(sofa).concat("> \"").concat(members).concat("\" . ");
                    cmd = cmd.concat("}");
                    try {
                        st.execute(cmd);
                    }catch (Exception e){
                        System.out.println(cmd);
                        e.printStackTrace();
                    }
                    continue;
                }

                //Fetching ID
                size = NodeMap.getLength();
                Node id_node = NodeMap.getNamedItem("xmi:id");
                String id = id_node.getNodeValue();
                if(id.equals("97403")){
                    System.out.println(id);
                }

                //Handling Type
                String[] types = type.split(":");
                cmd = cmd.concat("<").concat(id).concat("> <prefix> \"").concat(types[0]).concat("\" . ");
                cmd = cmd.concat("<").concat(id).concat("> <suffix> \"").concat(types[1]).concat("\" . ");


                //Handling Attributes
                int iAttr;
                for (iAttr = 0; iAttr < size; iAttr++) {
                    Node n = NodeMap.item(iAttr);
                    if (n.getNodeName() != "xmi:id") {
                        //String escaped_name = n.getNodeName().replace("\"", "&quot;").replace("{", "{{").replace("}", "}}");
                        String escaped_value = n.getNodeValue().replace("\"", "&quot;").replace("{", "{{").replace("}", "}}").replace("—", "-");
                        cmd = cmd.concat("<").concat(id).concat("> <").concat(n.getNodeName()).concat("> \"").concat(escaped_value).concat("\" . ");
                    }
                }
                if(iAttr <= 1){
                    //if there is no data in the field except the ID
                    continue;
                }

                //Handling Children
                if(curr_element.hasChildNodes()){
                    try {

                        //curr_element.normalize();
                        NodeList childMap = curr_element.getChildNodes();

                        String s =curr_element.getTextContent();
                        int children_count = childMap.getLength();
                        for (int iChild = 1; iChild < childMap.getLength(); iChild += 2) {
                            //Node n = NodeMap.item(iChild);
                            String element = childMap.item(iChild).getNodeName().toString();
                            String Content = childMap.item(iChild).getTextContent().toString();
                            cmd = cmd.concat("<").concat(id).concat("> <").concat(element).concat("> \"").concat(Content).concat("\" . ");
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                cmd = cmd.concat("}");
                // Execute Command
                try {
                    st.execute(cmd);
                }catch (Exception e){
                    System.out.println(cmd);
                    e.printStackTrace();
                }
            }

            //close Connection
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public String get_data(String graph_id){
        try{
            Class.forName("virtuoso.jdbc4.Driver");
            Connection conn = DriverManager.getConnection("jdbc:virtuoso://localhost:1111","dba","dba");


            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element xmi = doc.createElement("xmi:XMI");
            doc.appendChild(xmi);

            //get init String
            String cmd = "SPARQL SELECT * FROM  <".concat(graph_id).concat("> WHERE { <xmi:XMI> ?p ?o }");
            try {
                //TODO
                Statement st = conn.createStatement();
                ResultSet init_types = st.executeQuery(cmd);

                while(init_types.next()){
                    String p = init_types.getObject(1).toString();
                    String o = init_types.getObject(2).toString();
                    Attr attr = doc.createAttribute(p);
                    attr.setValue(o);
                    xmi.setAttributeNode(attr);
                }
            }catch (Exception e){
                System.out.println(cmd);
                e.printStackTrace();
            }


            try {
                //get all id's
                cmd = "SPARQL SELECT DISTINCT ?s FROM <" + graph_id + "> WHERE{ ?s ?p ?o } ORDER BY DESC(?s)";
                Statement st_1 = conn.createStatement();
                ResultSet ids = st_1.executeQuery(cmd);

                ResultSetMetaData rsmd = ids.getMetaData();
                int cnt = rsmd.getColumnCount();
                List<String> id_list = new ArrayList();
                String corry = "";
                try {
                    while (ids.next()) {
                        corry = ids.getObject(1).toString();
                        id_list.add(corry);
                    }
                } catch(Exception e){
                    //no issue the array is just empty
                }
                st_1.close();
                //ids.first();
                for(String current_id : id_list){
                    //Object current_id = ids.getObject(1);
                    if(isNumeric(current_id.toString())){
                        //Get Type of Annotation
                        String cmd_prefix = "SPARQL SELECT * FROM <".concat(graph_id).concat("> WHERE { <").concat(current_id.toString()).concat("> <prefix> ?o }");
                        String cmd_suffix = "SPARQL SELECT * FROM <".concat(graph_id).concat("> WHERE { <").concat(current_id.toString()).concat("> <suffix> ?o }");
                        String annotation_type = "";
                        try{
                            Statement st_2 = conn.createStatement();
                            ResultSet suffix = st_2.executeQuery(cmd_prefix);
                            ResultSet prefix = st_2.executeQuery(cmd_suffix);
                            suffix.next();
                            prefix.next();
                            annotation_type = suffix.getObject(1).toString() + ":" + prefix.getObject(1).toString();
                            st_2.close();
                        } catch(Exception e){
                            System.out.println(cmd_prefix);
                            e.printStackTrace();
                        }



                        //Create new Element
                        Element current_element = doc.createElement(annotation_type);

                        //Get all Attributes of an annotation
                        ResultSet attrs = null;
                        Statement st_3 = conn.createStatement();
                        try{
                            String cmd_attr = "SPARQL SELECT * FROM <".concat(graph_id).concat("> WHERE { <").concat(current_id.toString()).concat("> ?p ?o }");
                            attrs = st_3.executeQuery(cmd_attr);
                        } catch(Exception e){
                            System.out.println(cmd_prefix);
                            e.printStackTrace();
                        }

                        try {
                            while (attrs.next()) {
                                String prd = attrs.getObject(1).toString();
                                String obj = attrs.getObject(2).toString();
                                if ((!prd.equals("prefix")) && (!prd.equals("suffix")) && (!prd.equals("WikiDataHyponyms"))) {
                                    current_element.setAttribute(prd, obj);
                                    //Attr attr = doc.createAttribute(prd);
                                    //attr.setValue();
                                    //current_element.setAttribute(,);
                                } else if (prd.equals("WikiDataHyponyms")) {
                                    Element sub_element = doc.createElement(prd);
                                    sub_element.setTextContent(obj);
                                    current_element.appendChild(sub_element);
                                }
                            }
                        }catch (Exception e){
                            System.out.println(attrs.getObject(1));
                            System.out.println(attrs.getObject(2));
                            attrs.next();
                            e.printStackTrace();
                        }

                        st_3.close();
                        //add ID to current element
                        current_element.setAttribute("xmi:id", current_id);

                        //add Element to Xml
                        xmi.appendChild(current_element);
                        //String whole = doc_to_string(doc);
                    }
                }

                //handle Views
                Statement st_4 = conn.createStatement();
                String cmd_views = "SPARQL SELECT * FROM <".concat(graph_id).concat("> WHERE { <View> ?p ?o }");
                ResultSet views = st_4.executeQuery(cmd_views);
                try {
                    while (views.next()) {
                        String prd = views.getObject(1).toString();
                        String obj = views.getObject(2).toString();
                        Element curr_view = doc.createElement("cas:View");
                        curr_view.setAttribute("members", obj);
                        curr_view.setAttribute("sofa", prd);
                        xmi.appendChild(curr_view);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                st_4.close();


            }catch (Exception e){
                e.printStackTrace();
            }

            return doc_to_string(doc);

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static String doc_to_string(Document doc){
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch(Exception e) {
            e.printStackTrace();
            return "";
        }

    }

}
