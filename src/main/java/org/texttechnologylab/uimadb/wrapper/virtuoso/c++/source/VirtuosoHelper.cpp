#include "VirtuosoHelper.hpp"
#include <rpcdce.h>

VirtuosoHelper::VirtuosoHelper() {}

/**
*Sets the Graph ID
*/
const void VirtuosoHelper::set_graph_id(const std::wstring& id) { 
	graph_id = id;
}

/**
/* Fetches all graphnames from an Virtuoso Database and writes them to a Vector
/* @param: graphnames: vector where the Names shall be written to
/* @param: connector: connector to the Database
*/
const int VirtuosoHelper::get_all_graphnames(std::vector<std::wstring> graphnames, virt &connector) {
    std::wstring cmd = LR"(SPARQL SELECT  DISTINCT ?g WHERE  { GRAPH ?g {?s ?p ?o} } ORDER BY  ?g)";
    std::map<std::wstring, std::wstring> result;
    auto err = connector.ODBC_Execute(cmd.data());
    if (err != 0) { return -1; }
    err = connector.ODBC_ResultToString(result);
    if (err != 0) { return -1; }

    for (auto name : result) {
        graphnames.emplace_back(name.first);
    }
    return 0;
}


const std::wstring VirtuosoHelper::get_type_names(xml_node<wchar_t> &node) {
	std::wstringstream cmd;
	Sparkel sparkel(cmd);
	sparkel.open_insert(graph_id.c_str());

	for (xml_attribute<wchar_t>* attr = node.first_attribute(); attr; attr = attr->next_attribute())
	{
		sparkel.add_triplet(L"xmi:XMI", attr->name(), attr->value());
	}
	sparkel.close_query();
	return cmd.str();
}

/**
/* Parses an UIMA-Cas-Document and writes it in SPARQL-Statements to a sparkel(wstringstream).
/* @param first_o the first Node of the XMI/XML-File 
/* @param sparkel a SPARQL-Writer
*/
const void VirtuosoHelper::uima_to_rdf(xml_node<wchar_t>* first_o, Sparkel &sparkel) {
	std::wstringstream cmd;
	Sparkel sub_sparkel(cmd);
	std::wstring name = first_o->name();
	auto partial = util.split_at_delimiter(name, L":");
	auto annotation_type{ partial.second };
	sub_sparkel.add_triplet(L"{0}", L"prefix", partial.first.c_str());
	sub_sparkel.add_triplet(L"{0}", L"suffix", partial.second.c_str());
	std::wstring xmi_id{};

	/************************************* Get All Attributes *************************************/
	for (xml_attribute<wchar_t>* attr = first_o->first_attribute(); attr; attr = attr->next_attribute())
	{
		auto attr_name = (std::wstring)attr->name();
		auto attr_value = (std::wstring)attr->value();

		if (annotation_type == L"View") {
			if (attr_name == L"members") {
				sub_sparkel.add_triplet(L"View", L"{0}", attr_value.c_str());
			}else if(attr_name == L"sofa"){
				xmi_id = attr_value;
			} else {
				assert(false);
			}
		} else {
			if (attr_name == L"xmi:id") {
				xmi_id = attr_value;
				if (xmi_id == L"550643") {
					std::wcout << attr_value << std::endl;
				}
			} else {
				sub_sparkel.add_triplet(L"{0}", attr_name.c_str(), util.escape_braces(attr_value.c_str()).c_str());
			}
		}
	}
	for (auto second_o = first_o->first_node(); second_o; second_o = second_o->next_sibling()) {
		sub_sparkel.add_triplet(L"{0}", ((std::wstring)second_o->name()).c_str(), ((std::wstring)second_o->value()).c_str());
	}

	auto cmd_str = fmt::format(cmd.str().c_str(), xmi_id);
	sparkel.add_clean_element(cmd_str.c_str());
}








/*************************************************************************************/
/************************************* UIMA READ *************************************/
/*************************************************************************************/

/**
* Takes an XMI file and transforms it into sparqlquerys. The quereys will be stored in the queue
* @param: doc: xml-document already read by rapid-xml
*/
const int VirtuosoHelper::xmi_to_queue(xml_document<wchar_t> &doc) {
	using namespace rapidxml;

	std::wstring str{};
	std::wstring subject{};
	std::wstring name, value;
	std::wstringstream sstr_anno{};
	std::wstring quotes{};
	std::unordered_map<std::wstring, std::wstring> attr_map{};
	auto node = doc.first_node(L"xmi:XMI");

	/************************************* Get All XMI-Names *************************************/
	queue.push(get_type_names(*node));
	/************************************* Get All Siblings *************************************/
	for (auto first_o = node->first_node(); first_o; first_o = first_o ? first_o->next_sibling() : 0) {
		//Packaging the Annotations in commands with each containing 50
		std::wstringstream cmd;
		Sparkel sparkel(cmd);
		sparkel.open_insert(graph_id.c_str());

		for (int i = 0; i <= 20 && first_o != 0; i++, first_o = first_o->next_sibling()) {
			uima_to_rdf(first_o, sparkel);
		}

		if (first_o != 0) { uima_to_rdf(first_o, sparkel); }
		sparkel.close_query();
		
		queue.push(cmd.str());
		queue_cond.notify_one();
	}

	done = true;
	queue_cond.notify_one();
	return 1;
}


