#ifndef VirtuosoApi_H
#define VirtuosoApi_H

#include "pch.h"
#include "utils.hpp"
#include "virt.hpp"
//#include <docopt.h>
//#include "VirtuosoConnection.cpp"
#include "Virtuoso.hpp"


//class uima {
//
//private:
//
//	std::wstring filepath{};
//	std::wstring dsn{};
//	std::wstring pwd{};
//	std::wstring uid{};
//	std::wstring graphname{};
//	utils util{};
//	std::queue<std::wstring> s_queue;
//	bool done = false;
//
//public:
//	uima();
//	//Setter Functions
//	void set_dsn(const std::wstring& ui);
//	void set_pwd(const std::wstring& pw);
//	void set_uid(const std::wstring& us);
//	void set_filepath(const std::wstring& fp);
//
//	//Getter Functions
//	std::wstring get_filepath();
//	std::wstring get_dsn();
//	std::wstring get_pwd();
//	std::wstring get_uid();
//	std::wstring get_graphname();
//
//	//Functionality
//	void write();
//	void read();
//	std::vector<int> get_all_ids(virt& db);
//	void write_views(virt& db, Writer& writer);
//	void write_data(virt& db, Writer& writer, std::wstringstream& buf);
//	void write_to_file(std::wstring& file);
//};


//class wikidata {
//
//private:
//	bool done{};
//	std::queue<std::wstring> queue;
//	std::queue<std::wstring> s_queue;
//	std::wstring db_name{};
//	int package_size = 6; //has to be leual or larger than 4
//
//	std::wstring dsn{};
//	std::wstring uid{};
//	std::wstring pwd{};
//	std::wstring filename{};
//
//public:
//	wikidata();
//	int read();
//	int query_check(std::wstring& str, virt& db);
//	int write();
//};

#endif