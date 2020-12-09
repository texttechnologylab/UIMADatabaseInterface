#ifndef Virtuoso_H
#define Virtuoso_H

#include "pch.h"
#include "utils.hpp"
#include "virt.hpp"
#include <fmt/format.h>

#include "VirtuosoConfig.hpp"
#include "VirtuosoHelper.hpp"

class Virtuoso {
private:

	std::wstring filepath{};
	std::wstring dsn{};
	std::wstring pwd{};
	std::wstring uid{};
	std::wstring graphname{};
	utils util{};
	std::queue<std::wstring> s_queue;
	bool done = false;

	
	VirtuosoHelper v_helper;

public:
	virt connector;

	const Virtuoso(const std::wstring& config_file);
	const Virtuoso(const std::wstring& database_name, const std::wstring& user_id, const std::wstring& password);
	int connect();
	int disconnect();
	std::wstring UpdateElement(std::wstring& id, std::wstring &xmi_file);
	std::wstring CreateElement(std::wstring &xmi_file);
	int DeleteElement(std::wstring& id);
	int GetElement(std::wstring& id);
	int GetElementOut(std::wstring& id, std::wstring& out_file);
	int fire_query(std::wstring &query, std::map<std::wstring, std::wstring> &result);

};



#endif