/**
/* Taskes a queue with SPARQL-Statements and casts them
/* In this case it takes a queue of insertstatements and inserts the Data to the Database
/* The queue is a class-variable
/* @param connector to the Database
*/
const int VirtuosoHelper::queue_to_db(virt& connector) {
	std::wstringstream sstr{};
	int err_count = 0;
	std::wstring dsn = L"VC";
	std::wstring uid = L"dba";
	std::wstring pwd = L"dba";
	connector.ODBC_Connect(dsn.c_str(), uid.c_str(), pwd.c_str());

	/***** Create Graph *****/
	sstr << L"SPARQL CREATE GRAPH <" << graph_id << L">";
	auto err = connector.ODBC_Execute(sstr.str().c_str());
	std::unique_lock<std::mutex> locker(queue_mutex);
	locker.unlock();
	while (!done) {
		queue_cond.wait(locker, [&]() {return !queue.empty(); });
		err = connector.ODBC_Execute(queue.front().c_str());

		//ErrorHandling
		err_count += (err != 0) ? 1 : 0;
		if (err != 0) {
			err_count++;
			std::wcout << queue.front() << std::endl;
			std::wcout << L"*************************************************************************************" << std::endl;
		}
		queue.pop();
	}

	while (!queue.empty()) {
		err = connector.ODBC_Execute(queue.front().c_str());
		err_count += (err != 0) ? 1 : 0;

		if (err != 0) {
			err_count++;
			std::wcout << queue.front() << std::endl;
			std::wcout << L"*************************************************************************************" << std::endl;
		}
		queue.pop();
	}
	connector.ODBC_Disconnect();
	std::cout << "Errors: " << err_count << std::endl;
	return 1;
}

/**
/* Takes an ID and appends it to an XMI-String
/* @param id of the Cas-Document
/* @param xmi String the id shall be appended to
*/
const void VirtuosoHelper::write_id_to_xmi(std::wstring &id, std::wstring &xmi) {
	const std::wstring id_view= LR"(<cas:Sofa sofaID="uimabid" sofaString="{0}">)";
	xmi.append(fmt::format(id_view, id));
}

/**
/* Fetches an id from an xmi-file. If no id is in there the function returns an empty string
/* @param xcmi_file: xmi-file already read into the memory
*/
const std::wstring VirtuosoHelper::get_id_from_xmi(xml_document<wchar_t> &doc) {
	using namespace rapidxml;
	auto node =  doc.first_node(L"xmi:XMI");
	xml_node<wchar_t>* first_o;

	//look for ID Sofa
	for (first_o = node->first_node(); first_o; first_o = first_o ? first_o->next_sibling() : 0) {
		if (first_o->name() == L"cas:Sofa") {
			for (xml_attribute<wchar_t>* attr = first_o->first_attribute(); attr; attr = attr->next_attribute())
			{
				if (attr->name() == L"sofaID" && attr->value() == L"uimabid") {
					for (xml_attribute<wchar_t>* attr = first_o->first_attribute(); attr; attr = attr->next_attribute())
					{
						if (attr->name() == L"sofaString") {
							return attr->value();
						}
					}
				}
			}
		}
	}
	return L"";
}

