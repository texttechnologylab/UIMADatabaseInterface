#ifndef VirtuosoHelper_H
#define VirtuosoHelper_H

#include <ctime>

#include "sparqlib_light.hpp"
#include "rapidxml.hpp"
#include "rapidxml_iterators.hpp"
#include "rapidxml_print.hpp"
#include "rapidxml_utils.hpp"
#include "../3p/fmt/include/fmt/format.h"
#include "utils.hpp"
#include "virt.hpp"
#include <mutex>
#include <condition_variable>



using namespace rapidxml;

class VirtuosoHelper {


private:
	utils util;
	std::wstring graph_id;
	std::mutex queue_mutex;
	std::condition_variable queue_cond;
	bool done = false;


	const int get_all_graphnames(std::vector<std::wstring> graphnames, virt &connector);
	const void uima_to_rdf(xml_node<wchar_t>* first_o, Sparkel& sparkel);

public:
	std::queue<std::wstring> queue;
	//const VirtuosoHelper(const std::wstring& config_file);
	//const VirtuosoHelper(const std::wstring& database_name, const std::wstring& user_id, const std::wstring& password);
	const VirtuosoHelper();
	const void set_graph_id(const std::wstring& id);
	const std::wstring get_type_names(xml_node<wchar_t> &node);
	const int xmi_to_queue(/*std::wstring xmi_file*/xml_document<wchar_t> &doc);
	const int queue_to_db(virt& connector);
	const void write_id_to_xmi(std::wstring& id, std::wstring& xmi);
	const std::wstring get_id_from_xmi(xml_document<wchar_t> &doc/*std::wstring xmi_file*/);
	
	const void write_to_file(std::wstring& file, virt& connector);
	const int write_data(Writer& writer, virt& connector);
	const std::vector<int> get_all_ids(virt& connector);
	void write_views(Writer& writer, virt& connecotr);
	const std::wstring get_file(virt& connector);


	


};


#endif