/**
/* Fetches all data from the Database and writes it to a stream in xml-format
/* @param writer: Xml-Writer
/* @param buf: stringstream the xmlwriter writes to
/* @param connector: connector to the Database(for fetching Data)
*/
const int VirtuosoHelper::write_data( Writer& writer, virt& connector) {

	if (connector.ODBC_Connect() != 0) { return ERROR_DATABASE_FAILURE; }
	std::wstring cmd = L"SPARQL SELECT * FROM <" + graph_id + LR"(> WHERE { <xmi:XMI> ?p ?o })";
	std::map<std::wstring, std::wstring> xmi_map;
	auto err = connector.ODBC_Execute(cmd.data());
	if (err != 0) { return ERROR_DATABASE_FAILURE; }

	err = connector.ODBC_ResultToString(xmi_map);
	if (err != 0) { return ERROR_DATABASE_FAILURE; }

	for (auto& attr : xmi_map) {
		writer.attr(attr.first.c_str(), attr.second);
	}

	auto i_ids = get_all_ids(connector);

	for (auto id : i_ids) {
		std::wstringstream select_by_id;
		select_by_id << LR"(SPARQL SELECT * FROM <)" << graph_id << LR"(> WHERE { <)" << id << LR"(> ?p ?o })";
		err = connector.ODBC_Execute(select_by_id.str().c_str());
		if (err != 0) { return ERROR_DATABASE_FAILURE; }

		std::map<std::wstring, std::wstring> mappy;
		connector.ODBC_ResultToString(mappy);
		std::wstring start = mappy[L"prefix"] + L":" + mappy[L"suffix"];
		writer.openElt(start.c_str());
		
		std::stack<std::wstring> hypo_stack;
		for (auto p : mappy) {
			(p.second == L"WikiDataHyponyms") ? hypo_stack.push(p.second) : writer.attr(p.first.c_str(), p.second);
		}

		if (!hypo_stack.empty()) {
			writer.attr(L"xmi:id", std::to_wstring(id));
			while (!hypo_stack.empty()) {
				writer.openElt(L"WikiDataHyponyms").content(hypo_stack.top().c_str()).closeElt();
				hypo_stack.pop();
			}
			writer.closeElt();
		} else {
			writer.attr_close(L"xmi:id", std::to_wstring(id));
		}
	}
	connector.ODBC_Disconnect();
	return 1;
}
/**
/* Writes Data From Database to File
/* All is set to an uimaFormat
/* @param: out_file: Directory + filename where the data should be written.
/* @param: connector: connector to the Database
*/
const void VirtuosoHelper::write_to_file(std::wstring& out_file, virt & connector) {
	std::wstringstream buf;
	Writer writer(buf);

	writer.writeHeader();

	writer.openElt(L"xmi:XMI");		//xmi:XMI open
	write_data(writer, connector);
	write_views(writer, connector);
	writer.closeElt();				//xmi:XMI close

	std::wofstream sstrm(out_file);
	sstrm << buf.str();
	sstrm.flush();
	sstrm.close();
}

/**
/* Writes Data From Database to File
/* All is set to an uimaFormat
/* @param: out_file: Directory + filename where the data should be written.
/* @param: connector: connector to the Database
*/
const std::wstring VirtuosoHelper::get_file(virt& connector) {
	std::wstringstream buf;
	Writer writer(buf);

	writer.writeHeader();

	writer.openElt(L"xmi:XMI");		//xmi:XMI open
	write_data(writer, connector);
	write_views(writer, connector);
	writer.closeElt();				//xmi:XMI close
	
	return buf.str();
}



/**
/* Fetches all ID's inside the Database (ID's are always at the last position in this DB model)
/* @param connector: connector to the Database
*/
const std::vector<int> VirtuosoHelper::get_all_ids(virt& connector) {
	std::wstring cmd = LR"(SPARQL SELECT DISTINCT ?s FROM <{0}> )";
	auto cmd_str = fmt::format(cmd, graph_id);
	cmd_str += LR"(WHERE{ ?s ?p ?o } ORDER BY DESC(?s))";
	
	auto err = connector.ODBC_Execute(cmd_str.c_str());
	if (err != 0) {
		throw;
	}
	
	std::map<std::wstring, std::wstring> ids;
	std::vector<int> i_ids;
	connector.ODBC_ResultToString(ids);
	for (auto id : ids) {
		try {
			i_ids.emplace_back(std::stoi(id.first));
		}
		catch (std::exception & e) {
			std::cout << "Exception: "<<  e.what() << std::endl;
		}
	}
	std::sort(i_ids.begin(), i_ids.end());
	return i_ids;
}


/**
/* Fetches All views from a Database (a view has the word view in the subject-position) and writes these to the xml-string
/* @param Writer: XML-Writer where the Data shall be inserted
/* @param connector: connector to the Database
*/
void VirtuosoHelper::write_views(Writer& writer, virt& connecotr) {
	std::wstring cmd = LR"(SPARQL SELECT DISTINCT ?p FROM <{0}> )";
	auto view_id_cmd = fmt::format(cmd, graph_id);
	view_id_cmd += LR"(WHERE{ <View> ?p ?o } ORDER BY DESC(?s))";
	connecotr.ODBC_Connect();
	auto err = connecotr.ODBC_Execute(view_id_cmd.c_str());
	std::map<std::wstring, std::wstring> view_ids;
	connecotr.ODBC_ResultToString(view_ids);

	for (auto id : view_ids) {
		std::wstringstream view_cmd;
		view_cmd << LR"(SPARQL SELECT * FROM <)" << graph_id << LR"(> WHERE{ <View> <)" << id.first << LR"(> ?o } ORDER BY DESC(?o))";
		err = connecotr.ODBC_Execute(view_cmd.str().c_str());
		std::map<std::wstring, std::wstring> views;
		connecotr.ODBC_ResultToString(views);


		for (auto view : views) {
			writer.openElt(L"cas:View").attr(L"members", view.first).attr(L"sofa", id.first).quickClose();
		}
	}
	connecotr.ODBC_Disconnect();

}